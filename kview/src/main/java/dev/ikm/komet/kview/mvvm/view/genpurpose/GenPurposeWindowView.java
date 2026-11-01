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

import dev.ikm.komet.kview.controls.ContentSizedSplitPane;
import dev.ikm.komet.kview.controls.KlWindowControlToolbar;
import dev.ikm.komet.kview.fxutils.CssHelper;
import dev.ikm.komet.kview.mvvm.view.journal.VerticallyFilledPane;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
    private final HBox unpublishedHint = new HBox();
    private final Label unpublishedHintLabel = new Label();
    private final Hyperlink unpublishedShowLink = new Hyperlink("Show");
    private final Hyperlink unpublishedDiscardLink = new Hyperlink("Discard");
    private final VerticallyFilledPane propertiesSlideoutTrayPane = new VerticallyFilledPane();
    private final VerticallyFilledPane timelineSlideoutTrayPane = new VerticallyFilledPane();
    private final ContentSizedSplitPane mainContent = new ContentSizedSplitPane();
    private final WindowBanner banner = new WindowBanner();

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
        // The two hint strips stack under the toolbar; at most one shows at a time (create mode
        // has nothing published to compare against).
        bodyFrame.setTop(new VBox(createModeHint(), unpublishedHint()));
        bodyFrame.setRight(trays());
        bodyFrame.setCenter(mainCenter());
        return bodyFrame;
    }

    private SVGPath hintIcon() {
        SVGPath icon = new SVGPath();
        icon.getStyleClass().add("create-mode-hint-icon");
        icon.setFillRule(FillRule.EVEN_ODD);
        icon.setContent(HINT_ICON_PATH);
        return icon;
    }

    private Label createModeHint() {
        createModeHintLabel.setId("createModeHintLabel");
        createModeHintLabel.getStyleClass().add("create-mode-hint");
        createModeHintLabel.setMaxWidth(Double.MAX_VALUE);
        createModeHintLabel.setVisible(false);
        createModeHintLabel.setManaged(false);
        createModeHintLabel.setGraphic(hintIcon());
        return createModeHintLabel;
    }

    /**
     * Strip naming the changes that are saved but not published yet, with a link to the first of
     * them and one that discards them — the create-mode strip's styling, on the same row layout (see .unpublished-hint in
     * kview.css). Shown by the controller while the window has such changes.
     */
    private HBox unpublishedHint() {
        unpublishedHintLabel.setId("unpublishedHintLabel");
        unpublishedHintLabel.setGraphic(hintIcon());
        unpublishedHintLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(unpublishedHintLabel, Priority.ALWAYS);

        unpublishedShowLink.setId("unpublishedShowLink");
        unpublishedShowLink.getStyleClass().add("unpublished-show-link");
        unpublishedDiscardLink.setId("unpublishedDiscardLink");
        unpublishedDiscardLink.getStyleClass().add("unpublished-show-link");

        unpublishedHint.setId("unpublishedHint");
        unpublishedHint.getStyleClass().addAll("create-mode-hint", "unpublished-hint");
        unpublishedHint.setAlignment(Pos.CENTER_LEFT);
        unpublishedHint.setVisible(false);
        unpublishedHint.setManaged(false);
        unpublishedHint.getChildren().addAll(unpublishedHintLabel, unpublishedShowLink, unpublishedDiscardLink);
        return unpublishedHint;
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
        mainCenter.setTop(banner);
        return mainCenter;
    }

    public KlWindowControlToolbar getWindowControlToolbar() {
        return windowControlToolbar;
    }

    public Label getCreateModeHintLabel() {
        return createModeHintLabel;
    }

    /** The strip naming the changes not published yet; hidden until the controller shows it. */
    public HBox getUnpublishedHint() {
        return unpublishedHint;
    }

    /** The unpublished strip's text. */
    public Label getUnpublishedHintLabel() {
        return unpublishedHintLabel;
    }

    /** The unpublished strip's "Discard" link, cancelling the changes not published yet once confirmed. */
    public Hyperlink getUnpublishedDiscardLink() {
        return unpublishedDiscardLink;
    }

    /** The unpublished strip's "Show" link, taking the user to the first change. */
    public Hyperlink getUnpublishedShowLink() {
        return unpublishedShowLink;
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

    WindowBanner getBanner() {
        return banner;
    }
}
