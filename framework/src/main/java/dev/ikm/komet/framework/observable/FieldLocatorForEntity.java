package dev.ikm.komet.framework.observable;

import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;

public class FieldLocatorForEntity {

    static <T> ObservableField<T> locate(ObservableEntity observableEntity,
                                         ComponentFieldLocator componentFieldLocator,
                                         StampCalculator stampCalculator){
        return switch (observableEntity) {
            case ObservableConcept observableConcept -> locateFieldOnConcept(observableConcept, componentFieldLocator, stampCalculator);
            case ObservablePattern observablePattern -> locateFieldOnPattern(observablePattern, componentFieldLocator, stampCalculator);
            case ObservableSemantic observableSemantic -> locateFieldOnSemantic(observableSemantic, componentFieldLocator, stampCalculator);
            case ObservableStamp observableStamp -> locateFieldOnStamp(observableStamp, componentFieldLocator, stampCalculator);
        };
    }

    private static <T> ObservableField<T> locateFieldOnStamp(ObservableStamp observableStamp,
                                                             ComponentFieldLocator componentFieldLocator,
                                                             StampCalculator stampCalculator) {
        return observableStamp.getObservableFields().get(componentFieldLocator.category());
    }

    private static <T> ObservableField<T> locateFieldOnSemantic(ObservableSemantic observableSemantic,
                                                                ComponentFieldLocator componentFieldLocator,
                                                                StampCalculator stampCalculator) {
        return observableSemantic.getObservableFields().get(componentFieldLocator.category());
    }

    private static <T> ObservableField<T> locateFieldOnPattern(ObservablePattern observablePattern,
                                                               ComponentFieldLocator componentFieldLocator,
                                                               StampCalculator stampCalculator) {
        return observablePattern.getObservableFields().get(componentFieldLocator.category());
    }

    private static <T> ObservableField<T> locateFieldOnConcept(ObservableConcept observableConcept,
                                                               ComponentFieldLocator componentFieldLocator,
                                                               StampCalculator stampCalculator) {
        return observableConcept.getObservableFields().get(componentFieldLocator.category());
    }

}
