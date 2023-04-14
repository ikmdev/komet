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

import java.util.List;

public class DelegatingProofStep<C> extends Delegator<ProofStep<C>>
		implements ProofStep<C> {

	private int hashCode_ = 0;

	protected DelegatingProofStep(ProofStep<C> delegate) {
		super(delegate);
	}

	@Override
	public String getName() {
		return getDelegate().getName();
	}

	@Override
	public ProofNode<C> getConclusion() {
		return getDelegate().getConclusion();
	}

	@Override
	public List<? extends ProofNode<C>> getPremises() {
		return getDelegate().getPremises();
	}

	@Override
	public Inference<? extends C> getInference() {
		return getDelegate().getInference();
	}

	@Override
	public boolean equals(Object o) {
		return Inferences.equals(this, o);
	}

	@Override
	public synchronized int hashCode() {
		if (hashCode_ == 0) {
			hashCode_ = Inferences.hashCode(this);
		}
		return hashCode_;
	}

	@Override
	public String toString() {
		return Inferences.toString(this);
	}

}
