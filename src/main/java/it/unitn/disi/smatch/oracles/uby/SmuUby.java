package it.unitn.disi.smatch.oracles.uby;

import java.io.File;

import org.hibernate.CacheMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.service.ServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tudarmstadt.ukp.lmf.api.Uby;
import de.tudarmstadt.ukp.lmf.model.semantics.Synset;
import de.tudarmstadt.ukp.lmf.transform.DBConfig;
import de.tudarmstadt.ukp.lmf.transform.XMLToDBTransformer;

/**
 * Version of Uby with some additional fields in the db to speed up computations 
 *
 * @since 0.1
 */
public class SmuUby extends Uby {	
	
	
	public static final String SMATCH_UBY_URL = "https://github.com/s-match/s-match-uby";
			
	private static final Logger log = LoggerFactory.getLogger(SmuUby.class);
			
	public SmuUby(DBConfig dbConfig) {
		super(dbConfig);		
		
		if (dbConfig == null) {
			throw new IllegalArgumentException("database configuration is null");
		}
		
		this.dbConfig = dbConfig;
		
		// dav: note here we are overwriting cfg and sessionFactory					
		cfg = SmuUtils.getHibernateConfig(dbConfig);

		ServiceRegistryBuilder serviceRegistryBuilder = new ServiceRegistryBuilder()
				.applySettings(cfg.getProperties());
		sessionFactory = cfg.buildSessionFactory(serviceRegistryBuilder.buildServiceRegistry());
		session = sessionFactory.openSession();		
	}
	
	/**
	 * Augments the graph with is-a transitive closure and eventally adds symmetric 
	 * hyperym/hyponim relations.  
	 */
	// todo what about provenance? todo instances?
	public void augmentGraph(){
		
		/* 
		 Session session = sessionFactory.openSession();
		 
		Transaction tx = session.beginTransaction();

		Synset ss;
				
		// vars: :lexiconId_A  :lexiconId_B :depth :relType, :relName 
		String hqlInsert = "INSERT INTO SynsetRelation (synsetId, target, relType, relName, depth) "
				+ "SELECT SR_A.id, SR_B.target, "
				+ "FROM SynsetRelation SR_A, Synset SS_A, SynsetRelation SR_B, Synset SS_B"
				+ "WHERE SR_A.synsetId=SS_A.synsetId"
				+ "		 AND SS_A.lexiconId=:lexiconId_A"
				+ "		 AND SR_B.synsetId=SS_B.synsetId"				
				+ "		 AND SS_B.lexiconId=:lexiconId_B"
				+ "		 AND SR_B.target=SR_A.synsetId";				
				
		int createdEntities = session.createQuery( hqlInsert )
		        .executeUpdate();
		tx.commit();
		session.close();
		*/
	}
	
	
	/**
	 * 
	 * @param filepath
	 * @param lexicalResourceName todo meaning?  name seems not be required to be in the xml
	 */
	public void loadLmfXml(String filepath, String lexicalResourceName){
		
		XMLToDBTransformer trans = new XMLToDBTransformer(dbConfig);
		
		try {
			trans.transform(new File(filepath),lexicalResourceName); 
		} catch (Exception ex){
			throw new RuntimeException("Error while loading lmf xml " + filepath, ex);
		}		
		
		try {
			augmentGraph();
		} catch (Exception ex) {
			log.error("Error while augmenting graph with computed edges!" ,ex);
		}

	}

}
