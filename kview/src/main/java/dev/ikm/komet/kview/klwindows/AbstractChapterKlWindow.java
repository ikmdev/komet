package dev.ikm.komet.kview.klwindows;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.layout.window.KlWindow;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.scene.Node;

import java.util.Optional;

/**
 * All chapter window's will be supplied view properties and/or a komet preference.
 * @param <T> a derived JavaFX Node class.
 */
public abstract class AbstractChapterKlWindow<T extends Node>  implements KlWindow {
    private ViewProperties viewProperties;
    private KometPreferences preferences;
    private Optional<Runnable> optionalOnCloseRunnable = Optional.empty();
    private T paneWindow;


    public AbstractChapterKlWindow(ViewProperties viewProperties, KometPreferences preferences) {
        this.viewProperties = viewProperties;
        this.preferences = preferences;
    }

    public ViewProperties getViewProperties() {
        return viewProperties;
    }

    public KometPreferences getPreferences() {
        return preferences;
    }

    protected void setPreferences(KometPreferences preferences) {
        this.preferences = preferences;
    }

    public void setOnClose(Runnable runnable){
        this.optionalOnCloseRunnable = Optional.ofNullable(runnable);
    }

    protected Optional<Runnable> getOnClose() {
        return optionalOnCloseRunnable;
    }

    public T getPaneWindow() {
        return paneWindow;
    }
    protected void setPaneWindow(T paneWindow) {
        this.paneWindow = paneWindow;
    }
}
