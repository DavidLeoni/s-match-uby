package it.unitn.disi.smatch.oracles.uby.test;

import static org.junit.Assert.assertEquals;
import static it.unitn.disi.smatch.oracles.uby.test.LmfBuilder.lmf;
import de.tudarmstadt.ukp.lmf.transform.LMFDBTransformer;
import static org.junit.Assert.assertNotNull;
import static it.unitn.disi.smatch.oracles.uby.SmuUtils.checkNotNull;
import static it.unitn.disi.smatch.oracles.uby.SmuUtils.checkNotEmpty;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tudarmstadt.ukp.lmf.hibernate.UBYH2Dialect;
import de.tudarmstadt.ukp.lmf.model.core.LexicalEntry;
import de.tudarmstadt.ukp.lmf.model.core.LexicalResource;
import de.tudarmstadt.ukp.lmf.model.core.Lexicon;
import de.tudarmstadt.ukp.lmf.model.enums.ERelNameSemantics;
import de.tudarmstadt.ukp.lmf.model.enums.ERelTypeSemantics;
import de.tudarmstadt.ukp.lmf.model.miscellaneous.ConstraintSet;
import de.tudarmstadt.ukp.lmf.model.multilingual.SenseAxis;
import de.tudarmstadt.ukp.lmf.model.semantics.SemanticPredicate;
import de.tudarmstadt.ukp.lmf.model.semantics.SynSemCorrespondence;
import de.tudarmstadt.ukp.lmf.model.semantics.Synset;
import de.tudarmstadt.ukp.lmf.model.semantics.SynsetRelation;
import de.tudarmstadt.ukp.lmf.model.syntax.SubcategorizationFrame;
import de.tudarmstadt.ukp.lmf.model.syntax.SubcategorizationFrameSet;
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
import it.unitn.disi.smatch.oracles.uby.SmuSynsetRelation;
import it.unitn.disi.smatch.oracles.uby.SmuUby;
import it.unitn.disi.smatch.oracles.uby.SmuUtils;


public class UbyLinguisticOracleTest {

		
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
	
    /**
     * Creates the classifications, matches them and processes the results.
     *
     * @throws SMatchException SMatchException
     */
	@Test
    public void example() throws SMatchException {
		
		/*
        System.out.println("Starting example...");
        System.out.println("Creating MatchManager...");
        IMatchManager mm = MatchManager.getInstanceFromResource("/it/unitn/disi/smatch/oracles/uby/test/conf/s-match.xml");

        String example = "Courses";
        System.out.println("Creating source context...");
        IContext s = mm.createContext();
        s.createRoot(example);

        System.out.println("Creating target context...");
        IContext t = mm.createContext();
        INode root = t.createRoot("Course");
        INode node = root.createChild("College of Arts and Sciences");
        node.createChild("English");

        node = root.createChild("College Engineering");
        node.createChild("Civil and Environmental Engineering");

        System.out.println("Preprocessing source context...");
        mm.offline(s);

        System.out.println("Preprocessing target context...");
        mm.offline(t);

        System.out.println("Matching...");
        IContextMapping<INode> result = mm.online(s, t);

        System.out.println("Processing results...");
        System.out.println("Printing matches:");
        for (IMappingElement<INode> e : result) {
            System.out.println(e.getSource().nodeData().getName() + "\t" + e.getRelation() + "\t" + e.getTarget().nodeData().getName());
        }

        System.out.println("Done");
        */
    }
	
	
				

	/**
	 * Tests simple saving with Hibernate
	 * 
	 * @since 0.1
	 */
	@Test
	public void testHibernateSave(){
/*		try {
			SmuUtils.createTables(dbConfig);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Couldn't create tables in database " + dbConfig.getJdbc_url() + "!", e); // todo
		}
		
		SmuLinguisticOracle oracle = new SmuLinguisticOracle(dbConfig, null);
		
		SmuUby uby = oracle.getUby();
		
		uby.getSession().save(arg0)
*/
	}
	
	/**
	 * todo this seems a uby bug, it always return null !
	 */
	@Test
	@Ignore
	public void todo(){
		//Synset syn = uby.getSynsetIterator(null).next();
	}
	
	
	
