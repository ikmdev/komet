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
package org.liveontologies.puli.pinpointing;

import java.util.Collection;

/**
 * A collection of static methods for working with {@link PriorityComparator}s
 * 
 * @author Yevgeny Kazakov
 *
 */
public class PriorityComparators {

	private PriorityComparators() {
		// Forbid instantiation.
	}

	/**
	 * @return {@link PriorityComparator} that compares collections based on
	 *         their cardinality.
	 */
	public static <E> PriorityComparator<Collection<E>, Integer> cardinality() {
		return new NaturalPriorityComparator<Collection<E>, Integer>() {

			@Override
			public String toString() {
				return PriorityComparator.class.getSimpleName()
						+ ".cardinality";
			}

			@Override
			public Integer getPriority(final Collection<E> original) {
				return original.size();
			}

		};
	}

	/**
	 * @return {@link PriorityComparator} that compares objects based on the
	 *         length of their {@link Object#toString()}.
	 */
	public static <T> PriorityComparator<T, Integer> toStringLength() {
		return new NaturalPriorityComparator<T, Integer>() {

			@Override
			public String toString() {
				return PriorityComparator.class.getSimpleName() + ".length";
			}

			@Override
			public Integer getPriority(final T original) {
				return original.toString().length();
			}

		};
	}

}
