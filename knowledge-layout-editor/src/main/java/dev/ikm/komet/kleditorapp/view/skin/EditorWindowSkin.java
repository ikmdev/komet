package dev.ikm.komet.kleditorapp.view.skin;

import dev.ikm.komet.kleditorapp.view.control.EditorWindowControl;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

public class EditorWindowSkin extends SkinBase<EditorWindowControl> {

    /**
     * Width an "Auto" window renders at on the editor canvas. This is an editor-only convenience: the
     * realized window still sizes to its content; here we show a comfortable fixed width to design in.
     */
    private static final double EDITOR_AUTO_WIDTH = 550;

    private final VBox root = new VBox();
    private Label titleLabel;

    public EditorWindowSkin(EditorWindowControl control) {
        super(control);

        root.getStyleClass().add("window");

        // The Window's (view) size is author-controlled via the Window properties pane. Pin the root
        // to the control's preferred size so editing Width/Height resizes the whole window. On the
        // editor canvas an "Auto" width (USE_COMPUTED_SIZE) is shown at a fixed EDITOR_AUTO_WIDTH;
        // height stays content-driven when Auto.
        DoubleBinding editorWidth = Bindings.createDoubleBinding(
                () -> control.getPrefWidth() < 0 ? EDITOR_AUTO_WIDTH : control.getPrefWidth(),
                control.prefWidthProperty());
        root.minWidthProperty().bind(editorWidth);
        root.prefWidthProperty().bind(editorWidth);
        root.maxWidthProperty().bind(editorWidth);
        root.minHeightProperty().bind(control.prefHeightProperty());
        root.prefHeightProperty().bind(control.prefHeightProperty());
        root.maxHeightProperty().bind(control.prefHeightProperty());

        buildUI();

        getChildren().add(root);
    }

    private void buildUI() {
        // Create header
        BorderPane headerPane = createHeader();

        // Create stamp container
        BorderPane stampContainer = createStampContainer();

        // Create section container
        VBox sectionContainer = new VBox();
        VBox.setVgrow(sectionContainer, Priority.ALWAYS);

        Bindings.bindContent(sectionContainer.getChildren(), getSkinnable().getSectionViews());

        // Create add section button
        Button addSectionButton = createAddSectionButton();
        addSectionButton.onActionProperty().bind(getSkinnable().onAddSectionActionProperty());

        // Add all components
        root.getChildren().addAll(headerPane, stampContainer, sectionContainer, addSectionButton);
    }

    private BorderPane createHeader() {
        BorderPane headerPane = new BorderPane();
        headerPane.getStyleClass().add("window-header-container");

        // Title
        StackPane titleContainer = new StackPane();
        titleContainer.getStyleClass().add("title-container");
        titleLabel = new Label();
        titleLabel.textProperty().bind(getSkinnable().titleProperty());
        titleContainer.getChildren().add(titleLabel);
        headerPane.setTop(titleContainer);

        // Content with icons
        HBox contentContainer = new HBox();
        contentContainer.getStyleClass().add("content-container");

        // Coordinate and Timeline icons are control-bar options toggled from the Window properties pane.
        VBox coordinateContainer = createIconContainer("coordinate", "Coordinate", null);
        coordinateContainer.visibleProperty().bind(getSkinnable().coordinateVisibleProperty());
        coordinateContainer.managedProperty().bind(getSkinnable().coordinateVisibleProperty());

        VBox timelineContainer = createIconContainer("timeline", "Timeline", null);
        timelineContainer.visibleProperty().bind(getSkinnable().timelineVisibleProperty());
        timelineContainer.managedProperty().bind(getSkinnable().timelineVisibleProperty());

        // The divider only makes sense when both icons are shown.
        Separator separator = createSeparator();
        separator.visibleProperty().bind(Bindings.and(getSkinnable().coordinateVisibleProperty(), getSkinnable().timelineVisibleProperty()));
        separator.managedProperty().bind(separator.visibleProperty());

        // Add icon containers
        contentContainer.getChildren().addAll(
                coordinateContainer,
                separator,
                timelineContainer,
                createSpacer(),
                createCloseButton()
        );

        headerPane.setCenter(contentContainer);

        return headerPane;
    }

    private VBox createIconContainer(String cssClass, String labelText, String svgPath) {
        VBox container = new VBox();
        container.getStyleClass().add("icon-container");

        if (svgPath != null) {
            // SVG icon
            StackPane svgContainer = new StackPane();
            svgContainer.getStyleClass().add("svg-icon-container");

            SVGPath svg = new SVGPath();
            svg.setContent(svgPath);
            svg.getStyleClass().add("icon");

            svgContainer.getChildren().add(svg);
            container.getChildren().add(svgContainer);
        } else {
            // CSS-based icon
            Region icon = new Region();
            icon.getStyleClass().addAll("icon", cssClass);
            container.getChildren().add(icon);
        }

        Label label = new Label(labelText);
        label.setMinWidth(Control.USE_COMPUTED_SIZE);
        container.getChildren().add(label);

        return container;
    }

    private Separator createSeparator() {
        Separator separator = new Separator(Orientation.VERTICAL);
        HBox.setMargin(separator, new Insets(0, 5, 0, 5));
        return separator;
    }

    private Region createSpacer() {
        Region spacer = new Region();
        spacer.getStyleClass().add("spacer");
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private BorderPane createCloseButton() {
        BorderPane closeContainer = new BorderPane();
        closeContainer.getStyleClass().addAll("icon-container", "cross");

        Region crossIcon = new Region();
        crossIcon.getStyleClass().addAll("icon", "cross");
        closeContainer.setCenter(crossIcon);

        return closeContainer;
    }

    private BorderPane createStampContainer() {
        BorderPane stampContainer = new BorderPane();
        stampContainer.getStyleClass().add("stamp-container");

        StackPane centerPane = new StackPane();
        centerPane.setPrefHeight(150.0);
        centerPane.setPrefWidth(200.0);
        stampContainer.setCenter(centerPane);

        return stampContainer;
    }

    private Button createAddSectionButton() {
        Button button = new Button("ADD SECTION");
        button.getStyleClass().add("add-section");
        button.setMnemonicParsing(false);

        Region icon = new Region();
        icon.getStyleClass().addAll("icon", "plus-circle");
        button.setGraphic(icon);

        return button;
    }

    @Override
    public void dispose() {
        if (titleLabel != null) {
            titleLabel.textProperty().unbind();
        }
        super.dispose();
    }
}