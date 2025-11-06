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
package dev.ikm.komet.framework.observable;

import dev.ikm.komet.framework.observable.binding.Binding;
import dev.ikm.tinkar.entity.StampVersionRecord;
import org.eclipse.collections.api.list.MutableList;

import java.util.concurrent.atomic.AtomicReference;

public final class ObservableStampVersion
        extends ObservableVersion<ObservableStamp, StampVersionRecord> {

    ObservableStampVersion(ObservableStamp observableStamp, StampVersionRecord stampVersion) {
        super(observableStamp, stampVersion);
    }

    protected void addListeners() {
        stateProperty().addListener((observable, oldValue, newValue) -> {
            versionProperty.set(version().withStateNid(newValue.nid()));
        });

        timeProperty().addListener((observable, oldValue, newValue) -> {
            // TODO when to update the chronology with new record? At commit time? Automatically with reactive stream for commits?
            versionProperty.set(version().withTime(newValue.longValue()));
        });

        authorProperty().addListener((observable, oldValue, newValue) -> {
            versionProperty.set(version().withAuthorNid(newValue.nid()));
        });

        moduleProperty().addListener((observable, oldValue, newValue) -> {
            versionProperty.set(version().withModuleNid(newValue.nid()));
        });

        pathProperty().addListener((observable, oldValue, newValue) -> {
            versionProperty.set(version().withPathNid(newValue.nid()));
        });
    }

    @Override
    protected StampVersionRecord withStampNid(int stampNid) {
        throw new UnsupportedOperationException();
    }


    @Override
    public int patternNid() {
        return Binding.Stamp.pattern().nid();
    }

    @Override
    public int indexInPattern() {
        return Binding.Stamp.versionItemDefinitionIndex();
    }

    @Override
    public StampVersionRecord getVersionRecord() {
        return version();
    }

    // TODO: replace with JEP 502: Stable Values when finalized to allow lazy initialization of feature.
    private AtomicReference<FeatureWrapper> versionStatusFieldReference = new AtomicReference<>();
    private FeatureWrapper getVersionStatusField() {
        return versionStatusFieldReference.updateAndGet(currentValue -> currentValue != null
                ? currentValue
                : makeVersionStatusField());
    }
    private FeatureWrapper makeVersionStatusField() {
        FeatureKey locator = FeatureKey.Version.StampStatus(this.nid());
        return new FeatureWrapper(this.stateProperty(), Binding.Stamp.Version.pattern().nid(),
                Binding.Stamp.Version.statusFieldDefinitionIndex(),this, locator);
    }


    // TODO: replace with JEP 502: Stable Values when finalized to allow lazy initialization of feature.
    private AtomicReference<FeatureWrapper> versionTimeFieldReference = new AtomicReference<>();
    private FeatureWrapper getVersionTimeField() {
        return versionTimeFieldReference.updateAndGet(currentValue -> currentValue != null
                ? currentValue
                : makeVersionTimeField());
    }
    private FeatureWrapper makeVersionTimeField() {
        FeatureKey locator = FeatureKey.Version.StampTime(nid());
        return new FeatureWrapper(this.timeProperty(), Binding.Stamp.Version.pattern().nid(),
                Binding.Stamp.Version.timeFieldDefinitionIndex(), this, locator);
    }

    // TODO: replace with JEP 502: Stable Values when finalized to allow lazy initialization of feature.
    private AtomicReference<FeatureWrapper> versionAuthorFieldReference = new AtomicReference<>();
    private FeatureWrapper getVersionAuthorFeature() {
        return versionAuthorFieldReference.updateAndGet(currentValue -> currentValue != null
                ? currentValue
                : makeVersionAuthorFeature());
    }
    private FeatureWrapper makeVersionAuthorFeature() {
        FeatureKey locator = FeatureKey.Version.StampAuthor(this.nid());
        return new FeatureWrapper(this.authorProperty(), Binding.Stamp.Version.pattern().nid(),
                Binding.Stamp.Version.authorFieldDefinitionIndex(),this, locator);
    }

    // TODO: replace with JEP 502: Stable Values when finalized to allow lazy initialization of feature.
    private AtomicReference<FeatureWrapper> versionModuleFieldReference = new AtomicReference<>();
    private FeatureWrapper getVersionModuleFeature() {
        return versionModuleFieldReference.updateAndGet(currentValue -> currentValue != null
                ? currentValue
                : makeVersionModuleFeature());
    }
    private FeatureWrapper makeVersionModuleFeature() {
        FeatureKey locator = FeatureKey.Version.StampModule(this.nid());
        return new FeatureWrapper(this.moduleProperty(), Binding.Stamp.Version.pattern().nid(),
                Binding.Stamp.Version.moduleFieldDefinitionIndex(),this, locator);
    }


    // TODO: replace with JEP 502: Stable Values when finalized to allow lazy initialization of feature.
    private AtomicReference<FeatureWrapper> versionPathFieldReference = new AtomicReference<>();
    private FeatureWrapper getVersionPathField() {
        return versionPathFieldReference.updateAndGet(currentValue -> currentValue != null
                ? currentValue
                : makeVersionPathField());
    }
    private FeatureWrapper makeVersionPathField() {
        FeatureKey locator = FeatureKey.Version.StampPath(this.nid());
        return new FeatureWrapper(this.pathProperty(), Binding.Stamp.Version.pattern().nid(),
                Binding.Stamp.Version.pathFieldDefinitionIndex(),this, locator);
    }

    @Override
    protected void addAdditionalVersionFeatures(MutableList<Feature> features) {
        // Status
        features.add(getVersionStatusField());

        // Time
        features.add(getVersionTimeField());

        // Author
        features.add(getVersionAuthorFeature());

        // Module
        features.add(getVersionModuleFeature());

        // Path
        features.add(getVersionPathField());
    }

    @Override
    public ObservableEditableStampVersion getEditableVersion(ObservableStamp editStamp) {
        return ObservableEditableStampVersion.getOrCreate(getObservableEntity(), this, editStamp);
    }
}
