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

import dev.ikm.tinkar.common.validation.Validate;

import java.util.Collection;
import java.util.Set;

import java.util.Objects;

class DerivableFromProofNode<C> extends DerivableProofNode<C> {

	DerivableFromProofNode(ProofNode<C> delegate,
			DerivabilityChecker<ProofNode<?>, ProofStep<?>> checker) {
		super(delegate, checker);
	}

	DerivableFromProofNode(ProofNode<C> delegate,
			Set<? extends C> statedAxioms) {
		this(new AddAssertedProofNode<C>(delegate, statedAxioms),
				new ProofNodeDerivabilityChecker());
	}

	@Override
	public Collection<ProofStep<C>> getInferences() {
		Collection<ProofStep<C>> result = super.getInferences();
		Validate.isTrue(!result.isEmpty());
		return result;
	}

	@Override
	final void convert(DerivableProofStep<C> step) {
		ProofStep<C> delegate = step.getDelegate();
		if (Inferences.isAsserted(delegate)) {
			return;
		}
		// else
		convert(new DerivableFromProofStep<C>(delegate,
				getDerivabilityChecker()));
	}

	void convert(DerivableFromProofStep<C> step) {
		super.convert(step);
	}

}
