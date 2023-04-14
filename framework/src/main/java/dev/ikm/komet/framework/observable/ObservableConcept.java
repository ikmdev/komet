package dev.ikm.komet.framework.observable;

import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.ConceptVersionRecord;

public class ObservableConcept
        extends ObservableEntity<ObservableConceptVersion, ConceptVersionRecord>
        implements ConceptEntity<ObservableConceptVersion> {
    ObservableConcept(ConceptEntity<ConceptVersionRecord> conceptEntity) {
        super(conceptEntity);
    }

    @Override
    protected ObservableConceptVersion wrap(ConceptVersionRecord version) {
        return new ObservableConceptVersion(version);
    }

    @Override
    public ObservableConceptSnapshot getSnapshot(ViewCalculator calculator) {
        return new ObservableConceptSnapshot(calculator, this);
    }
}
