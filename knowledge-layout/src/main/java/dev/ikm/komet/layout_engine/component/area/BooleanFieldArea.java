package dev.ikm.komet.layout_engine.component.area;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.area.KlAreaForBoolean;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.EditableFieldAreaBlueprint;
import dev.ikm.komet.layout_engine.component.menu.LayoutContextMenu;
import dev.ikm.komet.layout_engine.controls.KlBooleanControl;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.beans.property.Property;
import javafx.scene.layout.StackPane;

/**
 * An editable area for Boolean fields.
 * <p>
 * This area uses {@link KlBooleanControl} and handles all the binding plumbing
 * via its parent class {@link EditableFieldAreaBlueprint}.
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * // Create the area
 * BooleanFieldArea booleanArea = BooleanFieldArea.create(preferencesFactory);
 *
 * // Get an editable field from ObservableComposer
 * ObservableField.Editable<Boolean> editable = ...;
 *
 * // Connect the area to the editable
 * booleanArea.setEditable(editable);
 *
 * // Add to your layout
 * parentPane.getChildren().add(booleanArea.fxObject());
 * }</pre>
 */
public final class BooleanFieldArea extends EditableFieldAreaBlueprint<Boolean, StackPane>
        implements KlAreaForBoolean<StackPane> {

    private KlBooleanControl booleanControl;

    {
        // Context menu setup
        fxObject().setOnContextMenuRequested(event ->
                LayoutContextMenu.makeContextMenu(this).show(fxObject(), event.getScreenX(), event.getScreenY()));
    }

    /**
     * Constructor for restoring from preferences.
     */
    public BooleanFieldArea(KometPreferences preferences) {
        super(preferences, new StackPane());
    }

    /**
     * Constructor for creating a new area.
     */
    public BooleanFieldArea(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory, new StackPane());
    }

    @Override
    protected void createControl() {
        booleanControl = new KlBooleanControl();
        fxObject().getChildren().add(booleanControl);
    }

    @Override
    protected Property<Boolean> getControlValueProperty() {
        return booleanControl.valueProperty();
    }

    @Override
    protected void bindControlToEditable(ObservableField.Editable<Boolean> editable) {
        // Bidirectional binding: UI <-> Editable
        booleanControl.valueProperty().bindBidirectional(editable.editableValueProperty());

        // Add change listener to update the editable's value
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
            booleanControl.valueProperty().unbindBidirectional(getEditable().editableValueProperty());
        }
    }

    @Override
    protected void updateControlTitle(String title) {
        booleanControl.setTitle(title);
    }

    // --- Factory Methods ---

    public static Factory factory() {
        return new Factory();
    }

    public static BooleanFieldArea restore(KometPreferences preferences) {
        return BooleanFieldArea.factory().restore(preferences);
    }

    public static BooleanFieldArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
        return BooleanFieldArea.factory().create(preferencesFactory, areaGridSettings);
    }

    public static BooleanFieldArea create(KlPreferencesFactory preferencesFactory) {
        return BooleanFieldArea.factory().create(preferencesFactory);
    }

    public static class Factory implements KlAreaForBoolean.Factory<StackPane> {

        public Factory() {}

        @Override
        public BooleanFieldArea restore(KometPreferences preferences) {
            return new BooleanFieldArea(preferences);
        }

        @Override
        public BooleanFieldArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            BooleanFieldArea area = new BooleanFieldArea(preferencesFactory, this);
            area.setAreaLayout(areaGridSettings);
            return area;
        }

        @Override
        public BooleanFieldArea create(KlPreferencesFactory preferencesFactory) {
            BooleanFieldArea area = new BooleanFieldArea(preferencesFactory, this);
            area.setAreaLayout(defaultAreaGridSettings());
            return area;
        }
    }
}