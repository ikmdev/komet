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

import java.util.Set;

import org.liveontologies.puli.AxiomPinpointingInference;
import org.liveontologies.puli.TestInputManifest;
import org.liveontologies.puli.TestManifest;

public interface AxiomPinpointingTestManifest<Q, A, I extends AxiomPinpointingInference<?, ? extends A>>
		extends TestManifest {

	/**
	 * @return the input used for computing the pinpointing results. Cannot be
	 *         {@code null}
	 */
	TestInputManifest<Q, A, I> getInput();

	/**
	 * @return the set of all justifications for the input or {@code null} if
	 *         not defined
	 */
	Set<? extends Set<? extends A>> getJustifications();

	/**
	 * @return the set of all repairs for the input or {@code null} if not
	 *         defined
	 */
	Set<? extends Set<? extends A>> getRepairs();

	/**
	 * @return the set of axioms appearing in justifications (and repairs),
	 *         i.e., the union of all justifications (= the union of all
	 *         repairs). This output cannot be {@code null}
	 */
	Set<? extends A> getUsefulAxioms();

}
