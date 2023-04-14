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
import java.util.Set;

import java.util.function.Function;
import org.eclipse.collections.impl.factory.primitive.IntLists;

public class SortedIdSet<E> extends AbstractSet<E>
		implements Function<Integer, E> {

	private final int[] elementIds_;

	private final IdMap<E> idMap_;

	SortedIdSet(int[] elementIds, IdMap<E> idMap) {
		this.elementIds_ = elementIds;
		this.idMap_ = idMap;
	}

	int[] getElementIds() {
		return elementIds_;
	}

	IdMap<E> getIdMap() {
		return idMap_;
	}

	@Override
	public boolean contains(Object o) {
		Integer id = idMap_.contains(o);
		if (id == null) {
			return false;
		}
		// else
		return Arrays.binarySearch(elementIds_, id) >= 0;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		if (c instanceof SortedIdSet<?>) {
			SortedIdSet<?> other = (SortedIdSet<?>) c;
			if (idMap_.equals(other.idMap_)) {
				return containsAll(elementIds_, other.elementIds_);
			}
		}
		return super.containsAll(c);
	}

	@Override
	public Iterator<E> iterator() {
		return IntLists.immutable.of(elementIds_).primitiveStream().mapToObj(value -> apply(value)).iterator();
	}

	@Override
	public int size() {
		return elementIds_.length;
	}

	@Override
	public E apply(Integer id) {
		return idMap_.getElement(id);
	}

	// TODO: move to some utilities

	static int[] getIds(Collection<? extends Integer> set) {
		int[] ids = new int[set.size()];
		int pos = 0;
		for (int e : set) {
			ids[pos++] = e;
		}
		Arrays.sort(ids);
		return ids;
	}

	static <E> int[] getIds(Collection<? extends E> set, IdMap<E> idMap) {
		int[] ids = new int[set.size()];
		int pos = 0;
		for (E e : set) {
			ids[pos++] = idMap.getId(e);
		}
		Arrays.sort(ids);
		return ids;
	}

	static <E> SortedIdSet<E> copyOf(Set<E> set, IdMap<E> idMap) {
		return new SortedIdSet<E>(getIds(set, idMap), idMap);
	}

	static boolean containsAll(int[] first, int[] second) {
		if (second.length > first.length) {
			return false;
		}
		int i = 0;
		for (int j = 0; j < second.length; j++) {
			int y = second[j];
			for (;;) {
				if (i == first.length) {
					return false;
				}
				int x = first[i];
				if (x > y) {
					return false;
				}
				if (x == y) {
					break;
				}
				i++;
			}
		}
		return true;
	}

	static int[] union(int[] first, int[] second) {
		int[] tmp = new int[first.length + second.length];
		int i = 0;
		int j = 0;
		int pos = 0;
		for (;;) {
			if (i == first.length) {
				int copied = second.length - j;
				System.arraycopy(second, j, tmp, pos, copied);
				pos += copied;
				break;
			}
			// else
			if (j == second.length) {
				int copied = first.length - i;
				System.arraycopy(first, i, tmp, pos, copied);
				pos += copied;
				break;
			}
			// else
			int x = first[i];
			int y = second[j];
			if (x < y) {
				tmp[pos] = x;
				i++;
			} else {
				tmp[pos] = y;
				j++;
				if (x == y) {
					i++;
				}
			}
			pos++;
		}
		if (pos == first.length) {
			return first;
		}
		// else
		if (pos == second.length) {
			return second;
		}
		// else
		if (pos == tmp.length) {
			return tmp;
		}
		// else
		return Arrays.copyOf(tmp, pos);
	}

}
