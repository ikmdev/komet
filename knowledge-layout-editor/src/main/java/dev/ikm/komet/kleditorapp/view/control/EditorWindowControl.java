package dev.ikm.komet.kleditorapp.view.control;

import dev.ikm.komet.kleditorapp.view.skin.EditorWindowSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class EditorWindowControl extends Control {

    private static final String DEFAULT_STYLE_CLASS = "editor-window-control";

    public EditorWindowControl() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);

        getSectionViews().addListener(this::onSectionsChanged);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new EditorWindowSkin(this);
    }

    private void onSectionsChanged(ListChangeListener.Change<? extends SectionViewControl> change) {
        while(change.next()) {
            if (change.wasAdded()) {
                change.getAddedSubList().forEach(section -> section.setParentWindow(this));
            }
        }
    }

    // -- title
    private final StringProperty title = new SimpleStringProperty(this, "title", "");
    public StringProperty titleProperty() { return title; }
    public String getTitle() { return title.get(); }
    public void setTitle(String title) { this.title.set(title); }

    // -- section views
    private final ObservableList<SectionViewControl> sectionViews = FXCollections.observableArrayList();
    public ObservableList<SectionViewControl> getSectionViews() { return sectionViews; }

    // -- on add section action
    private final ObjectProperty<EventHandler<ActionEvent>> onAddSectionAction = new SimpleObjectProperty<>();
    public EventHandler<ActionEvent> getOnAddSectionAction() { return onAddSectionAction.get(); }
    public ObjectProperty<EventHandler<ActionEvent>> onAddSectionActionProperty() { return onAddSectionAction; }
    public void setOnAddSectionAction(EventHandler<ActionEvent> action) { onAddSectionAction.set(action); }
}