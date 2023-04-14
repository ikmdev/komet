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

/**
 * An object using which one can obtain a {@link Proof} for given queries
 * 
 * @author Yevgeny Kazakov
 *
 * @param <Q>
 *                the type of queries supported by this {@link Prover}
 * @param <I>
 *                the type of inferences that this {@link Prover} may use
 */
public interface Prover<Q, I extends Inference<?>> {

	/**
	 * Computes a {@link Proof} deriving the given query.
	 * 
	 * @param query
	 *                  a conclusion that needs to be derived
	 * 
	 * @return a {@link Proof} using which it is possible to derive the given
	 *         query or {@code null} if the query cannot be derived.
	 * 
	 * 
	 * @see Proofs#isDerivable(Proof, Object)
	 * @see Proofs#emptyProof()
	 */
	Proof<? extends I> getProof(Q query);

}
