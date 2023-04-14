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

import java.util.Objects;

class AcyclicProofStep<C> extends ConvertedProofStep<C> {

	private final AcyclicProofNode<C> conclusion_;

	AcyclicProofStep(ProofStep<C> delegate, AcyclicProofNode<C> conclusion) {
		super(delegate);
		Objects.requireNonNull(conclusion);
		this.conclusion_ = conclusion;
	}

	@Override
	public AcyclicProofNode<C> getConclusion() {
		return conclusion_;
	}

	@Override
	protected AcyclicProofNode<C> convert(ProofNode<C> node) {
		return new AcyclicProofNode<C>(node, conclusion_);
	}

}
