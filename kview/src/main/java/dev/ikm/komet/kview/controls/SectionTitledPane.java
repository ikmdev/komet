package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.SectionTitledPaneSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Skin;
import javafx.scene.control.TitledPane;

public class SectionTitledPane extends TitledPane {

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SectionTitledPaneSkin(this);
    }

    private final ObjectProperty<EventHandler<ActionEvent>> onEditAction = new SimpleObjectProperty<>();
    public EventHandler<ActionEvent> getOnEditAction() { return onEditAction.get(); }
    public ObjectProperty<EventHandler<ActionEvent>> onEditActionProperty() { return onEditAction; }
    public void setOnEditAction(EventHandler<ActionEvent> onEditAction) { this.onEditAction.set(onEditAction); }
}