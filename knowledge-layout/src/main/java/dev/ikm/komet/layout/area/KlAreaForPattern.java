package dev.ikm.komet.layout.area;

import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.FullyQualifiedName;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import dev.ikm.tinkar.terms.PatternFacade;
import javafx.scene.layout.Region;


/**
 * Represents a specialized interface in the Knowledge Layout framework for managing
 * field areas associated with patterns. This interface extends {@code KlFieldArea}
 * and is used to define field areas that specifically interact with {@code PatternFacade}
 * objects and their associated JavaFX {@code Region} components.
 * <p>
 * This interface is annotated with {@code FullyQualifiedName}, {@code RegularName}, and
 * {@code ParentConcept} annotations, which offer metadata for describing its role and
 * relationship within the system.
 *
 * @param <FX> The type of JavaFX {@link Region} associated with this field area for managing
 *            or displaying pattern-related properties.
 */
@FullyQualifiedName("Knowledge layout pattern feature area")
@RegularName("Pattern area")
@ParentConcept(KlAreaForFeature.class)
public non-sealed interface KlAreaForPattern<FX extends Region>
        extends KlAreaForFeature<PatternFacade, Feature<PatternFacade>, FX> {

    /**
     * Represents a factory interface for creating and managing instances of {@code KlFieldAreaForPattern},
     * which links {@code PatternFacade} objects to JavaFX {@code Region} elements.
     * <p>
     * This interface extends the {@code KlFieldArea.Factory} by specializing it for patterns, where
     * the field areas are associated with {@code PatternFacade} data types and their corresponding
     * JavaFX components.
     *
     * @param <FX> The type of JavaFX {@link Region} associated with the field area for managing
     *             pattern-related properties.
     */
    interface Factory<FX extends Region>
            extends KlAreaForFeature.Factory<PatternFacade, Feature<PatternFacade>, FX, KlAreaForPattern<FX>> {
    }

}
