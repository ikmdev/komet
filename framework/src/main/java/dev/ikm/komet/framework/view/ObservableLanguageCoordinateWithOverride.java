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

import javafx.beans.value.ObservableValue;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.coordinate.language.LanguageCoordinate;
import dev.ikm.tinkar.coordinate.language.LanguageCoordinateRecord;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.PatternFacade;

public class ObservableLanguageCoordinateWithOverride extends ObservableLanguageCoordinateBase {

    private final ObservableLanguageCoordinate overriddenCoordinate;


    public ObservableLanguageCoordinateWithOverride(ObservableLanguageCoordinate overriddenCoordinate, String coordinateName) {
        super(overriddenCoordinate, coordinateName);
        if (overriddenCoordinate instanceof ObservableLanguageCoordinateWithOverride) {
            throw new IllegalStateException("Cannot override an overridden Coordinate. ");
        }
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
    public ListPropertyWithOverride<ConceptFacade> modulePreferenceListForLanguageProperty() {
        return (ListPropertyWithOverride<ConceptFacade>) super.modulePreferenceListForLanguageProperty();
    }

    @Override
    public ListPropertyWithOverride<ConceptFacade> descriptionTypePreferenceListProperty() {
        return (ListPropertyWithOverride<ConceptFacade>) super.descriptionTypePreferenceListProperty();
    }

    @Override
    public ListPropertyWithOverride<PatternFacade> dialectPatternPreferenceListProperty() {
        return (ListPropertyWithOverride<PatternFacade>) super.dialectPatternPreferenceListProperty();
    }

    @Override
    public ObjectPropertyWithOverride<ConceptFacade> languageConceptProperty() {
        return (ObjectPropertyWithOverride<ConceptFacade>) super.languageConceptProperty();
    }

    @Override
    protected SimpleEqualityBasedListProperty<PatternFacade> makeDescriptionPatternPreferenceListProperty(LanguageCoordinate languageCoordinate) {
        ObservableLanguageCoordinate overriddenCoordinate = (ObservableLanguageCoordinate) languageCoordinate;
        return new ListPropertyWithOverride<>(overriddenCoordinate.descriptionPatternPreferenceListProperty(), this);
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptFacade> makeLanguageProperty(LanguageCoordinate languageCoordinate) {
        ObservableLanguageCoordinate overriddenCoordinate = (ObservableLanguageCoordinate) languageCoordinate;
        return new ObjectPropertyWithOverride<>(overriddenCoordinate.languageConceptProperty(), this);
    }

    @Override
    protected SimpleEqualityBasedListProperty<PatternFacade> makeDialectPatternPreferenceListProperty(LanguageCoordinate languageCoordinate) {
        ObservableLanguageCoordinate overriddenCoordinate = (ObservableLanguageCoordinate) languageCoordinate;
        return new ListPropertyWithOverride<>(overriddenCoordinate.dialectPatternPreferenceListProperty(), this);
    }

    @Override
    protected SimpleEqualityBasedListProperty<ConceptFacade> makeDescriptionTypePreferenceListProperty(LanguageCoordinate languageCoordinate) {
        ObservableLanguageCoordinate overriddenCoordinate = (ObservableLanguageCoordinate) languageCoordinate;
        return new ListPropertyWithOverride<>(overriddenCoordinate.descriptionTypePreferenceListProperty(), this);
    }

    @Override
    protected SimpleEqualityBasedListProperty<ConceptFacade> makeModulePreferenceListProperty(LanguageCoordinate languageCoordinate) {
        ObservableLanguageCoordinate overriddenCoordinate = (ObservableLanguageCoordinate) languageCoordinate;
        return new ListPropertyWithOverride<>(overriddenCoordinate.modulePreferenceListForLanguageProperty(), this);
    }

    @Override
    public LanguageCoordinateRecord getOriginalValue() {
        return LanguageCoordinateRecord.make(languageConceptProperty().getOriginalValue().nid(),
                IntIds.list.of(dialectPatternPreferenceListProperty().getOriginalValue(), EntityFacade::toNid),
                IntIds.list.of(descriptionTypePreferenceListProperty().getOriginalValue(), EntityFacade::toNid),
                IntIds.list.of(dialectPatternPreferenceListProperty().getOriginalValue(), EntityFacade::toNid),
                IntIds.list.of(modulePreferenceListForLanguageProperty().getOriginalValue(), EntityFacade::toNid));
    }

    @Override
    protected LanguageCoordinateRecord baseCoordinateChangedListenersRemoved(ObservableValue<? extends LanguageCoordinateRecord> observable,
                                                                             LanguageCoordinateRecord oldValue,
                                                                             LanguageCoordinateRecord newValue) {
        this.languageConceptProperty().setValue(newValue.languageConcept());
        this.dialectPatternPreferenceListProperty().setAll(this.descriptionPatternPreferenceNidList().mapToList(PatternFacade::make));
        this.dialectPatternPreferenceListProperty().setAll(newValue.dialectPatternPreferenceNidList().mapToList(PatternFacade::make));
        this.descriptionTypePreferenceListProperty().setAll(newValue.descriptionTypePreferenceNidList().mapToList(ConceptFacade::make));
        this.modulePreferenceListForLanguageProperty().setAll(newValue.modulePreferenceNidListForLanguage().mapToList(ConceptFacade::make));
        return newValue;
    }

}
