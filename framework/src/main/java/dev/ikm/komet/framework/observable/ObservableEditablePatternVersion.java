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

import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.PatternRecord;
import dev.ikm.tinkar.entity.PatternVersionRecord;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Editable version wrapper for ObservablePatternVersion.
 * <p>
 * Provides editable properties for pattern purpose and meaning that can be
 * bound to GUI components. Changes are cached until save() or commit() is called.
 */
public final class ObservableEditablePatternVersion
        extends ObservableEditableVersion<ObservablePatternVersion, PatternVersionRecord> {

    private final SimpleObjectProperty<EntityFacade> editablePurposeProperty;
    private final SimpleObjectProperty<EntityFacade> editableMeaningProperty;

    ObservableEditablePatternVersion(ObservablePatternVersion observableVersion, ObservableStamp editStamp) {
        super(observableVersion, editStamp);

        // Initialize editable properties
        this.editablePurposeProperty = new SimpleObjectProperty<>(
                this,
                "purpose",
                Entity.getFast(observableVersion.semanticPurposeNid())
        );

        this.editableMeaningProperty = new SimpleObjectProperty<>(
                this,
                "meaning",
                Entity.getFast(observableVersion.semanticMeaningNid())
        );

        // Add listeners to update working version when properties change
        editablePurposeProperty.addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                workingVersion = workingVersion.withSemanticPurposeNid(newValue.nid());
            }
        });

        editableMeaningProperty.addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                workingVersion = workingVersion.withSemanticMeaningNid(newValue.nid());
            }
        });
    }

    /**
     * Gets or creates the canonical editable pattern version for the given stamp.
     * <p>
     * Returns the exact same instance for multiple calls with the same stamp, ensuring
     * a single canonical editable version per ObservableStamp.
     *
     * @param observableVersion the ObservablePatternVersion to edit
     * @param editStamp the ObservableStamp (typically identifying the author)
     * @return the canonical editable pattern version for this stamp
     */
    public static ObservableEditablePatternVersion getOrCreate(ObservablePatternVersion observableVersion, ObservableStamp editStamp) {
        return ObservableEditableVersion.getOrCreate(observableVersion, editStamp, ObservableEditablePatternVersion::new);
    }

    /**
     * Returns the editable purpose property for GUI binding.
     */
    public SimpleObjectProperty<EntityFacade> getPurposeProperty() {
        return editablePurposeProperty;
    }

    /**
     * Returns the editable meaning property for GUI binding.
     */
    public SimpleObjectProperty<EntityFacade> getMeaningProperty() {
        return editableMeaningProperty;
    }

    @Override
    protected PatternVersionRecord createVersionWithStamp(PatternVersionRecord version, int stampNid) {
        return version.withStampNid(stampNid);
    }

    @Override
    protected Entity<?> createAnalogue(PatternVersionRecord version) {
        return version.chronology().with(version).build();
    }

    @Override
    public void reset() {
        super.reset();
        // Reset properties to original values
        editablePurposeProperty.set(Entity.getFast(observableVersion.semanticPurposeNid()));
        editableMeaningProperty.set(Entity.getFast(observableVersion.semanticMeaningNid()));
    }
}
