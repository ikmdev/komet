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
package org.liveontologies.puli.pinpointing;

/*-
 * #%L
 * Proof Utility Library
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2021 Live Ontologies Project
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import dev.ikm.tinkar.common.validation.Validate;
import org.liveontologies.puli.AbstractBuilder;
import org.liveontologies.puli.AxiomPinpointingInference;
import org.liveontologies.puli.TestInputManifest;

import java.util.Objects;

/**
 * Builds {@link AxiomPinpointingTestManifest}
 * 
 * @author Yevgeny Kazakov
 *
 * @param <Q>
 * @param <A>
 * @param <I>
 */
public abstract class AxiomPinpointingTestManifestBuilder<Q, A, I extends AxiomPinpointingInference<?, ? extends A>>
		extends AbstractBuilder<AxiomPinpointingTestManifestBuilder<Q, A, I>>
		implements AxiomPinpointingTestManifest<Q, A, I> {

	private TestInputManifest<Q, A, I> input_;

	private Set<Set<? extends A>> justifications_, repairs_;
	private Set<A> usefulAxioms_;

	protected AxiomPinpointingTestManifestBuilder() {
		setBuilder(this);
		build();
		Objects.requireNonNull(input_);
	}

	protected abstract void build();

	public AxiomPinpointingTestManifestBuilder<Q, A, I> input(
			TestInputManifest<Q, A, I> input) {
		this.input_ = input;
		return getBuilder();
	}

	/**
	 * States that the given axioms form one of the justifications
	 * 
	 * @param axioms
	 * @return this builder
	 */
	@SuppressWarnings("unchecked")
	public AxiomPinpointingTestManifestBuilder<Q, A, I> justification(
			A... axioms) {
		if (justifications_ == null) {
			justifications_ = new HashSet<>();
		}
		justifications_.add(new HashSet<>(Arrays.asList(axioms)));
		addUsefulAxioms(axioms);
		return getBuilder();
	}

	/**
	 * States that the given axioms form one of the repairs
	 * 
	 * @param axioms
	 * @return this builder
	 */
	@SuppressWarnings("unchecked")
	public AxiomPinpointingTestManifestBuilder<Q, A, I> repair(A... axioms) {
		if (repairs_ == null) {
			repairs_ = new HashSet<>();
		}
		repairs_.add(new HashSet<>(Arrays.asList(axioms)));
		addUsefulAxioms(axioms);
		return getBuilder();
	}

	private void addUsefulAxioms(A[] axioms) {
		if (usefulAxioms_ == null) {
			usefulAxioms_ = new HashSet<>();
		}
		for (int i = 0; i < axioms.length; i++) {
			usefulAxioms_.add(axioms[i]);
		}
	}

	/**
	 * Lists all useful axioms, i.e., all axioms that belong to repairs and
	 * justifications.
	 * 
	 * @param axioms
	 * @return this builder
	 */
	@SuppressWarnings("unchecked")
	public AxiomPinpointingTestManifestBuilder<Q, A, I> usefulAxioms(
			A... axioms) {
		Validate.isTrue(usefulAxioms_ == null,
				"Useful axioms should not be provided when justifications or repairs are given");
		usefulAxioms_ = Collections
				.unmodifiableSet(new HashSet<>(Arrays.asList(axioms)));
		return this;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public String getName() {
		return input_.getName();
	}

	@Override
	public TestInputManifest<Q, A, I> getInput() {
		return input_;
	}

	@Override
	public Set<? extends Set<? extends A>> getJustifications() {
		if (justifications_ == null && repairs_ != null) {
			justifications_ = new HashSet<>(
					MinimalHittingSetEnumerator.compute(repairs_));
		}
		return justifications_;
	}

	@Override
	public Set<? extends Set<? extends A>> getRepairs() {
		if (repairs_ == null && justifications_ != null) {
			repairs_ = new HashSet<>(
					MinimalHittingSetEnumerator.compute(justifications_));
		}
		return repairs_;
	}

	@Override
	public Set<? extends A> getUsefulAxioms() {
		return usefulAxioms_ == null ? Collections.emptySet()
				: usefulAxioms_;
	}

}
