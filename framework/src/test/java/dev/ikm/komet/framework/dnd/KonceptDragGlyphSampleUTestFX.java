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
package dev.ikm.komet.framework.dnd;

import dev.ikm.komet.framework.controls.KonceptBadge;
import dev.ikm.komet.framework.controls.KonceptStatus;
import dev.ikm.komet.framework.testing.JavaFXThreadExtension;
import dev.ikm.komet.framework.testing.JavaFXThreadExtension.RunOnJavaFXThread;
import dev.ikm.tinkar.common.id.PublicIds;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import network.ike.docs.konceptcore.KonceptKind;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Renders the drag-glyph variant sheet to {@code target/drag-samples/} — the frame-refinement
 * judgment aid from the 2026-07-24 drag-rendering handoff: the same glyphs composited over the
 * two real backdrops (white compose area, grey navigator canvas), so a frame change can be
 * assessed from files without launching the app. Not a lock — the assertions only prove the
 * sheet was written. Like every {@code *UTestFX} it is excluded from the normal build; run it
 * with {@code -Dtest=KonceptDragGlyphSampleUTestFX}.
 */
@ExtendWith(JavaFXThreadExtension.class)
@RunOnJavaFXThread
class KonceptDragGlyphSampleUTestFX {

    /** The grey dotted navigator canvas tone the handoff names as the second real backdrop. */
    private static final int NAVIGATOR_GREY = 0xFFE9E9E9;

    private static final int MARGIN = 14;

    @Test
    void writeVariantSheetOverBothBackdrops() throws Exception {
        List<Image> variants = List.of(
                KonceptDragGlyph.image(KonceptKind.CONCEPT, KonceptStatus.NONE,
                        PublicIds.of("aaaaaaaa-1111-4000-8000-000000000001"),
                        "Bare concept (no stated definition)", false),
                KonceptDragGlyph.image(KonceptKind.CONCEPT, KonceptStatus.PRIMITIVE,
                        PublicIds.of("aaaaaaaa-2222-4000-8000-000000000002"),
                        "Primitive concept", false),
                KonceptDragGlyph.image(KonceptKind.CONCEPT, KonceptStatus.PRIMITIVE_MULTIPARENT,
                        PublicIds.of("aaaaaaaa-3333-4000-8000-000000000003"),
                        "Primitive multi-parent concept", false),
                KonceptDragGlyph.image(KonceptKind.CONCEPT, KonceptStatus.DEFINED,
                        PublicIds.of("aaaaaaaa-4444-4000-8000-000000000004"),
                        "Sufficiently defined concept", false),
                KonceptDragGlyph.image(KonceptKind.CONCEPT, KonceptStatus.ROOT,
                        PublicIds.of("aaaaaaaa-5555-4000-8000-000000000005"),
                        "Taxonomy root", false),
                KonceptDragGlyph.image(KonceptKind.PATTERN, KonceptStatus.NONE,
                        PublicIds.of("aaaaaaaa-6666-4000-8000-000000000006"),
                        "Description pattern", false),
                KonceptDragGlyph.multiImage(
                        PublicIds.of("aaaaaaaa-7777-4000-8000-000000000007"),
                        "Diabetes mellitus", false, 3),
                KonceptDragGlyph.image(KonceptKind.CONCEPT, KonceptStatus.PRIMITIVE,
                        PublicIds.of("aaaaaaaa-8888-4000-8000-000000000008"),
                        "Retired concept (drag)", true),
                chipSnapshot("On-screen chip (standalone)", false),
                chipSnapshot("Retired on-screen chip", true));

        Path dir = Path.of("target", "drag-samples");
        Files.createDirectories(dir);
        Path white = sheet(variants, 0xFFFFFFFF, dir.resolve("drag-glyphs-white.png"));
        Path grey = sheet(variants, NAVIGATOR_GREY, dir.resolve("drag-glyphs-grey.png"));

        assertTrue(Files.size(white) > 0 && Files.size(grey) > 0,
                "both backdrop sheets were written");
    }

    /**
     * A standalone-styled on-screen {@code KonceptBadge} snapshot — the JavaFX chip row of the
     * #865 side-by-side sheet. komet.css is attached via a temp-file URI (classpath-URL string
     * forms break under the ꞉ sibling paths) so the status cluster gets its stylesheet colours.
     */
    private static Image chipSnapshot(String name, boolean retired) throws Exception {
        KonceptBadge badge = new KonceptBadge(
                PublicIds.of("aaaaaaaa-9999-4000-8000-000000000009"), name);
        badge.setStatus(KonceptStatus.PRIMITIVE);
        badge.setStandaloneStyling(true);
        badge.setInactive(retired);

        Path css = Files.createTempFile("komet", ".css");
        try (InputStream in = KonceptDragGlyphSampleUTestFX.class.getResourceAsStream(
                "/dev/ikm/komet/framework/graphics/komet.css")) {
            Files.copy(in, css, StandardCopyOption.REPLACE_EXISTING);
        }
        css.toFile().deleteOnExit();
        Scene scene = new Scene(new Group(badge));
        scene.getStylesheets().add(css.toUri().toString());
        badge.applyCss();
        badge.resize(badge.prefWidth(-1), badge.prefHeight(-1));
        badge.layout();
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.WHITE);
        return badge.snapshot(params, null);
    }

    /** Stacks the variants with margins on a solid backdrop and writes the PNG. */
    private static Path sheet(List<Image> variants, int backdropArgb, Path file) throws Exception {
        int width = MARGIN * 2 + variants.stream()
                .mapToInt(image -> (int) image.getWidth()).max().orElse(1);
        int height = MARGIN + variants.stream()
                .mapToInt(image -> (int) image.getHeight() + MARGIN).sum();

        BufferedImage sheet = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                sheet.setRGB(x, y, backdropArgb);
            }
        }
        int y = MARGIN;
        for (Image image : variants) {
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
