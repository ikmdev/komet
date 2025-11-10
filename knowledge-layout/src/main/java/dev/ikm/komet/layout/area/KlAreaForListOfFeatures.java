package dev.ikm.komet.layout.area;

import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.komet.framework.observable.FeatureKey;
import dev.ikm.komet.framework.observable.FeatureList;
import dev.ikm.tinkar.common.binary.Decoder;
import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.common.binary.EncoderOutput;
import javafx.collections.ObservableList;
import javafx.scene.layout.Region;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.util.Optional;

/**
 * Represents a sealed interface within the Knowledge Layout (KL) framework for managing
 * areas that handle observable lists of feature components. This interface provides a
 * contract for defining and interacting with structured regions tied to observable
 * features within a JavaFX context. It extends the functionality of {@link KlAreaForList}
 * to focus on feature-related behavior and management.
 *
 * The primary purpose of this interface is to enable modular and dynamic management
 * of feature data and their visual representation within a defined UI area. Classes
 * implementing this interface must specialize in managing feature components and their
 * integration with JavaFX regions to ensure seamless interaction and data updates.
 *
 * @param <F> the type of elements that extend {@link Feature}, representing
 *             the observable feature components managed by this region
 * @param <FX> the type of JavaFX {@link Region} to which the managed features are associated
 */
public sealed interface KlAreaForListOfFeatures<F extends Feature<?>, FX extends Region>
        extends KlAreaForList<F, FeatureList<F>, FX>
        permits KlAreaForListOfFeatureDefinitions, KlListOfFieldArea, KlAreaForListOfVersions {

    enum PreferenceKeys {
        AREA_FEATURE_LIST_KEY,
        SELECTED_FEATURES_KEY,
    }


    class Selection implements Encodable {
        final ImmutableList<FeatureKey> featureKeys;

        public Selection(ImmutableList<FeatureKey> featureKeys) {
            this.featureKeys = featureKeys;
        }

        public Selection(ObservableList<Feature<?>> featureKeys) {
            this.featureKeys = Lists.immutable.ofAll(featureKeys.stream().map(f -> f.featureKey()).toList());
        }

        public Selection(FeatureList<?> featureKeys) {
            this.featureKeys = Lists.immutable.ofAll(featureKeys.stream().map(f -> f.featureKey()).toList());
        }

        public ImmutableList<FeatureKey> getFeatureKeys() {
            return featureKeys;
        }

        @Override
        public void encode(EncoderOutput out) {
            out.writeVarInt(featureKeys.size());
            for (FeatureKey featureKey : featureKeys) {
                out.write(featureKey);
            }
        }

        @Decoder
        public static Selection decode(DecoderInput in) {
            switch (Encodable.checkVersion(in)) {
                // if special handling for particular versions, add case condition.
                default -> {
                    int size = in.readVarInt();
                    MutableList<FeatureKey> selectedFeatureListItems = Lists.mutable.ofInitialCapacity(size);
                    for (int i = 0; i < size; i++) {
                        selectedFeatureListItems.add(in.decode());
                    }
                    return new Selection(selectedFeatureListItems.toImmutable());
                }
            }
        }

    }

    Selection EMPTY_SELECTION = new Selection(Lists.immutable.empty());

    default Optional<FeatureKey> getAreaFeatureListKey() {
        return preferences().getObject(PreferenceKeys.AREA_FEATURE_LIST_KEY);
    }

    default void setAreaFeatureListKey(FeatureKey areaFeatureListKey) {
        preferences().putObject(PreferenceKeys.AREA_FEATURE_LIST_KEY, areaFeatureListKey);
    }

    default ImmutableList<FeatureKey> getSelectedFeatureKeys() {
        if (preferences().hasKey(PreferenceKeys.SELECTED_FEATURES_KEY)) {
            Optional<Selection> selectedFeatures = preferences().getObject(PreferenceKeys.SELECTED_FEATURES_KEY);
            if (selectedFeatures.isPresent()) {
                return selectedFeatures.get().featureKeys;
            }
        }
        return Lists.immutable.empty();
    }

    default void setSelectedFeatureKeys(ImmutableList<FeatureKey> selectedFeatureKeys) {
        Selection selection = new Selection(selectedFeatureKeys);
        preferences().putObject(PreferenceKeys.SELECTED_FEATURES_KEY, selection);
    }

    /**
     * Represents a factory interface for constructing and managing instances of areas that work with
     * observable features and their corresponding JavaFX regions. This interface extends the
     * functionality of the {@code KlListArea.Factory} by focusing specifically on areas dealing
     * with observable features.
     * <p>
     * The factory is specialized to produce areas that manage observable lists of elements extending
     * {@code ObservableFeature} and are associated with JavaFX regions of type {@code Region}.
     * Implementations of this interface will provide the necessary mechanisms for creating and
     * customizing these areas within the Knowledge Layout (KL) framework.
     *
     * @param <F> the type of elements, extending {@code ObservableFeature}, representing the observable
     *             features to be managed within the list area.
     * @param <FX> the type of JavaFX region to which the managed features are associated, extending {@code Region}.
     * @param <KL> the type of list area, extending {@code KlListOfFeatureArea}, which represents the structure
     *             for managing observable features within the associated JavaFX region.
     */
    interface Factory<F extends Feature<?>, FX extends Region, KL extends KlAreaForListOfFeatures<F, FX>>
            extends KlAreaForList.Factory<F, FeatureList<F>, FX, KL> {
    }
}
