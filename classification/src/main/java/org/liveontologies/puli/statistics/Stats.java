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
package org.liveontologies.puli.statistics;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public class Stats {

	public static final String STAT_NAME_SEPARATOR = ".";

	private Stats() {
		// Forbid instantiation of a utility class.
	}

	public static Map<String, Object> copyIntoMap(final Object hasStats,
			final Map<String, Object> result) {
		Objects.requireNonNull(result);
		final Stream<Map.Entry<String, Object>> stats = getStats(hasStats);
		stats.forEach(stringObjectEntry -> result.put(stringObjectEntry.getKey(), stringObjectEntry.getValue()));
		return result;
	}

	public static Map<String, Object> copyIntoMap(final Object hasStats) {
		return copyIntoMap(hasStats, new HashMap<String, Object>());
	}

	public static Stream<Map.Entry<String, Object>> getStats(
			final Object hasStats) {
		return getStats(hasStats, "");
	}

	public static Stream<Map.Entry<String, Object>> getStats(
			final Object hasStats, final String statNamePrefix) {
		Objects.requireNonNull(hasStats);
		Objects.requireNonNull(statNamePrefix);
		if (hasStats instanceof Class) {
			return getStats((Class<?>) hasStats, null, statNamePrefix);
		} else {
			return getStats(hasStats.getClass(), hasStats, statNamePrefix);
		}
	}

	public static Stream<Map.Entry<String, Object>> getStats(
			final Class<?> hasStatsClass, final Object hasStats,
			final String statNamePrefix) {
		Objects.requireNonNull(hasStatsClass);
		Objects.requireNonNull(statNamePrefix);

		final Stream<Map.Entry<String, Object>> fieldStats = getAnnotatedElements(Stat.class, hasStatsClass.getFields())
				.map(field -> new AbstractMap.SimpleImmutableEntry<>(
						statNamePrefix + getStatName(field), checkedGet(field, hasStats)));

		final Stream<Map.Entry<String, Object>> methodStats = getAnnotatedElements(Stat.class, hasStatsClass.getMethods())
				.map(method -> new AbstractMap.SimpleImmutableEntry<>(
						statNamePrefix + getStatName(method), checkedInvoke(method, hasStats)));

		// Recursion into nested stats

		final Stream<Map.Entry<String, Object>> nesteds = getNested(hasStatsClass, hasStats, statNamePrefix);

		final MutableList<Map.Entry<String, Object>> nestedStatList = Lists.mutable.empty();
		nesteds.forEach(entry -> getStats(entry.getValue(),
				entry.getKey() + STAT_NAME_SEPARATOR).forEach(nestedEntry -> nestedStatList.add(nestedEntry)));

		return Stream.concat(Stream.concat(fieldStats, methodStats), nestedStatList.stream());
	}

	public static void resetStats(final Object hasStats) {
		Objects.requireNonNull(hasStats);
		if (hasStats instanceof Class) {
			resetStats((Class<?>) hasStats, null);
		} else {
			resetStats(hasStats.getClass(), hasStats);
		}
	}

	public static void resetStats(final Class<?> hasStatsClass,
			final Object hasStats) {
		Objects.requireNonNull(hasStatsClass);

		final Stream<Method> resetMethods = getAnnotatedElements(
				ResetStats.class, hasStatsClass.getMethods());
		resetMethods.forEach(resetMethod -> checkedInvoke(resetMethod, hasStats));

		// Recursion into nested stats

		final Stream<Map.Entry<String, Object>> nesteds = getNested(
				hasStatsClass, hasStats);
		nesteds.forEach(stat -> stat.getValue());

	}

	private static Stream<Map.Entry<String, Object>> getNested(
			final Class<?> hasStatsClass, final Object hasStats) {
		return getNested(hasStatsClass, hasStats, "");
	}

	private static Stream<Map.Entry<String, Object>> getNested(
			final Class<?> hasStatsClass, final Object hasStats,
			final String statNamePrefix) {
		Objects.requireNonNull(hasStatsClass);
		Objects.requireNonNull(statNamePrefix);

		final Stream<Map.Entry<String, Object>> nestedFields = getAnnotatedElements(NestedStats.class, hasStatsClass.getFields())
				.map(field -> new AbstractMap.SimpleImmutableEntry<>(
						statNamePrefix + getNestedStatsName(field), checkedGet(field, hasStats)));


		final Stream<Map.Entry<String, Object>> nestedMethods = getAnnotatedElements(NestedStats.class, hasStatsClass.getMethods())
				.map(method -> new AbstractMap.SimpleImmutableEntry<>(
						statNamePrefix + getNestedStatsName(method), checkedInvoke(method, hasStats)));

		return Stream.concat(nestedFields, nestedMethods);
	}

	private static Object checkedGet(final Field field, final Object object) {
		if (object == null && !Modifier.isStatic(field.getModifiers())) {
			throw new StatsException(
					"Can handle only static fields! Non-static field: "
							+ field);
		}
		try {
			return field.get(object);
		} catch (final IllegalAccessException e) {
			throw new StatsException(e);
		}
	}

	private static Object checkedInvoke(final Method method,
			final Object object) {
		if (object == null && !Modifier.isStatic(method.getModifiers())) {
			throw new StatsException(
					"Can handle only static methods! Non-static method: "
							+ method);
		}
		if (method.getParameterTypes().length != 0) {
			throw new StatsException(
					"Can handle only methods with no parameters! Method with parameters: "
							+ method);
		}
		try {
			return method.invoke(object);
		} catch (final IllegalAccessException e) {
			throw new StatsException(e);
		} catch (final InvocationTargetException e) {
			throw new StatsException(e);
		}
	}

	private static <E extends AnnotatedElement> Stream<E> getAnnotatedElements(
			final Class<? extends Annotation> presentAnnotation,
			final E[] elements) {
		return Arrays.stream(elements).filter(element -> element.isAnnotationPresent(presentAnnotation));
	}

	private static <E extends AnnotatedElement & Member> String getStatName(
			final E element) {
		final String name = element.getAnnotation(Stat.class).name();
		return name.isEmpty() ? element.getName() : name;
	}

	private static <E extends AnnotatedElement & Member> String getNestedStatsName(
			final E element) {
		final String name = element.getAnnotation(NestedStats.class).name();
		return name.isEmpty() ? element.getName() : name;
	}

}
