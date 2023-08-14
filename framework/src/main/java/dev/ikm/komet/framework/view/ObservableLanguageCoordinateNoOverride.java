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
import javafx.collections.FXCollections;
import org.eclipse.collections.api.list.ImmutableList;
import dev.ikm.komet.terms.KometTerm;
import dev.ikm.tinkar.coordinate.language.LanguageCoordinate;
import dev.ikm.tinkar.coordinate.language.LanguageCoordinateRecord;
import dev.ikm.tinkar.terms.*;

/**
 * The Class ObservableLanguageCoordinateImpl.
 *
 * 
 */
public final class ObservableLanguageCoordinateNoOverride
        extends ObservableLanguageCoordinateBase {

     /**
     * Instantiates a new observable language coordinate impl.
     *
     * @param languageCoordinate the language coordinate
     */
     public ObservableLanguageCoordinateNoOverride(LanguageCoordinate languageCoordinate, String coordinateName) {
         super(languageCoordinate, coordinateName);
     }

    public ObservableLanguageCoordinateNoOverride(LanguageCoordinate languageCoordinate) {
        super(languageCoordinate, "Language coordinate");
    }

    @Override
    public void setExceptOverrides(LanguageCoordinateRecord updatedCoordinate) {
        setValue(updatedCoordinate);
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptFacade> makeLanguageProperty(LanguageCoordinate languageCoordinate) {
        return new SimpleEqualityBasedObjectProperty<>(this,
                KometTerm.LANGUAGE_SPECIFICATION_FOR_LANGUAGE_COORDINATE.toXmlFragment(),
                languageCoordinate.languageConcept());
    }

    @Override
    protected SimpleEqualityBasedListProperty<PatternFacade> makeDialectPatternPreferenceListProperty(LanguageCoordinate languageCoordinate) {
        return new SimpleEqualityBasedListProperty<>(this,
                KometTerm.DIALECT_ASSEMBLAGE_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE.toXmlFragment(),
                FXCollections.observableArrayList(languageCoordinate.dialectPatternPreferenceNidList().mapToList(EntityProxy.Pattern::make)));
    }

    @Override
    protected SimpleEqualityBasedListProperty<ConceptFacade> makeDescriptionTypePreferenceListProperty(LanguageCoordinate languageCoordinate) {
        return new SimpleEqualityBasedListProperty<>(this,
                KometTerm.DESCRIPTION_TYPE_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE.toXmlFragment(),
                FXCollections.observableArrayList(languageCoordinate.descriptionTypePreferenceNidList().mapToList(EntityProxy.Concept::make)));
    }

    @Override
    protected SimpleEqualityBasedListProperty<ConceptFacade> makeModulePreferenceListProperty(LanguageCoordinate languageCoordinate) {
        ImmutableList<ConceptFacade> modulePreferenceList = languageCoordinate.modulePreferenceListForLanguage();
        return new SimpleEqualityBasedListProperty<>(this,
                KometTerm.MODULE_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE.toXmlFragment(),
                FXCollections.observableArrayList(modulePreferenceNidListForLanguage().mapToList(EntityProxy.Concept::make)));
    }

    @Override
    protected SimpleEqualityBasedListProperty<PatternFacade> makeDescriptionPatternPreferenceListProperty(LanguageCoordinate languageCoordinate) {
        return new SimpleEqualityBasedListProperty<>(this,
                "Description pattern list (fix)",
                FXCollections.observableArrayList(descriptionPatternPreferenceNidList().mapToList(EntityProxy.Pattern::make)));
    }

    @Override
    public LanguageCoordinateRecord getOriginalValue() {
        return getValue();
    }


    @Override
    protected LanguageCoordinateRecord baseCoordinateChangedListenersRemoved(
            ObservableValue<? extends LanguageCoordinateRecord> observable,
            LanguageCoordinateRecord oldValue, LanguageCoordinateRecord newValue) {
        this.languageConceptProperty().setValue(newValue.languageConcept());
        this.descriptionPatternPreferenceListProperty().setAll(newValue.dialectPatternPreferenceNidList().mapToList(PatternFacade::make));
        this.dialectPatternPreferenceListProperty().setAll(newValue.dialectPatternPreferenceNidList().mapToList(PatternFacade::make));
        this.descriptionTypePreferenceListProperty().setAll(newValue.descriptionTypePreferenceNidList().mapToList(ConceptFacade::make));
        this.modulePreferenceListForLanguageProperty().setAll(newValue.modulePreferenceNidListForLanguage().mapToList(ConceptFacade::make));
        return newValue;
    }

}
