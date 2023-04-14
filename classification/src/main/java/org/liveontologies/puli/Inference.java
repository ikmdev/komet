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
 * A general type of inferences, which can be used in proofs. If all premises of
 * an inference are provable, then one can prove its conclusion by applying this
 * inference.
 * 
 * @author Yevgeny Kazakov
 *
 * @param <C>
 *            the type of conclusions and premises this inference operates with
 */
public interface Inference<C> {

	/**
	 * @return the name of this inference
	 */
	String getName();

	/**
	 * @return the conclusion that is derived using this inference
	 */
	C getConclusion();

	/**
	 * @return the premises from which the conclusion of this inference is
	 *         derived
	 */
	List<? extends C> getPremises();

}
