package dev.ikm.komet.kview.controls;

import dev.ikm.tinkar.common.util.text.NaturalOrder;
import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

/**
 * A ComboBox that always shows its items sorted when its popup is showing.
 * The sort algorithm is given by `NaturalOrder.getObjectComparator()`.
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