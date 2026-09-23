package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLComponentComboBoxControlSkin;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.function.Function;

/**
 * <p>KLComponentComboBoxControl is an editable control for a component field whose value is
 * constrained to a predefined set of concepts: instead of the free-form search and drag-and-drop
 * of {@link KLComponentControl}, the user picks one of the {@link #getItems() items} from a
 * {@link FilterComboBox}, which they can also type in to search the items.
 *
 * <p>It has a title and a combo box to edit the {@link #valueProperty() value} property. Like
 * its {@link KLComponentControl} counterpart it holds no knowledge of the data store — the
 * items and the {@link #componentNameRendererProperty() name renderer} are wired externally
 * (see {@link KLComponentControlFactory#createComponentComboBoxControl}).
 */
public class KLComponentComboBoxControl extends Control {

    public KLComponentComboBoxControl() {
        getStyleClass().add("component-combo-box-control");

        // The stylesheet goes on the scene rather than on the control so that — as with
        // KLComponentControl — a hosting application's stylesheet can restyle the control by
        // selector specificity (a control's own stylesheets would outrank the scene's).
        sceneProperty().subscribe(newScene -> {
            if (newScene != null && !newScene.getStylesheets().contains(getUserAgentStylesheet())) {
                newScene.getStylesheets().add(getUserAgentStylesheet());
            }
        });
    }

    // -- title
    /**
     * A string property that sets the title of the control
     */
    private final StringProperty title = new SimpleStringProperty(this, "title");
    public final StringProperty titleProperty() { return title; }
    public final String getTitle() { return title.get(); }
    public final void setTitle(String value) { title.set(value); }

    // -- value
    /**
     * This property holds the component that is currently chosen in the control
     */
    private final ObjectProperty<EntityProxy> value = new SimpleObjectProperty<>(this, "value", null);
    public final ObjectProperty<EntityProxy> valueProperty() { return value; }
    public final EntityProxy getValue() { return value.get(); }
    public final void setValue(EntityProxy value) { this.value.set(value); }

    // -- items
    /**
     * The predefined set of components the user may choose from
     */
    private final ObservableList<EntityProxy> items = FXCollections.observableArrayList();
    public final ObservableList<EntityProxy> getItems() { return items; }

    // -- function to render the component's name and avoid entity.description()
    private final ObjectProperty<Function<EntityProxy, String>> componentNameRenderer =
            new SimpleObjectProperty<>(this, "componentNameRenderer", EntityProxy::toString);
    public final Function<EntityProxy, String> getComponentNameRenderer() { return componentNameRenderer.get(); }
    public final void setComponentNameRenderer(Function<EntityProxy, String> nameHandler) {
        componentNameRenderer.set(nameHandler);
    }
    public final ObjectProperty<Function<EntityProxy, String>> componentNameRendererProperty() {
        return componentNameRenderer;
    }

    // -- prompt text
    private final StringProperty promptText = new SimpleStringProperty(this, "promptText", "Choose Selection");
    public final StringProperty promptTextProperty() { return promptText; }
    public String getPromptText() { return promptText.get(); }
    public void setPromptText(String value) { this.promptText.set(value); }

    // -- clearable
    private static final PseudoClass CLEARABLE_PSEUDO_CLASS = PseudoClass.getPseudoClass("clearable");

    /**
     * Whether the user may clear the {@link #valueProperty() value} back to {@code null}: when true a
     * clear button shows inside the combo box, before its arrow button, while a value is chosen.
     * Off by default — a field that must hold one of its options has no "no value" state.
     * Mirrored as the {@code :clearable} pseudo-class for styling.
     */
    private final BooleanProperty clearable = new SimpleBooleanProperty(this, "clearable", false) {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(CLEARABLE_PSEUDO_CLASS, get());
        }
    };
    public final BooleanProperty clearableProperty() { return clearable; }
    public final boolean isClearable() { return clearable.get(); }
    public final void setClearable(boolean value) { clearable.set(value); }

    /** {@inheritDoc} */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLComponentComboBoxControlSkin(this);
    }

    /** {@inheritDoc} */
    @Override
    public String getUserAgentStylesheet() {
        return KLComponentComboBoxControl.class.getResource("component-combo-box-control.css").toExternalForm();
    }
}
