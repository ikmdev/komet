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
 * Represents an inference step in which a conclusion represented by a proof
 * node is obtained from premises represented by other proof nodes.
 * 
 * @author Yevgeny Kazakov
 *
 * @param <C>
 *            the type of conclusions and premises this inference operate with
 */
public interface ProofStep<C> extends Inference<ProofNode<C>> {

	/**
	 * @return the inference used in this {@link ProofStep}. The conclusion and
	 *         premises of the inference should be the respective members of the
	 *         conclusion and premises of this {@link ProofStep}, and the name
	 *         should be the same.
	 */
	Inference<? extends C> getInference();

}
