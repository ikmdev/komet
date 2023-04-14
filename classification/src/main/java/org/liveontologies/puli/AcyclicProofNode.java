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

import java.util.HashSet;
import java.util.Set;

class AcyclicProofNode<C> extends ConvertedProofNode<C> {

	private final AcyclicProofNode<C> parent_;

	private final Set<ProofNode<C>> blockedNodes_;

	AcyclicProofNode(ProofNode<C> delegate, AcyclicProofNode<C> parent) {
		super(delegate);
		this.parent_ = parent;
		blockedNodes_ = new HashSet<ProofNode<C>>();
		AcyclicProofNode<C> blocked = this;
		do {
			blockedNodes_.add(blocked.getDelegate());
			blocked = blocked.parent_;
		} while (blocked != null);
	}

	AcyclicProofNode(ProofNode<C> delegate) {
		this(delegate, null);
	}

	AcyclicProofNode<C> getParent() {
		return parent_;
	}

	@Override
	protected final void convert(ConvertedProofStep<C> step) {
		ProofStep<C> delegate = step.getDelegate();
		for (ProofNode<C> premise : delegate.getPremises()) {
			if (blockedNodes_.contains(premise)) {
				return;
			}
		}
		// else
		convert(new AcyclicProofStep<C>(delegate, this));
	}

	void convert(AcyclicProofStep<C> step) {
		super.convert(step);
	}

}
