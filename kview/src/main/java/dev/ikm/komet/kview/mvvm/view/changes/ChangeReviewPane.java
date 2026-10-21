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
package dev.ikm.komet.kview.mvvm.view.changes;

import dev.ikm.komet.kview.controls.KLWorkspace;
import dev.ikm.komet.kview.fxutils.CssHelper;
import dev.ikm.komet.kview.mvvm.view.changes.ChangeDiff.FieldDiff;
import dev.ikm.komet.layout_engine.window.DraggableSupport;
import dev.ikm.komet.layout_engine.window.WindowSupport;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;

/**
 * The review window's content: one component of the unpublished-changes tray at a time, with
 * each of its changes as a card of Before / After field rows, Previous / Next through the
 * tray's components, and the footer that opens, discards or publishes just this component.
 * Its window ({@link ChangeReviewKlWindow}) builds it, fills it with {@link #show} and relays its actions.
 */
final class ChangeReviewPane extends BorderPane {

    /** The window's height on the desktop; its width is the workspace's default. */
    private static final double WINDOW_HEIGHT = 600;

    /** The field-name column; the value columns share the rest of the card equally. */
    private static final double NAME_COLUMN_WIDTH = 118;

    private final ChangeReviewKlWindow.Actions actions;

    private final Button previousButton = new Button("Previous");
    private final Button nextButton = new Button("Next");
    private final Label positionLabel = new Label();
    private final Label typeGlyph = new Label();
    private final Label titleLabel = new Label();
    private final Label subtitleLabel = new Label();
    private final Label kindTag = new Label();
    private final HBox banner = new HBox();
    private final Region bannerIcon = new Region();
    private final Label bannerLabel = new Label();
    private final Label summaryLabel = new Label();
    private final CheckBox showUnchangedCheckBox = new CheckBox("Show unchanged fields");
    private final VBox cards = new VBox();
    private final Button openButton = new Button();
    private final Button discardButton = new Button("Discard");
    private final Button publishButton = new Button("Publish");

    private ChangeReviewKlWindow.Page page;

    /** True while the tray publishes or discards; the footer's buttons wait for it. */
    private final BooleanProperty busy = new SimpleBooleanProperty(this, "busy", false);

