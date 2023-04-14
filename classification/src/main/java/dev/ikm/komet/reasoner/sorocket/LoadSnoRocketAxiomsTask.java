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
