package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservableEntity;
import javafx.beans.property.ObjectProperty;


/**
 * Represents a single component presented in a Pane.
 */
public sealed interface KlComponentPane<OE extends ObservableEntity> extends KlPane
        permits KlConceptPane, KlPatternPane, KlSemanticPane, KlStampPane {
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
