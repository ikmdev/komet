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

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;

/**
 * The tooltip of a mark that stands inside something with a tooltip of its own: a status mark or a
 * kind sigil in a badge, a sign at the head of a row.
 *
 * <p>{@link Tooltip#install} alone does not show such a tooltip. A pointer's move is told to the
 * mark and then to each node around it, and each node with a tooltip takes the turn to show for
 * itself, so the outermost one is the one shown: the badge's, the row's. The mark's own, which says
 * what the mark is, is never seen. A mark installed here keeps the move to itself, so the pointer
 * at rest on the mark shows what the mark says, and at rest beside it shows what the surrounding
 * node says.
 */
public final class MarkTooltip {

    /** Keeps a pointer's move from the nodes around a mark, after the mark's tooltip has taken it. */
    private static final EventHandler<MouseEvent> KEPT = Event::consume;

    private MarkTooltip() {
    }

    /**
     * Gives a mark its tooltip, shown in place of the tooltip of anything the mark stands in.
     *
     * @param mark    the mark
     * @param tooltip what the pointer at rest on the mark shows
     */
    public static void install(Node mark, Tooltip tooltip) {
        Tooltip.install(mark, tooltip);
        // after the tooltip's own handler, which install has just added, and once however often a mark is installed
        mark.removeEventHandler(MouseEvent.MOUSE_MOVED, KEPT);
        mark.addEventHandler(MouseEvent.MOUSE_MOVED, KEPT);
    }

    /**
     * Gives a mark a tooltip of plain words, wrapped to a width a sentence or two reads well at.
     *
     * @param mark  the mark
     * @param words what the mark is
     * @return the tooltip, for a caller that changes its words later
     */
    public static Tooltip install(Node mark, String words) {
        Tooltip tooltip = new Tooltip(words);
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(340);
        install(mark, tooltip);
        return tooltip;
    }
}
