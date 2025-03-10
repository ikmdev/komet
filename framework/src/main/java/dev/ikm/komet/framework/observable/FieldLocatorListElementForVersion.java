package dev.ikm.komet.framework.observable;

import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.FieldDefinitionRecord;
import dev.ikm.tinkar.entity.FieldRecord;
import dev.ikm.tinkar.entity.PatternEntityVersion;

public class FieldLocatorListElementForVersion {
    static <T> ObservableField<T> locate(ObservableVersion observableVersion,
                                         ComponentFieldListElementLocator componentFieldListElementLocator,
                                         StampCalculator stampCalculator) {
        return switch (observableVersion) {
            case ObservableConceptVersion observableConceptVersion -> locateFieldListElementOnConceptVersion(observableConceptVersion, componentFieldListElementLocator, stampCalculator);
            case ObservablePatternVersion observablePatternVersion -> locateFieldListElementOnPatternVersion(observablePatternVersion, componentFieldListElementLocator, stampCalculator);
            case ObservableSemanticVersion observableSemanticVersion -> locateFieldListElementOnSemanticVersion(observableSemanticVersion, componentFieldListElementLocator, stampCalculator);
            case ObservableStampVersion observableStampVersion -> locateFieldListElementOnStampVersion(observableStampVersion, componentFieldListElementLocator, stampCalculator);
        };
    }

    static <T> ObservableField<T> locateFieldListElementOnStampVersion(ObservableStampVersion stampVersion,
                                                                       ComponentFieldListElementLocator componentFieldListElementLocator,
                                                                       StampCalculator stampCalculator) {
        throw new IllegalStateException("There are no list element fields on a stamp version");
    }

    static <T> ObservableField<T> locateFieldListElementOnSemanticVersion(ObservableSemanticVersion semanticVersion,
                                                                          ComponentFieldListElementLocator componentFieldListElementLocator,
                                                                          StampCalculator stampCalculator) {
        Latest<PatternEntityVersion> latestPatternVersion = stampCalculator.latestPatternEntityVersion(semanticVersion.patternNid());
        if (latestPatternVersion.isPresent()) {
            return semanticVersion.fields(latestPatternVersion.get()).get(componentFieldListElementLocator.index());
        }
        throw new IllegalStateException("There is no latest pattern version for: \n\n" + latestPatternVersion +
                "\n\n of semantic: \n\n" + semanticVersion);
    }


    static <T> ObservableField<T> locateFieldListElementOnPatternVersion(ObservablePatternVersion patternVersion,
                                                                         ComponentFieldListElementLocator componentFieldListElementLocator,
                                                                         StampCalculator stampCalculator) {
            return  switch(componentFieldListElementLocator.category()) {
                case PATTERN_FIELD_DEFINITION -> {
                    ObservableFieldDefinition patternVersionListElement = patternVersion.fieldDefinitions().get(componentFieldListElementLocator.index());
                    FieldDefinitionRecord fieldDefinitionRecord =  patternVersionListElement.fieldDefinitionReference.get();
                    FieldRecord fieldRecord = new FieldRecord(patternVersionListElement, patternVersion.nid(), patternVersion.stampNid(), fieldDefinitionRecord);
                    ObservableField observableField = new ObservableField(fieldRecord);
                    yield observableField;
                }
                case PUBLIC_ID_FIELD, COMPONENT_VERSIONS_LIST, VERSION_STAMP_FIELD, PATTERN_MEANING_FIELD,
                     PATTERN_PURPOSE_FIELD, PATTERN_FIELD_DEFINITION_LIST,
                     SEMANTIC_PATTERN_FIELD, SEMANTIC_REFERENCED_COMPONENT_FIELD, SEMANTIC_FIELD_LIST,
                     SEMANTIC_FIELD, STATUS_FIELD, TIME_FIELD, AUTHOR_FIELD, MODULE_FIELD, PATH_FIELD ->
                        throw new IllegalStateException("There are no fields of type "  + componentFieldListElementLocator.category()
                                + " a semantic version: " + patternVersion);
            };
    }

    static <T> ObservableField<T> locateFieldListElementOnConceptVersion(ObservableConceptVersion conceptVersion,
                                                                         ComponentFieldListElementLocator componentFieldListElementLocator,
                                                                         StampCalculator stampCalculator) {
        throw new IllegalStateException("There are no list element fields on a stamp version");
    }
}
