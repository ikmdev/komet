package dev.ikm.komet.layout.area;

import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.FullyQualifiedName;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import dev.ikm.tinkar.terms.SemanticFacade;
import javafx.scene.layout.Region;

/**
 * Represents a non-sealed interface for defining field areas dedicated to semantic data
 * within the Knowledge Layout framework. This interface specializes in managing
 * semantic-specific data representations and their integration with JavaFX regions.
 * <p>
 * Extending {@link KlFieldArea}, this interface introduces type-specific operations
 * for handling fields that utilize a {@link SemanticFacade} data type alongside
 * JavaFX regional components. It enforces a semantic context to the field area structure,
 * ensuring modular and extensible interaction with semantic data aspects.
 *
 * @param <FX> The type of JavaFX {@link Region} associated with this semantic field area.
 */
@FullyQualifiedName("Knowledge layout semantic feature area")
@RegularName("Semantic area")
@ParentConcept(KlAreaForFeature.class)
public non-sealed interface KlAreaForSemantic<FX extends Region>
        extends KlAreaForFeature<SemanticFacade, Feature<SemanticFacade>, FX> {

    /**
     * Represents a factory interface for creating and managing instances of semantic field areas in the
     * Knowledge Layout framework that are associated with a specific JavaFX {@link Region} component.
     * <p>
     * This interface extends {@code KlFieldArea.Factory}, specializing in the creation and configuration
     * of field areas that bind {@link SemanticFacade} data types to JavaFX regions. It defines a contract
     * for constructing semantic field areas with a focus on modular and extensible integration of semantic
     * data within a GUI layout.
     *
     * @param <FX> the type of JavaFX {@link Region} associated with the semantic field area,
     *             extending {@code Region}.
     */
    interface Factory<FX extends Region>
            extends KlAreaForFeature.Factory<SemanticFacade, Feature<SemanticFacade>, FX, KlAreaForSemantic<FX>> {
    }
}
