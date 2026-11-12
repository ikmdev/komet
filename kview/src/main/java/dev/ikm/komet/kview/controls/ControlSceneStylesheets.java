package dev.ikm.komet.kview.controls;

import javafx.scene.Scene;

import java.util.ArrayList;
import java.util.List;

/**
 * The stylesheets that kview controls keep on their scene, and the one safe way to put them there.
 *
 * <p>Some controls style themselves through their scene's stylesheets rather than their own: a
 * scene stylesheet reaches the popups a control opens, and a hosting application's stylesheet can
 * still restyle the control by selector specificity. The price is that every change to a scene's
 * stylesheets drops the styles JavaFX has cached for the whole scene and restyles every node in
 * it. A change made while the scene is being styled is worse: the root's cached styles are gone
 * for the rest of that pass, so every color the root defines ({@code -fx-text-base-color},
 * {@code -grey-8}, ...) fails to resolve, and JavaFX logs a warning for each property of each
 * node styled after the change. A control attached by a skin, as a titled pane's content is, is
 * attached during that pass.</p>
 *
 * <p>So a window that hosts these controls calls {@link #install(Scene)} once, when it sets up its
 * scene, and a control calls {@link #ensure(Scene, String)} when it joins a scene, which adds its
 * stylesheet only to a scene that was not set up that way, and only once.</p>
 */
public final class ControlSceneStylesheets {

    /** The control stylesheets, by file name beside this class, in the order they are installed. */
    private static final List<String> FILE_NAMES = List.of(
            "public-id-control.css",
            "public-id-list-control.css",
            "stamp-view-control.css",
            "read-only-data-control.css",
            "component-control.css",
            "component-combo-box-control.css",
            "component-list-control.css",
            "float-control.css",
            "ditree-control.css");

    private static final List<String> STYLESHEETS = FILE_NAMES.stream()
            .map(name -> ControlSceneStylesheets.class.getResource(name).toExternalForm())
            .toList();

    private ControlSceneStylesheets() {
    }

    /**
     * The stylesheets {@link #install(Scene)} adds.
     *
     * @return the stylesheet URLs, in installation order
     */
    public static List<String> stylesheets() {
        return STYLESHEETS;
    }

    /**
     * Adds every control stylesheet the scene lacks, in one change. Call it when the scene is set
     * up, after the application's own stylesheets and before the scene is styled.
     *
     * @param scene the scene to prepare
     */
    public static void install(Scene scene) {
        List<String> missing = new ArrayList<>(STYLESHEETS);
        missing.removeAll(scene.getStylesheets());
        if (!missing.isEmpty()) {
            scene.getStylesheets().addAll(missing);
        }
    }

    /**
     * Adds a control's stylesheet to the scene the control has joined, unless the scene already
     * has it. A scene prepared with {@link #install(Scene)} is left untouched.
     *
     * @param scene      the scene the control is in, or {@code null} when it has left its scene
     * @param stylesheet the control's stylesheet URL
     */
    public static void ensure(Scene scene, String stylesheet) {
        if (scene != null && !scene.getStylesheets().contains(stylesheet)) {
            scene.getStylesheets().add(stylesheet);
        }
    }
}
