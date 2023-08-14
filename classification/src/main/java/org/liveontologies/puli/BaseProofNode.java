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

import java.util.ArrayList;
import java.util.Collection;

import java.util.Objects;

class BaseProofNode<C> extends AbstractProofNode<C> {

	private final Proof<? extends Inference<? extends C>> proof_;

	private Collection<ProofStep<C>> steps_ = null;

	BaseProofNode(Proof<? extends Inference<? extends C>> proof, C member) {
		super(member);
		Objects.requireNonNull(proof);
		this.proof_ = proof;
	}

	public Proof<? extends Inference<? extends C>> getProof() {
		return proof_;
	}

	@Override
	public Collection<? extends ProofStep<C>> getInferences() {
		if (steps_ == null) {
			Collection<? extends Inference<? extends C>> original = proof_
					.getInferences(getMember());
			steps_ = new ArrayList<ProofStep<C>>(original.size());
			for (Inference<? extends C> inf : original) {
				convert(inf);
			}
		}
		return steps_;
	}

	void convert(Inference<? extends C> inf) {
		steps_.add(new BaseProofStep<C>(proof_, inf));
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof BaseProofNode) {
			BaseProofNode<?> other = (BaseProofNode<?>) o;
			return getMember().equals(other.getMember())
					&& proof_.equals(other.proof_);
		}
		// else
		return false;
	}

	@Override
	public int hashCode() {
		return BaseProofNode.class.hashCode() + getMember().hashCode()
				+ proof_.hashCode();
	}

}
