package dev.ikm.komet.kleditorapp.view;

import dev.ikm.komet.kleditorapp.view.control.EditorWindowBaseControl;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;

public abstract class ControlBasePropertiesPane<T extends EditorWindowBaseControl> extends Region {
    private final BorderPane mainContainer = new BorderPane();
    private final Button deleteButton = new Button();

    public ControlBasePropertiesPane() {
        mainContainer.setBottom(deleteButton);
        deleteButton.setOnAction(this::onDelete);
    }

    protected abstract void onDelete(ActionEvent event) ;

    public abstract void initControl(T control);
}