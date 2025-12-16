
package dev.ikm.komet.layout_engine.component.area;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.area.KlAreaForString;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.EditableFieldAreaBlueprint;
import dev.ikm.komet.layout_engine.component.menu.LayoutContextMenu;
import dev.ikm.komet.layout_engine.controls.KlStringControl;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.beans.property.Property;
import javafx.scene.layout.StackPane;

/**
 * An editable area for String fields.
 * <p>
 * This area uses {@link KlStringControl} and handles all the binding plumbing
 * via its parent class {@link EditableFieldAreaBlueprint}.
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * // Create the area
 * StringFieldArea stringArea = StringFieldArea.create(preferencesFactory);
 *
 * // Get an editable field from ObservableComposer
 * ObservableField.Editable<String> editable = ...;
 *
 * // Connect the area to the editable
 * stringArea.setEditable(editable);
 *
 * // Add to your layout
 * parentPane.getChildren().add(stringArea.fxObject());
 * }</pre>
 */
public final class StringFieldArea extends EditableFieldAreaBlueprint<String, StackPane>
        implements KlAreaForString<StackPane> {

    private KlStringControl stringControl;

    {
        fxObject().setOnContextMenuRequested(event ->
                LayoutContextMenu.makeContextMenu(this).show(fxObject(), event.getScreenX(), event.getScreenY()));
    }

    /**
     * Constructor for restoring from preferences.
     */
    public StringFieldArea(KometPreferences preferences) {
        super(preferences, new StackPane());
    }

    /**
     * Constructor for creating a new area.
     */
    public StringFieldArea(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory, new StackPane());
    }

    @Override
    protected void createControl() {
        stringControl = new KlStringControl();
        fxObject().getChildren().add(stringControl);
    }

    @Override
    protected Property<String> getControlValueProperty() {
        return stringControl.textProperty();
    }

    @Override
    protected void bindControlToEditable(ObservableField.Editable<String> editable) {
        stringControl.textProperty().bindBidirectional(editable.editableValueProperty());

        addEditableSubscription(
                editable.editableValueProperty().subscribe((oldVal, newVal) -> {
                    if (newVal != null && !newVal.equals(oldVal)) {
                        editable.setValue(newVal);
                    }
                })
        );
    }

    @Override
    protected void unbindControlFromEditable() {
        if (getEditable() != null) {
            stringControl.textProperty().unbindBidirectional(getEditable().editableValueProperty());
        }
    }

    @Override
    protected void updateControlTitle(String title) {
        stringControl.setTitle(title);
    }

    // --- Factory Methods ---

    public static Factory factory() {
        return new Factory();
    }

    public static StringFieldArea restore(KometPreferences preferences) {
        return StringFieldArea.factory().restore(preferences);
    }

    public static StringFieldArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
        return StringFieldArea.factory().create(preferencesFactory, areaGridSettings);
    }

    public static StringFieldArea create(KlPreferencesFactory preferencesFactory) {
        return StringFieldArea.factory().create(preferencesFactory);
    }

    public static class Factory implements KlAreaForString.Factory<StackPane> {

        public Factory() {}

        @Override
        public StringFieldArea restore(KometPreferences preferences) {
            return new StringFieldArea(preferences);
        }

        @Override
        public StringFieldArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            StringFieldArea area = new StringFieldArea(preferencesFactory, this);
            area.setAreaLayout(areaGridSettings);
            return area;
        }

        @Override
        public StringFieldArea create(KlPreferencesFactory preferencesFactory) {
            StringFieldArea area = new StringFieldArea(preferencesFactory, this);
            area.setAreaLayout(defaultAreaGridSettings());
            return area;
        }
    }
}