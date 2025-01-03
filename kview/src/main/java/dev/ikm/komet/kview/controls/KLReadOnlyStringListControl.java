package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLReadOnlyStringListControlSkin;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Skin;

public class KLReadOnlyStringListControl extends KLReadOnlyBaseControl {

    public KLReadOnlyStringListControl() {
        getStyleClass().add("read-only-string-list-control");
    }

    // -- prompt text
    private final StringProperty promptText = new SimpleStringProperty();
    public String getPromptText() { return promptText.get(); }
    public StringProperty promptTextProperty() { return promptText; }
    public void setPromptText(String text) { promptText.set(text); }

    // -- texts
    private final ObservableList<String> texts = FXCollections.observableArrayList();
    public ObservableList<String> getTexts() {
        return texts;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLReadOnlyStringListControlSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return KLInstantControl.class.getResource("read-only-string-list-control.css").toExternalForm();
    }
}