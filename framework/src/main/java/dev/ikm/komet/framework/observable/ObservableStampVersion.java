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
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.State;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
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
    public Editable getEditableVersion(ObservableStamp editStamp) {
        return Editable.getOrCreate(getObservableEntity(), this, editStamp);
    }

    /**
     * Editable version wrapper for ObservableStampVersion.
     * <p>
     * Provides editable properties for stamp fields (state, time, author, module, path)
     * that can be bound to GUI components. Changes are cached until save() or commit() is called.
     */
    public static final class Editable
            extends ObservableVersion.Editable<ObservableStamp, ObservableStampVersion, StampVersionRecord> {

        private final SimpleObjectProperty<State> editableStateProperty;
        private final SimpleLongProperty editableTimeProperty;
        private final SimpleObjectProperty<ConceptFacade> editableAuthorProperty;
        private final SimpleObjectProperty<ConceptFacade> editableModuleProperty;
        private final SimpleObjectProperty<ConceptFacade> editablePathProperty;

        private Editable(ObservableStamp observableStamp, ObservableStampVersion observableVersion, ObservableStamp editStamp) {
            super(observableStamp, observableVersion, editStamp);

            // Initialize editable properties
            StampVersionRecord version = observableVersion.version();

            this.editableStateProperty = new SimpleObjectProperty<>(this, "state", version.state());
            this.editableTimeProperty = new SimpleLongProperty(this, "time", version.time());
            this.editableAuthorProperty = new SimpleObjectProperty<>(this, "author",
                    EntityHandle.getConceptOrThrow(version.authorNid()));
            this.editableModuleProperty = new SimpleObjectProperty<>(this, "module",
                    EntityHandle.getConceptOrThrow(version.moduleNid()));
            this.editablePathProperty = new SimpleObjectProperty<>(this, "path",
                    EntityHandle.getConceptOrThrow(version.pathNid()));

            // Add listeners to update working version when properties change
            editableStateProperty.addListener((obs, oldValue, newValue) -> {
                if (newValue != null) {
                    workingVersion = workingVersion.withStateNid(newValue.nid());
                }
            });

            editableTimeProperty.addListener((obs, oldValue, newValue) -> {
                workingVersion = workingVersion.withTime(newValue.longValue());
            });

            editableAuthorProperty.addListener((obs, oldValue, newValue) -> {
                if (newValue != null) {
                    workingVersion = workingVersion.withAuthorNid(newValue.nid());
                }
            });

            editableModuleProperty.addListener((obs, oldValue, newValue) -> {
                if (newValue != null) {
                    workingVersion = workingVersion.withModuleNid(newValue.nid());
                }
            });

            editablePathProperty.addListener((obs, oldValue, newValue) -> {
                if (newValue != null) {
                    workingVersion = workingVersion.withPathNid(newValue.nid());
                }
            });
        }

        /**
         * Gets or creates the canonical editable stamp version for the given stamp.
         * <p>
         * Returns the exact same instance for multiple calls with the same stamp, ensuring
         * a single canonical editable version per ObservableStamp.
         *
         * @param observableVersion the ObservableStampVersion to edit
         * @param editStamp the ObservableStamp (typically identifying the author)
         * @return the canonical editable stamp version for this stamp
         */
        public static Editable getOrCreate(ObservableStamp observableStamp, ObservableStampVersion observableVersion, ObservableStamp editStamp) {
            return ObservableVersion.Editable.getOrCreate(observableStamp, observableVersion, editStamp, Editable::new);
        }

        /**
         * Returns the editable state property for GUI binding.
         */
        public SimpleObjectProperty<State> getStateProperty() {
            return editableStateProperty;
        }

        /**
         * Returns the editable time property for GUI binding.
         */
        public SimpleLongProperty getTimeProperty() {
            return editableTimeProperty;
        }

        /**
         * Returns the editable author property for GUI binding.
         */
        public SimpleObjectProperty<ConceptFacade> getAuthorProperty() {
            return editableAuthorProperty;
        }

        /**
         * Returns the editable module property for GUI binding.
         */
        public SimpleObjectProperty<ConceptFacade> getModuleProperty() {
            return editableModuleProperty;
        }

        /**
         * Returns the editable path property for GUI binding.
         */
        public SimpleObjectProperty<ConceptFacade> getPathProperty() {
            return editablePathProperty;
        }

        @Override
        protected StampVersionRecord createVersionWithStamp(StampVersionRecord version, int stampNid) {
            // Stamps don't have a separate stamp field - they are self-describing
            // Return the version as-is
            return version;
        }

        @Override
        protected Entity<?> createAnalogue(StampVersionRecord version) {
            return version.chronology().with(version).build();
        }

        @Override
        public void reset() {
            super.reset();
            // Reset properties to original values
            StampVersionRecord version = observableVersion.version();
            editableStateProperty.set(version.state());
            editableTimeProperty.set(version.time());
            editableAuthorProperty.set(EntityHandle.getConceptOrThrow(version.authorNid()));
            editableModuleProperty.set(EntityHandle.getConceptOrThrow(version.moduleNid()));
            editablePathProperty.set(EntityHandle.getConceptOrThrow(version.pathNid()));
        }
    }
}
