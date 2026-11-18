package dev.ikm.komet.kview.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.util.ResourceBundle;

/**
 * The "Show" row of the concept navigator, between the search box and the tree: a pill that names
 * the filter narrowing the tree ("All concepts" when there is none) and opens the picker that
 * sets it, a cross on the pill that clears the filter, and the number of concepts that match.
 * While a filter is set it also carries a Tree | List switch, which asks for the matching
 * concepts as a flat list in place of the tree.
 * <p>
 * The row only presents: what the filter is, how many concepts match and what a click does are
 * set by whoever owns it.
 */
public class ConceptFilterBar extends HBox {

    private static final PseudoClass SET_PSEUDO_CLASS = PseudoClass.getPseudoClass("set");
    private static final PseudoClass OPEN_PSEUDO_CLASS = PseudoClass.getPseudoClass("open");
    private static final PseudoClass CHOSEN_PSEUDO_CLASS = PseudoClass.getPseudoClass("chosen");

    private final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.concept-filter");
    private final HBox pill = new HBox();
    private final Label pillLabel = new Label();
    private final Region chevron = new Region();
    private final StackPane clearPane = new StackPane();
    private final HBox viewSwitch = new HBox();
    private final Label countLabel = new Label();

    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    public ConceptFilterBar() {
        Label caption = new Label(resources.getString("bar.caption"));
        caption.getStyleClass().add("filter-caption");
        caption.setMinWidth(Region.USE_PREF_SIZE);

        Region funnel = new Region();
        funnel.getStyleClass().addAll("filter-icon", "funnel");
        pillLabel.getStyleClass().add("filter-pill-label");
        chevron.getStyleClass().addAll("filter-icon", "chevron");
        Region cross = new Region();
        cross.getStyleClass().addAll("filter-icon", "cross");
        clearPane.getChildren().add(cross);
        clearPane.getStyleClass().add("filter-clear");
        clearPane.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && getOnClearAction() != null) {
                getOnClearAction().run();
            }
            event.consume();
        });
        pill.getChildren().addAll(funnel, pillLabel, chevron, clearPane);
        pill.getStyleClass().add("filter-pill");
        pill.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && getOnPillAction() != null) {
                getOnPillAction().run();
            }
        });

        countLabel.getStyleClass().add("filter-count");
        countLabel.setMinWidth(Region.USE_PREF_SIZE);

        // The switch and the count keep their width; a long filter name is what gives way.
        Button treeOption = new Button(resources.getString("bar.view.tree"));
        treeOption.getStyleClass().setAll("filter-view-option");
        treeOption.setOnAction(_ -> setListShown(false));
        Button listOption = new Button(resources.getString("bar.view.list"));
        listOption.getStyleClass().setAll("filter-view-option", "following");
        listOption.setOnAction(_ -> setListShown(true));
        viewSwitch.getChildren().addAll(treeOption, listOption);
        viewSwitch.getStyleClass().add("filter-view-switch");
        viewSwitch.setMinWidth(Region.USE_PREF_SIZE);
        listShown.subscribe(shown -> {
            treeOption.pseudoClassStateChanged(CHOSEN_PSEUDO_CLASS, !shown);
            listOption.pseudoClassStateChanged(CHOSEN_PSEUDO_CLASS, shown);
            showTrailing();
        });

        // The switch and the count sit together at the right end, taking the room the pill leaves.
        // The switch keeps its own height there, however tall the two-line count is.
        HBox trailing = new HBox(viewSwitch, countLabel);
        trailing.setFillHeight(false);
        trailing.getStyleClass().add("filter-trailing");
        trailing.setMinWidth(Region.USE_PREF_SIZE);
        HBox.setHgrow(trailing, Priority.ALWAYS);

        getChildren().addAll(caption, pill, trailing);
        getStyleClass().add("concept-filter-bar");
        getStylesheets().add(ConceptFilterBar.class.getResource("concept-filter.css").toExternalForm());

        filterText.subscribe(this::updatePill);
        pickerShowing.subscribe(showing -> pill.pseudoClassStateChanged(OPEN_PSEUDO_CLASS, showing));
        countLabel.textProperty().bind(countText);
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    /**
     * The pill, for placing the picker under it.
     */
    public Region getPill() {
        return pill;
    }

    /***************************************************************************
     *                                                                         *
     * Private Implementation                                                  *
     *                                                                         *
     **************************************************************************/

    /**
     * The switch is on the row while a filter is set. The count leaves the row while the list is
     * shown: the list's own header says how many concepts it holds, and the room goes to the pill.
     */
    private void showTrailing() {
        boolean set = getFilterText() != null;
        viewSwitch.setVisible(set);
        viewSwitch.setManaged(set);
        boolean counted = !(set && isListShown());
        countLabel.setVisible(counted);
        countLabel.setManaged(counted);
    }

    /** A set filter swaps the chevron for the cross, turns the pill green and brings the Tree | List switch. */
    private void updatePill(String text) {
        boolean set = text != null;
        showTrailing();
        pillLabel.setText(set ? text : resources.getString("bar.all"));
        // The row gives the pill what room is left, so a long name is cut short; the tooltip has it whole.
        pillLabel.setTooltip(set ? new Tooltip(text) : null);
        pill.pseudoClassStateChanged(SET_PSEUDO_CLASS, set);
        chevron.setVisible(!set);
        chevron.setManaged(!set);
        clearPane.setVisible(set);
        clearPane.setManaged(set);
    }

    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/

    // -- filterText
    /**
     * The words that name the filter in the pill, or null when the tree is not filtered.
     */
    private final StringProperty filterText = new SimpleStringProperty(this, "filterText");
    public StringProperty filterTextProperty() { return filterText; }
    public String getFilterText() { return filterText.get(); }
    public void setFilterText(String value) { filterText.set(value); }

    // -- countText
    /**
     * The text at the right end of the row: how many concepts match, or that they are being counted.
     */
    private final StringProperty countText = new SimpleStringProperty(this, "countText");
    public StringProperty countTextProperty() { return countText; }
    public String getCountText() { return countText.get(); }
    public void setCountText(String value) { countText.set(value); }

    // -- listShown
    /**
     * Which side of the Tree | List switch is chosen: true for the list of matching concepts.
     * The switch is on the row only while a filter is set; the choice is kept across filters.
     */
    private final BooleanProperty listShown = new SimpleBooleanProperty(this, "listShown");
    public BooleanProperty listShownProperty() { return listShown; }
    public boolean isListShown() { return listShown.get(); }
    public void setListShown(boolean value) { listShown.set(value); }

    // -- pickerShowing
    /**
     * Whether the picker the pill opens is on screen, which the pill shows as its open state.
     */
    private final BooleanProperty pickerShowing = new SimpleBooleanProperty(this, "pickerShowing");
    public BooleanProperty pickerShowingProperty() { return pickerShowing; }
    public boolean isPickerShowing() { return pickerShowing.get(); }
    public void setPickerShowing(boolean value) { pickerShowing.set(value); }

    // -- onPillAction
    /**
     * What a click on the pill does.
     */
    private final ObjectProperty<Runnable> onPillAction = new SimpleObjectProperty<>(this, "onPillAction");
    public ObjectProperty<Runnable> onPillActionProperty() { return onPillAction; }
    public Runnable getOnPillAction() { return onPillAction.get(); }
    public void setOnPillAction(Runnable value) { onPillAction.set(value); }

    // -- onClearAction
    /**
     * What a click on the pill's cross does.
     */
    private final ObjectProperty<Runnable> onClearAction = new SimpleObjectProperty<>(this, "onClearAction");
    public ObjectProperty<Runnable> onClearActionProperty() { return onClearAction; }
    public Runnable getOnClearAction() { return onClearAction.get(); }
    public void setOnClearAction(Runnable value) { onClearAction.set(value); }
}
