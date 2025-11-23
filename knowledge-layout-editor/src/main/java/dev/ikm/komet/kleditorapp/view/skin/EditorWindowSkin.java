package dev.ikm.komet.kleditorapp.view.skin;

import dev.ikm.komet.kleditorapp.view.control.EditorWindowControl;
import dev.ikm.komet.kview.mvvm.view.common.SVGConstants;
import javafx.beans.binding.Bindings;
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

    private final VBox root = new VBox();
    private Label titleLabel;

    public EditorWindowSkin(EditorWindowControl control) {
        super(control);

        root.getStyleClass().add("window");
        root.setMaxHeight(Control.USE_PREF_SIZE);
        root.setMaxWidth(Control.USE_PREF_SIZE);
        root.setMinHeight(Control.USE_PREF_SIZE);
        root.setMinWidth(Control.USE_PREF_SIZE);
        root.setPrefHeight(400.0);
        root.setPrefWidth(600.0);

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

        // Add icon containers
        contentContainer.getChildren().addAll(
                createIconContainer("coordinate", "Coordinate", null),
                createIconContainer(null, "Duplicate", SVGConstants.SAVE_SVG_PATH),
                createIconContainer(null, "Share", SVGConstants.SHARE_CONCEPT),
                createIconContainer(null, "Favorite", SVGConstants.FAVORITE),
                createIconContainer(null, "Reasoner", SVGConstants.REASONER),
                createSeparator(),
                createIconContainer("timeline", "Timeliene", null),
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