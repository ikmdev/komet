/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.liveontologies.puli;

import java.util.Objects;

class DerivableProofStep<C> extends ConvertedProofStep<C> {

	private final DerivabilityChecker<ProofNode<?>, ProofStep<?>> checker_;

	DerivableProofStep(ProofStep<C> delegate,
			DerivabilityChecker<ProofNode<?>, ProofStep<?>> checker) {
		super(delegate);
		Objects.requireNonNull(checker);
		this.checker_ = checker;
	}

	DerivabilityChecker<ProofNode<?>, ProofStep<?>> getDerivabilityChecker() {
		return checker_;
	}

	@Override
	protected DerivableProofNode<C> convert(ProofNode<C> premise) {
		return new DerivableProofNode<C>(premise, checker_);
	}

}
