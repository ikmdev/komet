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

/**
 * An {@link JustifiedInference} that delegates all method calls to the given
 * {@link JustifiedInference}.
 * 
 * @author Yevgeny Kazakov
 *
 * @param <C>
 *            The type of conclusions and premises this inference operates with.
 * @param <J>
 *            The type of justifications of the inferences.
 * @param <I>
 *            The type of the delegated inference.
 */
public class DelegatingJustifiedInference<C, J, I extends JustifiedInference<? extends C, ? extends J>>
		extends DelegatingInference<C, I> implements JustifiedInference<C, J> {

	public DelegatingJustifiedInference(I delegate) {
		super(delegate);
	}

	@Override
	public J getJustification() {
		return getDelegate().getJustification();
	}

}
