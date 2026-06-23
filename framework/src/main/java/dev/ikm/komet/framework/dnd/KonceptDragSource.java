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
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

/**
 * The single concept drag-source convention, shared by the {@link KonceptBadge} atom and by
 * {@code ListView}/{@code TreeView} cells whose graphic is a badge. It centralises three things
 * that were previously re-implemented per site:
 *
 * <ol>
 *   <li><b>Scene guard</b> — a re-rendered or recycled node (a streaming list, a rebuilt card)
 *       can be detached between the press and the drag threshold; {@code startDragAndDrop} throws
 *       "not in scene" in that case, so the gesture is skipped rather than thrown.</li>
 *   <li><b>Unconditional payload</b> — the concept proxy is placed on the clipboard via
 *       {@link KometClipboard#forConcept(int)}, which does not gate on {@code EntityHandle}
 *       presence; the prior presence-gated path left an unloaded concept's drag with no content,
 *       which drop targets silently rejected.</li>
 *   <li><b>Drag-view placement</b> — the cursor sits just to the <em>right</em> of the identicon
 *       with its tip on the image's <em>bottom border</em>, so the identicon detail and the
 *       dragged label are both fully visible at full resolution.</li>
 * </ol>
 */
public final class KonceptDragSource {

    /** Scaled-pixel gap placed to the right of the identicon before the cursor. */
    private static final double CURSOR_GAP = 4.0;

    private KonceptDragSource() {
    }

    /**
     * Wires {@code badge} as a concept drag source. Safe to call from the badge constructor.
     *
     * @param badge the badge to drag
     * @param nid   the concept nid the drag carries
     */
    public static void install(KonceptBadge badge, int nid) {
        badge.setOnDragDetected(event -> start(badge, badge, nid, event));
    }

    /**
     * Starts a concept drag from {@code source}, using {@code badge} as the drag image. For the
     * cell case — where the press lands on the cell (which swallows the first click) rather than
     * the badge — pass the cell as {@code source} and the cell's badge graphic as {@code badge}.
     *
     * @param source the node the drag gesture starts on; must be attached to a scene
     * @param badge  the badge providing the drag image and identicon geometry
     * @param nid    the concept nid the drag carries
     * @param event  the {@code DRAG_DETECTED} event; consumed on success
     * @return {@code true} if the drag started, {@code false} if a node was detached
     */
    public static boolean start(Node source, KonceptBadge badge, int nid, MouseEvent event) {
        if (source.getScene() == null || badge.getScene() == null) {
            return false;
        }
        Dragboard dragboard = source.startDragAndDrop(TransferMode.COPY);
        setBadgeDragView(dragboard, badge);
        dragboard.setContent(KometClipboard.forConcept(nid));
        event.consume();
        return true;
    }

    /**
     * Places the drag view so the cursor sits just right of the identicon with its tip on the
     * image's bottom border. {@link DragImageMaker} rescales every drag image to
     * {@link DragImageMaker#STANDARD_DRAG_IMAGE_HEIGHT}, so the identicon's local right edge is
     * scaled by the same factor to find the cursor's x in image space.
     *
     * @param dragboard the active dragboard
     * @param badge     the badge being dragged
     */
    private static void setBadgeDragView(Dragboard dragboard, KonceptBadge badge) {
        Image image = new DragImageMaker(badge).getDragImage();
        if (image == null) {
            return;
        }
        double badgeHeight = badge.getLayoutBounds().getHeight();
        double scale = badgeHeight > 0 ? DragImageMaker.STANDARD_DRAG_IMAGE_HEIGHT / badgeHeight : 1.0;
        double cursorX = (badge.identiconRightEdge() * scale) + CURSOR_GAP;
        dragboard.setDragView(image, cursorX, DragImageMaker.STANDARD_DRAG_IMAGE_HEIGHT);
    }
}
