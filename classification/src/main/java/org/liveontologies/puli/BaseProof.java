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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base implementation for proofs.
 * 
 * @author Yevgeny Kazakov
 * @author Peter Skocovsky
 *
 * @param <I>
 *            the type of inferences provided by this proof
 */
public class BaseProof<I extends Inference<?>>
		implements ModifiableProof<I>, DynamicProof<I> {

	private static final Logger LOGGER_ = LoggerFactory
			.getLogger(BaseProof.class);

	private final Map<Object, Collection<I>> inferences_ = new HashMap<Object, Collection<I>>();

	/**
	 * conclusion for which {@link #getInferences(Object)} was called and the
	 * result did not change since then
	 */
	private final Set<Object> queried_ = new HashSet<Object>();

	private final List<ChangeListener> listeners_ = new ArrayList<ChangeListener>();

	@Override
	public Collection<? extends I> getInferences(Object conclusion) {
		queried_.add(conclusion);
		Collection<? extends I> result = inferences_.get(conclusion);
		if (result == null) {
			result = Collections.emptyList();
		}
		return result;
	}

	@Override
	public void addListener(ChangeListener listener) {
		listeners_.add(listener);
	}

	@Override
	public void removeListener(ChangeListener listener) {
		listeners_.remove(listener);
	}

	@Override
	public void produce(final I inference) {
		LOGGER_.trace("{}: inference added", inference);
		final Object conclusion = inference.getConclusion();
		Collection<I> existing = inferences_.get(conclusion);
		if (existing == null) {
			existing = new ArrayList<I>();
			inferences_.put(conclusion, existing);
		}
		existing.add(inference);
		if (queried_.contains(conclusion)) {
			fireChanged();
		}
	}

	@Override
	public void clear() {
		if (inferences_.isEmpty()) {
			return;
		}
		// else
		LOGGER_.trace("inferences cleared");
		inferences_.clear();
		if (!queried_.isEmpty()) {
			fireChanged();
		}
	}

	protected void fireChanged() {
		queried_.clear();
		for (ChangeListener listener : listeners_) {
			listener.inferencesChanged();
		}
	}

	@Override
	public void dispose() {
		// no-op
	}

}
