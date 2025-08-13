package dev.ikm.komet.kview.common;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.terms.ComponentWithNid;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;

import java.util.function.Supplier;

public class ViewCalculatorUtils {

    public static <T extends ComponentWithNid> void initComboBox(ComboBox<T> comboBox, Supplier<ViewProperties> viewProperties) {
        initComboBox(comboBox, FXCollections.observableArrayList(), viewProperties);
    }

    public static <T extends ComponentWithNid> void initComboBox(ComboBox<T> comboBox, ObservableList items, Supplier<ViewProperties> viewProperties) {
        comboBox.setItems(items);

        comboBox.setCellFactory(_ -> createConceptListCell(viewProperties));
        comboBox.setButtonCell(createConceptListCell(viewProperties));
    }

    private static <T extends ComponentWithNid> ListCell<T> createConceptListCell(Supplier<ViewProperties> viewProperties) {
        return new ListCell<T>(){
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(getDescriptionTextWithFallbackOrNid(item, viewProperties.get()));
                }
            }
        };
    }

    public static <T extends ComponentWithNid> String getDescriptionTextWithFallbackOrNid(T conceptEntity, ViewProperties viewProperties) {
        String descr = "" + conceptEntity.nid();

        if (viewProperties != null) {
            descr = viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(conceptEntity.nid());
        }
        return descr;
    }
}