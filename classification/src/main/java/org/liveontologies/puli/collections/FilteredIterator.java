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

import java.util.Iterator;
import java.util.NoSuchElementException;

public class FilteredIterator<E> extends DelegatingIterator<E> {

	private final Condition<? super E> condition_;

	private E next_ = null;

	/**
	 * {@code true} if {@link #next_} is the current value of the delegate
	 * iterator
	 */
	boolean nextInSync = true;

	FilteredIterator(Iterator<E> delegate, Condition<? super E> condition) {
		super(delegate);
		this.condition_ = condition;
		advance();
	}

	void advance() {
		Iterator<E> delegate = getDelegate();
		while (delegate.hasNext()) {
			next_ = delegate.next();
			nextInSync = false;
			if (condition_.holds(next_)) {
				return;
			}
		}
		// not found
		next_ = null;
	}

	@Override
	public void remove() {
		if (nextInSync) {
			getDelegate().remove();
		} else {
			throw new UnsupportedOperationException("remove");
		}
	}

	@Override
	public boolean hasNext() {
		if (nextInSync) {
			advance();
		}
		return next_ != null;
	}

	@Override
	public E next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		nextInSync = true;
		return next_;
	}

}