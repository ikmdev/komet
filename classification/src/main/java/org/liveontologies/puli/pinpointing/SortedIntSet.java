/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.liveontologies.puli.pinpointing;

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

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.collections.impl.factory.primitive.IntLists;

public class SortedIntSet extends AbstractSet<Integer> {

	private final int[] elements_; // sorted!

	SortedIntSet(int[] elements) {
		this.elements_ = elements;
	}

	int[] getElements() {
		return elements_;
	}

	@Override
	public boolean contains(Object o) {
		if (o instanceof Integer) {
			return Arrays.binarySearch(elements_, (Integer) o) >= 0;
		}
		// else
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		if (c instanceof SortedIntSet) {
			SortedIntSet other = (SortedIntSet) c;
			return SortedIdSet.containsAll(elements_, other.elements_);
		}
		return super.containsAll(c);
	}

	@Override
	public Iterator<Integer> iterator() {
		return IntLists.immutable.of(elements_).primitiveStream().iterator();
	}

	@Override
	public int size() {
		return elements_.length;
	}

}
