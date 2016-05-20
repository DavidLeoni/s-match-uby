package it.unitn.disi.smatch.oracles.uby.test;

import static org.junit.Assert.assertEquals;
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
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tudarmstadt.ukp.lmf.hibernate.UBYH2Dialect;
import de.tudarmstadt.ukp.lmf.model.core.LexicalEntry;
import de.tudarmstadt.ukp.lmf.model.core.LexicalResource;
import de.tudarmstadt.ukp.lmf.model.core.Lexicon;
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
import it.unitn.disi.smatch.oracles.uby.test.experimental.MySynsetRelation;
import it.unitn.disi.smatch.oracles.uby.test.experimental.TestExtendingHibernate;


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
	 * Checks our extended model of uby with is actually returned by Hibernate
	 * 
	 * @since 0.1
	 */
	@Test
	public void testHibernateExtraAttributes(){

		try {
			SmuUtils.createTables(dbConfig);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Couldn't create tables in database " + dbConfig.getJdbc_url() + "!", e); // todo
		}
		
		LexicalResource lexicalResource = new LexicalResource();
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
		synsetRelation.setDepth(3);
		synsetRelation.setProvenance("a");
		synset.getSynsetRelations().add(synsetRelation);			

		SmuUtils.saveLexicalResourceToDb(dbConfig, lexicalResource, "lexical resource 1");

		SmuLinguisticOracle oracle = new SmuLinguisticOracle(dbConfig, null);
		
		SmuUby uby = oracle.getUby();	
		
		Synset syn = uby.getSynsetIterator(null).next();		
		List<SynsetRelation> synRels = syn.getSynsetRelations();		
		assertEquals(1, synRels.size());		
		SynsetRelation rel = synRels.get(0);
		assertNotNull(rel);

		log.info("Asserting rel is instance of " + SmuSynsetRelation.class);
		if (!(rel instanceof SmuSynsetRelation)) {
			throw new RuntimeException(
					"relation is not of type " + MySynsetRelation.class + " found instead " + rel.getClass());
		}
		
		SmuSynsetRelation myRel = (SmuSynsetRelation) rel;
		
		assertEquals (3, ((SmuSynsetRelation) rel).getDepth());
		assertEquals ("a", ((SmuSynsetRelation) rel).getProvenance());
	
	};

}

