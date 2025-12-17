package dev.ikm.komet.kview.klauthoring.editable.integerfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.kview.controls.KLIntegerControl;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.area.KlAreaForInteger;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.EditableFieldAreaBlueprint;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.beans.property.Property;

/**
 * An editable area for Integer fields.
 * <p>
 * This area uses {@link dev.ikm.komet.kview.controls.KLIntegerControl} and handles all the binding plumbing
 * via its parent class {@link EditableFieldAreaBlueprint}.
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * // Create the area
 * EditableIntegerFieldArea integerArea = EditableIntegerFieldArea.create(preferencesFactory);
 *
 * // Get an editable field from ObservableComposer
 * ObservableField.Editable<Integer> editable = ...;
 *
 * // Connect the area to the editable
 * integerArea.setEditable(editable);
 *
 * // Add to your layout
 * parentPane.getChildren().add(integerArea.fxObject());
 * }</pre>
 */
public final class EditableIntegerFieldArea extends EditableFieldAreaBlueprint<Integer, KLIntegerControl>
        implements KlAreaForInteger<KLIntegerControl> {

    /**
     * Constructor for restoring from preferences.
     */
    public EditableIntegerFieldArea(KometPreferences preferences) {
        super(preferences, new KLIntegerControl());
    }

    /**
     * Constructor for creating a new area.
     */
    public EditableIntegerFieldArea(KlPreferencesFactory preferencesFactory, KlAreaForInteger.Factory areaFactory) {
        super(preferencesFactory, areaFactory, new KLIntegerControl());
    }

    @Override
    protected void createControl() {
        setFxPeer(new KLIntegerControl());
    }

    @Override
    protected Property<Integer> getControlValueProperty() {
        return getFxPeer().valueProperty();
    }

    @Override
    protected void bindControlToEditable(ObservableField.Editable<Integer> editable) {
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

    public static EditableIntegerFieldArea restore(KometPreferences preferences) {
        return EditableIntegerFieldArea.factory().restore(preferences);
    }

    public static EditableIntegerFieldArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
        return EditableIntegerFieldArea.factory().create(preferencesFactory, areaGridSettings);
    }

    public static EditableIntegerFieldArea create(KlPreferencesFactory preferencesFactory) {
        return EditableIntegerFieldArea.factory().create(preferencesFactory);
    }

    public static class Factory implements KlAreaForInteger.Factory<KLIntegerControl> {

        public Factory() {}

        @Override
        public EditableIntegerFieldArea restore(KometPreferences preferences) {
            return new EditableIntegerFieldArea(preferences);
        }

        @Override
        public EditableIntegerFieldArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            EditableIntegerFieldArea area = new EditableIntegerFieldArea(preferencesFactory, this);
            area.setAreaLayout(areaGridSettings);
            return area;
        }

        @Override
        public EditableIntegerFieldArea create(KlPreferencesFactory preferencesFactory) {
            EditableIntegerFieldArea area = new EditableIntegerFieldArea(preferencesFactory, this);
            area.setAreaLayout(defaultAreaGridSettings());
            return area;
        }
    }
}