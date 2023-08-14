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
package org.semanticweb.elk.reasoner.saturation;

import org.semanticweb.elk.reasoner.ReasonerComputation;
import org.semanticweb.elk.reasoner.saturation.rules.factories.RuleApplicationFactory;
import org.semanticweb.elk.util.concurrent.computation.ConcurrentExecutor;

/**
 * A {@link ReasonerComputation} that uses a given
 * {@link RuleApplicationFactory} to process the {@link SaturationState} without
 * any further input.
 * 
 * @author Pavel Klinov
 * 
 * @author "Yevgeny Kazakov"
 * 
 */
public class ClassExpressionSaturationNoInput extends
		ReasonerComputation<ClassExpressionSaturationNoInputFactory> {

	public ClassExpressionSaturationNoInput(final ConcurrentExecutor executor,
			final int maxWorkers,
			final RuleApplicationFactory<?, ?> ruleAppFactory,
			final ContextModificationListener contextModificationListener) {

		super(new ClassExpressionSaturationNoInputFactory(ruleAppFactory,
				contextModificationListener), executor, maxWorkers);
	}

	/**
	 * Print statistics about the saturation computation
	 */

	public void printStatistics() {
		processorFactory.printStatistics();
	}

	public SaturationStatistics getRuleAndConclusionStatistics() {
		return processorFactory.getRuleAndConclusionStatistics();
	}

}
