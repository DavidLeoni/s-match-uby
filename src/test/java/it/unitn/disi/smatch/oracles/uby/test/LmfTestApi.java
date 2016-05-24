package it.unitn.disi.smatch.oracles.uby.test;

import de.tudarmstadt.ukp.lmf.model.core.LexicalResource;
import de.tudarmstadt.ukp.lmf.model.core.Lexicon;
import de.tudarmstadt.ukp.lmf.model.semantics.Synset;
import it.unitn.disi.smatch.oracles.uby.SmuNotFoundException;
import it.unitn.disi.smatch.oracles.uby.SmuUtils;
 

public class LmfTestApi {
    /**
     * 
     * Retrieves the synset with id  'synset ' + {@code idNum}
     * 
     * @param idNum index starts from 1
     * @throws SmuNotFoundException
     */
	public static Synset getSynset(LexicalResource lr, int idNum ){
		SmuUtils.checkArgument(idNum >= 1, "idNum must be positive, found instead " + idNum);
		
		for (Lexicon lexicon : lr.getLexicons()){
		    for (Synset synset : lexicon.getSynsets()){
		        if (synset.getId().equals("synset " + idNum)){
		            return synset;
		        }
		    }
		}
		throw new SmuNotFoundException("Couldn't find synset with id 'synset " + idNum);
	}
}
