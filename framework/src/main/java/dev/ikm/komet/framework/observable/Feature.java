package dev.ikm.komet.framework.observable;

import dev.ikm.tinkar.component.FeatureDefinition;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.PatternFacade;
import javafx.beans.property.ReadOnlyProperty;

/*
ComponentFeature?

DT extends Feature?

Always has a value?

 */
public sealed interface Feature<DT>
        permits FeatureList, FeatureWrapper, ObservableChronology,
        ObservableSemanticField, ObservableFeatureDefinition,
        ObservableVersion  {

    /*
    If I just implement ObservableFeature as:

    Meaning, Purpose, Value, and disconnect from all the other classes:

    DT can be:
        Object (Traditional Semantic Field)
        FieldDefinition (Pattern field definitions)
        List<ObservableFeature>

ObservableFeatureField

Field<LocatableField>


    ObservableDefinition can be encapsulated by a feature, but not a feature itself.

    ObservableFeature
        ObservableField
            ObservableFieldDirect
            ObservableFieldIndirect
        ObservableFeatureList<ObservableField>
     */

    FeatureKey featureKey();

    ObservableComponent containingComponent();

    int patternNid();

    default PatternFacade pattern() {
        return EntityProxy.Pattern.make(patternNid());
    }

    int indexInPattern();

    default FeatureDefinition definition(StampCalculator stampCalculator) {
        ObservablePattern observablePattern = ObservableEntityHandle.getPatternOrThrow(this.patternNid());
        Latest<ObservablePatternVersion> patternVersion = stampCalculator.latest(observablePattern);
        if (patternVersion.isPresent()) {
            return patternVersion.get().fieldDefinitions().get(this.indexInPattern());
        } else {
            throw new RuntimeException("Pattern version not found for " + this +
                    " in " + observablePattern);
        }
    }

    ReadOnlyProperty<? extends Feature<DT>> featureProperty();


    // @TODO delete default implementation.
    default DT value() {
        // TODO: This is a hack to get around a few problems. Needs to be rethought.
        return switch (this) {
            case FeatureList<?> featureList -> (DT) featureList;
            case ObservableSemanticField<DT> observableSemanticField -> observableSemanticField.value();
            case FeatureWrapper<DT> wrapper -> wrapper.value();

            case ObservableChronology chronologyFeature -> (DT) chronologyFeature.value();
            case ObservableFeatureDefinition observableFeatureDefinition -> (DT) observableFeatureDefinition;
            case ObservableVersion observableVersion -> (DT) observableVersion;
        };
    }

}
