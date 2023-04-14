package org.liveontologies.puli;

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

import java.util.Set;

/**
 * A {@link DerivabilityChecker} that can check if a conclusion is derivable
 * without using a given set of "blocked" conclusions. That is,
 * {@link #isDerivable(Object)} returns {@code true} if there exists a
 * derivation for the conclusion that does not used the conclusions in
 * {@link #getBlockedConclusions()}
 * 
 * @author Yevgeny Kazakov
 *
 * @param <C>
 *            the type of conclusions
 * @param <I>
 *            the type of inferences
 */
public interface DerivabilityCheckerWithBlocking<C, I extends Inference<? extends C>>
		extends DerivabilityChecker<C, I> {

	/**
	 * @return the set of conclusions that cannot be used in derivations when
	 *         checking for
	 */
	public Set<C> getBlockedConclusions();

	/**
	 * Prevent the given conclusion from being used in derivations
	 * 
	 * @param conclusion
	 * @return {@code true} if the set of blocked conclusions has changed as a
	 *         result of this operation and {@code false} otherwise
	 */
	public boolean block(C conclusion);

	/**
	 * Allow the given conclusion to be used in derivations
	 * 
	 * @param conclusion
	 * @return {@code true} if the set of blocked conclusions has changed as a
	 *         result of this operation and {@code false} otherwise
	 */
	public boolean unblock(C conclusion);

}
