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
package dev.ikm.komet.framework.controls;

import dev.ikm.komet.framework.settings.KometSettings;
import dev.ikm.komet.framework.settings.KonceptGlyphSettings;
import dev.ikm.komet.framework.testing.JavaFXThreadExtension;
import dev.ikm.komet.framework.testing.JavaFXThreadExtension.RunOnJavaFXThread;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.PreferencesServiceFactory;
import dev.ikm.tinkar.common.id.PublicIds;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Subscription;
import network.ike.docs.konceptcore.KonceptKind;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.prefs.BackingStoreException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks how a {@link KonceptBadge} follows the glyph settings (ikmdev/komet-desktop#153): on a
 * scene it draws the marks the settings show and re-draws as they change, keeping the resolved
 * status and kind underneath; off a scene it stops listening (so discarded badges are not kept
 * alive by the application-wide settings) and catches up when shown again.
 *
 * <p>The settings under test are backed by a throw-away preferences node, removed afterwards, so
 * the real per-user settings of whoever runs the build are never touched.
 */
@ExtendWith(JavaFXThreadExtension.class)
@RunOnJavaFXThread
class KonceptBadgeGlyphSettingsUTestFX {

    private static final String TEST_NODE = "dev/ikm/komet/framework/test/koncept-badge-glyph-settings";
    private static final String NAME = "Chronic sinusitis";

    private KometPreferences preferences;
    private KometSettings settings;
    private Subscription installed;

    @BeforeEach
    void installSettingsOnThrowAwayNode() throws BackingStoreException {
        preferences = PreferencesServiceFactory.provider().getUserPreferences().node(TEST_NODE);
        preferences.clear();
        settings = KometSettings.backedBy(preferences);
        installed = KonceptGlyphSettings.install(settings);
    }

    @AfterEach
    void uninstallAndRemoveThrowAwayNode() throws BackingStoreException {
        installed.unsubscribe();
        preferences.removeNode();
    }

    /** A presentation-only badge (no store) with an explicit status, the way the slot tests build one. */
    private static KonceptBadge badge(KonceptStatus status) {
        KonceptBadge badge = new KonceptBadge(PublicIds.newRandom(), NAME);
        badge.setStatus(status);
        return badge;
    }

    /** The badge's sigil slot: leading child of the published anatomy. */
    private static HBox sigilBox(KonceptBadge badge) {
        return (HBox) badge.getChildrenUnmodifiable().get(0);
    }

    /** The badge's status-cluster box: second child of the published anatomy. */
    private static HBox statusBox(KonceptBadge badge) {
        return (HBox) badge.getChildrenUnmodifiable().get(1);
    }

    @Test
    void onASceneTheStatusClusterFollowsTheSettings() {
        KonceptBadge badge = badge(KonceptStatus.DEFINED_MULTIPARENT);
        new Scene(new VBox(badge));
        assertEquals(2, statusBox(badge).getChildren().size(), "every mark on: the glyph and the fork");

        settings.setShowMultipleParents(false);
        assertEquals(1, statusBox(badge).getChildren().size(), "the fork is dropped, the glyph stays");
        assertEquals(KonceptStatus.DEFINED_MULTIPARENT, badge.getStatus(), "the resolved status is kept");

        settings.setShowDefinitionStatus(false);
        assertTrue(statusBox(badge).getChildren().isEmpty(), "no cluster at all");
        assertFalse(statusBox(badge).isManaged(), "the empty slot collapses");

        settings.setShowDefinitionStatus(true);
        settings.setShowMultipleParents(true);
        assertEquals(2, statusBox(badge).getChildren().size(), "both marks return");
    }

    @Test
    void aHiddenSigilCollapsesItsSlotAndKeepsTheKind() {
        KonceptBadge badge = badge(KonceptStatus.NONE);
        badge.setKind(KonceptKind.PATTERN);
        new Scene(new VBox(badge));
        assertTrue(sigilBox(badge).isManaged(), "a pattern leads with its sigil");

        settings.setShowKindSigil(false);
        assertFalse(sigilBox(badge).isManaged(), "the sigil slot collapses");
        assertFalse(sigilBox(badge).isVisible());
        assertEquals(KonceptKind.PATTERN, badge.getKind(), "the kind is kept: only the mark is hidden");

        settings.setShowKindSigil(true);
        assertTrue(sigilBox(badge).isManaged(), "the sigil returns");
    }

    @Test
    void aReservedSlotCollapsesWithTheDefinitionStatus() {
        KonceptBadge badge = badge(KonceptStatus.PRIMITIVE);
        badge.setStatusSlotReserved(true);
        new Scene(new VBox(badge));
        assertTrue(statusBox(badge).isManaged(), "reserved: the slot is held");

        settings.setShowDefinitionStatus(false);
        assertFalse(statusBox(badge).isManaged(),
                "no row has a cluster, so the column is aligned without the slot");

        settings.setShowDefinitionStatus(true);
        assertTrue(statusBox(badge).isManaged(), "the reservation returns with the status");
        assertTrue(statusBox(badge).getMinWidth() > 0, "at its constant width");
    }

    @Test
    void offASceneTheBadgeCatchesUpWhenShownAgain() {
        KonceptBadge badge = badge(KonceptStatus.DEFINED);
        VBox host = new VBox(badge);
        new Scene(host);
        assertEquals(1, statusBox(badge).getChildren().size());

        host.getChildren().clear();
        settings.setShowDefinitionStatus(false);
        host.getChildren().add(badge);
        assertTrue(statusBox(badge).getChildren().isEmpty(),
                "back on a scene, the badge shows the settings as they now are");

        settings.setShowDefinitionStatus(true);
        assertEquals(1, statusBox(badge).getChildren().size(), "and follows them again live");
    }
}
