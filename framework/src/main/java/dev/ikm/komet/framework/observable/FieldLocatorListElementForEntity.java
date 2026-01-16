package dev.ikm.komet.framework.observable;

import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;

public class FieldLocatorListElementForEntity {

    static <T> ObservableField<T> locate(ObservableEntity observableEntity,
                                         ComponentFieldListElementLocator componentFieldListElementLocator,
                                         StampCalculator stampCalculator) {
        return switch (observableEntity) {
            case ObservableConcept observableConcept -> locateFieldListElementOnConcept(observableConcept, componentFieldListElementLocator, stampCalculator);
            case ObservablePattern observablePattern -> locateFieldListElementOnPattern(observablePattern, componentFieldListElementLocator, stampCalculator);
            case ObservableSemantic observableSemantic -> locateFieldListElementOnSemantic(observableSemantic, componentFieldListElementLocator, stampCalculator);
            case ObservableStamp observableStamp -> locateFieldListElementOnStamp(observableStamp, componentFieldListElementLocator, stampCalculator);
        };
    }

    private static <T> ObservableField<T> locateFieldListElementOnStamp(ObservableStamp observableStamp,
                                                                        ComponentFieldListElementLocator componentFieldListElementLocator,
                                                                        StampCalculator stampCalculator) {
        return observableStamp.getObservableFields().get(componentFieldListElementLocator.category());
    }

    private static <T> ObservableField<T> locateFieldListElementOnSemantic(ObservableSemantic observableSemantic,
                                                                           ComponentFieldListElementLocator componentFieldListElementLocator,
                                                                           StampCalculator stampCalculator) {
        return observableSemantic.getObservableFields().get(componentFieldListElementLocator.category());
    }

    private static <T> ObservableField<T> locateFieldListElementOnPattern(ObservablePattern observablePattern,
                                                                          ComponentFieldListElementLocator componentFieldListElementLocator,
                                                                          StampCalculator stampCalculator) {
        return observablePattern.getObservableFields().get(componentFieldListElementLocator.category());
    }

    private static <T> ObservableField<T> locateFieldListElementOnConcept(ObservableConcept observableConcept,
                                                                          ComponentFieldListElementLocator componentFieldListElementLocator,
                                                                          StampCalculator stampCalculator) {
        return observableConcept.getObservableFields().get(componentFieldListElementLocator.category());
    }
}
