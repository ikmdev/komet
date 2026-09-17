package dev.ikm.komet.kview.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Control;

public abstract class KLReadOnlyBaseControl extends Control {

    /**
     * Active while the value shown comes from a version that is not published yet: the skin follows
     * the value with an amber dot (see the read-only-*.css files).
     */
    private static final PseudoClass UNPUBLISHED_PSEUDO_CLASS = PseudoClass.getPseudoClass("unpublished");

    // -- unpublished
    /**
     * Whether the value shown comes from a version that is not published yet. Mirrored as the
     * {@code :unpublished} pseudo-class for the stylesheets.
     */
    private final BooleanProperty unpublished = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(UNPUBLISHED_PSEUDO_CLASS, get());
        }
    };
    public boolean isUnpublished() { return unpublished.get(); }
    public BooleanProperty unpublishedProperty() { return unpublished; }
    public void setUnpublished(boolean unpublished) { this.unpublished.set(unpublished); }

    // -- title
    private StringProperty title = new SimpleStringProperty();
    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    public void setTitle(String title) { this.title.set(title); }

    // -- title visible
    /**
     * Whether the title is displayed above the field's value. Authored per field in the KL Editor;
     * defaults to shown.
     */
    private BooleanProperty titleVisible = new SimpleBooleanProperty(true);
    public boolean isTitleVisible() { return titleVisible.get(); }
    public BooleanProperty titleVisibleProperty() { return titleVisible; }
    public void setTitleVisible(boolean titleVisible) { this.titleVisible.set(titleVisible); }

    // -- prompt text
    private StringProperty promptText = new SimpleStringProperty("");
    public String getPromptText() { return promptText.get(); }
    public StringProperty promptTextProperty() { return promptText; }
    public void setPromptText(String text) { this.promptText.set(text); }

    // -- edit mode
    private BooleanProperty editMode = new SimpleBooleanProperty();
    public boolean isEditMode() { return editMode.get(); }
    public BooleanProperty editModeProperty() { return editMode; }
    public void setEditMode(boolean editMode) { this.editMode.set(editMode); }

    // -- preview mode
    private BooleanProperty previewMode = new SimpleBooleanProperty();
    public boolean isPreviewMode() { return previewMode.get(); }
    public BooleanProperty previewModeProperty() { return previewMode; }
    public void setPreviewMode(boolean previewMode) { this.previewMode.set(previewMode); }

    // -- on edit action
    private ObjectProperty<Runnable> onEditAction = new SimpleObjectProperty<>();
    public Runnable getOnEditAction() { return onEditAction.get(); }
    public ObjectProperty<Runnable> onEditActionProperty() { return onEditAction; }
    public void setOnEditAction(Runnable onEditAction) { this.onEditAction.set(onEditAction); }
}
