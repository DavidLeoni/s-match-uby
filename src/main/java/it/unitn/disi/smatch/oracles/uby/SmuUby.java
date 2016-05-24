package it.unitn.disi.smatch.oracles.uby;

import static  it.unitn.disi.smatch.oracles.uby.SmuUtils.checkNotEmpty;
import static  it.unitn.disi.smatch.oracles.uby.SmuUtils.checkNotNull;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static it.unitn.disi.smatch.oracles.uby.SmuUtils.SMATCH_CANONICAL_RELATIONS;

import org.hibernate.CacheMode;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.service.ServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import de.tudarmstadt.ukp.lmf.api.Uby;
import de.tudarmstadt.ukp.lmf.model.semantics.Synset;
import de.tudarmstadt.ukp.lmf.model.semantics.SynsetRelation;
import de.tudarmstadt.ukp.lmf.transform.DBConfig;
import de.tudarmstadt.ukp.lmf.transform.XMLToDBTransformer;

/**
 * Version of Uby with some additional fields in the db to speed up computations
 *
 * @since 0.1
 */
public class SmuUby extends Uby {
    

    private static final Logger log = LoggerFactory.getLogger(SmuUby.class);

    public SmuUby(DBConfig dbConfig) {
        super(dbConfig);

        if (dbConfig == null) {
            throw new IllegalArgumentException("database configuration is null");
        }

        this.dbConfig = dbConfig;

        // dav: note here we are overwriting cfg and sessionFactory
        cfg = SmuUtils.getHibernateConfig(dbConfig);

        ServiceRegistryBuilder serviceRegistryBuilder = new ServiceRegistryBuilder().applySettings(cfg.getProperties());
        sessionFactory = cfg.buildSessionFactory(serviceRegistryBuilder.buildServiceRegistry());
        session = sessionFactory.openSession();
    }

    /**
     * Augments the graph with is-a transitive closure and eventally adds
     * symmetric hyperym/hyponim relations.
     */
    // todo what about provenance? todo instances?
    public void augmentGraph() {

        normalizeGraph();

        // computeTransitiveClosure();

    }

    private void computeTransitiveClosure() {
        Session session = sessionFactory.openSession();

        Transaction tx = session.beginTransaction();

        Synset ss;
        int depth = 1;

        // vars:
        // :depth depth of edges to use to build new ones
        // :relName
        // :provenance_A = '' or SMATCH_UBY_URL

        String provenance = "''";

        // As: original edges
        // Bs: the edges computed so far

        String hqlInsert = "INSERT INTO SynsetRelation (synsetId, target, relType, relName, depth, provenance) "
                + "  SELECT SR_A.id, SR_B.target,  SR_A.relType, :relName, :depth + 1, " + getProvenanceId()
                + "  FROM SynsetRelation SR_A, Synset SS_A, SynsetRelation SR_B, Synset SS_B" + "  WHERE"
                + "		 	 SS_A.relName=:relName" + "		 AND SS_A.depth=:depth" + "		 AND SS_B.provenance=''"
                + "		 AND SS_B.relName=:relName" + "		 AND SR_A.target=SR_B.synsetId";

        Query query = session.createQuery(hqlInsert);

        // query.setParameter("provenance_A", "''")
        // .setParameter("depth", depth).setParameter("relName", relName);

        int createdEntities = session.createQuery(hqlInsert)

                                     .executeUpdate();
        tx.commit();
        session.close();

    }

    private static String makeSqlList(List<String> input) {
        StringBuilder sb = new StringBuilder("(");
        boolean first = true;
        for (String s : input) {
            if (first) {
                sb.append(s);
            } else {
                sb.append(", " + s);
            }
            first = false;
        }
        return sb.toString();

    }

    /**
     * Returns {@code true} if {@code source} contains a relation toward {@code target} synset. 
     * Returns false otherwise.
     */
    private static boolean containsRel(Synset source, Synset target,  String relName){        
        checkNotNull(source, "Invalid source!");
        checkNotNull(target, "Invalid target!");
        checkNotEmpty(relName, "Invalid relName!");
        
        for (SynsetRelation synRel : source.getSynsetRelations()){             
            if (relName.equals(synRel.getRelName())
                    && Objects.equals(synRel.getTarget().getId(), (target.getId()))                    ){
                return true;
            }
        }
        return false;
    }
    
    
    private static void incStat(Map<String, Integer> map, String key){
        checkNotEmpty(key, "Invalid key!");
        if (map.containsKey(key)){
            map.put(key, map.get(key) + 1);
        } else {
            map.put(key, 1);
        }
    }
    
   /*
    * Adds missing edges of depth 1 for relations we consider as canonical.
    */
   private void normalizeGraph() {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        
        String hql = "FROM Synset";        
        Query query = session.createQuery(hql);
        
        ScrollableResults synsets = query
                                        .setCacheMode(CacheMode.IGNORE)
                                        .scroll(ScrollMode.FORWARD_ONLY);
        int count=0;
        
        Map<String, Integer> relStats = new HashMap();
        
        
        while ( synsets.next() ) {
            
            Synset synset = (Synset) synsets.get(0);
            log.info("Processing synset with id " + synset.getId() + " ...");
                      
            
            List<SynsetRelation> relations = synset.getSynsetRelations();
                        
            for (SynsetRelation sr : relations){
                SmuSynsetRelation ssr = (SmuSynsetRelation) sr;
                
                if (SmuUtils.hasInverse(ssr.getRelName())){
                    String inverseRelName = SmuUtils.getInverse(ssr.getRelName());
                    if (SmuUtils.isCanonical(inverseRelName)
                            && !containsRel(ssr.getTarget(),
                                            ssr.getSource(),                                            
                                            inverseRelName)){
                        SmuSynsetRelation newSsr = new SmuSynsetRelation();
                        
                        newSsr.setDepth(1);
                        newSsr.setProvenance(SmuUby.getProvenanceId());
                        newSsr.setRelName(inverseRelName);
                        newSsr.setRelType(ssr.getRelType());
                        newSsr.setSource(ssr.getTarget());
                        newSsr.setTarget(ssr.getSource());
                        
                                                
                        ssr.getTarget().getSynsetRelations().add(newSsr);
                        session.save(newSsr);
                        session.saveOrUpdate(ssr.getTarget());
                        incStat(relStats, inverseRelName);
                    }
                }
                
            }
                        
            if ( ++count % 20 == 0 ) {
                //flush a batch of updates and release memory:
                session.flush();
                session.clear();
            }
        }
           
        tx.commit();
        session.close();
        
        long totEdges = 0;
        for (Integer v : relStats.values()){
            totEdges += v;
        }
        
        log.info("");        
        log.info("Inserted " + totEdges + " normalized edges:");
        for (String relName : relStats.keySet()){
            log.info("  " + relName + ":   " + relStats.get(relName) );
        }
    }
    
  
    /**
     * 
     * @param filepath
     * @param lexicalResourceName
     *            todo meaning? name seems not be required to be in the xml
     */
    public void loadLmfXml(String filepath, String lexicalResourceName) {

        XMLToDBTransformer trans = new XMLToDBTransformer(dbConfig);

        try {
            trans.transform(new File(filepath), lexicalResourceName);
        } catch (Exception ex) {
            throw new RuntimeException("Error while loading lmf xml " + filepath, ex);
        }

        try {
            augmentGraph();
        } catch (Exception ex) {
            log.error("Error while augmenting graph with computed edges!", ex);
        }

    }

    /**
     * Returns the fully qualified package name. 
     */
    public static String getProvenanceId(){
        return SmuUby.class.getPackage().getName();
    }
}
