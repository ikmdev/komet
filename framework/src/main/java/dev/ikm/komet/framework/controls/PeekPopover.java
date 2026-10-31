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

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PopOver;

/**
 * A peek: any card shown for a quick look where the reader is, as a transient popover beside the
 * gesture that asked for it, rather than a window added to the journal's layout (KEC 2026-10-31).
 * A click outside it or Esc dismisses it and leaves nothing behind; <em>Keep in journal</em> pins
 * it, handing the card to the host to place as an ordinary window beside the one it came from.
 * A peek may open another from inside it: the new one is owned by the first, so a click in the
 * inner peek does not dismiss the outer, and Esc closes the innermost first.
 *
 * <p>The definition pop-out of {@link KonceptBadge} is the same gesture for one section of a card;
 * this one shows the whole card.
 */
public final class PeekPopover {

    private static final double MAX_HEIGHT = 680;

    private final PopOver popover = new PopOver();
    private boolean pinned;

    private PeekPopover() {
    }

    /**
     * Shows a card as a peek.
     *
     * @param card      the card's node, sized by the peek
     * @param width     the card's width: the width its host gives it as a window, so it lays out alike
     * @param title     a few words naming what the card shows
     * @param anchor    the node the peek attaches to: the chip or row that asked for it
     * @param screenX   the screen x of the invoking gesture
     * @param screenY   the screen y of the invoking gesture
     * @param onPin     takes the card back to place it as a window; null offers no pin
     * @param onDismiss runs when the peek closes without being pinned, to release the card
     * @return the peek, already showing
     */
    public static PeekPopover show(Node card, double width, String title, Node anchor, double screenX, double screenY,
                                   Runnable onPin, Runnable onDismiss) {
        PeekPopover peek = new PeekPopover();
        Label name = new Label(title);
        name.setStyle("-fx-text-fill: #666666; -fx-font-size: 11;");
        Label hint = new Label("Esc or click outside to close");
        hint.setStyle("-fx-text-fill: #9a9a9a; -fx-font-size: 10.5;");
        Region spring = new Region();
        HBox.setHgrow(spring, Priority.ALWAYS);
        HBox bar = new HBox(8, name, spring, hint);
        bar.setAlignment(Pos.CENTER_LEFT);
        if (onPin != null) {
            Button keep = new Button("Keep in journal");
            keep.setId("peek-keep-button");
            keep.setTooltip(new Tooltip("Place this card in the journal, beside the one it came from"));
            keep.setOnAction(e -> {
                peek.pinned = true;
                peek.popover.hide();
                release(card);
                onPin.run();
            });
            bar.getChildren().add(keep);
        }
        bar.setPadding(new Insets(6, 8, 4, 8));
        if (card instanceof Region region) {
            region.setPrefWidth(width);
            region.setMinWidth(width);
            region.setMaxHeight(MAX_HEIGHT);
        }
        VBox content = new VBox(bar, card);
        content.setId("peek-content");
        content.setPrefWidth(width);
        content.setMaxHeight(MAX_HEIGHT + 40);
        VBox.setVgrow(card, Priority.ALWAYS);

        peek.popover.setContentNode(content);
        peek.popover.setAutoHide(true);
        peek.popover.setHideOnEscape(true);
        peek.popover.setDetachable(false);
        peek.popover.setHeaderAlwaysVisible(false);
        peek.popover.setTitle("");
        peek.popover.setOnHidden(e -> {
            if (!peek.pinned && onDismiss != null) {
                onDismiss.run();
            }
        });
        peek.popover.show(anchor, screenX, screenY);
        return peek;
    }

    /**
     * Hands a card back as the host first gave it: out of the peek, at no position and with no size
     * of the peek's, so the host places it as a new window rather than where it sat in the peek.
     */
    private static void release(Node card) {
        if (card.getParent() instanceof javafx.scene.layout.Pane parent) {
            parent.getChildren().remove(card);
        }
        card.relocate(0, 0);
        if (card instanceof Region region) {
            region.setPrefWidth(Region.USE_COMPUTED_SIZE);
            region.setMinWidth(Region.USE_COMPUTED_SIZE);
            region.setMaxHeight(Region.USE_COMPUTED_SIZE);
        }
    }

    /** The node a peek's content is: a host contributes a badge menu on it, as on its own windows. */
    public Node content() {
        return popover.getContentNode();
    }

    /** Closes the peek, as a click outside it does. */
    public void hide() {
        popover.hide();
    }
}
