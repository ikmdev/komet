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
package dev.ikm.komet.reasoner.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.PatternFacade;

public abstract class ReasonerServiceBase implements ReasonerService {

	private static final Logger LOG = LoggerFactory.getLogger(ReasonerServiceBase.class);

	protected ViewCalculator viewCalculator;

	protected PatternFacade statedAxiomPattern;

	protected PatternFacade inferredAxiomPattern;

	protected TrackingCallable<?> progressUpdater;

	@Override
	public ViewCalculator getViewCalculator() {
		return viewCalculator;
	}

	@Override
	public PatternFacade getStatedAxiomPattern() {
		return statedAxiomPattern;
	}

	@Override
	public PatternFacade getInferredAxiomPattern() {
		return inferredAxiomPattern;
	}

	@Override
	public TrackingCallable<?> getProgressUpdater() {
		return progressUpdater;
	}

	private static class NoopTrackingCallable extends TrackingCallable<Void> {

		@Override
		protected Void compute() throws Exception {
			return null;
		}

	}

	@Override
	public void setProgressUpdater(TrackingCallable<?> progressUpdater) {
		if (progressUpdater == null)
			progressUpdater = new NoopTrackingCallable();
		this.progressUpdater = progressUpdater;
	};

	@Override
	public void init(ViewCalculator viewCalculator, PatternFacade statedAxiomPattern,
			PatternFacade inferredAxiomPattern) {
		this.viewCalculator = viewCalculator;
		this.statedAxiomPattern = statedAxiomPattern;
		this.inferredAxiomPattern = inferredAxiomPattern;
		this.progressUpdater = null;
	}

}
