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
