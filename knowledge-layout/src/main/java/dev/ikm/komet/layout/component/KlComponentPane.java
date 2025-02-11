package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservableEntity;
import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.Pane;


/**
 * Defines a sealed interface representing component panes that handle observable entities.
 * This interface extends {@code KlPane}, with its generic type bound to {@code Pane},
 * integrating observable components into JavaFX pane structures.
 *
 * Subinterfaces of {@code KlComponentPane} include specialized implementations
 * for handling specific types of observable entities such as patterns, semantics, stamps,
 * concepts, and generic components. These subinterfaces add context-specific
 * functionalities while leveraging the base features provided by {@code KlComponentPane}.
 *
 * The interface permits concrete, non-sealed subinterfaces: {@code KlConceptPane},
 * {@code KlGenericComponentPane}, {@code KlPatternPane}, {@code KlSemanticPane},
 * and {@code KlStampPane}.
 */
public sealed interface KlComponentPane<OE extends ObservableEntity> extends KlPane<Pane>
        permits KlConceptPane, KlGenericComponentPane, KlPatternPane, KlSemanticPane, KlStampPane {
    /**
     * Retrieves the observable component associated with this pane.
     *
     * @return the observable component of type {@code OE}.
     */
    default OE observableComponent() {
        return componentProperty().get();
    }

    /**
     * Provides access to the JavaFX {@code ObjectProperty} holding the observable entity
     * associated with this component pane. This property allows for retrieving and observing
     * changes to the underlying component.
     *
     * @return the {@code ObjectProperty} encapsulating the observable component of type {@code OE}
     */
    ObjectProperty<OE> componentProperty();
}
