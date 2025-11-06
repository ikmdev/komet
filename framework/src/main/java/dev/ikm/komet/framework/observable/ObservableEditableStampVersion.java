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

import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.State;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Editable version wrapper for ObservableStampVersion.
 * <p>
 * Provides editable properties for stamp fields (state, time, author, module, path)
 * that can be bound to GUI components. Changes are cached until save() or commit() is called.
 */
public final class ObservableEditableStampVersion
        extends ObservableEditableVersion<ObservableStamp , ObservableStampVersion, StampVersionRecord> {

    private final SimpleObjectProperty<State> editableStateProperty;
    private final SimpleLongProperty editableTimeProperty;
    private final SimpleObjectProperty<ConceptFacade> editableAuthorProperty;
    private final SimpleObjectProperty<ConceptFacade> editableModuleProperty;
    private final SimpleObjectProperty<ConceptFacade> editablePathProperty;

    ObservableEditableStampVersion(ObservableStamp observableStamp, ObservableStampVersion observableVersion, ObservableStamp editStamp) {
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
    public static ObservableEditableStampVersion getOrCreate(ObservableStamp observableStamp, ObservableStampVersion observableVersion, ObservableStamp editStamp) {
        return ObservableEditableVersion.getOrCreate(observableStamp, observableVersion, editStamp, ObservableEditableStampVersion::new);
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
