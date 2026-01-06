package dev.ikm.komet.kview.klauthoring.editable.componentsetfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.kview.controls.KLComponentCollectionControl;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.area.KlAreaForIntIdSet;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.EditableFieldAreaBlueprint;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.id.IntIdSet;
import javafx.beans.property.Property;

/**
 * An editable area for Float fields.
 * <p>
 * This area uses {@link KLComponentCollectionControl} and handles all the binding plumbing
 * via its parent class {@link EditableFieldAreaBlueprint}.
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * // Create the area
 * EditableComponentSetFieldArea componentSetArea = EditableComponentSetFieldArea.create(preferencesFactory);
 *
 * // Get an editable field from ObservableComposer
 * ObservableField.Editable<IntIdSet> editable = ...;
 *
 * // Connect the area to the editable
 * componentSetArea.setEditable(editable);
 *
 * // Add to your layout
 * parentPane.getChildren().add(componentSetArea.fxObject());
 * }</pre>
 */
public final class EditableComponentSetFieldArea extends EditableFieldAreaBlueprint<IntIdSet, KLComponentCollectionControl<IntIdSet>>
        implements KlAreaForIntIdSet<KLComponentCollectionControl<IntIdSet>> {

    /**
     * Constructor for restoring from preferences.
     */
    public EditableComponentSetFieldArea(KometPreferences preferences) {
        super(preferences, new KLComponentCollectionControl<>());
    }

    /**
     * Constructor for creating a new area.
     */
    public EditableComponentSetFieldArea(KlPreferencesFactory preferencesFactory, Factory areaFactory) {
        super(preferencesFactory, areaFactory, new KLComponentCollectionControl<>());
    }

    @Override
    protected void createControl() {
        setFxPeer(new KLComponentCollectionControl<>());
    }

    @Override
    protected Property<IntIdSet> getControlValueProperty() {
        return getFxPeer().valueProperty();
    }

    @Override
    protected void bindControlToEditable(ObservableField.Editable<IntIdSet> editable) {
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

    public static EditableComponentSetFieldArea restore(KometPreferences preferences) {
        return EditableComponentSetFieldArea.factory().restore(preferences);
    }

    public static EditableComponentSetFieldArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
        return EditableComponentSetFieldArea.factory().create(preferencesFactory, areaGridSettings);
    }

    public static EditableComponentSetFieldArea create(KlPreferencesFactory preferencesFactory) {
        return EditableComponentSetFieldArea.factory().create(preferencesFactory);
    }

    public static class Factory implements KlAreaForIntIdSet.Factory<KLComponentCollectionControl<IntIdSet>> {

        public Factory() {}

        @Override
        public EditableComponentSetFieldArea restore(KometPreferences preferences) {
            return new EditableComponentSetFieldArea(preferences);
        }

        @Override
        public EditableComponentSetFieldArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            EditableComponentSetFieldArea area = new EditableComponentSetFieldArea(preferencesFactory, this);
            area.setAreaLayout(areaGridSettings);
            return area;
        }

        @Override
        public EditableComponentSetFieldArea create(KlPreferencesFactory preferencesFactory) {
            EditableComponentSetFieldArea area = new EditableComponentSetFieldArea(preferencesFactory, this);
            area.setAreaLayout(defaultAreaGridSettings());
            return area;
        }
    }
}