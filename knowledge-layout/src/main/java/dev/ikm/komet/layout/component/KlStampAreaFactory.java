package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservableStamp;
import javafx.scene.layout.Pane;

/**
 * A factory interface for creating instances of {@code KlStampPane} that associate
 * with {@code ObservableStamp}. This interface extends the {@code KlComponentPaneFactory}
 * to specialize its functionality for stamp-related panes. The {@code KlStampPaneFactory}
 * is responsible for dynamically constructing JavaFX panes tied to observable stamp entities.
 *
 * This factory provides the mechanisms essential for managing and initializing
 * stamp-specific UI components, enabling dynamic binding, user preferences integration,
 * and other features that support the display and interaction with {@code ObservableStamp} instances.
 *
 * @param <FX> the type of JavaFX {@code Pane} used in the {@code KlStampPane} implementation
 * @see KlComponentAreaFactory
 * @see KlStampArea
 * @see ObservableStamp
 */
public non-sealed interface KlStampAreaFactory<FX extends Pane>
        extends KlComponentAreaFactory<FX, KlStampArea<FX>, ObservableStamp> {
}
