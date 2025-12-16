package dev.ikm.komet.layout_engine.component.menu;

import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.KlParent;
import dev.ikm.komet.layout.KlPeerable;
import dev.ikm.komet.layout.LayoutKey;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.area.KlAreaForFeature;
import dev.ikm.komet.layout.area.KlFeaturePropertyForArea;
import dev.ikm.komet.layout_engine.layout.AreaLayoutPropertySheet;
import dev.ikm.tinkar.common.service.PluggableService;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;
import java.util.prefs.BackingStoreException;

import static javafx.stage.StageStyle.UTILITY;

public class LayoutContextMenu {

    private static final Logger LOG = LoggerFactory.getLogger(LayoutContextMenu.class);


    public static ContextMenu makeContextMenu(KlAreaForFeature<?, ?, ?> areaForChange) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editGridLayout = new MenuItem("Edit grid layout");
        editGridLayout.setOnAction(event -> {
            AreaLayoutPropertySheet areaLayoutPropertySheet = new AreaLayoutPropertySheet(areaForChange);
            Stage stage = new Stage(UTILITY);
            areaForChange.getFeature().ifPresentOrElse(feature -> {
                stage.setTitle("Edit grid layout for: " +
                        areaForChange.calculatorForContext().getDescriptionTextOrNid(feature.definition(areaForChange.calculatorForContext()).meaningNid()));
            }, () -> {
                stage.setTitle("Edit grid layout for: " + areaForChange.regularNames().getAny());
            });

            Scene scene = new Scene(new VBox(areaLayoutPropertySheet.getPropertySheet()));
            stage.setScene(scene);
            stage.show();
        });
        Menu changeFieldFactory = new Menu("Change field factory");

        ServiceLoader<KlArea.Factory> pluggableServices = PluggableService.load(KlArea.Factory.class);
        for (KlArea.Factory factory : pluggableServices) {
            MenuItem changeFieldFactoryItem = new MenuItem(factory.factoryName());
            changeFieldFactoryItem.setOnAction(event -> {
                LOG.info("Change field factory: " + factory.factoryName());
                try {
                    final KlParent<?> parentOfAreaForChange = (KlParent<?>) KlPeerable.getKlPeer(areaForChange.fxObject().getParent());
                    final AreaGridSettings areaSettingsWithNewFactory = areaForChange.getAreaLayout().withAreaFactoryClassName(factory.getClass().getName());
                    final LayoutKey.ForArea layoutKey = areaForChange.getLayoutKeyForArea();
                    areaForChange.getMasterLayout().layoutOverrides()
                            .addOverride(layoutKey, areaSettingsWithNewFactory);
                    parentOfAreaForChange.gridPaneForChildren().getChildren().remove(areaForChange.fxObject());
                    areaForChange.unsubscribeFromContext();
                    LOG.info("Removing node: " + areaForChange.preferences().absolutePath());
                    areaForChange.preferences().removeNode();
                    areaForChange.preferences().flush();

                    KlArea newArea = areaSettingsWithNewFactory.makeAndAddToParent(parentOfAreaForChange);
                    newArea.setId(areaForChange.getId());
                    if (newArea instanceof KlFeaturePropertyForArea newAreaWithFeature) {
                        newAreaWithFeature.setFeatureProperty(areaForChange.getFeatureProperty());
                    }
                    newArea.subscribeToContext();
                    LOG.info("Added node: " + newArea.preferences().absolutePath());
                } catch (BackingStoreException e) {
                    throw new RuntimeException(e);
                }
            });
            changeFieldFactory.getItems().add(changeFieldFactoryItem);
        }

        contextMenu.getItems().addAll(editGridLayout, changeFieldFactory);
        return contextMenu;
    }
}
