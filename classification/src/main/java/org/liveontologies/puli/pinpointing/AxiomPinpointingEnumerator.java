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
package org.liveontologies.puli.pinpointing;

/**
 * Performs enumeration of justifications and repairs for queries. The results
 * are reported using an {@link AxiomPinpointingListener}.
 * 
 * @author Yevgeny Kazakov
 * @author Peter Skocovsky
 *
 * @param <Q>
 *                the type of the query
 * @param <A>
 *                the type of axioms appearing in justifications and / or
 *                repairs
 */
public interface AxiomPinpointingEnumerator<Q, A> {

	/**
	 * Performs the axiom pinpointing enumeration whose results are reported
	 * using the provided {@link AxiomPinpointingListener}. If
	 * {@link AxiomPinpointingListener#computesJustifications()} is called, then
	 * the enumeration reports all computed justifications in subsequent calls
	 * of {@link AxiomPinpointingListener#newJustificationFound()}. If
	 * {@link AxiomPinpointingListener#computesRepairs()} is called, then the
	 * enumeration reports all computed repairs in subsequent calls of
	 * {@link AxiomPinpointingListener#newRepairFound()}. When
	 * {@link AxiomPinpointingListener#computationComplete()} is called, all
	 * justifications and / or all repairs (if supported) must have been
	 * reported.
	 * 
	 * @param query
	 *                     the query for which the results of axiom pinpointing
	 *                     are reported
	 * 
	 * @param listener
	 *                     The listener that is notified about the results of
	 *                     axiom pinpointing
	 */
	void enumerate(Q query, AxiomPinpointingListener<A> listener);

}
