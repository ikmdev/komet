package dev.ikm.komet.layout_engine.component.area;

import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.orchestration.OrchestrationService;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.SupplementalAreaBlueprint;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.service.PluggableService;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.stage.Stage;
import javafx.util.Subscription;

import java.util.concurrent.atomic.AtomicReference;

public class MenuArea extends SupplementalAreaBlueprint {

    AtomicReference<Subscription> transientSubscriptions = new AtomicReference<>(Subscription.EMPTY);

    {
        subscribeToSceneSet();
    }

    private MenuArea(KometPreferences preferences) {
        super(preferences);
    }

    private MenuArea(KlPreferencesFactory preferencesFactory, KlArea.Factory gadgetFactory) {
        super(preferencesFactory, gadgetFactory);
    }

    private void generateWindowMenu() {
        if (fxObject().getScene().getWindow() instanceof Stage stage) {
            MenuBar menuBar = new MenuBar();
            PluggableService.first(OrchestrationService.class).addMenuItems(stage, menuBar);
            this.fxObject().setTop(menuBar);
            transientSubscriptions.getAndSet(Subscription.EMPTY).unsubscribe();
        }

    }

    // Call this to set up the subscription
    private void subscribeToSceneSet() {
        if (fxObject().sceneProperty().get() instanceof Scene) {
            onSceneChanged();
        } else {
            transientSubscriptions.get().and(fxObject().sceneProperty().subscribe(this::onSceneChanged));
        }
    }

    private void onSceneChanged() {
        switch (fxObject().sceneProperty().get()) {
            case Scene scene -> {
                if (scene.getWindow() instanceof Stage) {
                    generateWindowMenu();
                } else {
                    transientSubscriptions.getAndSet(scene.windowProperty().subscribe(this::generateWindowMenu)).unsubscribe();
                }
            }
            //case null -> {}
        }
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


    public static MenuArea.Factory factory() {
        return new MenuArea.Factory();
    }

    public static MenuArea restore(KometPreferences preferences) {
        return factory().restore(preferences);
    }

    public static MenuArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
        return factory().create(preferencesFactory, areaGridSettings);
    }

    public static MenuArea create(KlPreferencesFactory preferencesFactory) {
        return factory().create(preferencesFactory);
    }

    public static class Factory
            implements SupplementalAreaBlueprint.Factory<MenuArea> {

        @Override
        public MenuArea restore(KometPreferences preferences) {
            MenuArea menuArea = new MenuArea(preferences);
            return menuArea;
        }

        @Override
        public MenuArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            MenuArea area = new MenuArea(preferencesFactory, areaGridSettings.makeAreaFactory());
            area.setAreaLayout(areaGridSettings);
            return area;
        }
    }
}