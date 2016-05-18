package it.unitn.disi.smatch.oracles.uby;

import static it.unitn.disi.smatch.oracles.uby.SmuUtils.checkNotEmpty;
import static it.unitn.disi.smatch.oracles.uby.SmuUtils.checkNotNull;

import java.io.FileNotFoundException;
import java.util.Iterator;

import javax.annotation.Nullable;

import de.tudarmstadt.ukp.lmf.model.core.LexicalEntry;
import de.tudarmstadt.ukp.lmf.model.core.LexicalResource;
import de.tudarmstadt.ukp.lmf.model.core.Lexicon;
import de.tudarmstadt.ukp.lmf.model.miscellaneous.ConstraintSet;
import de.tudarmstadt.ukp.lmf.model.multilingual.SenseAxis;
import de.tudarmstadt.ukp.lmf.model.semantics.SemanticPredicate;
import de.tudarmstadt.ukp.lmf.model.semantics.SynSemCorrespondence;
import de.tudarmstadt.ukp.lmf.model.semantics.Synset;
import de.tudarmstadt.ukp.lmf.model.syntax.SubcategorizationFrame;
import de.tudarmstadt.ukp.lmf.model.syntax.SubcategorizationFrameSet;
import de.tudarmstadt.ukp.lmf.transform.DBConfig;

import de.tudarmstadt.ukp.lmf.transform.LMFDBTransformer;

/**
 * 
 * Simple transformer to directly put into the db a LexicalResource complete with all the
 * lexicons, synsets, etc.
 * 
 * @since 0.1
 * @author David Leoni
 */
class JavaToDbTransformer extends LMFDBTransformer {

	private LexicalResource lexicalResource;
	private String lexicalResourceId;
	private Iterator<Lexicon> lexiconIter;
	private Iterator<LexicalEntry> lexicalEntryIter;
	private Iterator<SubcategorizationFrame> subcategorizationFrameIter;
	private Iterator<SubcategorizationFrameSet> subcategorizationFrameSetIter;
	private Iterator<SemanticPredicate> semanticPredicateIter;
	private Iterator<SynSemCorrespondence> synSemCorrespondenceIter;
	private Iterator<Synset> synsetIter;
	private Iterator<SenseAxis> senseAxisIter;
	private Iterator<ConstraintSet> constraintSetIter;

	/**
	 *
	 * 
	 * @param resource
	 *            a LexicalResource complete with all the lexicons, synsets, etc
	 * @param lexicalResourceId
	 *            todo don't know well the meaning
	 * @throws FileNotFoundException 
	 */
	public JavaToDbTransformer(DBConfig dbConfig, LexicalResource lexicalResource, String lexicalResourceId) throws FileNotFoundException {
		super(dbConfig);
		checkNotNull(lexicalResource);
		checkNotEmpty(lexicalResourceId, "Invalid lexicalResourceId!");

		this.lexicalResource = lexicalResource;
		this.lexicalResourceId = lexicalResourceId;

		this.lexiconIter = this.lexicalResource.getLexicons().iterator();
		this.senseAxisIter = lexicalResource.getSenseAxes().iterator();

	}

	@Override
	protected LexicalResource createLexicalResource() {
		return lexicalResource;
	}

	@Override
	protected Lexicon createNextLexicon() {
		if (lexiconIter.hasNext()) {
			Lexicon lexicon = lexiconIter.next();
			subcategorizationFrameIter = lexicon.getSubcategorizationFrames().iterator();
			subcategorizationFrameSetIter = lexicon.getSubcategorizationFrameSets().iterator();
			lexicalEntryIter = lexicon.getLexicalEntries().iterator();
			semanticPredicateIter = lexicon.getSemanticPredicates().iterator();
			synSemCorrespondenceIter = lexicon.getSynSemCorrespondences().iterator();
			constraintSetIter = lexicon.getConstraintSets().iterator();

			return lexicon;
		} else {
			return null;
		}

	}

	@Override
	protected LexicalEntry getNextLexicalEntry() {
		if (lexicalEntryIter.hasNext()) {
			LexicalEntry lexicalEntry = lexicalEntryIter.next();
			synsetIter = lexicalEntry.getSynsets().iterator();
			return lexicalEntry;
		} else {
			return null;
		}
	}

	/**
	 * Rreturn the next element of the iterator or {@code null} if there is none
	 */
	@Nullable
	private static <T> T next(Iterator<T> iter) {
		if (iter.hasNext()) {
			return iter.next();
		} else {
			return null;
		}
	}

	@Override
	protected SubcategorizationFrame getNextSubcategorizationFrame() {
		return next(subcategorizationFrameIter);
	}

	@Override
	protected SubcategorizationFrameSet getNextSubcategorizationFrameSet() {
		return next(subcategorizationFrameSetIter);
	}

	@Override
	protected SemanticPredicate getNextSemanticPredicate() {
		return next(semanticPredicateIter);
	}

	@Override
	protected Synset getNextSynset() {
		return next(synsetIter);
	}

	@Override
	protected SynSemCorrespondence getNextSynSemCorrespondence() {
		return next(synSemCorrespondenceIter);
	}

	@Override
	protected ConstraintSet getNextConstraintSet() {
		return next(constraintSetIter);
	}

	@Override
	protected SenseAxis getNextSenseAxis() {
		return next(senseAxisIter);
	}

	@Override
	protected void finish() {

	}

	@Override
	protected String getResourceAlias() {
		return lexicalResourceId;
	}

}
