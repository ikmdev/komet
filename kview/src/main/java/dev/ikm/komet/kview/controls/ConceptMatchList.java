package dev.ikm.komet.kview.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The concepts that match the concept navigator's "Show" filter as a flat list, shown in place of
 * the tree so they can be seen without expanding down to them: each row has the concept's name,
 * the path that leads to it in the tree, and a link that shows it there.
 * <p>
 * The list only presents. The matches are handed to it in the order to show them; the path of a
 * row is asked from {@link #pathProviderProperty()} when the row comes into view, and what
 * "Show in tree" does is set by whoever owns the list.
 */
public class ConceptMatchList extends VBox {

    private static final double ROW_HEIGHT = 50;

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

    // -- onShowInTree
    /**
     * What "Show in tree" on a row, or a double click on it, does with the row's concept.
     */
    private final ObjectProperty<Consumer<Match>> onShowInTree = new SimpleObjectProperty<>(this, "onShowInTree");
    public ObjectProperty<Consumer<Match>> onShowInTreeProperty() { return onShowInTree; }
    public Consumer<Match> getOnShowInTree() { return onShowInTree.get(); }
    public void setOnShowInTree(Consumer<Match> value) { onShowInTree.set(value); }

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

    /** A row: the name over the path, and the "Show in tree" link while the pointer is on the row. */
    private final class MatchCell extends ListCell<Match> {

        private final Label name = new Label();
        private final Label path = new Label();
        private final Button showInTree = new Button(resources.getString("list.showintree"));
        private final HBox row;

        MatchCell() {
            // A row is as wide as the list, whatever the length of its name: long names are cut, not scrolled to.
            setPrefWidth(0);
            name.getStyleClass().add("match-name");
            path.getStyleClass().add("match-path");
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
            setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    showInTree(getItem());
                }
            });
        }

        @Override
        protected void updateItem(Match match, boolean empty) {
            super.updateItem(match, empty);
            if (empty || match == null) {
                setGraphic(null);
            } else {
                name.setText(match.name());
                path.setText(getPathProvider() == null ? "" : getPathProvider().apply(match));
                setGraphic(row);
            }
        }
    }
}
