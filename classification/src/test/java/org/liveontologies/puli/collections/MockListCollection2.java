package org.liveontologies.puli.collections;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class MockListCollection2<C extends Collection<?>> extends ArrayList<C>
		implements Collection2<C> {

	private static final long serialVersionUID = -359260665565273216L;

	Iterable<C> filter(final Condition<Collection<?>> condition) {
		return new Iterable<C>() {
			@Override
			public Iterator<C> iterator() {
				return new FilteredIterator<C>(
						MockListCollection2.this.iterator(), condition);
			}
		};
	}

	@Override
	public Iterable<C> subCollectionsOf(final Collection<?> s) {
		return filter(new Condition<Collection<?>>() {

			@Override
			public boolean holds(Collection<?> o) {
				return s.containsAll(o);
			}

		});
	}

	@Override
	public Iterable<C> superCollectionsOf(final Collection<?> s) {
		return filter(new Condition<Collection<?>>() {

			@Override
			public boolean holds(Collection<?> o) {
				return o.containsAll(s);
			}

		});
	}

	@Override
	public boolean isMinimal(Collection<?> s) {
		return !subCollectionsOf(s).iterator().hasNext();
	}

	@Override
	public boolean isMaximal(Collection<?> s) {
		return !superCollectionsOf(s).iterator().hasNext();
	}

}
