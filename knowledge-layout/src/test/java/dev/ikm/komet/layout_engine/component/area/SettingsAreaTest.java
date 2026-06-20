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
package dev.ikm.komet.layout_engine.component.area;

import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Round-trip tests for {@link SettingsArea}: font family and size persist and restore through the area's
 * framework save/restore hooks, and the password is stored obfuscated (never in clear text) yet retrievable.
 * Runs on the JavaFX application thread; skips if the toolkit cannot start.
 */
class SettingsAreaTest {

    private static final String PASSWORD_PREF_KEY = "settings.password";

    private static boolean fxReady;

    @BeforeAll
    static void startFx() {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            fxReady = latch.await(10, TimeUnit.SECONDS);
        } catch (IllegalStateException alreadyStarted) {
            fxReady = true;
        } catch (Throwable cannotStart) {
            fxReady = false;
        }
    }

    private static void onFx(Runnable body) throws InterruptedException {
        assumeTrue(fxReady, "JavaFX toolkit unavailable");
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                body.run();
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(10, TimeUnit.SECONDS), "FX task did not complete");
        if (failure.get() != null) {
            throw new AssertionError(failure.get());
        }
    }

    @Test
    void fontAndSizePersistAndRestore() throws InterruptedException {
        onFx(() -> {
            KometPreferences root =
                    KometPreferencesImpl.getConfigurationRootPreferences().node("test-settings-area-714-font");
            try {
                KlPreferencesFactory factory = KlPreferencesFactory.create(root, SettingsArea.class);
                SettingsArea area = SettingsArea.create(factory);
                area.setSelectedFontFamily("Monospaced");
                area.setSelectedFontSize(22);
                area.subAreaSave();

                SettingsArea restored = SettingsArea.restore(area.preferences());
                restored.subAreaRestoreFromPreferencesOrDefault();

                assertEquals("Monospaced", restored.getSelectedFontFamily(), "font family restored");
                assertEquals(22, restored.getSelectedFontSize(), "font size restored");
            } finally {
                removeQuietly(root);
            }
        });
    }

    @Test
    void passwordIsStoredObfuscatedAndRetrievable() throws InterruptedException {
        onFx(() -> {
            KometPreferences root =
                    KometPreferencesImpl.getConfigurationRootPreferences().node("test-settings-area-714-pw");
            try {
                KlPreferencesFactory factory = KlPreferencesFactory.create(root, SettingsArea.class);
                SettingsArea area = SettingsArea.create(factory);
                area.setPassword("s3cret-pass".toCharArray());
                area.subAreaSave();

                KometPreferences node = area.preferences();
                assertEquals("s3cret-pass", new String(node.getPassword(PASSWORD_PREF_KEY).orElseThrow()),
                        "password round-trips via getPassword");
                assertTrue(node.get(PASSWORD_PREF_KEY).isPresent(), "an encrypted value is stored");
                assertNotEquals("s3cret-pass", node.get(PASSWORD_PREF_KEY).orElseThrow(),
                        "the stored value is not the clear-text password");
            } finally {
                removeQuietly(root);
            }
        });
    }

    private static void removeQuietly(KometPreferences node) {
        try {
            node.removeNode();
        } catch (Exception ignored) {
            // Best-effort cleanup of the test configuration node.
        }
    }
}
