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

import org.liveontologies.puli.AxiomPinpointingInference;
import org.liveontologies.puli.DelegatingProver;
import org.liveontologies.puli.Proof;
import org.liveontologies.puli.Prover;

import java.util.Objects;

/**
 * A skeleton implementation of axiom pinpointing computations from proofs.
 * 
 * A {@link Prover} is a collection of inferences. A derivation for a goal
 * conclusion is a subset of the proof using which the goal can be derived
 * starting from the empty set of conclusions and repeatedly applying the
 * inferences of the derivation. A justification for such a derivation is the
 * union of the axioms assigned to the inferences of this derivation by
 * {@link AxiomPinpointingInference#getJustification()}. A justification for the
 * goal and a {@link Prover} is a minimal justification over all derivations of
 * this goal in the proof. A repair is a minimal hitting set of all
 * justifications.
 * 
 * @author Yevgeny Kazakov
 *
 * @param <Q>
 *                the type of the query
 * @param <A>
 *                the type of axioms used in justification of inferences
 * @param <I>
 *                the type of inferences used in the proof
 */
public abstract class AbstractProofAxiomPinpointingEnumerator<Q, A, I extends AxiomPinpointingInference<?, ? extends A>>
		extends DelegatingProver<Q, I, Prover<? super Q, ? extends I>>
		implements AxiomPinpointingEnumerator<Q, A> {

	private final AxiomPinpointingInterruptMonitor monitor_;

	private final StatusListener statusListener_;

	public AbstractProofAxiomPinpointingEnumerator(
			final Prover<? super Q, ? extends I> prover,
			final AxiomPinpointingInterruptMonitor monitor,
			final StatusListener statusListener) {
		super(prover);
		this.monitor_ = Objects.requireNonNull(monitor);
		this.statusListener_ = Objects.requireNonNull(statusListener);
	}

	public AxiomPinpointingInterruptMonitor getInterruptMonitor() {
		return monitor_;
	}

	public void checkInterrupt() throws AxiomPinpointingInterruptedException {
		if (monitor_.isInterrupted()) {
			throw new AxiomPinpointingInterruptedException();
		}
	}

	@Override
	public void enumerate(Q query, AxiomPinpointingListener<A> listener) {
		Objects.requireNonNull(query);
		try {
			statusListener_.started();
			getQueryEnumerator(query).enumerate(listener);
		} catch (AxiomPinpointingInterruptedException e) {
			// TODO: handle
		} finally {
			statusListener_.finished();
		}
	}

	protected QueryEnumerator getQueryEnumerator(Q query) {
		return new QueryEnumerator(query);
	}

	public class QueryEnumerator {

		private final Q query_;

		private final Proof<? extends I> proof_;

		protected QueryEnumerator(Q query) {
			this.query_ = query;
			statusListener_.proofExtractionStarted();
			this.proof_ = AbstractProofAxiomPinpointingEnumerator.this
					.getProof(query);
			statusListener_.started();
			statusListener_.proofExtractionFinished();
		}

		public Q getQuery() {
			return query_;
		}

		public Proof<? extends I> getProof() {
			return proof_;
		}

		public void enumerate(AxiomPinpointingListener<A> listener) {
			// extend in subclasses
		}

	}

	public interface StatusListener {

		void started();

		void proofExtractionStarted();

		void proofExtractionFinished();

		void finished();

	}

	public static class DummyStatusListener implements StatusListener {

		@Override
		public void started() {
			// no-op
		}

		@Override
		public void proofExtractionStarted() {
			// no-op
		}

		@Override
		public void proofExtractionFinished() {
			// no-op
		}

		@Override
		public void finished() {
			// no-op
		}

	}

}