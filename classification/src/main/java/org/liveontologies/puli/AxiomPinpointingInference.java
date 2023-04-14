package org.liveontologies.puli;

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

import java.util.Set;

/**
 * A inference justified by a set of axioms
 * 
 * @author Yevgeny Kazakov
 *
 * @param <C>
 *            The type of conclusions and premises this inference operate with
 * @param <A>
 *            The type of axioms from inference justifications.
 */
public interface AxiomPinpointingInference<C, A>
		extends JustifiedInference<C, Set<? extends A>> {

	/**
	 * @return The set of axioms responsible for this inference to take place
	 */
	@Override
	Set<? extends A> getJustification();

}
