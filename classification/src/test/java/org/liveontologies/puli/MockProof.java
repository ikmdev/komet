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

import java.util.HashMap;
import java.util.Map;

public class MockProof<C> {

	private static final String INF_NAME = "inf";

	private final Map<C, MockProofNode<C>> proofNodes_ = new HashMap<C, MockProofNode<C>>();

	private MockProof() {
		// use create()
	}

	public static <C> MockProof<C> create() {
		return new MockProof<C>();
	}

	public ProofNode<C> getNode(C member) {
		return proofNodes_.get(member);
	}

	public MockProofNode<C> getCreateProofNode(C member) {
		MockProofNode<C> result = proofNodes_.get(member);
		if (result != null) {
			return result;
		}
		// else
		result = MockProofNode.create(member);
		proofNodes_.put(member, result);
		return result;
	}

	public MockProofStepBuilder conclusion(C conclusion) {
		MockProofNode<C> node = getCreateProofNode(conclusion);
		MockProofStep<C> inf = MockProofStep.create(INF_NAME, node);
		node.addInference(inf);
		return new MockProofStepBuilder(inf);
	}

	public class MockProofStepBuilder {
		private final MockProofStep<C> inference_;

		MockProofStepBuilder(MockProofStep<C> inference) {
			this.inference_ = inference;
		}

		public MockProofStepBuilder premise(C premise) {
			ProofNode<C> node = getCreateProofNode(premise);
			inference_.addPremise(node);
			return this;
		}

		public ProofStep<C> build() {
			return inference_;
		}

	}

}
