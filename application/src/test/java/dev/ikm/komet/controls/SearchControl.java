package dev.ikm.komet.controls;

import dev.ikm.komet.controls.skin.SearchControlSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class SearchControl extends Control {

    public SearchControl() {

        getStyleClass().add("search-control");
        getStylesheets().add(getUserAgentStylesheet());
    }

    // promptTextProperty
    private final StringProperty promptTextProperty = new SimpleStringProperty(this, "promptText");
    public final StringProperty promptTextProperty() {
       return promptTextProperty;
    }
    public final String getPromptText() {
       return promptTextProperty.get();
    }
    public final void setPromptText(String value) {
        promptTextProperty.set(value);
    }

    // textProperty
    private final StringProperty textProperty = new SimpleStringProperty(this, "text");
    public final StringProperty textProperty() {
       return textProperty;
    }
    public final String getText() {
       return textProperty.get();
    }
    public final void setText(String value) {
        textProperty.set(value);
    }

    // onActionProperty
    private final ObjectProperty<EventHandler<ActionEvent>> onActionProperty = new SimpleObjectProperty<>(this, "onAction");
    public final ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
       return onActionProperty;
    }
    public final EventHandler<ActionEvent> getOnAction() {
       return onActionProperty.get();
    }
    public final void setOnAction(EventHandler<ActionEvent> value) {
        onActionProperty.set(value);
    }
    
    // onFilterActionProperty
    private final ObjectProperty<EventHandler<ActionEvent>> onFilterActionProperty = new SimpleObjectProperty<>(this, "onFilterAction");
    public final ObjectProperty<EventHandler<ActionEvent>> onFilterActionProperty() {
       return onFilterActionProperty;
    }
    public final EventHandler<ActionEvent> getOnFilterAction() {
       return onFilterActionProperty.get();
    }
    public final void setOnFilterAction(EventHandler<ActionEvent> value) {
        onFilterActionProperty.set(value);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SearchControlSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return SearchControl.class.getResource("search-control.css").toExternalForm();
    }
}
