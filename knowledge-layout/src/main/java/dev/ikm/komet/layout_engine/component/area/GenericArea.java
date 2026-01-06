package dev.ikm.komet.layout_engine.component.area;

import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.area.KlAreaForObject;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.FeatureAreaBlueprint;
import dev.ikm.komet.layout_engine.component.menu.LayoutContextMenu;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;


public final class GenericArea extends FeatureAreaBlueprint<Object, Feature<Object>, ToolBar>
        implements KlAreaForObject<ToolBar> {

    final Label fieldMeaning = new Label( "Field Meaning");
    final Label fieldValue = new Label( "Field Value");
    final Tooltip fieldPurpose = new Tooltip();
    {
        fieldPurpose.setText("Field Purpose");
        fieldMeaning.setTooltip(fieldPurpose);
        fieldValue.setTooltip(fieldPurpose);
        fxObject().getItems().addAll(fieldMeaning, fieldValue);
        fieldMeaning.setContextMenu(LayoutContextMenu.makeContextMenu(this));
        fieldValue.setContextMenu(LayoutContextMenu.makeContextMenu(this));
        fxObject().setContextMenu(LayoutContextMenu.makeContextMenu(this));
    }

    private GenericArea(KometPreferences preferences) {
        super(preferences, new ToolBar());
    }

    private GenericArea(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory, new ToolBar());
    }

    @Override
    protected void subFeatureAreaBlueprintRestoreFromPreferencesOrDefault() {
        // Since this is a "leaf node", we don't need to worry about propagating the context change to children areas."
        getFeature().ifPresent(feature -> setDisplayValues(feature));
    }

    @Override
    protected void subAreaRevert() {

    }

    @Override
    protected void subAreaSave() {

    }

    @Override
    public void contextChanged() {
        getFeature().ifPresent(feature -> setDisplayValues(feature));
    }

    @Override
    protected void featureChanged(Feature<Object> oldFeature, Feature<Object> newFeature) {
        if (newFeature != null) {
            setDisplayValues(newFeature);
        } else {
            fieldMeaning.setText("Null field, null meaning");
            fieldPurpose.setText("Null field, null purpose");
            fieldValue.setText("Null field, null value");
        }
    }

    private void setDisplayValues(Feature newValue) {
        fieldMeaning.setText(calculatorForContext().getDescriptionTextOrNid(newValue.definition(calculatorForContext()).meaningNid()) + ": ");
        fieldPurpose.setText(calculatorForContext().getDescriptionTextOrNid(newValue.definition(calculatorForContext()).purposeNid()) + ": ");
        switch (newValue.value()) {
            case Long longValue -> fieldValue.setText(DateTimeUtil.format(longValue));
            case Number number -> fieldValue.setText(number.toString());
            case EntityFacade entity -> fieldValue.setText(calculatorForContext().getDescriptionTextOrNid(entity.nid()));
            case String string -> fieldValue.setText(string);
            case Object object -> fieldValue.setText(object.toString());
            case null -> fieldValue.setText("Null");
        }
    }

    public static Factory factory() {
        return new Factory();
    }

    public static class Factory implements KlAreaForObject.Factory {

        public Factory() {}

        @Override
        public String productName() {
            return "Standard Generic Field";
        }

        @Override
        public GenericArea restore(KometPreferences preferences) {
            GenericArea genericArea = new GenericArea(preferences);
            return genericArea;
        }

        @Override
        public KlArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            GenericArea area = new GenericArea(preferencesFactory, this);
            area.setAreaLayout(areaGridSettings);
            return area;
        }
    }

    public static BlueFactory blueFactory() {
        return new BlueFactory();
    }

    public static class BlueFactory implements KlAreaForObject.Factory {
        //TODO: This won't restore properly. Need a subclass with different constructor, or saving background color in preferences.
        //Maybe restore needs to restore via the factory...  And call the restore method on the factory, not the constructor!
        public BlueFactory() {}

        @Override
        public String productName() {
            return "Blue Generic Field";
        }

        public GenericArea restore(KometPreferences preferences) {
            GenericArea area = new GenericArea(preferences);
            area.fxObject().setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE,
                    CornerRadii.EMPTY, Insets.EMPTY)));
            return area;
        }

        @Override
        public KlArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            GenericArea area = new GenericArea(preferencesFactory, this);
            area.setAreaLayout(areaGridSettings);
            area.fxObject().setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE,
                    CornerRadii.EMPTY, Insets.EMPTY)));
            return area;
        }
    }
}
