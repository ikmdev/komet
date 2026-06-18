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
package dev.ikm.komet.framework.view;

import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.coordinate.language.LanguageCoordinate;
import dev.ikm.tinkar.coordinate.language.LanguageCoordinateRecord;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import javafx.beans.value.ObservableValue;
import org.eclipse.collections.api.list.ImmutableList;

public class ObservableLanguageCoordinateWithOverride extends ObservableLanguageCoordinateBase {

    private final ObservableLanguageCoordinate overriddenCoordinate;


    public ObservableLanguageCoordinateWithOverride(ObservableLanguageCoordinate overriddenCoordinate, String coordinateName) {
        super(overriddenCoordinate, coordinateName);
        // Depth-independent override nesting (ike-issues#663): an override may wrap another override.
        this.overriddenCoordinate = overriddenCoordinate;
    }

    public ObservableLanguageCoordinateWithOverride(ObservableLanguageCoordinate overriddenCoordinate) {
        this(overriddenCoordinate, overriddenCoordinate.getName());
    }

    @Override
    public void setExceptOverrides(LanguageCoordinateRecord updatedCoordinate) {
        if (hasOverrides()) {
            int languageConceptNid = updatedCoordinate.languageConceptNid();
            if (languageConceptProperty().isOverridden()) {
                languageConceptNid = languageConceptProperty().get().nid();
            }
            IntIdList descriptionPatternPreferenceNidList = updatedCoordinate.descriptionPatternPreferenceNidList();
            if (modulePreferenceListForLanguageProperty().isOverridden()) {
                descriptionPatternPreferenceNidList = descriptionPatternPreferenceNidList();
            }


            IntIdList modulePreferenceList = updatedCoordinate.modulePreferenceNidListForLanguage();
            if (modulePreferenceListForLanguageProperty().isOverridden()) {
                modulePreferenceList = modulePreferenceNidListForLanguage();
            }

            IntIdList descriptionTypePreferenceList = updatedCoordinate.descriptionTypePreferenceNidList();
            if (descriptionTypePreferenceListProperty().isOverridden()) {
                descriptionTypePreferenceList = descriptionTypePreferenceNidList();
            }

            IntIdList dialectAssemblagePreferenceList = updatedCoordinate.dialectPatternPreferenceNidList();
            if (dialectPatternPreferenceListProperty().isOverridden()) {
                dialectAssemblagePreferenceList = dialectPatternPreferenceNidList();
            }

            setValue(LanguageCoordinateRecord.make(languageConceptNid, descriptionPatternPreferenceNidList,
                    descriptionTypePreferenceList, dialectAssemblagePreferenceList, modulePreferenceList));

        } else {
            setValue(updatedCoordinate);
        }
    }

    @Override
    public OverrideOf<ImmutableList<ConceptFacade>> modulePreferenceListForLanguageProperty() {
        return (OverrideOf<ImmutableList<ConceptFacade>>) super.modulePreferenceListForLanguageProperty();
    }

    @Override
    public OverrideOf<ImmutableList<ConceptFacade>> descriptionTypePreferenceListProperty() {
        return (OverrideOf<ImmutableList<ConceptFacade>>) super.descriptionTypePreferenceListProperty();
    }

    @Override
    public OverrideOf<ImmutableList<PatternFacade>> dialectPatternPreferenceListProperty() {
        return (OverrideOf<ImmutableList<PatternFacade>>) super.dialectPatternPreferenceListProperty();
    }

    @Override
    public OverrideOf<ImmutableList<PatternFacade>> descriptionPatternPreferenceListProperty() {
        return (OverrideOf<ImmutableList<PatternFacade>>) super.descriptionPatternPreferenceListProperty();
    }

    @Override
    public OverrideOf<ConceptFacade> languageConceptProperty() {
        return (OverrideOf<ConceptFacade>) super.languageConceptProperty();
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ImmutableList<PatternFacade>> makeDescriptionPatternPreferenceListProperty(LanguageCoordinate languageCoordinate) {
        ObservableLanguageCoordinate overriddenCoordinate = (ObservableLanguageCoordinate) languageCoordinate;
        return new OverrideOf<>(overriddenCoordinate.descriptionPatternPreferenceListProperty(), this);
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptFacade> makeLanguageProperty(LanguageCoordinate languageCoordinate) {
        ObservableLanguageCoordinate overriddenCoordinate = (ObservableLanguageCoordinate) languageCoordinate;
        return new OverrideOf<>(overriddenCoordinate.languageConceptProperty(), this);
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ImmutableList<PatternFacade>> makeDialectPatternPreferenceListProperty(LanguageCoordinate languageCoordinate) {
        ObservableLanguageCoordinate overriddenCoordinate = (ObservableLanguageCoordinate) languageCoordinate;
        return new OverrideOf<>(overriddenCoordinate.dialectPatternPreferenceListProperty(), this);
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ImmutableList<ConceptFacade>> makeDescriptionTypePreferenceListProperty(LanguageCoordinate languageCoordinate) {
        ObservableLanguageCoordinate overriddenCoordinate = (ObservableLanguageCoordinate) languageCoordinate;
        return new OverrideOf<>(overriddenCoordinate.descriptionTypePreferenceListProperty(), this);
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ImmutableList<ConceptFacade>> makeModulePreferenceListProperty(LanguageCoordinate languageCoordinate) {
        ObservableLanguageCoordinate overriddenCoordinate = (ObservableLanguageCoordinate) languageCoordinate;
        return new OverrideOf<>(overriddenCoordinate.modulePreferenceListForLanguageProperty(), this);
    }

    @Override
    public LanguageCoordinateRecord getOriginalValue() {
        return LanguageCoordinateRecord.make(languageConceptProperty().getOriginalValue().nid(),
                IntIds.list.of(descriptionPatternPreferenceListProperty().getOriginalValue().castToList(), EntityFacade::toNid),
                IntIds.list.of(descriptionTypePreferenceListProperty().getOriginalValue().castToList(), EntityFacade::toNid),
                IntIds.list.of(dialectPatternPreferenceListProperty().getOriginalValue().castToList(), EntityFacade::toNid),
                IntIds.list.of(modulePreferenceListForLanguageProperty().getOriginalValue().castToList(), EntityFacade::toNid));
    }

    @Override
    protected LanguageCoordinateRecord baseCoordinateChangedListenersRemoved(ObservableValue<? extends LanguageCoordinateRecord> observable,
                                                                             LanguageCoordinateRecord oldValue,
                                                                             LanguageCoordinateRecord newValue) {
        if (!this.languageConceptProperty().isOverridden()) {
            this.languageConceptProperty().setValue(newValue.languageConcept());
        }

        if (!this.descriptionPatternPreferenceListProperty().isOverridden()) {
            this.descriptionPatternPreferenceListProperty().setValue(newValue.descriptionPatternPreferenceNidList().map(PatternFacade::make));
        }

        if (!this.dialectPatternPreferenceListProperty().isOverridden()) {
            this.dialectPatternPreferenceListProperty().setValue(newValue.dialectPatternPreferenceList());
        }

        if (!this.descriptionTypePreferenceListProperty().isOverridden()) {
            this.descriptionTypePreferenceListProperty().setValue(newValue.descriptionTypePreferenceList());
        }

        if (!this.modulePreferenceListForLanguageProperty().isOverridden()) {
            this.modulePreferenceListForLanguageProperty().setValue(newValue.modulePreferenceListForLanguage());
        }

        return newValue;
    }

}
