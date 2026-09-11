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

import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.PreferencesServiceFactory;
import javafx.util.Subscription;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.prefs.BackingStoreException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks the seam every glyph renderer reads (ikmdev/komet-desktop#153): until settings are
 * installed every mark shows; installed, the seam follows the settings live and folds the
 * definition status into the multiple-parents value the way the Settings dialog gates that
 * option; uninstalled, every mark shows again.
 *
 * <p>The settings under test are backed by a throw-away preferences node, removed afterwards, so
 * the real per-user settings of whoever runs the build are never touched.
 */
class KonceptGlyphSettingsTest {

    private static final String TEST_NODE = "dev/ikm/komet/framework/test/koncept-glyph-settings";

    private static KometPreferences preferences;
    private static KometSettings settings;

    private Subscription installed;

    @BeforeAll
    static void settingsOnThrowAwayNode() throws BackingStoreException {
        preferences = PreferencesServiceFactory.provider().getUserPreferences().node(TEST_NODE);
        preferences.clear();
        settings = KometSettings.backedBy(preferences);
    }

    @AfterAll
    static void removeThrowAwayNode() throws BackingStoreException {
        preferences.removeNode();
    }

    @BeforeEach
    void everyMarkOn() {
        settings.setShowKindSigil(true);
        settings.setShowDefinitionStatus(true);
        settings.setShowMultipleParents(true);
    }

    @AfterEach
    void uninstall() {
        if (installed != null) {
            installed.unsubscribe();
            installed = null;
        }
    }

    private static void assertEveryMarkShown(String why) {
        assertTrue(KonceptGlyphSettings.isShowKindSigil(), why);
        assertTrue(KonceptGlyphSettings.isShowDefinitionStatus(), why);
        assertTrue(KonceptGlyphSettings.isShowMultipleParents(), why);
    }

    @Test
    void everyMarkShowsUntilSettingsAreInstalled() {
        settings.setShowKindSigil(false);
        settings.setShowDefinitionStatus(false);
        assertEveryMarkShown("settings not installed: a bare host renders every mark");
    }

    @Test
    void installedTheSeamFollowsEachSettingLive() {
        installed = KonceptGlyphSettings.install(settings);
        assertEveryMarkShown("installed with every setting on");

        settings.setShowKindSigil(false);
        assertFalse(KonceptGlyphSettings.isShowKindSigil());
        assertFalse(KonceptGlyphSettings.showKindSigil().get(), "the observable form follows too");
        settings.setShowKindSigil(true);
        assertTrue(KonceptGlyphSettings.isShowKindSigil());

        settings.setShowMultipleParents(false);
        assertFalse(KonceptGlyphSettings.isShowMultipleParents());
        assertTrue(KonceptGlyphSettings.isShowDefinitionStatus(), "the classification glyph stays");
        settings.setShowMultipleParents(true);
        assertTrue(KonceptGlyphSettings.isShowMultipleParents());
    }

    @Test
    void hidingTheDefinitionStatusHidesTheForkWithIt() {
        installed = KonceptGlyphSettings.install(settings);
        settings.setShowDefinitionStatus(false);
        assertFalse(KonceptGlyphSettings.isShowDefinitionStatus());
        assertFalse(KonceptGlyphSettings.isShowMultipleParents(),
                "the fork only exists on top of the status glyph, and the dialog disables the option");
        assertFalse(KonceptGlyphSettings.showMultipleParents().get());
        assertTrue(settings.isShowMultipleParents(), "the user's own choice is kept for when the status returns");

        settings.setShowDefinitionStatus(true);
        assertTrue(KonceptGlyphSettings.isShowMultipleParents(), "the fork returns with the status");
    }

    @Test
    void uninstallingShowsEveryMarkAgain() {
        installed = KonceptGlyphSettings.install(settings);
        settings.setShowKindSigil(false);
        settings.setShowDefinitionStatus(false);
        installed.unsubscribe();
        installed = null;
        assertEveryMarkShown("uninstalled: back to the bare default");
        settings.setShowKindSigil(true);
        settings.setShowKindSigil(false);
        assertTrue(KonceptGlyphSettings.isShowKindSigil(), "no longer following the settings");
    }
}
