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

import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.*;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;
import org.eclipse.collections.api.list.MutableList;

public final class ObservableConcept
        extends ObservableEntity<ObservableConceptVersion>
        implements ConceptEntity<ObservableConceptVersion>, ObservableChronology {
    ObservableConcept(ConceptEntity<ConceptVersionRecord> conceptEntity) {
        super(conceptEntity);
    }

    @Override
    protected ObservableConceptVersion wrap(EntityVersion version) {
        return new ObservableConceptVersion(this, (ConceptVersionRecord) version);
    }


    /**
     * Retrieves the {@code ConceptRecord} associated with this {@code ObservableConcept}.
     *
     * This method overrides the {@code entity} method in the superclass to provide
     * the specific {@code ConceptRecord} type associated with this {@code ObservableConcept}.
     * It allows access to the detailed record containing concept-specific information.
     *
     * @return the {@code ConceptRecord} associated with this {@code ObservableConcept}
     */
    public ConceptRecord entity() {
        return (ConceptRecord) super.entity();
    }

    @Override
    public ObservableConceptSnapshot getSnapshot(ViewCalculator calculator) {
        return new ObservableConceptSnapshot(calculator, this);
    }

    @Override
    protected void addAdditionalChronologyFeatures(MutableList<Feature<?>> features) {
        // Nothing to add.
    }
}