    /**
     * @param actions what the controls do
     */
    ChangeReviewPane(ChangeReviewKlWindow.Actions actions) {
        this.actions = actions;
        getStyleClass().add("change-review-window");
        getStylesheets().add(CssHelper.defaultStyleSheet());
        getProperties().put(KLWorkspace.WINDOW_AUTHORED_HEIGHT_KEY, WINDOW_HEIGHT);
        // The workspace's hover outline is for the layout windows; this one draws its own chrome.
        getProperties().put(WindowSupport.HIGHLIGHT_DISABLED_KEY, Boolean.TRUE);

        setTop(new VBox(createChrome(), createNavigationStrip(), createHeader(), creteBanner(), createToolsRow()));
        setCenter(createBody());
        setBottom(createFooter());

        discardButton.disableProperty().bind(busy);
        publishButton.disableProperty().bind(busy);

        addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (!event.isAltDown()) {
                return;
            }
            if (event.getCode() == KeyCode.LEFT) {
                previousButton.fire();
                event.consume();
            } else if (event.getCode() == KeyCode.RIGHT) {
                nextButton.fire();
                event.consume();
            }
        });
    }


    // ---- Building -----------------------------------------------------------------------------

    /**
     * The title bar: one blue bar with rounded top corners, the title chip sitting on its bottom
     * edge at the left and the close button at the right. The bar drags the window.
     */
    private HBox createChrome() {
        Region dots = new Region();
        dots.getStyleClass().add("nine-dots-icon");
        Text tabText = new Text("Review changes");
        tabText.getStyleClass().add("lidr-tab-text");
        HBox tab = new HBox(dots, tabText);
        tab.getStyleClass().add("lidr-rounded-tab");
        // The chip's height is the bar's less this margin, so it rests on the bar's bottom edge.
        HBox.setMargin(tab, new Insets(8, 0, 0, 0));

        Region closeIcon = new Region();
        closeIcon.getStyleClass().add("close-window");
        Button closeButton = new Button();
        closeButton.setGraphic(closeIcon);
        closeButton.setFocusTraversable(false);
        closeButton.setOnAction(_ -> actions.close());

        HBox bar = new HBox(tab, createSpacer(), closeButton);
        bar.getStyleClass().add("window-header-control");
        DraggableSupport.addDraggableNodes(this, bar);
        return bar;
    }

    private HBox createNavigationStrip() {
        previousButton.getStyleClass().add("nav-button");
        previousButton.setGraphic(icon("chevron-left"));
        previousButton.setOnAction(_ -> actions.previous());
        nextButton.getStyleClass().add("nav-button");
        nextButton.setGraphic(icon("chevron-right"));
        nextButton.setContentDisplay(ContentDisplay.RIGHT);
        nextButton.setOnAction(_ -> actions.next());
        Label hint = new Label("Alt+← / Alt+→");
        hint.getStyleClass().add("key-hint");
        positionLabel.getStyleClass().add("position-label");
        HBox strip = new HBox(previousButton, nextButton, hint, createSpacer(), positionLabel);
        strip.getStyleClass().add("nav-strip");
        return strip;
    }

    /** The tray's row header, repeated: type badge, title over subtitle, kind tag. */
    private HBox createHeader() {
        typeGlyph.getStyleClass().add("type-glyph");
        titleLabel.getStyleClass().add("change-title");
        subtitleLabel.getStyleClass().add("change-subtitle");
        kindTag.getStyleClass().add("kind-tag");
        VBox text = new VBox(titleLabel, subtitleLabel);
        text.getStyleClass().add("change-text");
        text.setMinWidth(0);
        HBox.setHgrow(text, Priority.ALWAYS);
        HBox header = new HBox(typeGlyph, text, kindTag);
        header.getStyleClass().addAll("unpublished-row", "component-header");
        return header;
    }

    private HBox creteBanner() {
        bannerIcon.getStyleClass().add("banner-icon");
        bannerLabel.getStyleClass().add("banner-text");
        bannerLabel.setWrapText(true);
        bannerLabel.setMinWidth(0);
        HBox.setHgrow(bannerLabel, Priority.ALWAYS);
        banner.getStyleClass().add("banner");
        banner.getChildren().addAll(bannerIcon, bannerLabel);
        banner.setVisible(false);
        banner.setManaged(false);
        return banner;
    }

    private HBox createToolsRow() {
        summaryLabel.getStyleClass().add("summary-label");
        summaryLabel.setMinWidth(0);
        // Takes the row's slack, so the toggle sits at the right edge.
        summaryLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(summaryLabel, Priority.ALWAYS);
        showUnchangedCheckBox.getStyleClass().add("show-unchanged");
        showUnchangedCheckBox.setSelected(true);
        HBox row = new HBox(summaryLabel, showUnchangedCheckBox);
        row.getStyleClass().add("tools-row");
        return row;
    }

    private ScrollPane createBody() {
        cards.getStyleClass().add("cards");
        ScrollPane scrollPane = new ScrollPane(cards);
        scrollPane.getStyleClass().add("cards-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scrollPane;
    }

    private HBox createFooter() {
        openButton.getStyleClass().add("open-window-button");
        openButton.setGraphic(icon("open-window-icon"));
        openButton.setOnAction(_ -> actions.open(page.group()));
        discardButton.getStyleClass().add("discard-button");
        discardButton.setOnAction(_ -> actions.discard(page.group()));
        publishButton.getStyleClass().add("publish-button");
        publishButton.setOnAction(_ -> actions.publish(page.group()));
        HBox footer = new HBox(openButton, createSpacer(), discardButton, publishButton);
        footer.getStyleClass().add("footer");
        return footer;
    }

    private static Region createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    /** A glyph drawn by the stylesheet (its shape and size are in the given class's rule). */
    private static Region icon(String styleClass) {
        Region icon = new Region();
        icon.getStyleClass().add(styleClass);
        return icon;
    }

    // ---- Showing a page -----------------------------------------------------------------------

    /** Shows a component and its changes; the previous page is replaced. */
    void show(ChangeReviewKlWindow.Page page) {
        this.page = page;
        UnpublishedGroup group = page.group();
        UnpublishedChange.Kind kind = group.headingKind();
        String typeName = group.type().label().toLowerCase();
        int semanticChanges = group.semanticChanges().size();

        previousButton.setDisable(page.index() == 0);
        nextButton.setDisable(page.index() >= page.total() - 1);
        positionLabel.setText("Component " + (page.index() + 1) + " of " + page.total()
                + " · " + plural(page.changeTotal(), "change") + " in all");

        typeGlyph.setText(group.type().glyph());
        restyle(typeGlyph, "type-glyph", group.type().name().toLowerCase());
        titleLabel.setText(group.title());
        subtitleLabel.setText(group.type().label() + " · " + plural(group.changes().size(), "change")
                + (page.author().isBlank() ? "" : " · by " + page.author()));
        kindTag.setText(kind.label());
        restyle(kindTag, "kind-tag", kind.name().toLowerCase());

        showBanner(group, typeName, semanticChanges);
        summaryLabel.setText(summary(group, typeName, semanticChanges));
        openButton.setText("Open " + typeName + " window");

        boolean brandNew = kind == UnpublishedChange.Kind.NEW;
        cards.getChildren().clear();
        for (ChangeDiff diff : page.diffs()) {
            cards.getChildren().add(createCard(diff, brandNew));
        }
    }

    private void showBanner(UnpublishedGroup group, String typeName, int semanticChanges) {
        String text = switch (group.headingKind()) {
            case NEW -> "New " + typeName + ". It has never been published, so there is nothing to compare."
                    + " Everything below goes out for the first time.";
            case INACTIVATED -> "This " + typeName + " is being inactivated."
                    + (semanticChanges == 0 ? " Its semantics stay as they are and are not listed." : "");
            case EDITED -> null;
        };
        banner.setVisible(text != null);
        banner.setManaged(text != null);
        if (text != null) {
            bannerLabel.setText(text);
            restyle(banner, "banner", group.headingKind().name().toLowerCase());
        }
    }

    private static String summary(UnpublishedGroup group, String typeName, int semanticChanges) {
        if (group.headingKind() == UnpublishedChange.Kind.NEW) {
            return semanticChanges == 0
                    ? "The " + typeName + " is new."
                    : "The " + typeName + " and " + plural(semanticChanges, "semantic") + " on it are new.";
        }
        if (group.kind() == null) {
            return "The " + typeName + " itself is unchanged; " + plural(semanticChanges, "semantic")
                    + " on it changed.";
        }
        return semanticChanges == 0
                ? "Only the " + typeName + " itself changed."
                : "The " + typeName + " and " + plural(semanticChanges, "semantic") + " on it changed.";
    }

    /**
     * One change: its row header over a table of its fields. In a new component every value is
     * new, so the table has one value column; otherwise Before and After, with a new semantic's
     * Before column empty.
     */
    private VBox createCard(ChangeDiff diff, boolean brandNew) {
        UnpublishedChange change = diff.change();

        Label glyph = new Label(change.type().glyph());
        glyph.getStyleClass().addAll("type-glyph", change.type().name().toLowerCase());
        Label title = new Label(change.title());
        title.getStyleClass().add("change-title");
        Label subtitle = new Label(change.subtitle());
        subtitle.getStyleClass().add("change-subtitle");
        VBox text = new VBox(title, subtitle);
        text.getStyleClass().add("change-text");
        text.setMinWidth(0);
        HBox.setHgrow(text, Priority.ALWAYS);
        Label tag = new Label(change.kind().label());
        tag.getStyleClass().addAll("kind-tag", change.kind().name().toLowerCase());
        HBox header = new HBox(glyph, text, tag);
        header.getStyleClass().addAll("unpublished-row", "card-header");

        // A grid sizes its rows from the widths the columns end up with, so a wrapped value
        // takes exactly the lines it needs.
        GridPane table = new GridPane();
        table.getStyleClass().add("field-table");
        table.getColumnConstraints().add(new ColumnConstraints(NAME_COLUMN_WIDTH, NAME_COLUMN_WIDTH, NAME_COLUMN_WIDTH));
        String[] columns = brandNew ? new String[]{"Field", "Value"} : new String[]{"Field", "Before", "After"};
        for (int i = 1; i < columns.length; i++) {
            // A nominal preferred width: the value columns split the card's remaining width equally.
            ColumnConstraints column = new ColumnConstraints(0, 1, Double.MAX_VALUE);
            column.setHgrow(Priority.ALWAYS);
            table.getColumnConstraints().add(column);
        }
        for (int i = 0; i < columns.length; i++) {
            Label columnTitle = new Label(columns[i]);
            columnTitle.getStyleClass().add("column-title");
            table.add(cell(columnTitle, "header-cell"), i, 0);
        }
        int row = 1;
        for (FieldDiff field : diff.fields()) {
            addFieldRow(table, row++, field, brandNew);
        }

        VBox card = new VBox(header, table);
        // Not "card": the stylesheet dresses the journal's card windows under that class.
        card.getStyleClass().add("change-card");
        return card;
    }

    /** A field's row: its name, then its Before and After cells (After alone in a new component). */
    private void addFieldRow(GridPane table, int row, FieldDiff field, boolean brandNew) {
        Label name = new Label(field.name());
        name.getStyleClass().add("field-name");
        name.setWrapText(true);
        List<StackPane> cells = new ArrayList<>();
        cells.add(cell(name, "name-cell"));
        if (brandNew) {
            cells.add(valueCell(List.of(text(field.after(), null)), "add"));
        } else if (field.before() == null) {
            cells.add(valueCell(List.of(text("—", null)), "none"));
            cells.add(valueCell(List.of(text(field.after(), null)), "add"));
        } else if (!field.changed()) {
            cells.add(valueCell(List.of(text(field.before(), null)), "same"));
            cells.add(valueCell(List.of(text(field.after(), null)), "same"));
        } else {
            TextDiff split = TextDiff.of(field.before(), field.after());
            cells.add(valueCell(List.of(
                    text(split.prefix(), null),
                    text(split.beforeMiddle(), "mark-removed"),
                    text(split.suffix(), null)), "del"));
            cells.add(valueCell(List.of(
                    text(split.prefix(), null),
                    text(split.afterMiddle(), "mark-added"),
                    text(split.suffix(), null)), "add"));
        }
        for (int column = 0; column < cells.size(); column++) {
            StackPane cell = cells.get(column);
            if (!field.changed()) {
                cell.visibleProperty().bind(showUnchangedCheckBox.selectedProperty());
                cell.managedProperty().bind(showUnchangedCheckBox.selectedProperty());
            }
            table.add(cell, column, row);
        }
    }

    private static StackPane valueCell(List<Text> texts, String styleClass) {
        TextFlow flow = new TextFlow();
        flow.getStyleClass().add("value-flow");
        flow.getChildren().addAll(texts.stream().filter(text -> !text.getText().isEmpty()).toList());
        return cell(flow, styleClass);
    }

    /** A grid cell: it fills its slot, so its fill and bottom hairline span the row. */
    private static StackPane cell(Node content, String styleClass) {
        StackPane cell = new StackPane(content);
        cell.getStyleClass().addAll("cell", styleClass);
        cell.setMinWidth(0);
        cell.setMaxWidth(Double.MAX_VALUE);
        cell.setMaxHeight(Double.MAX_VALUE);
        return cell;
    }

    private static Text text(String value, String markClass) {
        Text text = new Text(value);
        text.getStyleClass().add("value-text");
        if (markClass != null) {
            text.getStyleClass().add(markClass);
        }
        return text;
    }

    /** Keeps the node's base style class and replaces the variant class with the given one. */
    private static void restyle(Region node, String base, String variant) {
        node.getStyleClass().removeIf(styleClass -> !styleClass.equals(base));
        node.getStyleClass().add(variant);
    }

    private static String plural(int count, String noun) {
        return count + " " + noun + (count == 1 ? "" : "s");
    }

    /*=========================================================================*
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     *=========================================================================*/

    // -- busy
    public final BooleanProperty busyProperty() { return busy; }
    public final boolean isBusy() { return busy.get(); }
    public final void setBusy(boolean busy) { this.busy.set(busy); }

}
