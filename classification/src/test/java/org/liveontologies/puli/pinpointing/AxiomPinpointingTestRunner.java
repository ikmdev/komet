package org.liveontologies.puli.pinpointing;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

/*-
 * #%L
 * Proof Utility Library
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2021 Live Ontologies Project
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.liveontologies.puli.AxiomPinpointingInference;
import org.liveontologies.puli.TestInputManifest;
import org.liveontologies.puli.TestRunner;

public class AxiomPinpointingTestRunner<C, A, I extends AxiomPinpointingInference<? extends C, ? extends A>>
		implements TestRunner<AxiomPinpointingTestManifest<C, A, I>> {

	private final ProverAxiomPinpointingEnumerationFactory<C, A> factory_;

	AxiomPinpointingTestRunner(
			ProverAxiomPinpointingEnumerationFactory<C, A> computationFactory) {
		this.factory_ = computationFactory;
	}

	@Override
	public String toString() {
		return factory_.toString();
	}

	@Override
	public String getName() {
		return toString();
	}

	@Override
	public void runTest(AxiomPinpointingTestManifest<C, A, I> manifest) {
		AxiomPinpointingCollector<A> actualResults = getActualResults(
				manifest.getInput());
		assumeTrue(actualResults.getUsefulAxioms() != null
				&& manifest.getUsefulAxioms() != null);
		verifyThat(actualResults.getJustifications(),
				manifest.getJustifications());
		verifyThat(actualResults.getRepairs(), manifest.getRepairs());
		verifyThat(actualResults.getUsefulAxioms(),
				manifest.getUsefulAxioms());
	}

	private static <E> void verifyThat(Set<? extends E> actual,
			Set<? extends E> expected) {
		if (actual != null && expected != null) {
			assertThat(actual, is(equalTo(expected)));
		}
	}

	private static <E> void verifyThat(Collection<? extends E> actual,
			Set<? extends E> expected) {
		if (actual != null && expected != null) {
			assertThat(new HashSet<>(actual), is(equalTo(expected)));
		}
	}

	private AxiomPinpointingCollector<A> getActualResults(
			TestInputManifest<C, A, I> input) {
		AxiomPinpointingCollector<A> actualResults = new AxiomPinpointingCollector<>();

		factory_.create(input, AxiomPinpointingInterruptMonitor.DUMMY)
				.enumerate(input.getQuery(), actualResults);

		return actualResults;
	}

}
