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

import java.util.List;

/**
 * An {@link Inference} that delegates all method calls to the given
 * {@link Inference}
 * 
 * @author Yevgeny Kazakov
 *
 * @param <C>
 *            the type of conclusions and premises this inference operates with
 * @param <I>
 *            the type of the delegated inference
 */
public class DelegatingInference<C, I extends Inference<? extends C>>
		extends Delegator<I> implements Inference<C> {

	public DelegatingInference(I delegate) {
		super(delegate);
	}

	@Override
	public String getName() {
		return getDelegate().getName();
	}

	@Override
	public C getConclusion() {
		return getDelegate().getConclusion();
	}

	@Override
	public List<? extends C> getPremises() {
		return getDelegate().getPremises();
	}

}
