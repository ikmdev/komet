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
package dev.ikm.komet.framework.dnd;

import dev.ikm.komet.framework.controls.KonceptBadge;
import dev.ikm.komet.framework.testing.JavaFXThreadExtension;
import dev.ikm.komet.framework.testing.JavaFXThreadExtension.RunOnJavaFXThread;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks the three invariants that make every identicon drag image look the same regardless of where
 * it is dragged from (ike-issues#741):
 *
 * <ol>
 *   <li><b>Standard size</b> — every drag image is exactly
 *       {@link DragImageMaker#STANDARD_DRAG_IMAGE_HEIGHT} tall.</li>
 *   <li><b>No truncation</b> — a badge that is width-constrained (ellipsized) on screen still drags
 *       at its full, unconstrained width.</li>
 *   <li><b>Cursor placement</b> — the cursor sits just to the right of the identicon, on the image's
 *       bottom border (and a node with no identicon gets a uniform small left gap, never the
 *       cursor-on-the-identicon legacy behaviour).</li>
 * </ol>
 *
 * <p>Uses presentation-only {@link KonceptBadge}s (a {@link PublicId} drives the identicon and label
 * with no store), so these are store-independent unit tests.
 */
@ExtendWith(JavaFXThreadExtension.class)
@RunOnJavaFXThread
class IdenticonDragUTestFX {

    private static final double STANDARD = DragImageMaker.STANDARD_DRAG_IMAGE_HEIGHT;
    private static final double CURSOR_GAP = 4.0;
    private static final String LONG_LABEL = "A REALLY QUITE LONG CONCEPT NAME FOR TRUNCATION (FINDING)";

    /**
     * Attaches {@code badge} to a wide scene and lays it out so the badge reaches its full preferred
     * (un-ellipsized) width — its width is not constrained by the parent.
     */
    private static void layoutUnconstrained(KonceptBadge badge) {
        HBox root = new HBox(badge);
        Scene scene = new Scene(root, 1000, 50);
        root.applyCss();
        root.resize(1000, 50);
        root.layout();
    }

    @Test
    void everyDragImageIsTheStandardHeight() {
        KonceptBadge badge = new KonceptBadge(PublicIds.newRandom(), "SOME CONCEPT (FINDING)");
        layoutUnconstrained(badge);

        Image image = new DragImageMaker(badge).getDragImage();

        assertEquals(STANDARD, image.getHeight(), 0.5,
                "drag image must be the single standard height regardless of source size");
    }

    @Test
    void constrainedBadgeDragsAtFullWidthWithoutTruncation() {
        PublicId id = PublicIds.newRandom();

        // Same concept rendered twice: once unconstrained (full label), once width-constrained by a
        // narrow parent (ellipsized on screen, exactly the performance-statement / card-title case).
        KonceptBadge unconstrained = new KonceptBadge(id, LONG_LABEL);
        layoutUnconstrained(unconstrained);
        double fullWidth = unconstrained.getLayoutBounds().getWidth();

        KonceptBadge constrained = new KonceptBadge(id, LONG_LABEL);
        HBox.setHgrow(constrained, Priority.ALWAYS);
        HBox narrowRoot = new HBox(constrained);
        narrowRoot.setMinWidth(40);
        narrowRoot.setPrefWidth(40);
        narrowRoot.setMaxWidth(40);
        Scene scene = new Scene(narrowRoot);
        narrowRoot.applyCss();
        narrowRoot.resize(40, 24);
        narrowRoot.layout();

        // Precondition: the badge really is constrained (ellipsized) on screen.
        double onScreenWidth = constrained.getLayoutBounds().getWidth();
        assertTrue(onScreenWidth < fullWidth - 20,
                "test setup: constrained badge must be much narrower on screen (" + onScreenWidth
                        + ") than unconstrained (" + fullWidth + ")");

        Image unconstrainedImage = new DragImageMaker(unconstrained).getDragImage();
        Image constrainedImage = new DragImageMaker(constrained).getDragImage();

        assertEquals(STANDARD, constrainedImage.getHeight(), 0.5);
        assertEquals(unconstrainedImage.getWidth(), constrainedImage.getWidth(), 2.0,
                "a width-constrained badge must drag at its full unconstrained width — no right-edge "
                        + "truncation of the label or the snapshot border");
    }

    @Test
    void cursorSitsJustRightOfTheIdenticonOnTheBottomBorder() {
        KonceptBadge badge = new KonceptBadge(PublicIds.newRandom(), "SOME CONCEPT (FINDING)");
        layoutUnconstrained(badge);

        Image image = new DragImageMaker(badge).getDragImage();
        double scale = STANDARD / badge.getLayoutBounds().getHeight();
        double identiconRightInImage = badge.identiconRightEdge() * scale;

        double cursorX = KonceptDragSource.cursorX(badge);

        assertTrue(identiconRightInImage > 0, "test setup: the badge must have a laid-out identicon");
        assertTrue(cursorX > identiconRightInImage,
                "cursor must sit to the right of the identicon, not on top of it");
        assertTrue(cursorX <= image.getWidth(), "cursor must fall within the drag image");
        assertEquals(identiconRightInImage + CURSOR_GAP, cursorX, 0.5,
                "cursor x is the identicon's right edge (scaled) plus the standard gap");
    }

    @Test
    void nodeWithoutIdenticonGetsAUniformLeftGapCursor() {
        Label plain = new Label("no identicon here");
        HBox root = new HBox(plain);
        Scene scene = new Scene(root);
        root.applyCss();
        root.layout();

        assertEquals(CURSOR_GAP, KonceptDragSource.cursorX(plain), 0.001,
                "a node with no identicon places the cursor a small uniform gap in from the left");
    }
}
