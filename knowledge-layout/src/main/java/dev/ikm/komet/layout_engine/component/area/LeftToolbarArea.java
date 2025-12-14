package dev.ikm.komet.layout_engine.component.area;

import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.SupplementalAreaBlueprint;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;

public class LeftToolbarArea extends SupplementalAreaBlueprint {

    {
        ToolBar windowToolBar = new ToolBar();
        windowToolBar.setOrientation(Orientation.VERTICAL);
        Label label = new Label("FxWindow containing Component Versions List");
        label.setStyle("-fx-rotate: -90;");
        windowToolBar.getItems().add(new Group(label));
        fxObject().setLeft(windowToolBar);
    }

    private LeftToolbarArea(KometPreferences preferences) {
        super(preferences);
    }

    private LeftToolbarArea(KlPreferencesFactory preferencesFactory, KlArea.Factory gadgetFactory) {
        super(preferencesFactory, gadgetFactory);
    }

    @Override
    protected void subAreaRevert() {

    }

    @Override
    protected void subAreaSave() {

    }

    @Override
    protected void subAreaRestoreFromPreferencesOrDefault() {
        // Nothing to restore.
    }

    @Override
    public void knowledgeLayoutUnbind() {
        // Nothing to unbind.
    }

    @Override
    public void knowledgeLayoutBind() {
        Platform.runLater(() -> this.lifecycleState.set(LifecycleState.BOUND));
    }


    public static LeftToolbarArea.Factory factory() {
        return new LeftToolbarArea.Factory();
    }

    public static LeftToolbarArea restore(KometPreferences preferences) {
        return factory().restore(preferences);
    }

    public static LeftToolbarArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
        return factory().create(preferencesFactory, areaGridSettings);
    }

    public static LeftToolbarArea create(KlPreferencesFactory preferencesFactory) {
        return factory().create(preferencesFactory);
    }

    public static class Factory
            implements SupplementalAreaBlueprint.Factory<LeftToolbarArea> {

        @Override
        public LeftToolbarArea restore(KometPreferences preferences) {
            LeftToolbarArea leftToolbarArea = new LeftToolbarArea(preferences);
            return leftToolbarArea;
        }

        @Override
        public LeftToolbarArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            LeftToolbarArea area = new LeftToolbarArea(preferencesFactory, areaGridSettings.makeAreaFactory());
            area.setAreaLayout(areaGridSettings);
            return area;
        }
    }
}
