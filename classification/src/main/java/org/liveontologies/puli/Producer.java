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
 * A general interface for object through which objects of a particular types
 * can be produced
 * 
 * @author Yevgeny Kazakov
 *
 * @param <O>
 *            the types of objects that can be produced by this {@link Producer}
 */
public interface Producer<O> {

	void produce(O object);
	
	static class Dummy<O> implements Producer<O> {
		
		private static Producer<Object> INSTANCE_ = new Dummy<Object>();

		@SuppressWarnings("unchecked")
		public static <O> Producer<O> get() {
			return (Producer<O>) INSTANCE_;
		}
		
		@Override
		public void produce(O object) {
			// no-op
		}
		
	}

}
