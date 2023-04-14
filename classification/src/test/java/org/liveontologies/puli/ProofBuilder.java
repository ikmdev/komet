/*-
 * #%L
 * Proof Utility Library
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2017 Live Ontologies Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.liveontologies.puli;

public class ProofBuilder<C, A, B extends ProofBuilder<C, A, B>>
		extends AbstractBuilder<B> {

	private final BaseProof<AxiomPinpointingInference<C, A>> proof_ = new BaseProof<>();

	public BaseProof<AxiomPinpointingInference<C, A>> getProof() {
		return proof_;
	}

	public ProofInferenceBuilder conclusion(C conclusion) {
		ProofInferenceBuilder result = new ProofInferenceBuilder();
		return result.conclusion(conclusion);
	}
	
	public class ProofInferenceBuilder extends
			AxiomPinpointingInferenceBuilder<C, A, ProofInferenceBuilder> {

		protected ProofInferenceBuilder() {
			super(INF_NAME);
			setBuilder(this);
		}

		public B add() {
			proof_.produce(build());
			return ProofBuilder.this.getBuilder();
		}

	}

}
