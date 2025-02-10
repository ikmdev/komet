package dev.ikm.komet.kview.klwindows;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.scene.Node;

import java.util.Optional;

/**
 * Base class for "chapter windows" in Komet that require {@link ViewProperties}
 * and {@link KometPreferences} for configuration. Subclasses define how the root
 * {@link Node} is created and managed within the UI.
 *
 * @param <T> A JavaFX {@link Node} subclass serving as the root pane for this window.
 */
public abstract class AbstractChapterKlWindow<T extends Node> implements ChapterKlWindow<T> {

    private final ViewProperties viewProperties;
    private KometPreferences preferences;
    private Optional<Runnable> optionalOnCloseRunnable = Optional.empty();
    private T paneWindow;

    /**
     * Constructs a base "chapter window" with the specified view properties and preferences.
     *
     * @param viewProperties The {@link ViewProperties} providing contextual info for rendering.
     * @param preferences    The {@link KometPreferences} for user or application-level settings.
     */
    public AbstractChapterKlWindow(ViewProperties viewProperties, KometPreferences preferences) {
        this.viewProperties = viewProperties;
        this.preferences = preferences;
    }

    /**
     * @return The {@link ViewProperties} for this window.
     */
    public ViewProperties getViewProperties() {
        return viewProperties;
    }

    /**
     * @return The current {@link KometPreferences} for this window.
     */
    public KometPreferences getPreferences() {
        return preferences;
    }

    /**
     * Updates the preferences used by this window.
     *
     * @param preferences New {@link KometPreferences} to be used.
     */
    protected void setPreferences(KometPreferences preferences) {
        this.preferences = preferences;
    }

    /**
     * Sets a callback that is invoked when this window is closed.
     *
     * @param runnable A {@link Runnable} to be executed on close, or null if no action is required.
     */
    public void setOnClose(Runnable runnable) {
        this.optionalOnCloseRunnable = Optional.ofNullable(runnable);
    }

    /**
     * @return An {@link Optional} containing the close callback, if any.
     */
    protected Optional<Runnable> getOnClose() {
        return optionalOnCloseRunnable;
    }

    @Override
    public T getRootPane() {
        return paneWindow;
    }

    /**
     * Sets the root JavaFX {@link Node} for this window.
     *
     * @param paneWindow The node serving as the root pane.
     */
    protected void setRootPane(T paneWindow) {
        this.paneWindow = paneWindow;
    }
}
