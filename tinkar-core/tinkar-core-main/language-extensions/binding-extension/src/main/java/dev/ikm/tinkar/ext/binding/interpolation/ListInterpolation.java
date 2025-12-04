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
package dev.ikm.tinkar.ext.binding.interpolation;

import dev.ikm.tinkar.ext.binding.interpolation.interpolate.ListInterpolate;

import java.util.List;
import java.util.function.Consumer;

/**
 * A List Interpolation
 * @param <T> Object type to be interpolated
 */
public class ListInterpolation<T> extends Interpolation {

	private final ListInterpolate<T> listInterpolate;
	private final List<T> input;

	public ListInterpolation(String key, List<T> input, ListInterpolate<T> listInterpolate) {
		super(key);
		this.listInterpolate = listInterpolate;
		this.input = input;
	}

	@Override
	public void interpolate(Consumer<String> outputConsumer) {
		listInterpolate.apply(input, outputConsumer);
	}
}
