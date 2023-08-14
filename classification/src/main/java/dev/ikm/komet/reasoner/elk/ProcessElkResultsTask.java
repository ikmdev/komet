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
package dev.ikm.komet.reasoner.elk;

import au.csiro.ontology.classification.IReasoner;
import dev.ikm.komet.reasoner.AxiomData;
import dev.ikm.komet.reasoner.ClassifierResults;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.PatternFacade;
import org.semanticweb.elk.owl.interfaces.ElkClass;
import org.semanticweb.elk.reasoner.taxonomy.model.Taxonomy;

public class ProcessElkResultsTask extends TrackingCallable<ClassifierResults> {
    public ProcessElkResultsTask(Taxonomy<ElkClass> taxonomy, ViewCalculator viewCalculator, PatternFacade inferredAxiomPattern,
                                 AxiomData axiomData) {

    }

    @Override
    protected ClassifierResults compute() throws Exception {
        return null;
    }
}
