package dev.ikm.komet.kleditorapp.view;

import dev.ikm.komet.kleditorapp.view.control.EditorWindowBaseControl;
import javafx.beans.property.Property;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.util.Subscription;

import java.util.Objects;
import java.util.function.Function;

public abstract class ControlBasePropertiesPane<T extends EditorWindowBaseControl> extends Region {
    protected final BorderPane mainContainer = new BorderPane();

    private final HBox bottomContainer = new HBox();
    private final Button deleteButton = new Button();

    protected T currentlyShownControl;
    protected T previouslyShownControl;

    public ControlBasePropertiesPane(boolean isDeletable) {
        // Delete button
        if (isDeletable) {
            deleteButton.setText("DELETE");
            deleteButton.setOnAction(this::onDelete);
            deleteButton.getStyleClass().add("dark");

            bottomContainer.getChildren().add(deleteButton);
            mainContainer.setBottom(bottomContainer);
        }

        getChildren().add(mainContainer);

        // CSS
        bottomContainer.getStyleClass().add("bottom-container");
    }

    public static <T, U> Subscription bindBidirectionalWithConverter(
            Property<T> property1,
            Property<U> property2,
            Function<U, T> converter1to2,
            Function<T, U> converter2to1) {

        Subscription subscription = property1.subscribe(val -> {
            U converted = converter2to1.apply(val);
            if (!Objects.equals(property2.getValue(), converted)) {
                property2.setValue(converted);
            }
        });

        return subscription.and(property2.subscribe(val -> {
            T converted = converter1to2.apply(val);
            if (!Objects.equals(property1.getValue(), converted)) {
                property1.setValue(converted);
            }
        }));
    }

    private void onDelete(ActionEvent event) {
        currentlyShownControl.delete();
    }

    /**
     * Initializes the current properties panel using the passed in Control.
     *
     * @param control the control to initialize the properties panel to.
     */
    public final void initControl(T control){
        previouslyShownControl = currentlyShownControl;
        currentlyShownControl = control;
        doInit(control);
    }

    protected abstract void doInit(T control);

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