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
package dev.ikm.tinkar.reasoner.hybrid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.reasoner.elksnomed.ElkSnomedData;
import dev.ikm.tinkar.reasoner.elksnomed.ElkSnomedDataBuilder;
import dev.ikm.tinkar.reasoner.service.ReasonerService;
import dev.ikm.tinkar.terms.TinkarTerm;

public abstract class HybridReasonerTestBase extends SnomedTestBase {

	private static final Logger LOG = LoggerFactory.getLogger(HybridReasonerTestBase.class);

	protected static String test_case;

	// This is overridden in the version test cases
	protected ViewCalculator getViewCalculator() {
		return PrimitiveDataTestUtil.getViewCalculator();
	}

	public ElkSnomedData buildSnomedData() throws Exception {
		LOG.info("buildSnomedData");
		ViewCalculator viewCalculator = getViewCalculator();
		ElkSnomedData data = new ElkSnomedData();
		ElkSnomedDataBuilder builder = new ElkSnomedDataBuilder(viewCalculator,
				TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN, data);
		builder.build();
		return data;
	}

	public ReasonerService initReasonerService() {
		ReasonerService rs = PluggableService.load(ReasonerService.class).stream()
				.filter(x -> x.type().getSimpleName().equals(HybridReasonerService.class.getSimpleName())) //
				.findFirst().get().get();
		rs.init(getViewCalculator(), TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN,
				TinkarTerm.EL_PLUS_PLUS_INFERRED_AXIOMS_PATTERN);
		rs.setProgressUpdater(null);
		return rs;
	}

}
