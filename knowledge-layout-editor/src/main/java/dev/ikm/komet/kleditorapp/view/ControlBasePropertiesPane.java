package dev.ikm.komet.kleditorapp.view;

import dev.ikm.komet.kleditorapp.view.control.EditorWindowBaseControl;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

public abstract class ControlBasePropertiesPane<T extends EditorWindowBaseControl> extends Region {
    protected final BorderPane mainContainer = new BorderPane();

    private final HBox bottomContainer = new HBox();
    private final Button deleteButton = new Button();

    protected T currentlyShownControl;

    public ControlBasePropertiesPane() {
        bottomContainer.getChildren().add(deleteButton);
        mainContainer.setBottom(bottomContainer);

        // Delete button
        deleteButton.setText("DELETE");
        deleteButton.setOnAction(this::onDelete);

        getChildren().add(mainContainer);

        // CSS
        bottomContainer.getStyleClass().add("bottom-container");
    }

    private void onDelete(ActionEvent event) {
        currentlyShownControl.delete();
    }

    /**
     * Inits the current properties panel using the passed in Control.
     * Overriding methods in subclasses must call super at the end.
     *
     * @param control the control to initialize the properties panel to.
     */
    public void initControl(T control){
        currentlyShownControl = control;
    }

    @Override
    protected void layoutChildren() {
        double leftInsets = snappedLeftInset();
        double rightInsets = snappedRightInset();
        double topInsets = snappedTopInset();
        double bottomInsets = snappedBottomInset();
        double width = getWidth();
        double height = getHeight();

        mainContainer.resizeRelocate(leftInsets, topInsets,
                width - leftInsets - rightInsets, height - topInsets - bottomInsets);
    }
}