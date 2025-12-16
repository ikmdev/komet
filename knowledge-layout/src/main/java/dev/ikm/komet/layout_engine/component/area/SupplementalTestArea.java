package dev.ikm.komet.layout_engine.component.area;

import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.SupplementalAreaBlueprint;
import dev.ikm.komet.layout_engine.component.menu.ViewContextMenuButtonArea;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;

public class SupplementalTestArea extends SupplementalAreaBlueprint {

    {
        ToolBar windowToolBar = new ToolBar();
        windowToolBar.setOrientation(Orientation.VERTICAL);
        Label label = new Label("FxWindow containing Component Versions List");
        label.setStyle("-fx-rotate: -90;");
        windowToolBar.getItems().add(new Group(label));
        this.fxObject().setLeft(windowToolBar);
    }

    public SupplementalTestArea(KometPreferences preferences) {
        super(preferences);
    }

    public SupplementalTestArea(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory);
        ViewContextMenuButtonArea viewContextMenuButtonArea =
                ViewContextMenuButtonArea.factory().create(this.childPreferencesFactory(ViewContextMenuButtonArea.class));
        this.addChild(viewContextMenuButtonArea);
    }

    public static Factory factory() {
        return new Factory();
    }

    public static SupplementalTestArea restore(KometPreferences preferences) {
        return factory().restore(preferences);
    }

    public static SupplementalTestArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
        return factory().create(preferencesFactory, areaGridSettings);
    }

    public static SupplementalTestArea create(KlPreferencesFactory preferencesFactory) {
        return factory().create(preferencesFactory);
    }

    @Override
    protected void subAreaRestoreFromPreferencesOrDefault() {

    }

    @Override
    protected void subAreaRevert() {

    }

    @Override
    protected void subAreaSave() {

    }

    @Override
    public void knowledgeLayoutUnbind() {
        // Nothing to unbind.
    }

    @Override
    public void knowledgeLayoutBind() {
        Platform.runLater(() -> this.lifecycleState.set(LifecycleState.BOUND));
    }

    public static class Factory
            implements SupplementalAreaBlueprint.Factory<SupplementalTestArea> {

        @Override
        public SupplementalTestArea restore(KometPreferences preferences) {
            SupplementalTestArea supplementalTestArea = new SupplementalTestArea(preferences);
            return supplementalTestArea;
        }

        @Override
        public SupplementalTestArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            SupplementalTestArea area = new SupplementalTestArea(preferencesFactory, this);
            area.setAreaLayout(areaGridSettings.with(this.getClass()));
            return area;
        }
    }

}
