package dev.ikm.komet.layout.component.version;

import dev.ikm.komet.framework.observable.ObservableStampVersion;


/**
 * The {@code KlStampVersionPane} interface represents a pane that displays a single version
 * of a stamp entity.
 *
 * This interface is a specialization of {@link KlVersionPane} for handling
 * {@link ObservableStampVersion} types.
 *
 * @see KlVersionPane
 * @see ObservableStampVersion
 */public non-sealed interface KlStampVersionPane extends KlVersionPane<ObservableStampVersion> {
}
