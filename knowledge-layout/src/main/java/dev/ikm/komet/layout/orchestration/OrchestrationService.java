package dev.ikm.komet.layout.orchestration;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.MenuBar;
import javafx.stage.Stage;

/**
 * The OrchestrationService interface represents an orchestrator that manages the lifecycle and execution flow
 * of an application.
 */
public interface OrchestrationService {
    /**
     * Returns the primary stage of the application.
     *
     * @return the primary stage
     */
    Stage primaryStage();
    /**
     * Retrieves the lifecycle property of the OrchestrationService.
     *
     * @return the lifecycle property of the OrchestrationService
     */
    SimpleObjectProperty<Lifecycle> lifecycleProperty();

    /**
     * Adds menu items to the specified menu bar based on the provided MenuService
     * implementation, and then appends a Window menu from the WindowMenuService.
     *
     * @param stage the stage this menu bar will be attached to.
     * @param menuBar the menu bar to add the menu items to
     */
    void addMenuItems(Stage stage, MenuBar menuBar);
}
