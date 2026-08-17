/*
 * Copyright © 2026 Knowledge Graphlet / IKE Network
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.framework.controls;

import network.ike.docs.konceptcore.KonceptKind;

import dev.ikm.komet.framework.testing.JavaFXThreadExtension;
import dev.ikm.komet.framework.testing.JavaFXThreadExtension.RunOnJavaFXThread;
import dev.ikm.tinkar.common.id.PublicIds;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Renders the multi-line badge judgment sheet to {@code target/badge-multiline-samples/}
 * ({@code IKE-Network/ike-issues#1036}): the ellipsis form a table cell shows, the expanded
 * multi-line rendering the tooltip and click-to-expand popover show for it, and directly
 * multi-line badges (narrow, retired, kind-sigiled) — so the wrap treatment can be assessed
 * from files without launching the app. Not a lock — the assertion only proves the sheet was
 * written. Like every {@code *UTestFX} it is excluded from the normal build; run it with
 * {@code -DrunUTestFX} or {@code -Dtest=KonceptBadgeMultiLineSampleUTestFX}.
 */
@ExtendWith(JavaFXThreadExtension.class)
@RunOnJavaFXThread
class KonceptBadgeMultiLineSampleUTestFX {

    private static final String LONG_NAME =
            "Severe chronic obstructive pulmonary disease with acute lower respiratory infection";

    private static final int MARGIN = 14;

    @Test
    void writeMultiLineSampleSheet() throws Exception {
        List<Image> rows = new ArrayList<>();

        // The table-cell form: the same badge a cell clamps, ellipsised at 180px.
        KonceptBadge cellBadge = badge(LONG_NAME, "aaaaaaaa-1111-4000-8000-00000000000a");
        rows.add(snapshotAt(cellBadge, 180));

        // What its tooltip / click-to-expand shows: the expanded multi-line rendering.
        rows.add(snapshotNatural(cellBadge.expandedRendering(340)));

        // A directly multi-line badge squeezed hard — the name folds further, never truncates.
        KonceptBadge narrow = badge(LONG_NAME, "aaaaaaaa-2222-4000-8000-00000000000b");
        narrow.setMultiLineLabel(true);
        rows.add(snapshotAt(narrow, 180));

        // Retired: the wrapped name carries the strikethrough and retired colour on every line.
        KonceptBadge retired = badge(LONG_NAME, "aaaaaaaa-3333-4000-8000-00000000000c");
        retired.setInactive(true);
        rows.add(snapshotNatural(retired.expandedRendering(340)));

        // A non-concept kind keeps its sigil beside the wrapped name.
        KonceptBadge pattern = badge("Inclusivity study result observation pattern with provenance anchors",
                "aaaaaaaa-4444-4000-8000-00000000000d");
        pattern.setKind(KonceptKind.PATTERN);
        rows.add(snapshotNatural(pattern.expandedRendering(340)));

        Path dir = Path.of("target", "badge-multiline-samples");
        Files.createDirectories(dir);
        Path sheet = sheet(rows, 0xFFFFFFFF, dir.resolve("badge-multiline-white.png"));
        assertTrue(Files.size(sheet) > 0, "the sample sheet was written");
    }

    /** A presentation badge with standalone styling and a status cluster, like an assistant chip. */
    private static KonceptBadge badge(String name, String uuid) {
        KonceptBadge badge = new KonceptBadge(PublicIds.of(uuid), name);
        badge.setStatus(KonceptStatus.PRIMITIVE);
        badge.setStandaloneStyling(true);
        badge.setAmbientFontSize(13);
        return badge;
    }

    /**
     * Snapshots the badge laid out at the given width (its preferred height for it). The width is
     * pinned on the preference: {@code snapshot} runs the scene's own layout pass, and a
     * {@code Group} auto-sizes managed children back to their preferred sizes — a plain
     * {@code resize} would be undone before the pixels are read.
     */
    private static Image snapshotAt(KonceptBadge badge, double width) {
        badge.setPrefWidth(width);
        badge.setMaxWidth(width);
        new Scene(new Group(badge));
        badge.applyCss();
        badge.resize(width, badge.prefHeight(width));
        badge.layout();
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.WHITE);
        return badge.snapshot(params, null);
    }

    /** Snapshots the badge at its own preference (the expanded rendering carries its width cap). */
    private static Image snapshotNatural(KonceptBadge badge) {
        double width = badge.getPrefWidth() > 0 ? badge.getPrefWidth() : badge.prefWidth(-1);
        return snapshotAt(badge, width);
    }

    /** Stacks the rows with margins on a solid backdrop and writes the PNG. */
    private static Path sheet(List<Image> rows, int backdropArgb, Path file) throws Exception {
        int width = MARGIN * 2 + rows.stream()
                .mapToInt(image -> (int) image.getWidth()).max().orElse(1);
        int height = MARGIN + rows.stream()
                .mapToInt(image -> (int) image.getHeight() + MARGIN).sum();

        BufferedImage sheet = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                sheet.setRGB(x, y, backdropArgb);
            }
        }
        int y = MARGIN;
        for (Image image : rows) {
            PixelReader reader = image.getPixelReader();
            for (int dy = 0; dy < (int) image.getHeight(); dy++) {
                for (int dx = 0; dx < (int) image.getWidth(); dx++) {
                    sheet.setRGB(MARGIN + dx, y + dy, reader.getArgb(dx, dy));
                }
            }
            y += (int) image.getHeight() + MARGIN;
        }
        ImageIO.write(sheet, "png", file.toFile());
        return file;
    }
}
