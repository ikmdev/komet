package dev.ikm.komet.kview.controls;

import dev.ikm.tinkar.common.util.text.NaturalOrder;
import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

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
    }

    private void onItemsChanged(Observable observable) {
        addListenerToItems();
    }

    private void addListenerToItems() {
        getItems().addListener(this::onChanged);
    }

    private void onChanged(ListChangeListener.Change<? extends T> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                getItems().sort(NaturalOrder.getObjectComparator());
            }
        }
    }
}