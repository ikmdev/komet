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

import java.util.Collection;

/**
 * Represents (possibly recursive) derivations for the given conclusion
 * 
 * @author Yevgeny Kazakov
 *
 * @param <C>
 *            the type of conclusions in derivations represented by this node
 */
public interface ProofNode<C> {

	/**
	 * @return the conclusion represented by this {@link ProofNode}
	 */
	C getMember();

	/**
	 * @return the {@link ProofStep}s corresponding to inferences deriving the
	 *         member; the conclusion of each inference must be equal to this
	 *         {@link ProofNode}
	 * 
	 * @see ProofStep#getConclusion()
	 */
	Collection<? extends ProofStep<C>> getInferences();

}
