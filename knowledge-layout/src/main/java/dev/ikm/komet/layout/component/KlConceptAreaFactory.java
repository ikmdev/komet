package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservableConcept;
import javafx.scene.layout.Pane;

/**
 * A non-sealed factory interface for creating instances of {@code KlComponentPane}
 * specifically associated with concepts observable through the {@code ObservableConcept} type.
 *
 * The {@code KlConceptPaneFactory} interface extends the core functionality provided
 * by {@code KlComponentPaneFactory}, while tightly integrating the factory with
 * {@code ObservableConcept} entities. This enables the creation of JavaFX-based
 * UI components representing distinct concept entities, facilitating their use
 * within the context of the Komet framework's modular layout system.
 *
 * @param <FX> the type of JavaFX pane created by this factory
 * @param <KL> the type of {@code KlComponentPane} produced, which is tied to both {@code FX}
 *             and {@code ObservableConcept}
 * @param <OE> the type parameter representing the {@code ObservableConcept} associated
 *             with the created {@code KlComponentPane}
 * @see KlComponentAreaFactory
 * @see ObservableConcept
 */
public non-sealed interface KlConceptAreaFactory<FX extends Pane, KL extends KlComponentArea<OE, FX>, OE extends ObservableConcept>
        extends KlComponentAreaFactory<FX, KL, OE> {
}