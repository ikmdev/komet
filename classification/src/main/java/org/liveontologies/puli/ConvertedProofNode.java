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

import java.util.Collection;
import java.util.HashSet;

public class ConvertedProofNode<C> extends DelegatingProofNode<C> {

	private Collection<ProofStep<C>> inferences_;

	protected ConvertedProofNode(ProofNode<C> delegate) {
		super(delegate);
	}

	@Override
	public Collection<ProofStep<C>> getInferences() {
		if (inferences_ == null) {
			convertInferences();
		}
		return inferences_;
	}

	protected void convertInferences() {
		Collection<? extends ProofStep<C>> original = super.getInferences();
		inferences_ = new HashSet<ProofStep<C>>(original.size());
		for (ProofStep<C> step : original) {
			convert(step);
		}
	}

	final void convert(ProofStep<C> step) {
		convert(new ConvertedProofStep<C>(step));
	}

	protected void convert(ConvertedProofStep<C> step) {
		inferences_.add(step);
	}

}
