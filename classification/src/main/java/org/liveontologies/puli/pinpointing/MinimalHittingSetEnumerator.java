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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.liveontologies.puli.AxiomPinpointingInference;
import org.liveontologies.puli.Proof;
import org.liveontologies.puli.Prover;


/**
 * Computes the minimal hitting sets of the given collection of sets.
 * 
 * A hitting set of the collection of sets s1, s2,..., sn is a set containing
 * one element from each of these sets. A minimal hitting set is a hitting set
 * such that all its proper subsets are not hitting sets. For example, the
 * minimal hitting sets of the sets s1 = {a, b} and s2 = {b, c} are {a, c} and
 * {b}.
 * 
 * @author Peter Skocovsky
 * @author Yevgeny Kazakov
 *
 * @param <E>
 */
public class MinimalHittingSetEnumerator<E> implements
		AxiomPinpointingEnumerator<Collection<? extends Set<? extends E>>, E> {

	private static final Object CONCLUSION_ = new Object();

	private final ProverAxiomPinpointingEnumerationFactory<Object, E> repairComputationFactory_;

	private final AxiomPinpointingInterruptMonitor monitor_;

	public MinimalHittingSetEnumerator(
			final ProverAxiomPinpointingEnumerationFactory<Object, E> repairComputationFactory,
			final AxiomPinpointingInterruptMonitor monitor) {
		this.repairComputationFactory_ = repairComputationFactory;
		this.monitor_ = monitor;
	}

	@Override
	public void enumerate(final Collection<? extends Set<? extends E>> query,
			final AxiomPinpointingListener<E> listener) {
		repairComputationFactory_
				.create(new Prover<Object, SetWrapperInference>() {

					@Override
					public Proof<? extends SetWrapperInference> getProof(
							Object ignore) {
						return new SetWrapperProof(query);
					}

				}, monitor_).enumerate(CONCLUSION_, listener);
	}

	private class SetWrapperProof implements Proof<SetWrapperInference> {

		private final Collection<? extends Set<? extends E>> originalSets_;

		private SetWrapperProof(
				final Collection<? extends Set<? extends E>> originalSets) {
			this.originalSets_ = originalSets;
		}

		@Override
		public Collection<? extends SetWrapperInference> getInferences(
				final Object conclusion) {
			if (conclusion == CONCLUSION_) {
				return originalSets_.stream().map(es -> new SetWrapperInference(es)).toList();
			}
			// else
			return Collections.emptySet();
		}

	}

	private class SetWrapperInference
			implements AxiomPinpointingInference<Object, E> {

		private final Set<? extends E> originalSet_;

		private SetWrapperInference(final Set<? extends E> originalSet) {
			this.originalSet_ = originalSet;
		}

		@Override
		public String getName() {
			return getClass().getSimpleName();
		}

		@Override
		public Object getConclusion() {
			return CONCLUSION_;
		}

		@Override
		public List<? extends Object> getPremises() {
			return Collections.emptyList();
		}

		@Override
		public Set<? extends E> getJustification() {
			return originalSet_;
		}

	}

	public static <E> Collection<? extends Set<? extends E>> compute(
			final Collection<? extends Set<? extends E>> sets) {

		AxiomPinpointingCollector<E> collector = new AxiomPinpointingCollector<E>();

		new MinimalHittingSetEnumerator<E>(
				TopDownRepairComputation.<Object, E> getFactory(),
				AxiomPinpointingInterruptMonitor.DUMMY).enumerate(sets,
						collector);

		return collector.getRepairs();
	}

}
