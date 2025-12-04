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

import dev.ikm.tinkar.ext.binding.interpolation.interpolate.EmptyInterpolate;
import dev.ikm.tinkar.ext.binding.interpolation.interpolate.ListInterpolate;
import dev.ikm.tinkar.ext.binding.interpolation.interpolate.SingleInterpolate;
import dev.ikm.tinkar.ext.binding.interpolation.interpolate.StreamInterpolate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Interpolator provides a general purpose interpolation engine that can handle Empty (no input), single input, List
 * input, and Stream input. The main process for interpolation is to read the provided template top to bottom. When a
 * key, emphasized by ${key}, in the template is identified (top to bottom), the associated Interpolation function will
 * be performed and the result will be written to the provided Consumer (via the run method). This is an attempt to reduce
 * overhead and or memory footprint when interpolating among lots of records.
 */
public class Interpolator {

	private final String template;
	private final List<Interpolation> interpolations;

	private Interpolator(String template, List<Interpolation> interpolations) {
		this.template = template;
		this.interpolations = interpolations;
	}

	/**
	 * Perform the interpolation on the provide String template. Within this run there is one exception that can be
	 * thrown and will be thrown when the template contains a key that has no correlating interpolation action.
	 * @param output - A functional way to enable the user to handle the outputs of the string
	 *                                interpolation
	 */
	public void run(Consumer<String> output) {

		//Sort to identify order to fire interpolations based on passed in template
		Map<Integer, Interpolation> sortedInterpolations = sortInterpolations();

		//Iterate through sorted interpolations to write non-interpolated strings out and perform interpolations
		AtomicInteger previousEnd = new AtomicInteger(0);
		sortedInterpolations.forEach((keyIndex, value) -> {
            int endIndex = keyIndex + value.getKey().length() - 1;

            if (previousEnd.get() == 0) {
				if (keyIndex != 0) {
					output.accept(template.substring(0, keyIndex));
				}
                //interpolate
                value.interpolate(output);
            } else {
                output.accept(template.substring(previousEnd.get() + 1, keyIndex));
                value.interpolate(output);
            }

            previousEnd.set(endIndex);
        });

		//Add remaining string from the template
		output.accept(template.substring(previousEnd.get() + 1));
	}

	/**
	 * In order to handle possibly large interpolation use cases, sorting the provided Interpolation objects into the
	 * order in which they are used (top to bottom) within the template, helps with performance.
	 * @return Sorted Map with Key being based on starting index of template for interpolation and the value being the
	 * interpolation object itself.
	 */
	private Map<Integer, Interpolation> sortInterpolations() {
		Map<Integer, Interpolation> interpolationLocations = new LinkedHashMap<>();
		interpolations.forEach(interpolation -> {
			interpolationLocations.put(template.indexOf(interpolation.getKey()), interpolation);
		});

		List<Map.Entry<Integer, Interpolation>> entries = new ArrayList<>(interpolationLocations.entrySet());
		entries.sort(Comparator.comparingInt(Map.Entry::getKey));

		Map<Integer, Interpolation> sortedInterpolations = new LinkedHashMap<>();
		for (Map.Entry<Integer, Interpolation> entry : entries) {
			sortedInterpolations.put(entry.getKey(), entry.getValue());
		}
		return sortedInterpolations;
	}

	/**
	 * Builder class that implements the builder pattern for configuring the overarching Interpolator class.
	 */
	public static class Builder {

		private final List<Interpolation> interpolations;
		private final String template;

		public Builder(String template) {
			this.interpolations = new ArrayList<>();
			this.template = template;
		}

		public Builder empty(String key, EmptyInterpolate emptyInterpolate) {
			interpolations.add(new EmptyInterpolation(key, emptyInterpolate));
			return this;
		}

		public <T> Builder single(String key, T input, SingleInterpolate<T> singleInterpolate) {
			interpolations.add(new SingleInterpolation<>(key, input, singleInterpolate));
			return this;
		}

		public <T> Builder list(String key, List<T> input, ListInterpolate<T> listInterpolate) {
			interpolations.add(new ListInterpolation<>(key, input, listInterpolate));
			return this;
		}

		public <T> Builder stream(String key, Stream<T> input, StreamInterpolate<T> streamInterpolate) {
			interpolations.add(new StreamInterpolation<>(key, input, streamInterpolate));
			return this;
		}

		public Interpolator build() {
			//Identify any missing Interpolations
			checkForMissingInterpolations();
			return new Interpolator(template, interpolations);
		}

		/**
		 * This method processed the interpolation template and compares all keys identified with the keys that are bound
		 * to a specific interpolation. If there ever is any keys found in the template that are orphaned from an
		 * interpolation the entire Interpolator will throw a RunTimeException
		 */
		private void checkForMissingInterpolations() {
			//Regex to find all ${} based keys within the template
			ArrayList<String> foundKeys = new ArrayList<>();
			Matcher matcher = Pattern
					.compile("\\$\\{[^{|}]*\\}")
					.matcher(template);
			while (matcher.find()) {
				foundKeys.add(matcher.group());
			}

			//For each discovered key compare to the list of known keys associated with interpolations. If no matches are
			//found then throw Run Time Exception
			foundKeys.forEach(foundKey -> {
				long keyMatches = interpolations.stream()
						.map(Interpolation::getKey)
						.filter(foundKey::equals).count();

				if (keyMatches == 0) {
					throw new RuntimeException("Interpolation '" + foundKey + "' not found");
				}
			});
		}
	}

}
