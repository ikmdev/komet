package dev.ikm.komet.framework.observable;

import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampVersionRecord;

public class ObservableStamp
        extends ObservableEntity<ObservableStampVersion, StampVersionRecord> {
    ObservableStamp(StampEntity<StampVersionRecord> stampEntity) {
        super(stampEntity);
    }

    @Override
    protected ObservableStampVersion wrap(StampVersionRecord version) {
        return new ObservableStampVersion(version);
    }

    @Override
    public ObservableEntitySnapshot getSnapshot(ViewCalculator calculator) {
        throw new UnsupportedOperationException();
    }
}