	/**
	 * Checks our extended model of uby with is actually returned by Hibernate
	 * 
	 * @since 0.1
	 */
	@Test
	public void testHibernateExtraAttributes(){

		SmuUtils.createTables(dbConfig);		
		
		LexicalResource lexicalResource = new LexicalResource();
		lexicalResource.setName("lexicalResource 1");
		Lexicon lexicon = new Lexicon();
		lexicalResource.addLexicon(lexicon);
		lexicon.setId("lexicon 1");
		LexicalEntry lexicalEntry = new LexicalEntry();
		lexicon.addLexicalEntry(lexicalEntry);
		lexicalEntry.setId("lexicalEntry 1");
		Synset synset = new Synset();
		lexicon.getSynsets().add(synset);
		synset.setId("synset 1");
		SmuSynsetRelation synsetRelation = new SmuSynsetRelation();
		synsetRelation.setRelType(ERelTypeSemantics.taxonomic);
		synsetRelation.setRelName(ERelNameSemantics.HYPERNYM);
		synsetRelation.setDepth(3);
		synsetRelation.setProvenance("a");
	    synsetRelation.setSource(synset);
		synsetRelation.setTarget(synset);
		synset.getSynsetRelations().add(synsetRelation);			

		SmuUtils.saveLexicalResourceToDb(dbConfig, lexicalResource, "lexical resource 1");

		SmuLinguisticOracle oracle = new SmuLinguisticOracle(dbConfig, null);
		
		SmuUby uby = oracle.getUby();	
		
		assertNotNull(uby.getLexicalResource("lexicalResource 1"));
		assertEquals(1, uby.getLexicons().size());
		
		Lexicon rlexicon = uby.getLexicons().get(0);
				
		List<Synset> rsynsets = rlexicon.getSynsets();		
		
		assertEquals(1, rsynsets.size());
		
		List<SynsetRelation> synRels = rsynsets.get(0).getSynsetRelations();		
		assertEquals(1, synRels.size());		
		SynsetRelation rel = synRels.get(0);
		assertNotNull(rel);

		log.info("Asserting rel is instance of " + SmuSynsetRelation.class);
		if (!(rel instanceof SmuSynsetRelation)) {
			throw new RuntimeException(
					"relation is not of type " + SmuSynsetRelation.class + " found instead " + rel.getClass());
		}
		
		SmuSynsetRelation smuRel = (SmuSynsetRelation) rel;
		
		assertEquals (3, smuRel.getDepth());
		assertEquals ("a", smuRel.getProvenance());	
	};
	
	
	
	@Test
	public void testNormalize(){
		
		// lexicalResource 1 -> lexicon 1 -> synset 2  hyponymOf synset 1
		//
		// expect: 
		//lexicalResource 1 -> lexicon 1 -> synset 1  hypernymOf synset 2
		
		
		SmuUtils.createTables(dbConfig);

		LexicalResource lexicalResource = lmf().lexicon().synset().synset()
				.synsetRelation(ERelNameSemantics.HOLONYM,1).build();
		
		
		SmuUtils.saveLexicalResourceToDb(dbConfig, lexicalResource, "lexical resource 1");
		
		
		
		SmuLinguisticOracle oracle = new SmuLinguisticOracle(dbConfig, null);
		
		SmuUby uby = oracle.getUby();	
		
		assertNotNull(uby.getLexicalResource("lexicalResource 1"));
		assertEquals(1, uby.getLexicons().size());
		
		Lexicon rlexicon = uby.getLexicons().get(0);
				
		List<Synset> rsynsets = rlexicon.getSynsets();		
		
		assertEquals(2, rsynsets.size());
		
		List<SynsetRelation> synRels = rsynsets.get(0).getSynsetRelations();		
		assertEquals(2, synRels.size());		
		
		SmuSynsetRelation smuRel1 = (SmuSynsetRelation) synRels.get(0);		
		assertEquals (ERelNameSemantics.HOLONYM, smuRel1.getRelName());

		SmuSynsetRelation smuRel2 = (SmuSynsetRelation) synRels.get(0);		
		assertEquals (ERelNameSemantics.HYPERNYM, smuRel2.getRelName());

		

	}

}

