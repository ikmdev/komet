package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservableEntity;


/**
 * Represents a single component presented in a Pane.
 */
public interface KlComponentPane<OE extends ObservableEntity> extends KlPane {
    /**
     * Retrieves the observable entity associated with this pane.
     *
     * @return the observable entity of type ObservableEntity.
     */
    OE observableEntity();
}
