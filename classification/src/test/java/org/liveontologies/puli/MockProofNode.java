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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Pavel Klinov pavel.klinov@uni-ulm.de
 * 
 * @author Yevgeny Kazakov
 *
 * @param <C>
 *            the type of the member of this node
 */
public class MockProofNode<C> implements ProofNode<C> {

	private final C member_;

	private final List<MockProofStep<C>> inferences_;

	public static <C> MockProofNode<C> create(C member,
			List<MockProofStep<C>> inferences) {
		return new MockProofNode<C>(member, inferences);
	}

	public static <C> MockProofNode<C> create(C member) {
		return new MockProofNode<C>(member);
	}

	private MockProofNode(C member, List<MockProofStep<C>> inferences) {
		member_ = member;
		inferences_ = inferences;
	}

	private MockProofNode(C member) {
		this(member, new ArrayList<MockProofStep<C>>());
	}

	public MockProofNode<C> addInference(MockProofStep<C> inf) {
		inferences_.add(inf);
		return this;
	}

	@Override
	public C getMember() {
		return member_;
	}

	@Override
	public Collection<? extends ProofStep<C>> getInferences() {
		return inferences_;
	}

	@Override
	public String toString() {
		return member_.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((member_ == null) ? 0 : member_.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;

		if (obj instanceof MockProofNode<?>) {
			// unwrapping
			return member_.equals(((MockProofNode<?>) obj).member_);
		}
		// else
		return false;
	}

}
