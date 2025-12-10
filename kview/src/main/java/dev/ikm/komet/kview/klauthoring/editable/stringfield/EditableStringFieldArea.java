package dev.ikm.komet.kview.klauthoring.editable.stringfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.kview.controls.KLStringControl;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.area.KlAreaForString;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.EditableFieldAreaBlueprint;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.beans.property.Property;

/**
 * An editable area for String fields.
 * <p>
 * This area uses {@link KLStringControl} and handles all the binding plumbing
 * via its parent class {@link EditableFieldAreaBlueprint}.
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * // Create the area
 * EditableStringFieldArea stringArea = EditableStringFieldArea.create(preferencesFactory);
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
public final class EditableStringFieldArea extends EditableFieldAreaBlueprint<String, KLStringControl>
        implements KlAreaForString<KLStringControl> {

    /**
     * Constructor for restoring from preferences.
     */
    public EditableStringFieldArea(KometPreferences preferences) {
        super(preferences, new KLStringControl());
    }

    /**
     * Constructor for creating a new area.
     */
    public EditableStringFieldArea(KlPreferencesFactory preferencesFactory, KlAreaForString.Factory areaFactory) {
        super(preferencesFactory, areaFactory, new KLStringControl());
    }

    @Override
    protected void createControl() {
        setFxPeer(new KLStringControl());
    }

    @Override
    protected Property<String> getControlValueProperty() {
        return getFxPeer().textProperty();
    }

    @Override
    protected void bindControlToEditable(ObservableField.Editable<String> editable) {
        getFxPeer()
                .textProperty()
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
            getFxPeer().textProperty().unbindBidirectional(getEditable().editableValueProperty());
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

    public static EditableStringFieldArea restore(KometPreferences preferences) {
        return EditableStringFieldArea.factory().restore(preferences);
    }

    public static EditableStringFieldArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
        return EditableStringFieldArea.factory().create(preferencesFactory, areaGridSettings);
    }

    public static EditableStringFieldArea create(KlPreferencesFactory preferencesFactory) {
        return EditableStringFieldArea.factory().create(preferencesFactory);
    }

    public static class Factory implements KlAreaForString.Factory<KLStringControl> {

        public Factory() {}

        @Override
        public EditableStringFieldArea restore(KometPreferences preferences) {
            return new EditableStringFieldArea(preferences);
        }

        @Override
        public EditableStringFieldArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            EditableStringFieldArea area = new EditableStringFieldArea(preferencesFactory, this);
            area.setAreaLayout(areaGridSettings);
            return area;
        }

        @Override
        public EditableStringFieldArea create(KlPreferencesFactory preferencesFactory) {
            EditableStringFieldArea area = new EditableStringFieldArea(preferencesFactory, this);
            area.setAreaLayout(defaultAreaGridSettings());
            return area;
        }
    }
}