package dev.ikm.komet.kview.klauthoring.editable.componentlistfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.kview.controls.KLComponentCollectionControl;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.area.KlAreaForIntIdList;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.EditableFieldAreaBlueprint;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.id.IntIdList;
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
 * EditableComponentListFieldArea componentListArea = EditableComponentListFieldArea.create(preferencesFactory);
 *
 * // Get an editable field from ObservableComposer
 * ObservableField.Editable<IntIdList> editable = ...;
 *
 * // Connect the area to the editable
 * componentListArea.setEditable(editable);
 *
 * // Add to your layout
 * parentPane.getChildren().add(componentListArea.fxObject());
 * }</pre>
 */
public final class EditableComponentListFieldArea extends EditableFieldAreaBlueprint<IntIdList, KLComponentCollectionControl<IntIdList>>
        implements KlAreaForIntIdList<KLComponentCollectionControl<IntIdList>> {

    /**
     * Constructor for restoring from preferences.
     */
    public EditableComponentListFieldArea(KometPreferences preferences) {
        super(preferences, new KLComponentCollectionControl<>());
    }

    /**
     * Constructor for creating a new area.
     */
    public EditableComponentListFieldArea(KlPreferencesFactory preferencesFactory, Factory areaFactory) {
        super(preferencesFactory, areaFactory, new KLComponentCollectionControl<>());
    }

    @Override
    protected void createControl() {
        setFxPeer(new KLComponentCollectionControl<>());
    }

    @Override
    protected Property<IntIdList> getControlValueProperty() {
        return getFxPeer().valueProperty();
    }

    @Override
    protected void bindControlToEditable(ObservableField.Editable<IntIdList> editable) {
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

    public static EditableComponentListFieldArea restore(KometPreferences preferences) {
        return EditableComponentListFieldArea.factory().restore(preferences);
    }

    public static EditableComponentListFieldArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
        return EditableComponentListFieldArea.factory().create(preferencesFactory, areaGridSettings);
    }

    public static EditableComponentListFieldArea create(KlPreferencesFactory preferencesFactory) {
        return EditableComponentListFieldArea.factory().create(preferencesFactory);
    }

    public static class Factory implements KlAreaForIntIdList.Factory<KLComponentCollectionControl<IntIdList>> {

        public Factory() {}

        @Override
        public EditableComponentListFieldArea restore(KometPreferences preferences) {
            return new EditableComponentListFieldArea(preferences);
        }

        @Override
        public EditableComponentListFieldArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            EditableComponentListFieldArea area = new EditableComponentListFieldArea(preferencesFactory, this);
            area.setAreaLayout(areaGridSettings);
            return area;
        }

        @Override
        public EditableComponentListFieldArea create(KlPreferencesFactory preferencesFactory) {
            EditableComponentListFieldArea area = new EditableComponentListFieldArea(preferencesFactory, this);
            area.setAreaLayout(defaultAreaGridSettings());
            return area;
        }
    }
}