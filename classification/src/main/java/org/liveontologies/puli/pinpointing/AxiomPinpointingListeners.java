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

import org.liveontologies.puli.AxiomPinpointingInference;
import org.liveontologies.puli.Prover;

/**
 * Collection of static methods for working with
 * {@link AxiomPinpointingListener}s
 * 
 * @author Yevgeny Kazakov
 *
 */
public class AxiomPinpointingListeners {

	@SafeVarargs
	public static <A> AxiomPinpointingListener<A> combine(
			AxiomPinpointingListener<A>... listeners) {
		return new AxiomPinpointingCombinedListener<>(listeners);
	}

	public static <A> AxiomPinpointingListener<A> adapt(
			final UsefulAxiomListener<A> listener) {
		return new UsefulAxiomListenerAdapter<A>(listener);
	}

	public static <A> AxiomPinpointingListener<A> adapt(
			final JustificationListener<A> listener) {
		return new JustificationListenerAdapter<A>(listener);
	}

	public static <A> AxiomPinpointingListener<A> adapt(
			final RepairListener<A> listener) {
		return new RepairListenerAdapter<A>(listener);
	}

	public static <Q, A> ProverAxiomPinpointingEnumerationFactory<Q, A> appendListener(
			final ProverAxiomPinpointingEnumerationFactory<Q, A> factory,
			final AxiomPinpointingListener<A> extraListener) {

		return new ProverAxiomPinpointingEnumerationFactory<Q, A>() {

			@Override
			public String toString() {
				return factory.toString();
			}

			@Override
			public <I extends AxiomPinpointingInference<?, ? extends A>> AxiomPinpointingEnumerator<Q, A> create(
					Prover<? super Q, ? extends I> prover,
					AxiomPinpointingInterruptMonitor monitor) {
				return appendListener(factory.create(prover, monitor),
						extraListener);
			}

		};

	}

	public static <Q, A> AxiomPinpointingEnumerator<Q, A> appendListener(
			final AxiomPinpointingEnumerator<Q, A> enumerator,
			final AxiomPinpointingListener<A> extraListener) {
		return new AxiomPinpointingEnumerator<Q, A>() {

			@Override
			public void enumerate(Q query,
					AxiomPinpointingListener<A> listener) {
				enumerator.enumerate(query, combine(listener, extraListener));
			}

		};

	}

}
