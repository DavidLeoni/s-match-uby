package it.unitn.disi.smatch.oracles.uby.test;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tudarmstadt.ukp.lmf.hibernate.UBYH2Dialect;
import de.tudarmstadt.ukp.lmf.model.enums.ERelNameSemantics;
import de.tudarmstadt.ukp.lmf.transform.DBConfig;
import it.unitn.disi.smatch.oracles.uby.SmuNotFoundException;
import it.unitn.disi.smatch.oracles.uby.SmuUtils;



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