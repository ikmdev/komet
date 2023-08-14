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
package org.semanticweb.elk.reasoner.config;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.ImmutableMap;
import org.semanticweb.elk.util.collections.CapacityBalancingEvictor;
import org.semanticweb.elk.util.collections.CountingEvictor;
import org.semanticweb.elk.util.collections.Evictor;
import org.semanticweb.elk.util.collections.NQEvictor;
import org.semanticweb.elk.util.collections.RecencyEvictor;

public class EvictorBuilder {

	public static final ImmutableMap<String, Class<?>> EVICTOR_CLASS_SHORTCUT;

	static {
		EVICTOR_CLASS_SHORTCUT = Maps.immutable.of(
				RecencyEvictor.class.getSimpleName(), RecencyEvictor.class,
				CapacityBalancingEvictor.class.getSimpleName(), CapacityBalancingEvictor.class,
				CountingEvictor.class.getSimpleName(), CountingEvictor.class,
				NQEvictor.class.getSimpleName(), NQEvictor.class);

	}

	public static Evictor.Builder valueOf(final String value)
			throws IllegalArgumentException {

		final int indexOfArgs = value.indexOf("(");
		if (indexOfArgs < 0) {
			throw new IllegalArgumentException(
					"Cannot separate type from arguments! value: " + value);
		}
		final String typeName = value.substring(0, indexOfArgs).trim();

		Class<?> clazz = EVICTOR_CLASS_SHORTCUT.get(typeName);
		if (clazz == null) {
			try {
				clazz = Class.forName(typeName);
			} catch (final ClassNotFoundException e) {
				throw new IllegalArgumentException(
						"There is no class for the type! value: " + value, e);
			}
		}

		if (!Evictor.Builder.class.isAssignableFrom(clazz)) {
			// Search for the builder class inside!
			Class<?> builderClass = null;
			for (final Class<?> innerClass : clazz.getClasses()) {
				if (Evictor.Builder.class.isAssignableFrom(innerClass)) {
					if (builderClass != null) {
						throw new IllegalArgumentException(
								"The type contains multiple builder classes! value: "
										+ value);
					}
					builderClass = innerClass;
				}
			}
			if (builderClass == null) {
				throw new IllegalArgumentException(
						"The type does not contains builder class! value: "
								+ value);
			}
			clazz = builderClass;
		}

		final Method method = ReasonerConfiguration.getValueOfMethod(clazz);
		if (method == null) {
			throw new IllegalArgumentException(
					"The builder class does not have public static method valueOf with a String argument! value: "
							+ value);
		}

		final Object result;
		try {
			result = method.invoke(null, value);
		} catch (final IllegalAccessException e) {
			throw new IllegalArgumentException(
					"Exception when calling valueOf of the builder class! value: "
							+ value,
					e);
		} catch (final InvocationTargetException e) {
			final Throwable cause = e.getCause();
			if (cause instanceof IllegalArgumentException) {
				throw (IllegalArgumentException) cause;
			}
			throw new IllegalArgumentException(
					"Exception when calling valueOf of the builder class! value: "
							+ value,
					e);
		}

		if (!(result instanceof Evictor.Builder)) {
			throw new IllegalArgumentException(
					"The result of calling valueOf of the builder class is not a builder! value: "
							+ value);
		}

		return (Evictor.Builder) result;
	}

}
