package dev.ikm.komet.framework.observable;

import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.PatternVersionRecord;

public class ObservablePattern
        extends ObservableEntity<ObservablePatternVersion, PatternVersionRecord>
        implements PatternEntity<ObservablePatternVersion> {
    ObservablePattern(PatternEntity<PatternVersionRecord> patternEntity) {
        super(patternEntity);
    }

    @Override
    protected ObservablePatternVersion wrap(PatternVersionRecord version) {
        return new ObservablePatternVersion(version);
    }

    @Override
    public ObservablePatternSnapshot getSnapshot(ViewCalculator calculator) {
        return new ObservablePatternSnapshot(calculator, this);
    }

}
