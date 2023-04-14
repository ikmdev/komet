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
 * Receives notifications about computation of repairs for an entailment. A
 * repair is a minimal subset of axioms without which the entailment does not
 * holds.
 * 
 * @author Yevgeny Kazakov
 *
 * @param <A>
 *                the type of axioms appearing in repairs reported by this
 *                listener
 */
public interface RepairListener<A> extends UsefulAxiomListener<A> {

	/**
	 * Notifies that the computation of repairs is supported. This is needed to
	 * know that all repairs have been computed if there are no repairs, in
	 * which case {@link #newRepairFound()} is never called.
	 */
	void computesRepairs();

	/**
	 * Reports that a new repair has been found. All subsequent calls of
	 * {@link #usefulAxiom(Object)} before {@link #newRepairComplete()} report
	 * the elements of this repair exactly once.
	 */
	void newRepairFound();

	/**
	 * Reports that the computation of current repair has completed. This means
	 * that after the last call of {@link #newRepairFound()} for every element
	 * of this repair, the method {@link #usefulAxiom(Object)} must have been
	 * called exactly once.
	 */
	void newRepairComplete();

}
