package dev.ikm.komet.kview.klauthoring.editable.booleanfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.kview.controls.KLBooleanControl;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.area.KlAreaForBoolean;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.EditableFieldAreaBlueprint;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.beans.property.Property;

/**
 * An editable area for Boolean fields.
 * <p>
 * This area uses {@link KLBooleanControl} and handles all the binding plumbing
 * via its parent class {@link EditableFieldAreaBlueprint}.
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * // Create the area
 * EditableBooleanFieldArea booleanArea = EditablebooleanFieldArea.create(preferencesFactory);
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
public final class EditableBooleanFieldArea extends EditableFieldAreaBlueprint<Boolean, KLBooleanControl>
        implements KlAreaForBoolean<KLBooleanControl> {

    /**
     * Constructor for restoring from preferences.
     */
    public EditableBooleanFieldArea(KometPreferences preferences) {
        super(preferences, new KLBooleanControl());
    }

    /**
     * Constructor for creating a new area.
     */
    public EditableBooleanFieldArea(KlPreferencesFactory preferencesFactory, KlAreaForBoolean.Factory areaFactory) {
        super(preferencesFactory, areaFactory, new KLBooleanControl());
    }

    @Override
    protected void createControl() {
        setFxPeer(new KLBooleanControl());
    }

    @Override
    protected Property<Boolean> getControlValueProperty() {
        return getFxPeer().valueProperty();
    }

    @Override
    protected void bindControlToEditable(ObservableField.Editable<Boolean> editable) {
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

    public static EditableBooleanFieldArea restore(KometPreferences preferences) {
        return EditableBooleanFieldArea.factory().restore(preferences);
    }

    public static EditableBooleanFieldArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
        return EditableBooleanFieldArea.factory().create(preferencesFactory, areaGridSettings);
    }

    public static EditableBooleanFieldArea create(KlPreferencesFactory preferencesFactory) {
        return EditableBooleanFieldArea.factory().create(preferencesFactory);
    }

    public static class Factory implements KlAreaForBoolean.Factory<KLBooleanControl> {

        public Factory() {}

        @Override
        public EditableBooleanFieldArea restore(KometPreferences preferences) {
            return new EditableBooleanFieldArea(preferences);
        }

        @Override
        public EditableBooleanFieldArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            EditableBooleanFieldArea area = new EditableBooleanFieldArea(preferencesFactory, this);
            area.setAreaLayout(areaGridSettings);
            return area;
        }

        @Override
        public EditableBooleanFieldArea create(KlPreferencesFactory preferencesFactory) {
            EditableBooleanFieldArea area = new EditableBooleanFieldArea(preferencesFactory, this);
            area.setAreaLayout(defaultAreaGridSettings());
            return area;
        }
    }
}