package dev.ikm.komet.framework.observable;

import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;

public class FieldLocatorForVersion {

    static <T> ObservableField<T> locate(ObservableVersion observableVersion,
                                         ComponentFieldLocator componentFieldLocator,
                                         StampCalculator stampCalculator) {
        return switch (observableVersion) {
            case ObservableConceptVersion observableConceptVersion ->
                    locateFieldOnConceptVersion(observableConceptVersion, componentFieldLocator, stampCalculator);
            case ObservablePatternVersion observablePatternVersion ->
                    locateFieldOnPatternVersion(observablePatternVersion, componentFieldLocator, stampCalculator);
            case ObservableSemanticVersion observableSemanticVersion ->
                    locateFieldOnSemanticVersion(observableSemanticVersion, componentFieldLocator, stampCalculator);
            case ObservableStampVersion observableStampVersion ->
                    locateFieldOnStampVersion(observableStampVersion, componentFieldLocator, stampCalculator);
        };
    }

    private static <T> ObservableField<T> locateFieldOnStampVersion(ObservableStampVersion observableStampVersion,
                                                                    ComponentFieldLocator componentFieldLocator,
                                                                    StampCalculator stampCalculator) {
        return observableStampVersion.getObservableFields().get(componentFieldLocator.category());
    }

    private static <T> ObservableField<T> locateFieldOnSemanticVersion(ObservableSemanticVersion observableSemanticVersion,
                                                                       ComponentFieldLocator componentFieldLocator,
                                                                       StampCalculator stampCalculator) {
        return observableSemanticVersion.getObservableFields().get(componentFieldLocator.category());
    }

    private static <T> ObservableField<T> locateFieldOnPatternVersion(ObservablePatternVersion observablePatternVersion,
                                                                      ComponentFieldLocator componentFieldLocator,
                                                                      StampCalculator stampCalculator) {
        return observablePatternVersion.getObservableFields().get(componentFieldLocator.category());
    }

    private static <T> ObservableField<T> locateFieldOnConceptVersion(ObservableConceptVersion observableConceptVersion,
                                                                      ComponentFieldLocator componentFieldLocator,
                                                                      StampCalculator stampCalculator) {
        return observableConceptVersion.getObservableFields().get(componentFieldLocator.category());
    }


}
