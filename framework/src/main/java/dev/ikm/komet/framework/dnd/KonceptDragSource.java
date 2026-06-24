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

import dev.ikm.komet.framework.StyleClasses;
import dev.ikm.komet.framework.controls.KonceptBadge;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

import java.util.ArrayDeque;
import java.util.Deque;

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
     * @param nid   the koncept's nid; a description is resolved to the concept it describes
     */
    public static void install(KonceptBadge badge, int nid) {
        badge.setOnDragDetected(event -> start(badge, badge, nid, event));
    }

    /**
     * Wires an arbitrary identicon-bearing node (for example a transcript chip that is not a
     * {@link KonceptBadge}) as a concept drag source, with the same canonical placement.
     *
     * @param source the node to drag; its leading identicon positions the drag cursor
     * @param nid    the koncept's nid; a description is resolved to the concept it describes
     */
    public static void install(Node source, int nid) {
        source.setOnDragDetected(event -> start(source, source, nid, event));
    }

    /**
     * Starts a concept drag from {@code source}, using {@code dragImageNode} as the drag image. For
     * the cell case — where the press lands on the cell (which swallows the first click) rather than
     * the badge — pass the cell as {@code source} and the cell's badge graphic as
     * {@code dragImageNode}.
     *
     * @param source        the node the drag gesture starts on; must be attached to a scene
     * @param dragImageNode the node providing the drag image and identicon geometry
     * @param nid           the koncept's nid; a description is resolved to the concept it describes
     * @param event         the {@code DRAG_DETECTED} event; consumed on success
     * @return {@code true} if the drag started, {@code false} if a node was detached
     */
    public static boolean start(Node source, Node dragImageNode, int nid, MouseEvent event) {
        if (source.getScene() == null || dragImageNode.getScene() == null) {
            return false;
        }
        Dragboard dragboard = source.startDragAndDrop(TransferMode.COPY);
        setDragView(dragboard, dragImageNode);
        dragboard.setContent(KometClipboard.forComponent(nid));
        event.consume();
        return true;
    }

    /**
     * Places the canonical drag view for {@code node} on {@code dragboard}: the standard-height
     * image from {@link DragImageMaker}, with the cursor just to the right of the node's identicon
     * and its tip on the image's bottom border, so the identicon detail and the dragged label stay
     * fully visible. This is the single placement convention every concept/component drag source
     * should use — call it in place of a bare {@code dragboard.setDragView(image)}.
     *
     * <p>{@link DragImageMaker} rescales every image to
     * {@link DragImageMaker#STANDARD_DRAG_IMAGE_HEIGHT}, so the identicon's local right edge is
     * scaled by the same factor to find the cursor's x in image space.
     *
     * @param dragboard the active dragboard
     * @param node      the node providing the drag image and identicon geometry
     */
    public static void setDragView(Dragboard dragboard, Node node) {
        Image image = new DragImageMaker(node).getDragImage();
        if (image == null) {
            return;
        }
        dragboard.setDragView(image, cursorX(node), DragImageMaker.STANDARD_DRAG_IMAGE_HEIGHT);
    }

    /**
     * The canonical cursor x (image space) for {@code node}: the identicon's right edge scaled to
     * the standard drag-image height, plus the gap. Package-private so the placement convention can
     * be unit-tested without simulating a full drag gesture.
     *
     * @param node the node being dragged
     * @return the cursor's x offset within the drag image
     */
    static double cursorX(Node node) {
        double height = node.getLayoutBounds().getHeight();
        double scale = height > 0 ? DragImageMaker.STANDARD_DRAG_IMAGE_HEIGHT / height : 1.0;
        return (identiconRightEdge(node) * scale) + CURSOR_GAP;
    }

    /**
     * The identicon's right-edge x within {@code node}'s local coordinates, used to place the drag
     * cursor just to its right. A {@link KonceptBadge} reports it directly; for any other node the
     * leading {@link ImageView} (preferring one tagged {@code koncept-identicon}) is located and its
     * right edge mapped into {@code node}-local space. Returns {@code 0} when no identicon is found,
     * so the cursor falls a small gap in from the left edge — still uniform, never on the identicon.
     *
     * @param node the node being dragged
     * @return the identicon's right-edge x in {@code node}-local coordinates, or {@code 0}
     */
    private static double identiconRightEdge(Node node) {
        if (node instanceof KonceptBadge badge) {
            return badge.identiconRightEdge();
        }
        ImageView identicon = findIdenticon(node);
        if (identicon == null || identicon == node) {
            return 0;
        }
        Bounds inScene = identicon.localToScene(identicon.getLayoutBounds());
        if (inScene == null) {
            return 0;
        }
        Bounds inNode = node.sceneToLocal(inScene);
        return inNode == null ? 0 : Math.max(0, inNode.getMaxX());
    }

    /**
     * The leading identicon image within {@code root}: an {@link ImageView} carrying the
     * {@code koncept-identicon} style class if present, otherwise the first {@link ImageView}
     * found in a breadth-first traversal.
     *
     * @param root the node being dragged
     * @return the identicon image view, or {@code null} if the node holds no image
     */
    private static ImageView findIdenticon(Node root) {
        ImageView firstImageView = null;
        Deque<Node> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            if (current instanceof ImageView imageView) {
                if (imageView.getStyleClass().contains(StyleClasses.KONCEPT_IDENTICON.toString())) {
                    return imageView;
                }
                if (firstImageView == null) {
                    firstImageView = imageView;
                }
            }
            if (current instanceof Parent parentNode) {
                queue.addAll(parentNode.getChildrenUnmodifiable());
            }
        }
        return firstImageView;
    }
}
