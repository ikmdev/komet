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
import org.liveontologies.puli.Proof;
import org.liveontologies.puli.Prover;

/**
 * A factory for creating axiom pinpointing computations for a {@link Prover}
 * which derives conclusions using {@link AxiomPinpointingInference}s
 * 
 * A derivation for a goal conclusion is a subset of the proof using which the
 * goal can be derived starting from the empty set of conclusions and repeatedly
 * applying the inferences of the derivation. A justification for such a
 * derivation is the union of the axioms assigned to the inferences of this
 * derivation by {@link AxiomPinpointingInference#getJustification()}. A
 * justification for the goal and a {@link Proof} is a minimal justification
 * over all derivations of this goal in the proof. A repair is a minimal set of
 * axioms such that the goal conclusion has no derivation whose justification is
 * disjoint with this set. Equivalently, it is a minimal hitting set of all
 * justifications.
 * 
 * @author Yevgeny Kazakov
 * @author Peter Skocovsky
 * 
 * @param <Q>
 *                the type of the query
 * @param <A>
 *                the type of axioms used by the inferences
 */
public interface ProverAxiomPinpointingEnumerationFactory<Q, A> {

	/**
	 * Creates an {@link AxiomPinpointingEnumerator} for the given proof
	 * 
	 * @param prover
	 *                    a {@link Prover} which derives conclusions using
	 *                    {@link AxiomPinpointingInference}s
	 * @param monitor
	 *                    an {@link AxiomPinpointingInterruptMonitor} using
	 *                    which the computation can be notified if it needs to
	 *                    be interrupted
	 * 
	 * @return a new {@link AxiomPinpointingEnumerator} using which
	 *         justifications and repairs of the proofs can be reported
	 * 
	 */
	<I extends AxiomPinpointingInference<?, ? extends A>> AxiomPinpointingEnumerator<Q, A> create(
			Prover<? super Q, ? extends I> prover,
			AxiomPinpointingInterruptMonitor monitor);

}
