package dev.ikm.komet.kleditorapp.view;

import dev.ikm.komet.kleditorapp.KLEditorSession;
import dev.ikm.komet.layout.editor.model.EditorModelBase;
import javafx.beans.property.Property;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.util.Subscription;

import java.util.Objects;
import java.util.function.Function;

public abstract class ControlBasePropertiesPane<T extends EditorModelBase> extends Region {
    protected final BorderPane mainContainer = new BorderPane();

    /**
     * Scrolls the pane's properties when they don't fit the pane's height, so that the DELETE
     * button — which sits outside it, at the bottom of {@link #mainContainer} — stays visible
     * however tall the properties grow.
     */
    private final ScrollPane contentScrollPane = new ScrollPane();

    private final HBox bottomContainer = new HBox();
    private final Button deleteButton = new Button();

    protected T currentlyShownModel;
    protected T previouslyShownModel;

    public ControlBasePropertiesPane(boolean isDeletable) {
        contentScrollPane.setFitToWidth(true);
        mainContainer.setCenter(contentScrollPane);

        // ScrollPane's skin caches its viewport as a bitmap, which drops LCD subpixel
        // antialiasing and makes the properties' text render differently. Uncache it so the text
        // renders the same as it did without the ScrollPane.
        contentScrollPane.skinProperty().subscribe(skin -> {
            if (skin != null) {
                Node viewport = contentScrollPane.lookup(".viewport");
                if (viewport != null) {
                    viewport.setCache(false);
                }
            }
        });

        // Delete button
        if (isDeletable) {
            deleteButton.setText("DELETE");
            deleteButton.setOnAction(this::onDelete);
            deleteButton.getStyleClass().add("dark");

            bottomContainer.getChildren().add(deleteButton);
            mainContainer.setBottom(bottomContainer);
        }

        getChildren().add(mainContainer);

        KLEditorSession.getInstance().sessionStateProperty().subscribe(() -> {
           if (KLEditorSession.getInstance().getSessionState() == KLEditorSession.SessionState.ENDING) {
               onSessionEnding();
           }
        });

        // CSS
        contentScrollPane.getStyleClass().add("content-scroll-pane");
        bottomContainer.getStyleClass().add("bottom-container");
    }

    /**
     * Sets the properties shown by this pane. The content scrolls when it is taller than the pane.
     *
     * @param content the pane's properties
     */
    protected void setContent(Node content) {
        contentScrollPane.setContent(content);
    }

    protected void onSessionEnding() { }

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
        currentlyShownModel.delete();
    }

    /**
     * Initializes the current properties panel using the passed in Control.
     *
     * @param control the control to initialize the properties panel to.
     */
    public final void initControl(T control){
        previouslyShownModel = currentlyShownModel;
        currentlyShownModel = control;
        doInit();
    }

    protected abstract void doInit();

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