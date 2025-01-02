package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLStringControlSkin;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class KLStringControl extends Control {

    public KLStringControl() {
        getStyleClass().add("string-control");
    }

    // -- title
    private StringProperty title = new SimpleStringProperty();
    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    public void setTitle(String title) { this.title.set(title); }

    // -- text
    private StringProperty text = new SimpleStringProperty();
    public String getText() { return text.get(); }
    public StringProperty textProperty() { return text; }
    public void setText(String text) { this.text.set(text); }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLStringControlSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return KLStringControl.class.getResource("string-control.css").toExternalForm();
    }
}