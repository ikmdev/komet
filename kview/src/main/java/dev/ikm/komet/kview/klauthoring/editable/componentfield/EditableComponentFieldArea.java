package dev.ikm.komet.kview.klauthoring.editable.componentfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.area.KlAreaForEntityFacade;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.EditableFieldAreaBlueprint;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.beans.property.Property;

/**
 * An editable area for EntityFacade fields.
 * <p>
 * This area uses {@link KLComponentControl} and handles all the binding plumbing
 * via its parent class {@link EditableFieldAreaBlueprint}.
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * // Create the area
 * EditableComponentFieldArea componentArea = EditableComponentFieldArea.create(preferencesFactory);
 *
 * // Get an editable field from ObservableComposer
 * ObservableField.Editable<EntityFacade> editable = ...;
 *
 * // Connect the area to the editable
 * componentArea.setEditable(editable);
 *
 * // Add to your layout
 * parentPane.getChildren().add(componentArea.fxObject());
 * }</pre>
 */
public final class EditableComponentFieldArea extends EditableFieldAreaBlueprint<EntityFacade, KLComponentControl>
        implements KlAreaForEntityFacade<KLComponentControl> {

    /**
     * Constructor for restoring from preferences.
     */
    public EditableComponentFieldArea(KometPreferences preferences) {
        super(preferences, new KLComponentControl());
    }

    /**
     * Constructor for creating a new area.
     */
    public EditableComponentFieldArea(KlPreferencesFactory preferencesFactory, KlAreaForEntityFacade.Factory<?,?> areaFactory) {
        super(preferencesFactory, areaFactory, new KLComponentControl());
    }

    @Override
    protected void createControl() {
        setFxPeer(new KLComponentControl());
    }

    @Override
    protected Property<EntityFacade> getControlValueProperty() {
        return getFxPeer().entityProperty();
    }

    @Override
    protected void bindControlToEditable(ObservableField.Editable<EntityFacade> editable) {
        getFxPeer()
                .entityProperty()
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
            getFxPeer().entityProperty().unbindBidirectional(getEditable().editableValueProperty());
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

    public static EditableComponentFieldArea restore(KometPreferences preferences) {
        return EditableComponentFieldArea.factory().restore(preferences);
    }

    public static EditableComponentFieldArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
        return EditableComponentFieldArea.factory().create(preferencesFactory, areaGridSettings);
    }

    public static EditableComponentFieldArea create(KlPreferencesFactory preferencesFactory) {
        return EditableComponentFieldArea.factory().create(preferencesFactory);
    }

    public static class Factory implements KlAreaForEntityFacade.Factory {

        public Factory() {}

        @Override
        public EditableComponentFieldArea restore(KometPreferences preferences) {
            return new EditableComponentFieldArea(preferences);
        }

        @Override
        public EditableComponentFieldArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            EditableComponentFieldArea area = new EditableComponentFieldArea(preferencesFactory, this);
            area.setAreaLayout(areaGridSettings);
            return area;
        }

        @Override
        public EditableComponentFieldArea create(KlPreferencesFactory preferencesFactory) {
            EditableComponentFieldArea area = new EditableComponentFieldArea(preferencesFactory, this);
            area.setAreaLayout(defaultAreaGridSettings());
            return area;
        }
    }
}