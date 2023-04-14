package dev.ikm.komet.framework.observable;

import dev.ikm.tinkar.entity.StampVersionRecord;

public final class ObservableStampVersion
        extends ObservableVersion<StampVersionRecord> {

    ObservableStampVersion(StampVersionRecord stampVersion) {
        super(stampVersion);
    }

    protected void addListeners() {
        stateProperty.addListener((observable, oldValue, newValue) -> {
            versionProperty.set(version().withStateNid(newValue.nid()));
        });

        timeProperty.addListener((observable, oldValue, newValue) -> {
            // TODO when to update the chronology with new record? At commit time? Automatically with reactive stream for commits?
            versionProperty.set(version().withTime(newValue.longValue()));
        });

        authorProperty.addListener((observable, oldValue, newValue) -> {
            versionProperty.set(version().withAuthorNid(newValue.nid()));
        });

        moduleProperty.addListener((observable, oldValue, newValue) -> {
            versionProperty.set(version().withModuleNid(newValue.nid()));
        });

        pathProperty.addListener((observable, oldValue, newValue) -> {
            versionProperty.set(version().withPathNid(newValue.nid()));
        });
    }

    @Override
    protected StampVersionRecord withStampNid(int stampNid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StampVersionRecord getVersionRecord() {
        return version();
    }
}
