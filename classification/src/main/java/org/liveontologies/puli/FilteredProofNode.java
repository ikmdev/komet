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
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FilteredProofNode<C> extends ConvertedProofNode<C> {

	// logger for this class
	private static final Logger LOGGER_ = LoggerFactory
			.getLogger(FilteredProofNode.class);

	private final Set<? extends ProofNode<C>> forbidden_;

	FilteredProofNode(ProofNode<C> delegate,
			Set<? extends ProofNode<C>> forbidden) {
		super(delegate);
		Objects.requireNonNull(forbidden);
		this.forbidden_ = forbidden;
	}

	@Override
	protected final void convert(ConvertedProofStep<C> step) {
		ProofStep<C> delegate = step.getDelegate();
		for (ProofNode<C> premise : delegate.getPremises()) {
			if (forbidden_.contains(premise)) {
				LOGGER_.trace("{}: ignored: {} is forbiden", delegate, premise);
				return;
			}
		}
		// else
		convert(new FilteredProofStep<C>(delegate, forbidden_));
	}

	void convert(FilteredProofStep<C> step) {
		super.convert(step);
	}

}
