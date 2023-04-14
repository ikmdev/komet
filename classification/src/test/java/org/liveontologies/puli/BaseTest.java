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

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.liveontologies.puli.pinpointing.AxiomPinpointingTestManifest;

@RunWith(Parameterized.class)
public abstract class BaseTest<TM extends TestManifest, R extends TestRunner<TM>> {

	@Parameter(0)
	public TM testManifest;

	@Parameter(1)
	public R testRunner;

	@Test
	public void test() throws Exception {
		testRunner.runTest(testManifest);
	}
	
	public static Iterable<Object[]> data(
			List<? extends TestRunner<?>> testRunners, List<String> testInputs)
			throws Exception {
		final List<Object[]> parameters = new ArrayList<Object[]>();
		for (String testInput : testInputs) {
			for (TestManifest manifest : BaseTest.getInstances(
					AxiomPinpointingTestManifest.class, testInput)) {
				for (final TestRunner<?> runner : testRunners) {
					parameters.add(new Object[] { manifest, runner });
				}
			}
		}
		return parameters;
	}

	public static final String CLASS_FILE_EXT = ".class";

	public static <T> List<T> getInstances(Class<T> cls,
			final String testInputSubpkg)
			throws URISyntaxException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		List<T> result = new ArrayList<>();
		addInstances(cls, testInputSubpkg, result);
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> void addInstances(Class<T> cls,
			final String testInputSubpkg, Collection<T> instances)
			throws URISyntaxException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {

		final String pkgName = BaseTest.class.getPackage().getName();
		final String inputsLocation = pkgName.replace('.', '/') + "/"
				+ testInputSubpkg.replace('.', '/');
		final URI inputsUri = BaseTest.class.getClassLoader()
				.getResource(inputsLocation).toURI();
		final File inputsDir = new File(inputsUri);
		String[] fileNames = inputsDir.list(new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String name) {
				return name.endsWith(CLASS_FILE_EXT);
			}
		});
		if (fileNames == null) {
			throw new RuntimeException("Cannot find test files");
		}
		for (final String filename : fileNames) {
			final String inputClassName = pkgName + "." + testInputSubpkg + "."
					+ filename.substring(0,
							filename.length() - CLASS_FILE_EXT.length());
			final Class<?> inputClass = Class.forName(inputClassName);
			if (cls.isAssignableFrom(inputClass)
					&& !Modifier.isAbstract(inputClass.getModifiers())) {
				// System.out.println(inputClass.getCanonicalName());
				instances.add((T) inputClass.newInstance());
			}
		}
	}

}
