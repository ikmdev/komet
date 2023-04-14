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

import java.util.Collections;
import java.util.Set;

/**
 * Adapts the given {@link Inference} to an {@link AxiomPinpointingInference}
 * whose justification is the singleton conclusion for asserted inferences and
 * the empty set for other inferences.
 * 
 * @author Yevgeny Kazakov
 *
 * @param <C>
 *            The type of conclusions and premises this inference operate with
 * 
 * @see Inferences#isAsserted(Inference)
 */
public class AssertedAxiomPinpointingInferenceAdapter<C>
		extends AxiomPinpointingInferenceAdapter<C, C>
		implements AxiomPinpointingInference<C, C> {

	public AssertedAxiomPinpointingInferenceAdapter(
			Inference<? extends C> delegate) {
		super(delegate);
	}

	@Override
	public Set<? extends C> getJustification() {
		return Inferences.isAsserted(getDelegate())
				? Collections.singleton(getDelegate().getConclusion())
				: Collections.emptySet();
	}

}
