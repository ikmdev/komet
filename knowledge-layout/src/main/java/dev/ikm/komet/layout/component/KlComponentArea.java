package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservableEntity;
import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.Pane;


/**
 * Represents a component pane that integrates an observable entity and a JavaFX {@code Pane}.
 *
 * The {@code KlComponentPane} interface defines a contract for panes that manage an
 * observable entity alongside a JavaFX {@code Pane}. This interface enables managing
 * specific types of observable entities by extending it for various implementations
 * such as {@code KlConceptPane}, {@code KlPatternPane}, {@code KlSemanticPane},
 * {@code KlStampPane}, or {@code KlGenericComponentPane}.
 *
 * The {@code OE} generic parameter defines the type of observable entity, while the
 * {@code P} parameter specifies the type of JavaFX {@code Pane}.
 *
 * @param <OE> the type of observable entity associated with this component pane
 * @param <FX> the type of JavaFX {@code Pane} for this component pane
 */
public sealed interface KlComponentArea<OE extends ObservableEntity, FX extends Pane> extends KlArea<FX>
        permits KlConceptArea, KlGenericComponentArea, KlPatternArea, KlSemanticArea, KlStampArea {
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
