/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.framework.settings;

import javafx.collections.ListChangeListener;
import javafx.scene.Scene;
import javafx.stage.PopupWindow;
import javafx.stage.Window;
import javafx.util.Subscription;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Puts {@link KometSettings#textSizeProperty()} into effect. Komet's stylesheets give every text
 * size in {@code em}, so all text follows the font size of the scene root; this class sets that
 * root size — a one-rule stylesheet on the scene of every window — and swaps the stylesheet as
 * the setting changes, so the whole application re-flows at once.
 *
 * <p>Popup windows (context menus, tooltips, combo-box lists) are left alone: JavaFX mirrors the
 * owner scene's stylesheets into a popup and resolves its fonts against the node that owns it,
 * which already sits under a sized root.
 */
public final class TextSizeStylesheet {

    private TextSizeStylesheet() {
    }

    /**
     * Builds the one-rule stylesheet that sets a scene root's font size for {@code size} and
     * returns it as a {@code data:} URL, ready for {@code scene.getStylesheets()}. Nothing is
     * written to disk, and each step always yields the same string, which is what identifies the
     * stylesheet on a scene when it is swapped for another step's.
     */
    public static String stylesheetUrlFor(TextSize size) {
        String css = ".root { -fx-font-size: " + size.fontSize() + "px; }";
        return "data:text/css;base64," + Base64.getEncoder().encodeToString(css.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Starts sizing the windows already showing and every window shown from now on after
     * {@code settings}. Call once, on the FX thread, before the first window is shown.
     *
     * @return a subscription that stops the tracking and takes the stylesheet off every window
     */
    public static Subscription install(KometSettings settings) {
        Installer installer = new Installer(settings);
        return installer::uninstall;
    }

    private static final class Installer {

        private final Map<Window, Subscription> windows = new HashMap<>();
        private final ListChangeListener<Window> onWindows = change -> {
            while (change.next()) {
                change.getRemoved().forEach(this::forget);
                change.getAddedSubList().forEach(this::track);
            }
        };
        private final Subscription onTextSize;
        /** The stylesheet currently on every tracked scene. */
        private String url;

        Installer(KometSettings settings) {
            url = stylesheetUrlFor(settings.getTextSize());
            onTextSize = settings.textSizeProperty().subscribe(() -> swap(stylesheetUrlFor(settings.getTextSize())));
            Window.getWindows().forEach(this::track);
            Window.getWindows().addListener(onWindows);
        }

        private void track(Window window) {
            if (window instanceof PopupWindow) {
                return;
            }
            windows.put(window, window.sceneProperty().subscribe(scene -> {
                if (scene != null && !scene.getStylesheets().contains(url)) {
                    scene.getStylesheets().add(url);
                }
            }));
        }

        private void forget(Window window) {
            Subscription subscription = windows.remove(window);
            if (subscription != null) {
                subscription.unsubscribe();
            }
        }

        private void swap(String newUrl) {
            for (Window window : windows.keySet()) {
                Scene scene = window.getScene();
                scene.getStylesheets().remove(url);
                scene.getStylesheets().add(newUrl);
            }
            url = newUrl;
        }

        private void uninstall() {
            Window.getWindows().removeListener(onWindows);
            onTextSize.unsubscribe();
            for (Map.Entry<Window, Subscription> entry : windows.entrySet()) {
                entry.getValue().unsubscribe();
                entry.getKey().getScene().getStylesheets().remove(url);
            }
            windows.clear();
        }
    }
}
