package dev.ikm.komet.layout.area;

import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.komet.framework.observable.ObservableFeatureDefinition;
import javafx.scene.layout.Region;

public non-sealed interface KlAreaForFeatureDefinition<FX extends Region>
        extends KlAreaForFeature<ObservableFeatureDefinition, Feature<ObservableFeatureDefinition>, FX> {

    interface Factory<FX extends Region>
            extends KlAreaForFeature.Factory<ObservableFeatureDefinition, Feature<ObservableFeatureDefinition>, FX, KlAreaForFeatureDefinition<FX>> {
    }

}
