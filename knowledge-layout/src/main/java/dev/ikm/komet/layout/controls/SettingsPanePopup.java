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
package dev.ikm.komet.layout.controls;

import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.util.function.Supplier;

/**
 * The standardized settings pane ({@code IKE-Network/ike-issues#1043}): durable preferences
 * presented the way the platform already presents view options — an <em>adjacent pane</em>
 * flanking the owning window, dark, with the shared header grammar, stacked section cards
 * (title, current value, chevron) and drill-in section content. One presentation for every
 * settings surface, instead of a dialog here and a menu there.
 *
 * <p>Consumers register sections ({@link #addSection}) and wire an invoking button with
 * {@link #attachTo}: press toggles the pane, positioned by the same flanking strategy the View
 * Options popup uses ({@link ViewOptionsPopupHelper#flankWindow}), with the
 * {@code filter-showing} pseudo-class on the button while open. Section content is built fresh
 * on each drill-in and applies its own changes — the pane standardizes presentation, not
 * transaction semantics.
 */
public final class SettingsPanePopup extends PopupControl {

    /** The pane's design width — the shared stylesheet's fixed pane width. */
    private static final double PANE_WIDTH = 326.0;

    private final SettingsPaneRoot root;

    /**
     * Creates the pane.
     *
     * @param title the pane title, centred in the header
     */
    public SettingsPanePopup(String title) {
        this.root = new SettingsPaneRoot(title, this::hide);
        setAutoHide(true);
        setAnchorLocation(AnchorLocation.WINDOW_TOP_LEFT);
        setSkin(new Skin<>() {
            @Override
            public SettingsPanePopup getSkinnable() {
                return SettingsPanePopup.this;
            }

            @Override
            public Node getNode() {
                return root;
            }

            @Override
            public void dispose() {
            }
        });
    }

    /**
     * Registers a section: a card on the pane's list carrying the title and a live summary,
     * drilling into the content the supplier builds fresh on each entry.
     *
     * @param title   the section title
     * @param summary supplies the card's current-value line (re-read on show and on drill-back)
     * @param content builds the drill-in content node, fresh per entry
     */
    public void addSection(String title, Supplier<String> summary, Supplier<Node> content) {
        root.addSection(title, summary, content);
    }

    /** Re-reads every section card's summary line — for a consumer whose setting changed live. */
    public void refreshSummaries() {
        root.refreshSummaries();
    }

    /**
     * Wires the invoking button: primary press toggles the pane, shown flanking
     * {@code owningPane} on whichever side has screen room, top-aligned and sized against it
     * (a stable pref set before showing — the ike-issues#681 rule). While showing, the button
     * carries the shared {@code filter-showing} pseudo-class.
     *
     * @param button     the toolbar affordance that opens the pane
     * @param owningPane the pane the popup flanks (the owning card or window content)
     */
    public void attachTo(ButtonBase button, Pane owningPane) {
        button.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() != MouseButton.PRIMARY) {
                return;
            }
            if (isShowing()) {
                e.consume();
                hide();
            } else {
                root.setPrefHeight(Math.max(owningPane.getHeight(), 480));
                root.showSections();
                ViewOptionsPopupHelper.flankWindow(this, owningPane, PANE_WIDTH);
            }
        });
        showingProperty().subscribe(showing ->
                button.pseudoClassStateChanged(ViewOptionsPopupHelper.FILTER_SHOWING, showing));
    }
}
