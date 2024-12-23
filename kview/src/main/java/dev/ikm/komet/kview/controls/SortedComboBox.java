package dev.ikm.komet.kview.controls;

import dev.ikm.tinkar.common.util.text.NaturalOrder;
import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;

/**
 * A ComboBox that always shows its items sorted when its popup is showing.
 * The sort algorithm is given by `NaturalOrder.getObjectComparator()`.
 *
 * If the programmer has set a Converter in this SortedComboBox, it is used to convert the item that is selected
 * to a string and then that string is set as the text that is shown.
 *
 * @param <T> The type of the value that has been selected or otherwise entered in to this ComboBox.
 */
public class SortedComboBox<T> extends ComboBox<T> {

    public SortedComboBox() {
        init();
    }

    public SortedComboBox(ObservableList<T> items) {
        super(items);
        init();
    }

    private void init() {
        itemsProperty().addListener(this::onItemsChanged);
        addListenerToItems();
        sortItemsInComboBox();

        // Code below fixes IIA-1139. JavaFX Comboboxes have a weird behavior in that the prompt text can get cleared when interacting
        // with them and never comes back again
        setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(getPromptText());
                } else {
                    if (getConverter() != null) {
                        setText(getConverter().toString(item));
                    } else {
                        setText(item.toString());
                    }
                }
            }
        });
    }

    private void onItemsChanged(Observable observable) {
        addListenerToItems();
        sortItemsInComboBox();
    }

    private void addListenerToItems() {
        getItems().addListener(this::onChanged);
    }

    private void onChanged(ListChangeListener.Change<? extends T> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                sortItemsInComboBox();
            }
        }
    }

    private void sortItemsInComboBox() {
        getItems().sort(NaturalOrder.getObjectComparator());
    }
}