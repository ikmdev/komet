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

/**
 * Receives notifications about computation of useful axioms for an entailment.
 * An axiom is useful if it appears in at least one justification (equivalently:
 * in at least one repair).
 * 
 * @author Yevgeny Kazakov
 *
 * @param <A>
 *                the type of useful axioms reported by this listener
 * 
 * @see JustificationListener
 * @see RepairListener
 */
public interface UsefulAxiomListener<A> {

	/**
	 * Notifies about a found useful axioms. The same axiom can be reported
	 * several times.
	 * 
	 * @param axiom
	 *                  an axiom that appears in some justification
	 *                  (equivalently: some repair)
	 */
	void usefulAxiom(A axiom);

	/**
	 * Notifies that all useful axioms have been computed. For each of these
	 * axioms the method {@link #usefulAxiom(Object)} should have been
	 * previously called at least once.
	 */
	void computationComplete();

}
