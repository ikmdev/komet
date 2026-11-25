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
package dev.ikm.komet.kview.controls.test;

import dev.ikm.komet.kview.controls.ShortcutKeys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A shortcut's display text becomes one keycap per key, whichever way the platform spells it.
 */
class ShortcutKeysTest {

    @Test
    @DisplayName("Windows spelling: the keys are what the plus signs separate")
    void windowsSpelling() {
        assertEquals(List.of("Ctrl", "Shift", "I"), ShortcutKeys.keysOf("Ctrl+Shift+I"));
        assertEquals(List.of("Ctrl", "Page Up"), ShortcutKeys.keysOf("Ctrl+Page Up"));
        assertEquals(List.of("F5"), ShortcutKeys.keysOf("F5"));
    }

    @Test
    @DisplayName("macOS spelling: each modifier symbol is a key, and what follows is the last one")
    void macSpelling() {
        assertEquals(List.of("⇧", "⌘", "I"), ShortcutKeys.keysOf("⇧⌘I"));
        assertEquals(List.of("⌃", "⌥", "⌘", "Page Up"),
                ShortcutKeys.keysOf("⌃⌥⌘Page Up"));
        assertEquals(List.of("⌘", "↵"), ShortcutKeys.keysOf("⌘↵"));
    }

    @Test
    @DisplayName("The plus key is a key, not a separator")
    void plusKey() {
        assertEquals(List.of("Ctrl", "+"), ShortcutKeys.keysOf("Ctrl++"));
        assertEquals(List.of("⌘", "+"), ShortcutKeys.keysOf("⌘+"));
    }
}
