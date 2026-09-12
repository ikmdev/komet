/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.kview.mvvm.view.genpurpose;

import dev.ikm.komet.kview.controls.ComponentItemNode;
import dev.ikm.komet.kview.controls.ComponentItemNodeFactory;
import dev.ikm.komet.kview.controls.ContentSizedSplitPane;
import dev.ikm.komet.kview.controls.KlWindowControlToolbar;
import dev.ikm.komet.kview.controls.PublicIDListControl;
import dev.ikm.komet.kview.controls.StampViewControl;
import dev.ikm.komet.kview.fxutils.CssHelper;
import dev.ikm.komet.kview.mvvm.view.journal.VerticallyFilledPane;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;

/**
 * The scene graph of a general-purpose KL window, built in code: the toolbar with the window's
 * title tab on top, under it the body frame holding the create-mode hint, the banner (title,
 * identifiers, STAMP) above the split pane the sections go into, and the properties and timeline
 * slide-out trays on the right. Style classes and nesting are what kview.css matches on, so the
 * tree is kept exactly as the window's former FXML described it.
 *
 * <p>This is layout only: the parts are exposed for {@link GenPurposeDetailsController} to wire
 * behavior onto, and nothing here reads the store or the window's model.
 */
public final class GenPurposeWindowView extends BorderPane {

    /** Info icon of the create-mode hint: outlined circle + dot + stem, one even-odd path. */
    private static final String HINT_ICON_PATH = "M6.5 0 A6.5 6.5 0 1 0 6.5 13 A6.5 6.5 0 1 0 6.5 0 Z "
            + "M6.5 1.2 A5.3 5.3 0 1 1 6.5 11.8 A5.3 5.3 0 1 1 6.5 1.2 Z "
            + "M6.5 2.95 A0.95 0.95 0 1 0 6.5 4.85 A0.95 0.95 0 1 0 6.5 2.95 Z "
            + "M5.85 5.9 H7.15 V10.1 H5.85 Z";

    private final KlWindowControlToolbar windowControlToolbar = new KlWindowControlToolbar();
    private final Label createModeHintLabel = new Label();
    private final VerticallyFilledPane propertiesSlideoutTrayPane = new VerticallyFilledPane();
    private final VerticallyFilledPane timelineSlideoutTrayPane = new VerticallyFilledPane();
    private final ContentSizedSplitPane mainContent = new ContentSizedSplitPane();
    private final PublicIDListControl identifierControl = new PublicIDListControl();
    private final ComponentItemNode windowConceptTitle = ComponentItemNodeFactory.create();
    private final Tooltip windowConceptTitleTooltip = new Tooltip("Empty Tooltip");
    private final StampViewControl stampViewControl = new StampViewControl();

    public GenPurposeWindowView() {
        setId("detailsOuterBorderPane");
        getStyleClass().addAll("concept-detail-pane", "lidr-container", "pattern-window", "gen-purpose-window");
        getStylesheets().add(CssHelper.defaultStyleSheet());
        setMaxHeight(Double.POSITIVE_INFINITY);
        setMaxWidth(Double.POSITIVE_INFINITY);
        setPrefWidth(727);

        BorderPane detailsCenterBorderPane = new BorderPane();
        detailsCenterBorderPane.setId("detailsCenterBorderPane");
        detailsCenterBorderPane.setPrefWidth(762);
        // The toolbar also carries the window's title tab on its top row.
        windowControlToolbar.setId("windowControlToolbar");
        detailsCenterBorderPane.setTop(windowControlToolbar);
        detailsCenterBorderPane.setCenter(bodyFrame());
        setCenter(detailsCenterBorderPane);
    }

    /**
     * Wraps everything below the toolbar (hint, content and the slide-out trays), so the
     * create-mode dashed frame drawn on it follows the window's outside edges even when the
     * properties bumpout is open. The toolbar and tab carry their own darker create-mode dashes
     * (see kview.css).
     */
    private BorderPane bodyFrame() {
        BorderPane bodyFrame = new BorderPane();
        bodyFrame.getStyleClass().add("window-body-frame");
        bodyFrame.setTop(createModeHint());
        bodyFrame.setRight(trays());
        bodyFrame.setCenter(mainCenter());
        return bodyFrame;
    }

    private Label createModeHint() {
        SVGPath icon = new SVGPath();
        icon.getStyleClass().add("create-mode-hint-icon");
        icon.setFillRule(FillRule.EVEN_ODD);
        icon.setContent(HINT_ICON_PATH);

        createModeHintLabel.setId("createModeHintLabel");
        createModeHintLabel.getStyleClass().add("create-mode-hint");
        createModeHintLabel.setMaxWidth(Double.MAX_VALUE);
        createModeHintLabel.setVisible(false);
        createModeHintLabel.setManaged(false);
        createModeHintLabel.setGraphic(icon);
        return createModeHintLabel;
    }

