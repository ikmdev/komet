package dev.ikm.komet.kview.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The concepts that match the concept navigator's "Show" filter as a flat list, shown in place of
 * the tree so they can be seen without expanding down to them: each row has the concept's name,
 * the path that leads to it in the tree, and a link that shows it there. A row acts as
 * a row of the tree does: it opens its concept on a double click, can be dragged, and has a menu.
 * <p>
 * The list only presents. The matches are handed to it in the order to show them; the path of a
 * row is asked from {@link #pathProviderProperty()} when the row comes into view, and what
 * "Show in tree" does is set by whoever owns the list.
 */
public class ConceptMatchList extends VBox {

    private static final double ROW_HEIGHT = 50;
    private static final PseudoClass FIRST_PSEUDO_CLASS = PseudoClass.getPseudoClass("first");
    private static final PseudoClass SELF_PSEUDO_CLASS = PseudoClass.getPseudoClass("self");

    private final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.concept-filter");
    private final NumberFormat countFormat = NumberFormat.getIntegerInstance();

    private final FilteredList<Match> shownMatches;
    private final ListView<Match> list;
    private final Label summaryLabel = new Label();
    private final Label messageLabel = new Label();

    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    public ConceptMatchList() {
        shownMatches = new FilteredList<>(matches);
        list = new ListView<>(shownMatches);
        list.setFixedCellSize(ROW_HEIGHT);
        list.setCellFactory(_ -> new MatchCell());
        list.getStyleClass().add("match-list-rows");
        VBox.setVgrow(list, Priority.ALWAYS);

        Label title = new Label(resources.getString("list.title"));
        title.getStyleClass().add("match-list-title");
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        summaryLabel.getStyleClass().add("match-list-summary");
        HBox header = new HBox(title, headerSpacer, summaryLabel);
        header.getStyleClass().add("match-list-header");

        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(Double.MAX_VALUE);
        messageLabel.getStyleClass().add("match-list-message");

        getChildren().addAll(header, messageLabel, list);
        getStyleClass().add("concept-match-list");
        getStylesheets().add(ConceptMatchList.class.getResource("concept-filter.css").toExternalForm());

        filterText.subscribe(this::narrow);
        shownMatches.subscribe(this::showSummary);
        message.subscribe(this::showSummary);
        pathProvider.subscribe(list::refresh);
        lineageProvider.subscribe(list::refresh);
        showSummary();
    }

    /***************************************************************************
     *                                                                         *
     * Private Implementation                                                  *
     *                                                                         *
     **************************************************************************/

    private void narrow(String text) {
        String needle = text == null ? "" : text.strip();
        shownMatches.setPredicate(needle.isEmpty() ? null : match -> containsIgnoringCase(match.name(), needle));
    }

    /** Compares in place: lower-casing every name on each keystroke would copy the whole list each time. */
    private static boolean containsIgnoringCase(String name, String needle) {
        for (int start = 0, last = name.length() - needle.length(); start <= last; start++) {
            if (name.regionMatches(true, start, needle, 0, needle.length())) {
                return true;
            }
        }
        return false;
    }

    /** The header says how many concepts are listed; a line stands in for the rows when there are none to show. */
    private void showSummary() {
        int all = matches.size();
        int shown = shownMatches.size();
        String line = getMessage() != null ? getMessage()
                : all > 0 && shown == 0 ? resources.getString("list.none")
                : null;
        summaryLabel.setText(getMessage() != null || all == 0 ? ""
                : shown == all ? MessageFormat.format(resources.getString("list.summary"), countFormat.format(all))
                : MessageFormat.format(resources.getString("list.summary.narrowed"), countFormat.format(shown), countFormat.format(all)));
        messageLabel.setText(line);
        messageLabel.setVisible(line != null);
        messageLabel.setManaged(line != null);
        list.setVisible(line == null);
        list.setManaged(line == null);
    }

    private void showInTree(Match match) {
        if (match != null && getOnShowInTree() != null) {
            getOnShowInTree().accept(match);
        }
    }

    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/

    // -- matches
    /**
     * The concepts that match the filter, in the order they are listed.
     */
    private final ObservableList<Match> matches = FXCollections.observableArrayList();
    public ObservableList<Match> getMatches() { return matches; }

    // -- filterText
    /**
     * Text that narrows the list to the concepts whose name contains it, whatever the case; null
     * or blank lists them all.
     */
    private final StringProperty filterText = new SimpleStringProperty(this, "filterText");
    public StringProperty filterTextProperty() { return filterText; }
    public String getFilterText() { return filterText.get(); }
    public void setFilterText(String value) { filterText.set(value); }

    // -- message
    /**
     * A line shown in place of the rows, while the matches are being read for instance; null
     * shows the rows.
     */
    private final StringProperty message = new SimpleStringProperty(this, "message");
    public StringProperty messageProperty() { return message; }
    public String getMessage() { return message.get(); }
    public void setMessage(String value) { message.set(value); }

    // -- pathProvider
    /**
     * Gives the second line of a row: the path that leads to the concept in the tree. It is asked
     * for each row that comes into view, so it should answer quickly.
     */
    private final ObjectProperty<Function<Match, String>> pathProvider = new SimpleObjectProperty<>(this, "pathProvider");
    public ObjectProperty<Function<Match, String>> pathProviderProperty() { return pathProvider; }
    public Function<Match, String> getPathProvider() { return pathProvider.get(); }
    public void setPathProvider(Function<Match, String> value) { pathProvider.set(value); }

    // -- lineageProvider
    /**
     * Gives what the tooltip of a row shows: the whole path that leads to the row's concept in the
     * tree. It is asked when the tooltip is about to show, not for every row. While it is null the
     * rows have no tooltip.
     */
    private final ObjectProperty<Function<Match, Lineage>> lineageProvider = new SimpleObjectProperty<>(this, "lineageProvider");
    public ObjectProperty<Function<Match, Lineage>> lineageProviderProperty() { return lineageProvider; }
    public Function<Match, Lineage> getLineageProvider() { return lineageProvider.get(); }
    public void setLineageProvider(Function<Match, Lineage> value) { lineageProvider.set(value); }

    // -- onShowInTree
    /**
     * What "Show in tree" on a row does with the row's concept.
     */
    private final ObjectProperty<Consumer<Match>> onShowInTree = new SimpleObjectProperty<>(this, "onShowInTree");
    public ObjectProperty<Consumer<Match>> onShowInTreeProperty() { return onShowInTree; }
    public Consumer<Match> getOnShowInTree() { return onShowInTree.get(); }
    public void setOnShowInTree(Consumer<Match> value) { onShowInTree.set(value); }

    // -- onOpen
    /**
     * What a double click on a row does with the row's concept.
     */
    private final ObjectProperty<Consumer<Match>> onOpen = new SimpleObjectProperty<>(this, "onOpen");
    public ObjectProperty<Consumer<Match>> onOpenProperty() { return onOpen; }
    public Consumer<Match> getOnOpen() { return onOpen.get(); }
    public void setOnOpen(Consumer<Match> value) { onOpen.set(value); }

    // -- onOpenAlternative
    /**
     * What a double click on a row with Shift held does with the row's concept: the other way of
     * opening it.
     */
    private final ObjectProperty<Consumer<Match>> onOpenAlternative = new SimpleObjectProperty<>(this, "onOpenAlternative");
    public ObjectProperty<Consumer<Match>> onOpenAlternativeProperty() { return onOpenAlternative; }
    public Consumer<Match> getOnOpenAlternative() { return onOpenAlternative.get(); }
    public void setOnOpenAlternative(Consumer<Match> value) { onOpenAlternative.set(value); }

    // -- onDragStarted
    /**
     * Puts a row's concept on the dragboard when the row starts being dragged. While it is null
     * the rows are not dragged.
     */
    private final ObjectProperty<BiConsumer<Match, Dragboard>> onDragStarted = new SimpleObjectProperty<>(this, "onDragStarted");
    public ObjectProperty<BiConsumer<Match, Dragboard>> onDragStartedProperty() { return onDragStarted; }
    public BiConsumer<Match, Dragboard> getOnDragStarted() { return onDragStarted.get(); }
    public void setOnDragStarted(BiConsumer<Match, Dragboard> value) { onDragStarted.set(value); }

    // -- contextMenuProvider
    /**
     * Gives the menu a right click on a row brings, for the row's concept. While it is null the
     * rows have no menu.
     */
    private final ObjectProperty<Function<Match, ContextMenu>> contextMenuProvider = new SimpleObjectProperty<>(this, "contextMenuProvider");
    public ObjectProperty<Function<Match, ContextMenu>> contextMenuProviderProperty() { return contextMenuProvider; }
    public Function<Match, ContextMenu> getContextMenuProvider() { return contextMenuProvider.get(); }
    public void setContextMenuProvider(Function<Match, ContextMenu> value) { contextMenuProvider.set(value); }

    /***************************************************************************
     *                                                                         *
     * Supporting Classes                                                      *
     *                                                                         *
     **************************************************************************/

    /**
     * A concept that matches the filter.
     *
     * @param nid  the nid of the concept
     * @param name the name shown, which is also what the list is narrowed by
     */
    public record Match(long nid, String name) {
    }

    /**
     * The whole path that leads to a concept in the tree.
     *
     * @param ancestors        the names of the concept's ancestors, from the top of the tree down to its parent
     * @param otherParentCount the number of parents the concept has besides the one on this path
     */
    public record Lineage(List<String> ancestors, int otherParentCount) {
    }

    /** A row: the name over the path, and the "Show in tree" link while the pointer is on the row. */
    private final class MatchCell extends ListCell<Match> {

        private final Label name = new Label();
        private final Label path = new Label();
        private final Button showInTree = new Button(resources.getString("list.showintree"));
        private final Tooltip lineageTooltip = new Tooltip();
        private final HBox row;

        MatchCell() {
            // A row is as wide as the list, whatever the length of its name: long names are cut, not scrolled to.
            setPrefWidth(0);
            name.getStyleClass().add("match-name");
            path.getStyleClass().add("match-path");
            // A path too long for the row loses its start: the parent, at its end, is what places the concept.
            path.setTextOverrun(OverrunStyle.LEADING_WORD_ELLIPSIS);
            VBox text = new VBox(name, path);
            text.setAlignment(Pos.CENTER_LEFT);
            text.setMinWidth(0);
            HBox.setHgrow(text, Priority.ALWAYS);
            Region target = new Region();
            target.getStyleClass().add("match-target-icon");
            showInTree.setGraphic(target);
            showInTree.getStyleClass().setAll("match-show-in-tree");
            showInTree.setMinWidth(Region.USE_PREF_SIZE);
            showInTree.setOnAction(_ -> showInTree(getItem()));
            showInTree.visibleProperty().bind(hoverProperty());
            showInTree.managedProperty().bind(hoverProperty());
            row = new HBox(text, showInTree);
            row.getStyleClass().add("match-row");
            // The tooltip is the whole path, drawn when it is about to show: reading it for every row would be wasted.
            lineageTooltip.getStyleClass().add("match-lineage-tooltip");
            lineageTooltip.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            lineageTooltip.setOnShowing(_ -> lineageTooltip.setGraphic(
                    isEmpty() || getLineageProvider() == null ? null : createLineageNode(getItem())));
            // What a row of the tree does: a double click opens the concept, with Shift the
            // other way; the whole row drags the concept; a right click brings its menu.
            setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !isEmpty()) {
                    Consumer<Match> open = event.isShiftDown() ? getOnOpenAlternative() : getOnOpen();
                    if (open != null) {
                        open.accept(getItem());
                    }
                }
            });
            setOnDragDetected(event -> {
                if (event.getButton() == MouseButton.PRIMARY && !isEmpty() && getOnDragStarted() != null) {
                    getOnDragStarted().accept(getItem(), startDragAndDrop(TransferMode.COPY_OR_MOVE));
                    event.consume();
                }
            });
            setOnContextMenuRequested(event -> {
                if (!isEmpty() && getContextMenuProvider() != null) {
                    getContextMenuProvider().apply(getItem())
                            .show(getScene().getWindow(), event.getScreenX(), event.getScreenY());
                    event.consume();
                }
            });
        }

        @Override
        protected void updateItem(Match match, boolean empty) {
            super.updateItem(match, empty);
            if (empty || match == null) {
                setGraphic(null);
                setTooltip(null);
            } else {
                name.setText(match.name());
                path.setText(getPathProvider() == null ? "" : getPathProvider().apply(match));
                setGraphic(row);
                setTooltip(getLineageProvider() == null ? null : lineageTooltip);
            }
        }

        /** The path as a list on a rail: an ancestor a line, the concept last, then a word on its other parents. */
        private Node createLineageNode(Match match) {
            Lineage lineage = getLineageProvider().apply(match);
            Label title = new Label(resources.getString("list.lineage.title"));
            title.getStyleClass().add("match-lineage-title");
            VBox steps = new VBox();
            for (int index = 0; index < lineage.ancestors().size(); index++) {
                steps.getChildren().add(createLineageStep(lineage.ancestors().get(index), index == 0, false));
            }
            steps.getChildren().add(createLineageStep(match.name(), lineage.ancestors().isEmpty(), true));
            VBox content = new VBox(title, steps);
            content.getStyleClass().add("match-lineage");
            if (lineage.otherParentCount() > 0) {
                Label others = new Label(lineage.otherParentCount() == 1
                        ? resources.getString("list.lineage.others.one")
                        : MessageFormat.format(resources.getString("list.lineage.others"), lineage.otherParentCount()));
                others.getStyleClass().add("match-lineage-others");
                others.setMaxWidth(Double.MAX_VALUE);
                VBox.setMargin(others, new Insets(7, 0, 0, 0));
                content.getChildren().add(others);
            }
            return content;
        }

        /** A line of the path: its dot on the rail, which starts at the first line and ends at the concept's. */
        private Node createLineageStep(String text, boolean first, boolean self) {
            Region rail = new Region();
            rail.getStyleClass().add("match-lineage-rail");
            Region dot = new Region();
            dot.getStyleClass().add("match-lineage-dot");
            StackPane marker = new StackPane(rail, dot);
            marker.getStyleClass().add("match-lineage-marker");
            Label label = new Label(text);
            label.getStyleClass().add("match-lineage-name");
            HBox step = new HBox(marker, label);
            step.getStyleClass().add("match-lineage-step");
            step.pseudoClassStateChanged(FIRST_PSEUDO_CLASS, first);
            step.pseudoClassStateChanged(SELF_PSEUDO_CLASS, self);
            return step;
        }
    }
}
