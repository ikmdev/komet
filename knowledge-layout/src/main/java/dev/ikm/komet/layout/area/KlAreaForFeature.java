package dev.ikm.komet.layout.area;

import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.komet.framework.observable.FeatureKey;
import dev.ikm.komet.layout.KlArea;
import dev.ikm.tinkar.common.bind.ClassConceptBinding;
import javafx.beans.property.Property;
import javafx.scene.layout.Region;

import java.util.Optional;

/**
 * Represents a non-sealed interface within the Knowledge Layout framework for managing and
 * interacting with specific features of a given data type and their associated JavaFX regions.
 * This interface defines methods for observing, updating, and managing feature properties in
 * a type-safe and context-aware manner.
 *
 * @param <DT> the data type of the observable features managed by this feature area
 * @param <FX> the type of the JavaFX {@code Region} associated with this feature area
 */
public sealed interface KlAreaForFeature<DT, F extends Feature<DT>, FX extends Region>
        extends KlArea<FX>, KlFeaturePropertyForArea<F>, ClassConceptBinding
        permits KlFieldArea, KlAreaForBoolean, KlAreaForConcept, KlAreaForEntity, KlAreaForObject,
        KlAreaForPattern, KlAreaForPublicId, KlAreaForSemantic, KlAreaForStamp, KlAreaForFeatureDefinition,
        KlAreaForString {

    enum PreferenceKeys {
        AREA_FEATURE_KEY
    }

    default Optional<FeatureKey> getAreaFeatureKey() {
        return preferences().getObject(PreferenceKeys.AREA_FEATURE_KEY);
    }

    /**
     * Sets the {@link FeatureKey} used to identify the feature associated with this area.
     * <p>
     * The provided key must be resolvable; that is, it must be sufficiently specified
     * to uniquely identify and retrieve a single feature.
     * If the given key is not resolvable (for example, if it is a wildcard or ambiguous),
     * this method throws an {@link IllegalArgumentException}.
     *
     * @param areaFeature the feature key to associate with this area
     * @throws IllegalArgumentException if {@code areaFeature} is not resolvable
     */
    default void setAreaFeatureKey(FeatureKey areaFeature) {
        if (areaFeature.isResolvable()) {
            preferences().putObject(PreferenceKeys.AREA_FEATURE_KEY, areaFeature);
        } else {
            throw new IllegalArgumentException("FeatureKey must be realizable: " + areaFeature);
        }
    }

    /**
     * Sets the specified feature in this feature area. This method assigns an observable
     * feature of type {@code DT} to the property of the area, allowing it to be observed
     * and managed within the current context.
     *
     * @param feature the observable feature of type {@code DT} to be set in the feature area
     */
    default void setFeature(F feature) {
        if (getFeatureProperty() != null && getFeatureProperty() instanceof Property property) {
            property.setValue(feature);
            setAreaFeatureKey(feature.featureKey());
        } else {
            throw new IllegalStateException("Property is ReadOnly");
        }
    }

    /**
     * Retrieves the observable feature managed by this feature area. The returned observable feature
     * represents the current value of type {@code DT} associated with the property of the feature area.
     *
     * @return an {@code ObservableFeature} of type {@code DT}, representing the current value managed
     * within this feature area.
     */
    Optional<F> getFeature();

    non-sealed interface Factory<DT, F extends Feature<DT>, FX extends Region, KL extends KlAreaForFeature<DT, F, FX>>
            extends KlArea.Factory<FX, KL> {

    }

}
