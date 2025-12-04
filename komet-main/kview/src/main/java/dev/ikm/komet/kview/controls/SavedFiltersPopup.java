package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.SavedFiltersPopupSkin;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;

import java.util.function.Consumer;

public class SavedFiltersPopup extends PopupControl {

    private final Consumer<String> onRemove;
    private final Consumer<String> onAccept;

    public SavedFiltersPopup(Consumer<String> onAccept, Consumer<String> onRemove) {
        this.onAccept = onAccept;
        this.onRemove = onRemove;
        getStyleClass().add("saved-filters-popup");
    }

    private final ObservableList<String> savedFiltersList = FXCollections.observableArrayList();
    public final ObservableList<String> getSavedFiltersList() {
        return savedFiltersList;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SavedFiltersPopupSkin(this, onAccept, onRemove);
    }
}
