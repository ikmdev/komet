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
package org.liveontologies.puli;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A special inference representing that a conclusion is derivable from no
 * premises; usually it means that the conclusion is preset in the initial set
 * from which other conclusions are derived.
 * 
 * @author Yevgeny Kazakov
 *
 * @param <C>
 *            the type of conclusions and premises of this inference
 */
public class AssertedConclusionInference<C> extends Delegator<C>
		implements AxiomPinpointingInference<C, C> {

	public static String NAME = "Asserted Conclusion";

	public AssertedConclusionInference(C conclusion) {
		super(conclusion);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public C getConclusion() {
		return getDelegate();
	}

	@Override
	public List<? extends C> getPremises() {
		return Collections.emptyList();
	}

	@Override
	public String toString() {
		return Inferences.toString(this);
	}

	@Override
	public Set<? extends C> getJustification() {
		return Collections.singleton(getConclusion());
	}

}
