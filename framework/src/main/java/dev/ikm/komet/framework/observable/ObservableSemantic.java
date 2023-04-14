package dev.ikm.komet.framework.observable;

import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticVersionRecord;

public class ObservableSemantic
        extends ObservableEntity<ObservableSemanticVersion, SemanticVersionRecord>
        implements SemanticEntity<ObservableSemanticVersion> {
    ObservableSemantic(SemanticEntity<SemanticVersionRecord> semanticEntity) {
        super(semanticEntity);
    }

    @Override
    protected ObservableSemanticVersion wrap(SemanticVersionRecord version) {
        return new ObservableSemanticVersion(version);
    }

    @Override
    public ObservableSemanticSnapshot getSnapshot(ViewCalculator calculator) {
        return new ObservableSemanticSnapshot(calculator, this);
    }

    @Override
    public int referencedComponentNid() {
        return ((SemanticEntity) entity()).referencedComponentNid();
    }

    @Override
    public int patternNid() {
        return ((SemanticEntity) entity()).patternNid();
    }

}
