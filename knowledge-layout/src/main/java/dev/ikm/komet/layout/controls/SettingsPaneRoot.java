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

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * The content root of a {@link SettingsPanePopup}: the View Options presentation grammar —
 * dark pane, {@code header-box} with the close glyph and centred title, stacked section cards
 * (blue upper-case title, current-value summary, chevron) drilling into a section's own content
 * with a back row — built as a plain region so its structure and navigation are testable without
 * the popup shell. Shares {@code filter-options-popup.css} (the root carries the
 * {@code filter-options-popup} style class) with the settings-card extension in
 * {@code settings-pane.css}, so the two panes cannot drift apart visually
 * ({@code IKE-Network/ike-issues#1043}).
 */
final class SettingsPaneRoot extends VBox {

    /** One registered section: a card on the main list, drilling into its content. */
    private record Section(String title, Supplier<String> summary, Supplier<Node> content,
                           Label summaryLabel) {
    }

    private final List<Section> sections = new ArrayList<>();
    private final VBox sectionList = new VBox();
    private final StackPane body = new StackPane();
    private final Runnable closeAction;

    /**
     * Creates the root.
     *
     * @param title       the pane title, shown centred in the header
     * @param closeAction invoked by the header's close glyph (the popup's hide)
     */
    SettingsPaneRoot(String title, Runnable closeAction) {
        this.closeAction = closeAction;
        getStyleClass().addAll("filter-options-popup", "settings-pane");
        String shared = ViewOptionsPopupHelper.class
                .getResource("/dev/ikm/komet/layout/controls/filter-options-popup.css")
                .toExternalForm();
        String own = ViewOptionsPopupHelper.class
                .getResource("/dev/ikm/komet/layout/controls/settings-pane.css")
                .toExternalForm();
        getStylesheets().addAll(shared, own);

        // Header per the shared grammar: close glyph, centred title, and a placeholder region
        // balancing the close button so the title's fixed pref width stays centred.
        Region closeIcon = new Region();
        closeIcon.getStyleClass().addAll("icon", "close");
        StackPane closePane = new StackPane(closeIcon);
        closePane.getStyleClass().add("region");
        closePane.setOnMouseClicked(e -> closeAction.run());
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("title");
        Region balance = new Region();
        balance.getStyleClass().add("region");
        HBox headerBox = new HBox(closePane, titleLabel, balance);
        headerBox.getStyleClass().add("header-box");

        sectionList.getStyleClass().add("settings-sections");
        body.getChildren().add(sectionList);
        VBox.setVgrow(body, Priority.ALWAYS);
        getChildren().addAll(headerBox, body);
    }

    /**
     * Registers a section: a card carrying the title and live summary, drilling into the
     * content the supplier builds fresh on each entry.
     *
     * @param title   the section title (rendered upper-case, per the grammar)
     * @param summary supplies the card's current-value line, re-read by {@link #refreshSummaries()}
     * @param content builds the drill-in content, fresh per entry
     */
    void addSection(String title, Supplier<String> summary, Supplier<Node> content) {
        Label summaryLabel = new Label(summary.get());
        summaryLabel.getStyleClass().add("section-value");
        Section section = new Section(title, summary, content, summaryLabel);
        sections.add(section);

        Label titleLabel = new Label(title.toUpperCase(Locale.ROOT));
        titleLabel.getStyleClass().add("section-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label chevron = new Label("›");
        chevron.getStyleClass().add("section-chevron");
        HBox titleLine = new HBox(titleLabel, spacer, chevron);
        titleLine.getStyleClass().add("section-line");
        titleLine.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(titleLine, summaryLabel);
        card.getStyleClass().add("settings-section-card");
        card.setOnMouseClicked(e -> drillInto(section));
        sectionList.getChildren().add(card);
    }

    /** Re-reads every section's summary line — on show, and on return from a drill-in. */
    void refreshSummaries() {
        for (Section section : sections) {
            section.summaryLabel().setText(section.summary().get());
        }
    }

    /** Swaps the body to the section's content under a back row. */
    private void drillInto(Section section) {
        Label back = new Label("‹");
        back.getStyleClass().add("drill-back");
        Label drillTitle = new Label(section.title());
        drillTitle.getStyleClass().add("drill-title");
        HBox backRow = new HBox(back, drillTitle);
        backRow.getStyleClass().add("drill-back-row");
        backRow.setAlignment(Pos.CENTER_LEFT);
        backRow.setOnMouseClicked(e -> showSections());

        Node contentNode = section.content().get();
        // The drill-in owns the pane's whole body height: the section content grows to fill it
        // rather than floating as a small box above empty space (KEC 2026-08-17). Content that
        // wants less keeps its own max.
        VBox.setVgrow(contentNode, javafx.scene.layout.Priority.ALWAYS);
        VBox drill = new VBox(backRow, contentNode);
        drill.getStyleClass().add("settings-drill");
        body.getChildren().setAll(drill);
    }

    /** Returns the body to the section list, with summaries re-read. */
    void showSections() {
        refreshSummaries();
        body.getChildren().setAll(sectionList);
    }

    /** Whether the body currently shows the section list (not a drill-in). Test observation point. */
    boolean isShowingSections() {
        return body.getChildren().size() == 1 && body.getChildren().get(0) == sectionList;
    }

    /** The number of registered sections. Test observation point. */
    int sectionCount() {
        return sections.size();
    }
}
