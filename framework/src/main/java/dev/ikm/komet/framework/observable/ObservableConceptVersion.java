package dev.ikm.komet.framework.observable;

import dev.ikm.tinkar.entity.ConceptEntityVersion;
import dev.ikm.tinkar.entity.ConceptVersionRecord;

public final class ObservableConceptVersion extends ObservableVersion<ConceptVersionRecord> implements ConceptEntityVersion {
    ObservableConceptVersion(ConceptVersionRecord conceptVersionRecord) {
        super(conceptVersionRecord);
    }

    @Override
    protected ConceptVersionRecord withStampNid(int stampNid) {
        return version().withStampNid(stampNid);
    }

    @Override
    public ConceptVersionRecord getVersionRecord() {
        return version();
    }
}
