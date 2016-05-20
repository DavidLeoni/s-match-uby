package it.unitn.disi.smatch.oracles.uby;

import java.io.File;
import java.util.List;

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

		//computeTransitiveClosure();

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
				+ "  SELECT SR_A.id, SR_B.target,  SR_A.relType, :relName, :depth + 1, " + SMATCH_UBY_URL
				+ "  FROM SynsetRelation SR_A, Synset SS_A, SynsetRelation SR_B, Synset SS_B" + "  WHERE"
				+ "		 	 SS_A.relName=:relName" + "		 AND SS_A.depth=:depth" + "		 AND SS_B.provenance=''"
				+ "		 AND SS_B.relName=:relName" + "		 AND SR_A.target=SR_B.synsetId";

		Query query = session.createQuery(hqlInsert);

		//query.setParameter("provenance_A", "''")
	 //.setParameter("depth", depth).setParameter("relName", relName);

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
	 * Adds missing edges for relations we consider as canonical
	 */
	private void normalizeGraph() {

		log.info("Going to normalizing graph with canonical relations ...");
		
		Session session = sessionFactory.openSession();

		Transaction tx = session.beginTransaction();

		for (String relName : SMATCH_CANONICAL_RELATIONS) {

			log.info("Normalizing graph with canonical relation " + relName + " ...");

			String inverseRelName = SmuUtils.getInverse(relName);

			String hqlInsert = "INSERT INTO SynsetRelation (synsetId, target, relType, relName, depth, provenance) "
					+ "  SELECT SR.target, SR.synsetId,  SR.relType, :relName,  1, " + SMATCH_UBY_URL
					+ "  FROM SynsetRelation SR" + "  WHERE" + "		   SR.relName=:inverseRelName"
					+ "	   AND SR.depth=1" + "	   AND SR.provenance=''" + "	   AND (SR.target, SR.synsetId) NOT IN " // so
																															// to
																															// avoid
																															// duplicates
					+ "			(" + "				SELECT SR2.synsetId, SR2.target"
					+ "                FROM SynsetRelation SR2" + "                WHERE 	  "
					+ "				         SR2.relName=:relName" + "                  AND SR2.depth=1"
					+ "			)";

			Query query = session.createQuery(hqlInsert);

			query.setParameter("relName", relName).setParameter("inverseRelName", inverseRelName);

			int createdEntities = session.createQuery(hqlInsert).executeUpdate();
			log.info("Inserted " + createdEntities + " " + relName + " edges.");

		}

		tx.commit();
		session.close();
		
		log.info("Done normalizing graph with canonical relations.");

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

}
