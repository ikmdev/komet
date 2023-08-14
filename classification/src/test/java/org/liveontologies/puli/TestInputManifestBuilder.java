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

/**
 * Building instances of {@link TestInputManifest}
 * 
 * @author Yevgeny Kazakov
 *
 * @param <Q>
 * @param <A>
 */
public abstract class TestInputManifestBuilder<Q, A>
		extends ProofBuilder<Q, A, TestInputManifestBuilder<Q, A>> implements
		TestInputManifest<Q, A, AxiomPinpointingInference<?, ? extends A>> {

	private String name_;

	private Q query_;

	protected TestInputManifestBuilder() {
		setBuilder(this);
		name(getClass().getSimpleName());
		build();
		Objects.requireNonNull(name_);
		Objects.requireNonNull(query_);
	}

	protected abstract void build();

	@Override
	public Proof<? extends AxiomPinpointingInference<?, ? extends A>> getProof(
			Q query) {
		return getProof();
	}

	public TestInputManifestBuilder<Q, A> query(Q query) {
		this.query_ = query;
		return getBuilder();
	}

	public TestInputManifestBuilder<Q, A> name(String name) {
		this.name_ = name;
		return getBuilder();
	}

	@Override
	public String getName() {
		return name_;
	}

	@Override
	public Q getQuery() {
		return query_;
	}

}
