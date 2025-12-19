
package dev.ikm.komet.kview.klauthoring.readonly.componentfield;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.komet.framework.observable.FeatureWrapper;
import dev.ikm.komet.framework.observable.ObservableEntityHandle;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentControl;
import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.area.KlAreaForEntityFacade;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.ReadOnlyFieldAreaBlueprint;
import dev.ikm.komet.layout_engine.component.menu.LayoutContextMenu;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.StackPane;

/**
 * An editable area for KlAreaForEntityFacade fields.
 * <p>
 * This area uses {@link KlAreaForEntityFacade} and handles all the binding plumbing
 * via its parent class {@link ReadOnlyFieldAreaBlueprint}.
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * // Create the area
 * ReadOnlyEntityFacadeFieldArea readOnlyEntityFacadeFieldArea = ReadOnlyEntityFacadeFieldArea.create(preferencesFactory);
 *
 * // Get an read-only field
 * ObservableField<EnitityFacade> obsField = ...;
 *
 *
 * // Add to your layout
 * parentPane.getChildren().add(readOnlyEntityFacadeFieldArea.fxObject());
 * }</pre>
 */
public final class ReadOnlyEntityFacadeFieldArea extends ReadOnlyFieldAreaBlueprint<EntityFacade, StackPane>
        implements KlAreaForEntityFacade<StackPane> {

    private KLReadOnlyComponentControl componentControl;
    private ObjectProperty<EntityFacade> entityProperty;
    {
        fxObject().setOnContextMenuRequested(event ->
                LayoutContextMenu.makeContextMenu(this).show(fxObject(), event.getScreenX(), event.getScreenY()));
    }

    /**
     * Constructor for restoring from preferences.
     */
    public ReadOnlyEntityFacadeFieldArea(KometPreferences preferences) {
        super(preferences, new StackPane());
    }

    /**
     * Constructor for creating a new area.
     */
    public ReadOnlyEntityFacadeFieldArea(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory, new StackPane());
    }

    @Override
    protected void createControl() {
        componentControl = new KLReadOnlyComponentControl();
        // create custom binding
        entityProperty = new SimpleObjectProperty<>();

        fxObject().getChildren().add(componentControl);
    }

    @Override
    protected Property<EntityFacade> getControlValueProperty() {
        return entityProperty;
    }
    @Override
    protected void featureChanged(Feature<EntityFacade> oldFeature, Feature<EntityFacade> newFeature) {
        if (newFeature != null) {
            setDisplayValues(newFeature);
        }
    }
    /**
     * Sets the display values of the read-only Component field area and title (based on meaning).
     * @param newValue the new Feature<ObservableChronology> to display
     */
    private void setDisplayValues(Feature<EntityFacade> newValue) {
        if (newValue != null && newValue.value() != null) {
            String purpose = calculatorForContext().getDescriptionTextOrNid(newValue.definition(calculatorForContext()).purposeNid());

            int nid1 = ((EntityFacade)((ReadOnlyObjectProperty<?>)((FeatureWrapper<?>) newValue)
                    .valueProperty()
                    .getValue())
                    .getValue()).nid();
//            int nid2 = ((EntityFacade)((ReadOnlyProperty<Feature<EntityFacade>>)((FeatureWrapper<?>) newValue).valueProperty().getValue()).getValue()).nid();
//            int nid3 = ((ReadOnlyProperty<Feature<EntityFacade>>)newValue.featureProperty()).getValue().value().nid();
//            int nid4 = ((ReadOnlyObjectProperty<EntityFacade>)newValue.featureProperty().getValue().value()).get().nid();
//            int nid5 = newValue.value().nid();
            int nid = nid1;
            ObservableEntityHandle.get(nid).entity().ifPresent((observableEntity -> {
                PublicId pid = observableEntity.publicId();
                ComponentItem componentItem = new ComponentItem(purpose, Identicon.generateIdenticonImage(pid), nid);
                componentControl.valueProperty().set(componentItem);
            }));
        }
    }
    @Override
    protected void bindControlToObservableField(ObservableField<EntityFacade> observableField) {
        ObjectBinding<EntityFacade> customBinding = Bindings.createObjectBinding(() -> {
            ComponentItem componentItem = componentControl.valueProperty().get();
            if (componentItem!=null) {
                return EntityHandle.get(componentItem.getNid()).entity().orElse(null);
            } else {
                return null;
            }
        });
        // listen when the component item changes, and update the observable field
        addObservableSubscription(
                customBinding.subscribe((oldVal, newVal) -> {
                    if (newVal != null && !newVal.equals(oldVal)) {
                        if (oldVal != null && oldVal.nid() == newVal.nid()) {
                            // no change
                            return;
                        }
                        observableField.editableValueProperty().set(newVal);
                    }
                })
        );
        addObservableSubscription(
            observableField.valueProperty().subscribe((oldVal, newVal) -> {
                if (newVal != null && !newVal.equals(oldVal)) {
                    if (oldVal != null && oldVal.nid() == newVal.nid()) {
                        // no change
                        return;
                    }
                    componentControl.valueProperty().set(
                            new ComponentItem(
                                    calculatorForContext().getDescriptionTextOrNid(newVal.nid()),
                                    Identicon.generateIdenticonImage(newVal.publicId()),
                                    newVal.nid()));
                }
            })
        );
    }

    @Override
    protected void unbindControlFromObservableField() {
        if (getEditable() != null) {
            entityProperty.unbind();
        }
    }

    @Override
    protected void updateControlTitle(String title) {
        componentControl.setTitle(title);
    }

    // --- Factory Methods ---

    public static Factory factory() {
        return new Factory();
    }

    public static ReadOnlyEntityFacadeFieldArea restore(KometPreferences preferences) {
        return ReadOnlyEntityFacadeFieldArea.factory().restore(preferences);
    }

    public static ReadOnlyEntityFacadeFieldArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
        return ReadOnlyEntityFacadeFieldArea.factory().create(preferencesFactory, areaGridSettings);
    }

    public static ReadOnlyEntityFacadeFieldArea create(KlPreferencesFactory preferencesFactory) {
        return ReadOnlyEntityFacadeFieldArea.factory().create(preferencesFactory);
    }

    @Override
    protected void subAreaRevert() {

    }

    public static class Factory implements KlAreaForEntityFacade.Factory<StackPane, ReadOnlyEntityFacadeFieldArea> {

        public Factory() {}

        @Override
        public ReadOnlyEntityFacadeFieldArea restore(KometPreferences preferences) {
            return new ReadOnlyEntityFacadeFieldArea(preferences);
        }

        @Override
        public ReadOnlyEntityFacadeFieldArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            ReadOnlyEntityFacadeFieldArea area = new ReadOnlyEntityFacadeFieldArea(preferencesFactory, this);
            area.setAreaLayout(areaGridSettings);
            return area;
        }

        @Override
        public ReadOnlyEntityFacadeFieldArea create(KlPreferencesFactory preferencesFactory) {
            ReadOnlyEntityFacadeFieldArea area = new ReadOnlyEntityFacadeFieldArea(preferencesFactory, this);
            area.setAreaLayout(defaultAreaGridSettings());
            return area;
        }
    }
}