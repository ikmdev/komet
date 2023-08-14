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
package org.liveontologies.puli;

import java.util.List;

import java.util.function.Function;

class TransformedInference<F, T> extends Delegator<Inference<? extends F>>
		implements Inference<T> {

	private final Function<? super F, ? extends T> function_;

	public TransformedInference(final Inference<? extends F> inference,
			final Function<? super F, ? extends T> function) {
		super(inference);
		this.function_ = function;
	}

	@Override
	public String getName() {
		return getDelegate().getName();
	}

	@Override
	public T getConclusion() {
		return function_.apply(getDelegate().getConclusion());
	}

	@Override
	public List<? extends T> getPremises() {
		return getDelegate().getPremises().stream().map(function_).toList();
	}

}
