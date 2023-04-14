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
 * Receives notifications about computation of justifications for an entailment.
 * A justification is a minimal subset of axioms for which the entailment holds.
 * 
 * @author Yevgeny Kazakov
 *
 * @param <A>
 *                the type of axioms appearing in justifications reported by
 *                this listener
 */
public interface JustificationListener<A> extends UsefulAxiomListener<A> {

	/**
	 * Notifies that the computation of justification is supported. This means
	 * that for each justification, the methods {@link #newJustificationFound()}
	 * and subsequently {@link #newJustificationComplete()} will be called
	 * exactly once.
	 */
	void computesJustifications();

	/**
	 * Reports that a new justification has been found. All subsequent calls of
	 * {@link #usefulAxiom(Object)} before {@link #newJustificationComplete()}
	 * report the elements of this justification exactly once.
	 */
	void newJustificationFound();

	/**
	 * Reports that the computation of current justification has completed. This
	 * means that after the last call of {@link #newJustificationFound()} for
	 * every element of this justification, the method
	 * {@link #usefulAxiom(Object)} must have been called exactly once.
	 */
	void newJustificationComplete();

}
