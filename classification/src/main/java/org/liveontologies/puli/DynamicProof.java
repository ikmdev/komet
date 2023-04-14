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
 * An {@link Proof}, changes in which can be monitored
 * 
 * @author Yevgeny Kazakov
 *
 * @param <I>
 *            the type of inferences provided by this proof
 */
public interface DynamicProof<I extends Inference<?>> extends Proof<I> {

	public void addListener(ChangeListener listener);

	public void removeListener(ChangeListener listener);

	/**
	 * Release external resources occupied by this {@link Proof}. This
	 * {@link Proof} should not be used after calling of this method
	 */
	public void dispose();

	/**
	 * A listener to monitor if inferences have changed
	 * 
	 * @author Yevgeny Kazakov
	 *
	 */
	public interface ChangeListener {

		/**
		 * called whenever the inferences already returned for some conclusions
		 * by {@link Proof#getInferences(Object)} may have changed, i.e.,
		 * calling this method again with the same input may produce a different
		 * result
		 */
		void inferencesChanged();

	}

}