    private HBox trays() {
        propertiesSlideoutTrayPane.setId("propertiesSlideoutTrayPane");
        propertiesSlideoutTrayPane.getStyleClass().add("slideout-tray-pane");
        timelineSlideoutTrayPane.setId("timelineSlideoutTrayPane");
        timelineSlideoutTrayPane.getStyleClass().add("slideout-tray-pane");

        HBox trays = new HBox(propertiesSlideoutTrayPane, timelineSlideoutTrayPane);
        trays.getStyleClass().add("main-right-container");
        BorderPane.setAlignment(trays, Pos.CENTER);
        return trays;
    }

    private BorderPane mainCenter() {
        mainContent.setId("mainContent");
        mainContent.setOrientation(Orientation.VERTICAL);
        HBox.setHgrow(mainContent, Priority.ALWAYS);

        HBox contentRow = new HBox(mainContent);
        contentRow.setPrefWidth(711);
        VBox.setVgrow(contentRow, Priority.ALWAYS);

        VBox content = new VBox(contentRow);
        content.setFocusTraversable(true);
        content.setMaxHeight(Double.MAX_VALUE);
        content.setPrefWidth(762);

        BorderPane mainCenter = new BorderPane();
        mainCenter.setPrefWidth(762);
        mainCenter.getStyleClass().add("main-center-container");
        BorderPane.setAlignment(mainCenter, Pos.CENTER);
        mainCenter.setCenter(content);
        mainCenter.setTop(banner());
        return mainCenter;
    }

    private BorderPane banner() {
        ColumnConstraints iconColumn = new ColumnConstraints();
        iconColumn.setHalignment(HPos.LEFT);
        iconColumn.setMaxWidth(586);
        iconColumn.setMinWidth(Region.USE_PREF_SIZE);
        iconColumn.setPrefWidth(42);
        ColumnConstraints titleColumn = new ColumnConstraints();
        titleColumn.setHalignment(HPos.LEFT);
        titleColumn.setMaxWidth(586);
        titleColumn.setMinWidth(234);
        titleColumn.setPrefWidth(416);
        RowConstraints titleRow = new RowConstraints();
        titleRow.setVgrow(Priority.NEVER);
        RowConstraints identifierRow = new RowConstraints();
        identifierRow.setMaxHeight(77);
        identifierRow.setMinHeight(0);
        identifierRow.setVgrow(Priority.SOMETIMES);

        identifierControl.setId("identifierControl");
        identifierControl.getStyleClass().add("concept-definition-title");
        GridPane.setColumnIndex(identifierControl, 0);
        GridPane.setColumnSpan(identifierControl, 3);
        GridPane.setRowIndex(identifierControl, 1);
        GridPane.setHalignment(identifierControl, HPos.LEFT);
        GridPane.setValignment(identifierControl, VPos.TOP);

        windowConceptTitle.setId("windowConceptTitle");
        windowConceptTitle.setMaxHeight(Double.MAX_VALUE);
        windowConceptTitle.setPrefWidth(416);
        windowConceptTitle.getStyleClass().add("window-main-title");
        windowConceptTitle.setWrapText(true);
        windowConceptTitle.setTooltip(windowConceptTitleTooltip);
        GridPane.setColumnIndex(windowConceptTitle, 0);
        GridPane.setColumnSpan(windowConceptTitle, 3);

        GridPane bannerGrid = new GridPane();
        bannerGrid.setMaxHeight(Double.MAX_VALUE);
        bannerGrid.setMaxWidth(Double.MAX_VALUE);
        bannerGrid.setPrefWidth(510);
        bannerGrid.getStyleClass().addAll("concept-detail-banner-background", "window-banner-grid");
        bannerGrid.getColumnConstraints().addAll(iconColumn, titleColumn, new ColumnConstraints());
        bannerGrid.getRowConstraints().addAll(titleRow, identifierRow);
        bannerGrid.getChildren().addAll(identifierControl, windowConceptTitle);

        stampViewControl.setId("stampViewControl");
        StackPane stampHolder = new StackPane(stampViewControl);
        BorderPane.setAlignment(stampHolder, Pos.CENTER);
        BorderPane.setMargin(stampHolder, new Insets(5, 5, 5, 0));

        BorderPane banner = new BorderPane();
        banner.setPrefWidth(894);
        banner.getStyleClass().add("concept-detail-banner-background");
        BorderPane.setAlignment(banner, Pos.CENTER);
        banner.setCenter(bannerGrid);
        banner.setRight(stampHolder);
        return banner;
    }

    public KlWindowControlToolbar getWindowControlToolbar() {
        return windowControlToolbar;
    }

    public Label getCreateModeHintLabel() {
        return createModeHintLabel;
    }

    public VerticallyFilledPane getPropertiesSlideoutTrayPane() {
        return propertiesSlideoutTrayPane;
    }

    public VerticallyFilledPane getTimelineSlideoutTrayPane() {
        return timelineSlideoutTrayPane;
    }

    public ContentSizedSplitPane getMainContent() {
        return mainContent;
    }

    public PublicIDListControl getIdentifierControl() {
        return identifierControl;
    }

    public ComponentItemNode getWindowConceptTitle() {
        return windowConceptTitle;
    }

    public Tooltip getWindowConceptTitleTooltip() {
        return windowConceptTitleTooltip;
    }

    public StampViewControl getStampViewControl() {
        return stampViewControl;
    }
}
