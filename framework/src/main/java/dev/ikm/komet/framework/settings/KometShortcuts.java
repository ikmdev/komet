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

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 * Komet's keyboard shortcuts, one constant per action. The window that installs a shortcut and
 * the places that show it to the user (the Settings dialog, a tooltip) read the same constant, so
 * the keys shown are always the keys that work.
 *
 * <p>{@link KeyCombination#SHORTCUT_DOWN} is Command on macOS and Ctrl elsewhere, and
 * {@link KeyCombination#getDisplayText()} spells a combination the way its platform does
 * ({@code ⇧⌘I} on macOS, {@code Ctrl+Shift+I} on Windows).
 */
public final class KometShortcuts {

    /**
     * Shows or hides the identicons in a journal's left side panel: the
     * {@link KometSettings#showSidePanelIdenticonsProperty() side panel identicons} setting.
     */
    public static final KeyCombination TOGGLE_SIDE_PANEL_IDENTICONS =
            new KeyCodeCombination(KeyCode.I, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);

    private KometShortcuts() {
    }
}
