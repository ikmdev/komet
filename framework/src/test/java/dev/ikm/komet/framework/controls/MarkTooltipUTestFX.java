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

import dev.ikm.komet.framework.testing.JavaFXThreadExtension;
import dev.ikm.komet.framework.testing.JavaFXThreadExtension.RunOnJavaFXThread;
import dev.ikm.tinkar.common.id.PublicIds;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import network.ike.docs.konceptcore.KonceptKind;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A mark's tooltip is the one shown when the pointer rests on the mark, though the mark stands in
 * a node with a tooltip of its own. The tooltip a pointer's move has activated is the one that
 * shows once the pointer has rested, so these tests read which tooltip a move activated.
 */
@ExtendWith(JavaFXThreadExtension.class)
@RunOnJavaFXThread
class MarkTooltipUTestFX {

    /** Shows a row in a window, tells it the pointer moved over one of its nodes, and closes the window. */
    private static void pointerMovesOver(Node within, HBox row) {
        Stage stage = new Stage();
        stage.setScene(new Scene(row, 300, 60));
        stage.show();
        try {
            Event.fireEvent(within, new MouseEvent(MouseEvent.MOUSE_MOVED, 2, 2, 2, 2, MouseButton.NONE, 0,
                    false, false, false, false, false, false, false, true, false, false, null));
        } finally {
            stage.hide();
            stage.getScene().setRoot(new HBox());            // so the row can be shown again
        }
    }

    @Test
    void installedPlainlyTheSurroundingNodesTooltipIsTheOneShown() {
        // what MarkTooltip exists for: each node with a tooltip takes the pointer's move in turn, outermost last
        Label mark = new Label("⊞");
        HBox row = new HBox(mark, new Label("Sitting systolic blood pressure"));
        Tooltip rowSays = new Tooltip("the concept");
        Tooltip markSays = new Tooltip("a complex concept");
        Tooltip.install(row, rowSays);
        Tooltip.install(mark, markSays);

        pointerMovesOver(mark, row);

        assertTrue(rowSays.isActivated(), "the row takes the move after the mark, and the last to take it is the one shown");
    }

    @Test
    void aMarksTooltipIsShownInPlaceOfTheSurroundingNodes() {
        Label mark = new Label("⊞");
        Label name = new Label("Sitting systolic blood pressure");
        HBox row = new HBox(mark, name);
        Tooltip rowSays = new Tooltip("the concept");
        Tooltip markSays = new Tooltip("a complex concept");
        Tooltip.install(row, rowSays);
        MarkTooltip.install(mark, markSays);

        pointerMovesOver(mark, row);

        assertTrue(markSays.isActivated(), "a rest on the mark shows what the mark is");
        assertFalse(rowSays.isActivated(), "and not what it stands in");

        pointerMovesOver(name, row);
        assertTrue(rowSays.isActivated(), "a rest beside the mark still shows what the row is");
    }

    @Test
    void aMarkInstalledTwiceKeepsTheMoveOnce() {
        Label mark = new Label("?");
        Tooltip first = new Tooltip("first");
        MarkTooltip.install(mark, first);
        Tooltip said = MarkTooltip.install(mark, "Words only");

        HBox row = new HBox(mark);
        pointerMovesOver(mark, row);

        assertTrue(said.isActivated(), "the tooltip installed last is the mark's");
        assertFalse(first.isActivated());
        assertTrue(said.isWrapText(), "plain words wrap");
        assertEquals("Words only", said.getText());
    }

    @Test
    void aBadgesStatusMarkAndKindSigilSayWhatTheyAre() {
        KonceptBadge badge = new KonceptBadge(PublicIds.newRandom(), "Sitting systolic blood pressure");
        badge.setStatus(KonceptStatus.COMPLEX);
        badge.setKind(KonceptKind.UNKNOWN);
        Node sigilBox = badge.getChildrenUnmodifiable().get(0);
        Node statusBox = badge.getChildrenUnmodifiable().get(1);
        Tooltip identity = (Tooltip) badge.getProperties().get("javafx.scene.control.Tooltip");
        Tooltip status = (Tooltip) statusBox.getProperties().get("javafx.scene.control.Tooltip");
        Node sigil = ((javafx.scene.Parent) sigilBox).getChildrenUnmodifiable().get(0);
        Tooltip kind = (Tooltip) sigil.getProperties().get("javafx.scene.control.Tooltip");

        Node tile = ((javafx.scene.Parent) statusBox).getChildrenUnmodifiable().get(0);
        assertTrue(tile.getStyleClass().contains(MarkTile.STYLE_CLASS), "a complex concept's status mark is a tile, not a glyph");
        assertEquals(tile.prefWidth(-1), tile.prefHeight(-1), 0.001, "a square one");

        pointerMovesOver(statusBox, new HBox(badge));
        assertTrue(status.isActivated(), "a rest on the status mark shows the status");
        assertTrue(status.getText().startsWith("Complex concept"));
        assertFalse(identity.isActivated(), "and not the badge's identity");

        pointerMovesOver(sigil, new HBox(badge));
        assertTrue(kind.isActivated(), "a rest on the kind sigil shows the kind");
        assertEquals(KonceptKind.UNKNOWN.accessibleName(), kind.getText());
    }
}
