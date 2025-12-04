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

import java.util.function.Consumer;

/**
 * Individual interpolation
 */
public abstract class Interpolation {

	private final String key;

	public Interpolation(String key) {
		this.key = formatKey(key);
	}

	/**
	 * Convenience method to format key
	 * @param key String key used in interpolation template
	 * @return Formated interpolation key
	 */
	private String formatKey(String key) {
		return String.format("${%1$s}", key);
	}

	/**
	 * Get interpolation key
	 * @return Formated interpolation key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Perform an interpolation
	 * @param outputProcess User defined interpolation results output
	 */
	public abstract void interpolate(Consumer<String> outputProcess);

}
