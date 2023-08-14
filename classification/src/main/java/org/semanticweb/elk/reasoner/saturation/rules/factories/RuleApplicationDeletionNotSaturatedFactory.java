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
package org.semanticweb.elk.reasoner.saturation.rules.factories;

import org.semanticweb.elk.reasoner.saturation.SaturationCheckingWriter;
import org.semanticweb.elk.reasoner.saturation.SaturationState;
import org.semanticweb.elk.reasoner.saturation.SaturationStateWriter;
import org.semanticweb.elk.reasoner.saturation.context.Context;
import org.semanticweb.elk.util.concurrent.computation.InterruptMonitor;

/**
 * A {@link RuleApplicationFactory} that works similarly to
 * {@link RuleApplicationDeletionFactory} except that it modifies only
 * {@link Context}s that are not saturated.
 * 
 * @see Context#isSaturated()
 * 
 * @author "Yevgeny Kazakov"
 * @author Pavel Klinov
 * 
 *         pavel.klinov@uni-ulm.de
 */
public class RuleApplicationDeletionNotSaturatedFactory extends
		RuleApplicationDeletionFactory {

	public RuleApplicationDeletionNotSaturatedFactory(
			final InterruptMonitor interrupter,
			final SaturationState<? extends Context> saturationState) {
		super(interrupter, saturationState);
	}

	@Override
	protected SaturationStateWriter<Context> getFinalWriter(SaturationStateWriter<? extends Context> writer) {
		// only write to non-saturated contexts
		return new SaturationCheckingWriter<Context>(writer, getSaturationState());
	}

}
