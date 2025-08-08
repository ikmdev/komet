package dev.ikm.komet.kview.mvvm.view;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.terms.ComponentWithNid;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;

import java.util.function.Supplier;

public class ControllerUtils<T extends ComponentWithNid> {

    private final Supplier<ViewProperties> viewPropertiesSupplier;

    public ControllerUtils(Supplier<ViewProperties> viewPropertiesSupplier) {
        this.viewPropertiesSupplier = viewPropertiesSupplier;
    }

    public void initComboBox(ComboBox<T> comboBox) {
        initComboBox(comboBox, FXCollections.observableArrayList());
    }

    public void initComboBox(ComboBox<T> comboBox, ObservableList<T> items) {
        comboBox.setItems(items);

        comboBox.setCellFactory(_ -> createConceptListCell());
        comboBox.setButtonCell(createConceptListCell());
    }

    private ListCell<T> createConceptListCell() {
        return new ListCell<>(){
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(getDescriptionTextWithFallbackOrNid(item));
                }
            }
        };
    }

    private String getDescriptionTextWithFallbackOrNid(T conceptEntity) {
        String descr = "" + conceptEntity.nid();
        ViewProperties viewProperties = viewPropertiesSupplier.get();

        if (viewProperties != null) {
            descr = viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(conceptEntity.nid());
        }
        return descr;
    }
}