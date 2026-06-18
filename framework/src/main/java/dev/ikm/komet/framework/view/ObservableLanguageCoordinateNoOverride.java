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

import dev.ikm.komet.terms.KometTerm;
import dev.ikm.tinkar.coordinate.language.LanguageCoordinate;
import dev.ikm.tinkar.coordinate.language.LanguageCoordinateRecord;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import javafx.beans.value.ObservableValue;
import org.eclipse.collections.api.list.ImmutableList;

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
    protected SimpleEqualityBasedObjectProperty<ImmutableList<PatternFacade>> makeDialectPatternPreferenceListProperty(LanguageCoordinate languageCoordinate) {
        return new SimpleEqualityBasedObjectProperty<>(this,
                KometTerm.DIALECT_ASSEMBLAGE_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE.toXmlFragment(),
                languageCoordinate.dialectPatternPreferenceList());
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ImmutableList<ConceptFacade>> makeDescriptionTypePreferenceListProperty(LanguageCoordinate languageCoordinate) {
        return new SimpleEqualityBasedObjectProperty<>(this,
                KometTerm.DESCRIPTION_TYPE_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE.toXmlFragment(),
                languageCoordinate.descriptionTypePreferenceList());
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ImmutableList<ConceptFacade>> makeModulePreferenceListProperty(LanguageCoordinate languageCoordinate) {
        return new SimpleEqualityBasedObjectProperty<>(this,
                KometTerm.MODULE_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE.toXmlFragment(),
                languageCoordinate.modulePreferenceListForLanguage());
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ImmutableList<PatternFacade>> makeDescriptionPatternPreferenceListProperty(LanguageCoordinate languageCoordinate) {
        return new SimpleEqualityBasedObjectProperty<>(this,
                "Description pattern list (fix)",
                languageCoordinate.descriptionPatternPreferenceNidList().map(PatternFacade::make));
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
        this.descriptionPatternPreferenceListProperty().setValue(newValue.descriptionPatternPreferenceNidList().map(PatternFacade::make));
        this.dialectPatternPreferenceListProperty().setValue(newValue.dialectPatternPreferenceList());
        this.descriptionTypePreferenceListProperty().setValue(newValue.descriptionTypePreferenceList());
        this.modulePreferenceListForLanguageProperty().setValue(newValue.modulePreferenceListForLanguage());
        return newValue;
    }

}
