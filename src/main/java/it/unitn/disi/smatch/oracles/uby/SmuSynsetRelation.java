/*******************************************************************************
 * Copyright 2015
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package it.unitn.disi.smatch.oracles.uby;

import de.tudarmstadt.ukp.lmf.model.miscellaneous.EVarType;
import de.tudarmstadt.ukp.lmf.model.miscellaneous.VarType;
import de.tudarmstadt.ukp.lmf.model.semantics.SynsetRelation;

/**
 * Holds some extra field for S-Match 
 * 
 * 
 * See <a href="https://github.com/dkpro/dkpro-uby/blob/master/de.tudarmstadt.ukp.uby.lmf.model-asl/src/main/java/de/tudarmstadt/ukp/lmf/model/enums/ERelNameSemantics.java"
 * target="_blank"> ERelNameSemantics</a> for possible relation names.
 * @since 0.1
 */
public class SmuSynsetRelation extends SynsetRelation {
	
	@VarType(type = EVarType.ATTRIBUTE)
	protected int depth;
	
	public SmuSynsetRelation() {
		super();
		this.depth = 0;		
	}

	public int getDepth() {
		return depth;
	}
	
	public void setDepth(int depth) {
		this.depth = depth;
	}
			
}
