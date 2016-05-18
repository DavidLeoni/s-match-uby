package it.unitn.disi.smatch.oracles.uby.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tudarmstadt.ukp.lmf.hibernate.UBYH2Dialect;
import de.tudarmstadt.ukp.lmf.model.enums.ERelNameSemantics;
import de.tudarmstadt.ukp.lmf.model.semantics.SynsetRelation;
import de.tudarmstadt.ukp.lmf.transform.DBConfig;
import de.tudarmstadt.ukp.lmf.transform.XMLToDBTransformer;
import it.unitn.disi.smatch.IMatchManager;
import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.SMatchException;
import it.unitn.disi.smatch.data.ling.ISense;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.oracles.uby.SmuLinguisticOracle;
import it.unitn.disi.smatch.oracles.uby.SmuNotFoundException;
import it.unitn.disi.smatch.oracles.uby.SmuSynsetRelation;
import it.unitn.disi.smatch.oracles.uby.SmuUby;
import it.unitn.disi.smatch.oracles.uby.SmuUtils;
import it.unitn.disi.smatch.oracles.uby.test.experimental.MySynsetRelation;
import it.unitn.disi.smatch.oracles.uby.test.experimental.TestExtendingHibernate;



public class SmuUtilsTest {

		
	private static final Logger log = LoggerFactory.getLogger(UbyLinguisticOracleTest.class);
	
	private DBConfig dbConfig;
		
	
	@Before
	public void beforeMethod(){
		 dbConfig = new DBConfig("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "org.h2.Driver",
				UBYH2Dialect.class.getName(), "root", "pass", true);		 		
	}
	
	@After
	public void afterMethod(){
		dbConfig = null;
	}

	@Test
	public void testInverses(){
		assertTrue(SmuUtils.isInverse(ERelNameSemantics.HYPERNYM, ERelNameSemantics.HYPONYM));
		assertTrue(SmuUtils.isInverse(ERelNameSemantics.HYPONYM, ERelNameSemantics.HYPERNYM));
		
		assertFalse(SmuUtils.isInverse("a", ERelNameSemantics.HYPERNYM));
		
		try {
			SmuUtils.getInverse("a");
			Assert.fail("Shouldn't arrive here!");
		} catch (SmuNotFoundException ex){
			
		}
	}
	
}