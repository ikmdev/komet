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
package dev.ikm.tinkar.ext.binding.interpolation.interpolate;

import java.util.List;
import java.util.function.Consumer;

/**
 * A functional interpolation that takes a List of type T Objects
 * @param <T> Type of list to be interpolated
 */
@FunctionalInterface
public interface ListInterpolate<T> {

	/**
	 * Perform interpolation
	 * @param input List of Objects of type T to be interpolated
	 * @param outputConsumer User defined consumer to process interpolation output
	 */
 	void apply(List<T> input, Consumer<String> outputConsumer);
}
