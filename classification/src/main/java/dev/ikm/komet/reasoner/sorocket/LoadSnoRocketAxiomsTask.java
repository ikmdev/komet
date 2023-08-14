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
package dev.ikm.komet.reasoner.sorocket;

import au.csiro.ontology.classification.IReasoner;
import au.csiro.snorocket.core.SnorocketReasoner;
import dev.ikm.komet.reasoner.AxiomData;
import dev.ikm.tinkar.common.service.TrackingCallable;

public class LoadSnoRocketAxiomsTask extends TrackingCallable<IReasoner> {
    final AxiomData axiomData;
    final IReasoner reasoner = new SnorocketReasoner();

    public LoadSnoRocketAxiomsTask(AxiomData axiomData) {
        super(true, true);
        this.axiomData = axiomData;
        updateTitle("Loading axioms into reasoner. ");
    }

    @Override
    protected IReasoner compute() throws Exception {
        int axiomCount = this.axiomData.processedSemantics.get();
        updateProgress(0, axiomCount);
        this.reasoner.loadAxioms(this.axiomData.axiomsSet, this);
        updateMessage("Load in " + durationString());
        return this.reasoner;
    }
}
