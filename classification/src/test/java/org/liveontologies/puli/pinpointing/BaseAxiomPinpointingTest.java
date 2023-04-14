package org.liveontologies.puli.pinpointing;

/*-
 * #%L
 * Proof Utility Library
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2022 Live Ontologies Project
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

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.liveontologies.puli.AxiomPinpointingInference;
import org.liveontologies.puli.BaseTest;

public abstract class BaseAxiomPinpointingTest<C, A, I extends AxiomPinpointingInference<? extends C, ? extends A>>
		extends
		BaseTest<AxiomPinpointingTestManifest<C, A, I>, AxiomPinpointingTestRunner<C, A, I>> {

	public static final String TEST_INPUT_JUSTIFICATIONS_SUBPKG = "pinpointing.input.justifications";
	public static final String TEST_INPUT_REPAIRS_SUBPKG = "pinpointing.input.repairs";

	public static Iterable<Object[]> data(
			Stream<ProverAxiomPinpointingEnumerationFactory<?, ?>> computationFactories)
			throws Exception {
		return data(computationFactories, Stream.of(
				TEST_INPUT_JUSTIFICATIONS_SUBPKG, TEST_INPUT_REPAIRS_SUBPKG));
	}

	public static Iterable<Object[]> data(
			Stream<ProverAxiomPinpointingEnumerationFactory<?, ?>> computationFactories,
			String testInput) throws Exception {
		return data(computationFactories, Stream.of(testInput));
	}

	public static Iterable<Object[]> data(
			Stream<ProverAxiomPinpointingEnumerationFactory<?, ?>> computationFactories,
			Stream<String> testInputs) throws Exception {
		return data(
				computationFactories.map(
						computationFactory -> new AxiomPinpointingTestRunner<>(
								computationFactory))
						.collect(Collectors.toList()),
				testInputs.collect(Collectors.toList()));
	}	

}
