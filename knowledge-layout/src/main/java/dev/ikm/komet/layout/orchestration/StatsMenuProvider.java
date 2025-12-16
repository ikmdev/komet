package dev.ikm.komet.layout.orchestration;

import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.entity.export.ExportEntitiesToProtobufFile;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.eclipse.collections.api.multimap.ImmutableMultimap;
import org.eclipse.collections.api.multimap.MutableMultimap;
import org.eclipse.collections.impl.factory.Multimaps;

import java.io.File;

/**
 * The StatsMenuProvider class implements the MenuService interface to provide statistical menu items.
 */
public class StatsMenuProvider implements MenuService {
    /**
     * Retrieves the menu items for a specific window.
     *
     * @param window the window for which the menu items are retrieved
     * @return an ImmutableMultimap containing the menu items for the window
     */
    @Override
    public ImmutableMultimap<String, MenuItem> getMenuItems(Window window) {

        MutableMultimap<String, MenuItem> menuItems = Multimaps.mutable.list.empty();

        MenuItem countEntitiesMenuItem = new MenuItem("Count Entities");
        countEntitiesMenuItem.setOnAction(event -> {
            TinkExecutor.threadPool().submit(new CountEntities());
        });
        menuItems.put("Stats", countEntitiesMenuItem);

        MenuItem countConceptsMenuItem = new MenuItem("Count Concepts");
        countConceptsMenuItem.setOnAction(event -> {
            TinkExecutor.threadPool().submit(new CountConcepts());
        });
        menuItems.put("Stats", countConceptsMenuItem);

        MenuItem countSemanticsMenuItem = new MenuItem("Count Semantics");
        countSemanticsMenuItem.setOnAction(event -> {
            TinkExecutor.threadPool().submit(new CountSemantics());
        });
        menuItems.put("Stats", countSemanticsMenuItem);

        MenuItem exportMenuItem = new MenuItem("Export All");
        exportMenuItem.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choose export file");
            // Optional: suggest a default file name
            chooser.setInitialFileName("export.protobuf");
            // Optional: limit to protobuf or all files
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Protobuf Files (*.protobuf, *.pb)", "*.protobuf", "*.pb"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
            );

            File exportFile = chooser.showSaveDialog(window);
            TinkExecutor.threadPool().submit(new ExportEntitiesToProtobufFile(exportFile));
        });
        menuItems.put("Stats", exportMenuItem);




        return menuItems.toImmutable();
    }
}
