package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.FilterComboBox;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Bounds;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;

import java.util.Locale;

/**
 * Default skin implementation for the {@link FilterComboBox} control: a {@link ComboBox} over the
 * control's items narrowed by the search text, and a label showing the search text over the combo
 * box's button cell while searching.
 *
 * @param <T> the type of the options
 * @see FilterComboBox
 */
public class FilterComboBoxSkin<T> extends SkinBase<FilterComboBox<T>> {

    private final ComboBox<T> comboBox = new ComboBox<>();
    private final ListCell<T> buttonCell;

    /** The combo box's items: the control's items, narrowed by the search text */
    private final FilteredList<T> filteredItems;

    /** The text typed to narrow the options; empty when not searching */
    private String searchText = "";

    /** Shows the {@link #searchText} over the combo box's button cell while searching */
    private final Label searchLabel = new Label();

    /** The combo box's popup list, available once the combo box has its skin */
    private ListView<T> popupList;

    /** Set while the items are being narrowed, so the value changes that causes are not pushed to the control */
    private boolean filtering;

    /**
     * Creates a new FilterComboBoxSkin instance.
     *
     * @param control The control that this skin should be installed onto.
     */
    public FilterComboBoxSkin(FilterComboBox<T> control) {
        super(control);
        // The combo box takes the focus in the control's stead
        control.setFocusTraversable(false);
        control.focusedProperty().subscribe(() -> {
            if (control.isFocused()) {
                comboBox.requestFocus();
            }
        });
        getChildren().add(comboBox);

        // The value is synced by hand rather than bound bidirectionally: narrowing the items while
        // searching makes the ComboBox's selection model shift or clear its value, and those
        // changes must not reach the control.
        filteredItems = new FilteredList<>(control.getItems());
        comboBox.setItems(filteredItems);
        control.valueProperty().subscribe(comboBox::setValue);
        comboBox.valueProperty().subscribe((_, newValue) -> {
            if (!filtering) {
                control.setValue(newValue);
            }
        });

        comboBox.converterProperty().bind(control.converterProperty());
        comboBox.promptTextProperty().bind(control.promptTextProperty());
        comboBox.placeholderProperty().bind(control.placeholderProperty());
        comboBox.setMaxWidth(Double.MAX_VALUE);

        // Renders the prompt text for a null value — a plain ComboBox can lose its prompt text
        // once a value has been chosen and cleared again
        buttonCell = new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? control.getPromptText() : control.getConverter().toString(item));
            }
        };
        comboBox.setButtonCell(buttonCell);

        // Type to search. While the popup shows, key events go to its (focused) list view first,
        // so the list view gets the same handlers as the combo box.
        Region searchIcon = new Region();
        searchIcon.getStyleClass().add("search-icon");
        searchLabel.setGraphic(searchIcon);
        searchLabel.getStyleClass().add("search-text");
        searchLabel.setManaged(false);
        searchLabel.setMouseTransparent(true);
        searchLabel.setVisible(false);
        getChildren().add(searchLabel);
        comboBox.addEventFilter(KeyEvent.KEY_TYPED, this::onKeyTyped);
        comboBox.skinProperty().subscribe(this::installPopupListHandlers);
        comboBox.showingProperty().subscribe(() -> {
            if (!comboBox.isShowing()) {
                setSearchText("");
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);

        if (searchLabel.isVisible()) {
            Bounds cellBounds = comboBox.localToParent(buttonCell.getBoundsInParent());
            searchLabel.setPadding(buttonCell.getPadding());
            searchLabel.resizeRelocate(cellBounds.getMinX(), cellBounds.getMinY(),
                    cellBounds.getWidth(), cellBounds.getHeight());
        }
    }

    @SuppressWarnings("unchecked")
    private void installPopupListHandlers(Skin<?> comboBoxSkin) {
        if (comboBoxSkin instanceof ComboBoxListViewSkin<?> listViewSkin) {
            popupList = (ListView<T>) listViewSkin.getPopupContent();
            popupList.addEventFilter(KeyEvent.KEY_TYPED, this::onKeyTyped);
            popupList.addEventFilter(KeyEvent.KEY_PRESSED, this::onKeyPressed);
        }
    }

    /** A printable character extends the search text, opening the popup if needed */
    private void onKeyTyped(KeyEvent keyEvent) {
        String character = keyEvent.getCharacter();
        // Shortcuts are not text; AltGr (Ctrl+Alt on Windows) still types characters
        boolean shortcut = (keyEvent.isControlDown() || keyEvent.isMetaDown()) && !keyEvent.isAltDown();
        // A leading space keeps its usual meaning of toggling the popup
        if (shortcut || character.isEmpty() || Character.isISOControl(character.charAt(0))
                || (searchText.isEmpty() && character.isBlank())) {
            return;
        }
        if (!comboBox.isShowing()) {
            comboBox.show();
        }
        setSearchText(searchText + character);
        keyEvent.consume();
    }

    /** While searching: Backspace edits the search text, Enter picks the highlighted option */
    private void onKeyPressed(KeyEvent keyEvent) {
        if (searchText.isEmpty()) {
            return;
        }
        switch (keyEvent.getCode()) {
            case BACK_SPACE -> setSearchText(searchText.substring(0, searchText.length() - 1));
            case ENTER -> {
                T highlighted = popupList.getFocusModel().getFocusedItem();
                if (highlighted != null) {
                    comboBox.setValue(highlighted);
                }
                comboBox.hide();
            }
            // A space is part of the search text: keep the list view from closing the popup on it
            case SPACE -> { }
            default -> {
                return;
            }
        }
        keyEvent.consume();
    }

    /**
     * Narrows the options to those whose text contains the given text, and highlights the current
     * value if it is among them, else the first match. An empty text shows all the options.
     */
    private void setSearchText(String text) {
        searchText = text;
        searchLabel.setText(text);
        searchLabel.setVisible(!text.isEmpty());
        buttonCell.setOpacity(text.isEmpty() ? 1 : 0);

        String lowerCaseText = text.toLowerCase(Locale.ROOT);
        filtering = true;
        filteredItems.setPredicate(text.isEmpty() ? null : item -> getSkinnable().getConverter()
                .toString(item).toLowerCase(Locale.ROOT).contains(lowerCaseText));
        // Undo whatever the ComboBox's selection model did as the items changed: it can leave the
        // selection on another item than the value, so reselect the value from scratch
        comboBox.getSelectionModel().clearSelection();
        comboBox.getSelectionModel().select(getSkinnable().getValue());
        comboBox.setValue(getSkinnable().getValue());
        filtering = false;

        if (!text.isEmpty() && popupList != null && !filteredItems.isEmpty()) {
            int highlightIndex = Math.max(0, filteredItems.indexOf(getSkinnable().getValue()));
            popupList.getFocusModel().focus(highlightIndex);
            popupList.scrollTo(highlightIndex);
        }
        getSkinnable().requestLayout();
    }
}
