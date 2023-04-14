package org.liveontologies.puli;

/*-
 * #%L
 * Proof Utility Library
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2022 Live Ontologies Project
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

public class DelegatingProver<Q, I extends Inference<?>, P extends Prover<? super Q, ? extends I>>
		extends Delegator<P> implements Prover<Q, I> {

	public DelegatingProver(P delegate) {
		super(delegate);
	}

	@Override
	public Proof<? extends I> getProof(Q query) {
		return getDelegate().getProof(query);
	}

}
