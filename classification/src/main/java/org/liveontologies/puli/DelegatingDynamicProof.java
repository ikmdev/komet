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

public class DelegatingDynamicProof<I extends Inference<?>, S extends DynamicProof<? extends I>>
		extends DelegatingProof<I, S> implements DynamicProof<I> {

	public DelegatingDynamicProof(S delegate) {
		super(delegate);
	}

	@Override
	public void addListener(DynamicProof.ChangeListener listener) {
		getDelegate().addListener(listener);
	}

	@Override
	public void removeListener(DynamicProof.ChangeListener listener) {
		getDelegate().removeListener(listener);
	}

	@Override
	public void dispose() {
		getDelegate().dispose();
	}

}
