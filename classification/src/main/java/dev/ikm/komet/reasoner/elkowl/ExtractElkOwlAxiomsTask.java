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
package dev.ikm.komet.reasoner.elkowl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.TinkarTerm;

public class ExtractElkOwlAxiomsTask extends TrackingCallable<ElkOwlAxiomData> {

	private static final Logger LOG = LoggerFactory.getLogger(ExtractElkOwlAxiomsTask.class);

	final ViewCalculator viewCalculator;

	final PatternFacade statedAxiomPattern;

	public ExtractElkOwlAxiomsTask(ViewCalculator viewCalculator, PatternFacade statedAxiomPattern) {
		super(false, true);
		this.viewCalculator = viewCalculator;
		this.statedAxiomPattern = statedAxiomPattern;
		updateTitle("Fetching axioms from: "
				+ viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(statedAxiomPattern));
	}

	@Override
	protected ElkOwlAxiomData compute() throws Exception {
		ElkOwlAxiomData axiomData = new ElkOwlAxiomData();
		ElkOwlAxiomDataBuilder builder = new ElkOwlAxiomDataBuilder(viewCalculator,
				TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN, axiomData);
		builder.setProgressUpdater(this);
		builder.build();
		updateMessage("Extract in " + durationString());
		LOG.info("Extract in " + durationString());
		return axiomData;
	}

}
