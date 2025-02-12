package dev.ikm.komet.layout.component.version;

import dev.ikm.komet.framework.observable.ObservableStampVersion;
import javafx.beans.property.ObjectProperty;


/**
 * The {@code KlStampVersionPane} interface represents a pane that displays a single version
 * of a stamp entity.
 *
 * This interface is a specialization of {@link KlVersionPane} for handling
 * {@link ObservableStampVersion} types.
 *
 * @see KlVersionPane
 * @see ObservableStampVersion
 */
public non-sealed interface KlStampVersionPane extends KlVersionPane<ObservableStampVersion> {

     default ObservableStampVersion stampVersion() {
        return KlVersionPane.super.version();
    }

    default ObjectProperty<ObservableStampVersion> stampVersionProperty() {
         return versionProperty();
    }
}
