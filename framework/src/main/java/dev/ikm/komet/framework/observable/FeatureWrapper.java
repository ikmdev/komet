package dev.ikm.komet.framework.observable;

import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.Calculators;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculatorWithCache;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;

import java.util.Optional;

/**
 * The Feature class represents an attribute or characteristic within an {@code ObservableComponent}.
 * It serves as a data encapsulation for metadata and associated values of a field in the component.
 * <p>
 *  Features contain their field definition, and therefore require a {@code StampCalculator} to determine
 *  the correct pattern version based on the {@code StampCalculator}, which is used to determine current meaning and purpose.
 *  <p>
 *  Because JavaFX writable properties (the {@code Property} interface) extend {@code ReadOnlyProperty}, feature may be
 *  writable if the valueProperty is a type of {@code WritableProperty}.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * public void updateIfWritable(ReadOnlyProperty<?> property, Object value) {
 *     if (property instanceof Property<?> writable) {
 *         writable.setValue(value); // it's writable
 *     } else {
 *         // it's read-only
 *     }
 * }
 * }
 * </pre>

 * @param <DT> the data type of the value associated with this Feature
 */
public final class FeatureWrapper<DT> implements Feature<DT> {
    private final ObservableComponent containingComponent;
    private final int patternNid;
    private final int patternIndex;
    private final ReadOnlyProperty<DT> valueProperty;
    private final ReadOnlyProperty<Feature<DT>> featureProperty;
    public final FeatureKey locator;


    public FeatureWrapper(Property<DT> valueProperty, int patternNid,
                          int patternIndex, ObservableComponent containingComponent, FeatureKey featureKey) {
        this.featureProperty = new ReadOnlyObjectWrapper(this).getReadOnlyProperty();
        this.containingComponent = containingComponent;
        this.patternNid = patternNid;
        this.patternIndex = patternIndex;
        this.locator = featureKey;
        this.valueProperty = valueProperty;
    }

    public FeatureWrapper(DT value, int patternNid, int patternIndex, ObservableComponent containingComponent,
                          FeatureKey featureKey) {
        this.containingComponent = containingComponent;
        this.patternNid = patternNid;
        this.patternIndex = patternIndex;
        this.locator = featureKey;
        this.valueProperty = new ReadOnlyObjectWrapper(value).getReadOnlyProperty();
        this.featureProperty = new ReadOnlyObjectWrapper(this).getReadOnlyProperty();
    }

    public int patternNid() {
        return patternNid;
    }

    public DT value() {
        return valueProperty.getValue();
    }

    public ReadOnlyProperty<DT> valueProperty() {
        return valueProperty;
    }

    public ReadOnlyProperty<Feature<DT>>  featureProperty() {
        return featureProperty;
    }

    public Optional<Property<DT>> writableValueProperty() {
        if (valueProperty instanceof Property<DT> writable) {
            return Optional.of(writable);
        } else {
            return Optional.empty();
        }
    }

    public int indexInPattern() {
        return patternIndex;
    }

    public ObservableComponent containingComponent() {
        return containingComponent;
    }

    public FeatureKey featureKey() { return locator; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("FeatureWrapper[");

        //TODO ideally get viewCalculator from a ScopedValue in the future... Set default scope to latest in KB.
        ViewCalculatorWithCache viewCalculator = Calculators.View.Default();
        Latest<PatternEntityVersion> latestPatternEntityVerion = viewCalculator.latestPatternEntityVersion(patternNid);
        latestPatternEntityVerion.ifPresentOrElse(patternVersion -> {
            String featureMeaning = viewCalculator.getDescriptionTextOrNid(patternVersion.fieldDefinitions().get(patternIndex).meaningNid());
            sb.append(featureMeaning);
        }, () -> {
            sb.append("Can't calculate");
        });
        String valueString = switch (valueProperty.getValue()) {
            case null -> "null";
            case Long timeAsLong -> DateTimeUtil.format(timeAsLong);
            case ConceptFacade conceptFacade -> "\"" + viewCalculator.getDescriptionTextOrNid(conceptFacade.nid()) + "\"";
            case String s -> "\"" + s + "\"";
            default -> valueProperty.getValue().toString();
        };
        sb.append("]: ").append(valueString);

        return sb.toString();
    }
}
