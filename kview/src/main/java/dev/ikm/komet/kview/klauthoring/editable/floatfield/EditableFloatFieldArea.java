package dev.ikm.komet.kview.klauthoring.editable.floatfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.kview.controls.KLFloatControl;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.area.KlAreaForFloat;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.EditableFieldAreaBlueprint;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.beans.property.Property;

/**
 * An editable area for Float fields.
 * <p>
 * This area uses {@link KLFloatControl} and handles all the binding plumbing
 * via its parent class {@link EditableFieldAreaBlueprint}.
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * // Create the area
 * EditableFloatFieldArea floatArea = EditablefloatFieldArea.create(preferencesFactory);
 *
 * // Get an editable field from ObservableComposer
 * ObservableField.Editable<Float> editable = ...;
 *
 * // Connect the area to the editable
 * floatArea.setEditable(editable);
 *
 * // Add to your layout
 * parentPane.getChildren().add(floatArea.fxObject());
 * }</pre>
 */
public final class EditableFloatFieldArea extends EditableFieldAreaBlueprint<Float, KLFloatControl>
        implements KlAreaForFloat<KLFloatControl> {

    /**
     * Constructor for restoring from preferences.
     */
    public EditableFloatFieldArea(KometPreferences preferences) {
        super(preferences, new KLFloatControl());
    }

    /**
     * Constructor for creating a new area.
     */
    public EditableFloatFieldArea(KlPreferencesFactory preferencesFactory, KlAreaForFloat.Factory areaFactory) {
        super(preferencesFactory, areaFactory, new KLFloatControl());
    }

    @Override
    protected void createControl() {
        setFxPeer(new KLFloatControl());
    }

    @Override
    protected Property<Float> getControlValueProperty() {
        return getFxPeer().valueProperty();
    }

    @Override
    protected void bindControlToEditable(ObservableField.Editable<Float> editable) {
        getFxPeer()
                .valueProperty()
                .bindBidirectional(editable.editableValueProperty());

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
            getFxPeer().valueProperty().unbindBidirectional(getEditable().editableValueProperty());
        }
    }

    @Override
    protected void updateControlTitle(String title) {
        getFxPeer().setTitle(title);
    }

    // --- Factory Methods ---

    public static Factory factory() {
        return new Factory();
    }

    public static EditableFloatFieldArea restore(KometPreferences preferences) {
        return EditableFloatFieldArea.factory().restore(preferences);
    }

    public static EditableFloatFieldArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
        return EditableFloatFieldArea.factory().create(preferencesFactory, areaGridSettings);
    }

    public static EditableFloatFieldArea create(KlPreferencesFactory preferencesFactory) {
        return EditableFloatFieldArea.factory().create(preferencesFactory);
    }

    public static class Factory implements KlAreaForFloat.Factory<KLFloatControl> {

        public Factory() {}

        @Override
        public EditableFloatFieldArea restore(KometPreferences preferences) {
            return new EditableFloatFieldArea(preferences);
        }

        @Override
        public EditableFloatFieldArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            EditableFloatFieldArea area = new EditableFloatFieldArea(preferencesFactory, this);
            area.setAreaLayout(areaGridSettings);
            return area;
        }

        @Override
        public EditableFloatFieldArea create(KlPreferencesFactory preferencesFactory) {
            EditableFloatFieldArea area = new EditableFloatFieldArea(preferencesFactory, this);
            area.setAreaLayout(defaultAreaGridSettings());
            return area;
        }
    }
}