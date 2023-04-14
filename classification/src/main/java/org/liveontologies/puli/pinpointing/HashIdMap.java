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

import org.eclipse.collections.api.bimap.MutableBiMap;
import org.eclipse.collections.api.factory.BiMaps;
import org.eclipse.collections.impl.bimap.mutable.HashBiMap;

public class HashIdMap<E> implements IdMap<E> {
//TODO if we only use integers for ids, then maybe this map goes away...
	MutableBiMap<E, Integer> baseBiMap_;

	int nextId_ = 0;

	private HashIdMap() {
		baseBiMap_ = BiMaps.mutable.empty();
	}

	private HashIdMap(int expectedSize) {
		baseBiMap_ = new HashBiMap(expectedSize);
	}

	public static <E> IdMap<E> create() {
		return new HashIdMap<E>();
	}

	public static <E> IdMap<E> create(int expectedSize) {
		return new HashIdMap<E>(expectedSize);
	}

	@Override
	public int getId(E element) {
		Integer result = baseBiMap_.get(element);
		if (result != null) {
			return result;
		}
		// else
		baseBiMap_.put(element, nextId_);
		return (nextId_++);
	}

	@Override
	public E getElement(int id) {
		return baseBiMap_.inverse().get(id);
	}

	@Override
	public Integer contains(Object o) {
		return baseBiMap_.get(o);
	}

}
