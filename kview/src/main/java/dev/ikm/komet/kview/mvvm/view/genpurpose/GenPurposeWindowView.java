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

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.ContentSizedSplitPane;
import dev.ikm.komet.kview.controls.KlWindowControlToolbar;
import dev.ikm.komet.kview.controls.SectionTitledPane;
import dev.ikm.komet.kview.fxutils.CssHelper;
import dev.ikm.komet.kview.mvvm.view.journal.VerticallyFilledPane;
import dev.ikm.komet.layout.controls.FilterOptionsPopup;
import dev.ikm.komet.layout_engine.window.WindowSupport;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;

import static dev.ikm.komet.kview.mvvm.view.common.ChapterWindowHelper.setupViewCoordinateOptionsPopup;
import static dev.ikm.komet.layout_engine.window.DraggableSupport.addDraggableNodes;

/**
 * The scene graph of a general-purpose KL window, built in code: the toolbar with the window's
 * title tab on top, under it the body frame holding the create-mode hint, the banner (title,
 * identifiers, STAMP) above the split pane the sections go into, and the properties and timeline
 * slide-out trays on the right. Style classes and nesting are what kview.css matches on, so the
 * tree is kept exactly as the window's former FXML described it.
 *
 * <p>This is layout only: {@link GenPurposeDetailsController} drives it through this class's
 * properties and methods, which say what the window shows (its title, whether it is creating,
 * the hints, the toolbar's buttons) without ever handing out the controls that show it. Nothing
 * here reads the store or the window's model.
 */
public final class GenPurposeWindowView extends BorderPane {

    /** Info icon of the create-mode hint: outlined circle + dot + stem, one even-odd path. */
    private static final String HINT_ICON_PATH = "M6.5 0 A6.5 6.5 0 1 0 6.5 13 A6.5 6.5 0 1 0 6.5 0 Z "
            + "M6.5 1.2 A5.3 5.3 0 1 1 6.5 11.8 A5.3 5.3 0 1 1 6.5 1.2 Z "
            + "M6.5 2.95 A0.95 0.95 0 1 0 6.5 4.85 A0.95 0.95 0 1 0 6.5 2.95 Z "
            + "M5.85 5.9 H7.15 V10.1 H5.85 Z";

    /** Ghost-window styling while the window frames a component that doesn't exist yet (see kview.css). */
    private static final PseudoClass CREATE_MODE = PseudoClass.getPseudoClass("create-mode");

    /** The window's focus shadow (see kview.css). */
    private static final PseudoClass WINDOW_FOCUSED = PseudoClass.getPseudoClass("window-focused");

    private final KlWindowControlToolbar windowControlToolbar = new KlWindowControlToolbar();
    private final Label createModeHintLabel = new Label();
    private final HBox unpublishedHint = new HBox();
    private final Label unpublishedHintLabel = new Label();
    private final Hyperlink unpublishedShowLink = new Hyperlink("Show");
    private final Hyperlink unpublishedDiscardLink = new Hyperlink("Discard");
    private final VerticallyFilledPane propertiesSlideoutTrayPane = new VerticallyFilledPane();
    private final VerticallyFilledPane timelineSlideoutTrayPane = new VerticallyFilledPane();
    private final ContentSizedSplitPane mainContent = new ContentSizedSplitPane();
    private final ObservableList<SectionTitledPane<EntityFacade>> sections = FXCollections.observableArrayList();
    private final WindowBanner banner = new WindowBanner();

    /** The properties tray, once a properties panel is set. */
    private PropertiesTray propertiesTray;
    /** Grows the window to fit the open properties tray, and gives the height back as it closes. */
    private final WindowHeightHelper windowHeightHelper;

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

        // The DRAFT chip and the hint show while creating; the dashed frame follows through the
        // pseudo-class (see creating).
        windowControlToolbar.draftVisibleProperty().bind(creating);
        createModeHintLabel.visibleProperty().bind(creating);
        createModeHintLabel.managedProperty().bind(creating);
        createModeHintLabel.textProperty().bind(createModeHintText);
        unpublishedHint.visibleProperty().bind(unpublishedHintVisible);
        unpublishedHint.managedProperty().bind(unpublishedHintVisible);
        unpublishedHintLabel.textProperty().bind(unpublishedHintText);
        unpublishedDiscardLink.visibleProperty().bind(unpublishedDiscardVisible);
        unpublishedDiscardLink.managedProperty().bind(unpublishedDiscardVisible);
        unpublishedShowLink.setOnAction(_ -> getOnShowUnpublished().run());
        unpublishedDiscardLink.setOnAction(_ -> getOnDiscardUnpublished().run());
        windowControlToolbar.propertiesSelectedProperty().bindBidirectional(propertiesTrayOpen);

        // This window opts out of WindowSupport's hover/resize outline: the edge resize cursors
        // are the resize affordance. Must be set before addDraggableNodes below creates the
        // window support.
        getProperties().put(WindowSupport.HIGHLIGHT_DISABLED_KEY, Boolean.TRUE);

        // The toolbar's own title tab is part of the toolbar control, so it drags the window
        // through the toolbar handle.
        addDraggableNodes(this, windowControlToolbar);

        // Window focus shadow: the window counts as focused while keyboard focus is anywhere
        // inside it (focus-within). A mouse press anywhere in the window routes focus to the
        // window root first — the filter runs before the pressed control claims focus for
        // itself — so clicking non-focusable areas also focuses the window. Focus moving into
        // another window clears it. Drives :window-focused in kview.css.
        focusWithinProperty().subscribe(() -> pseudoClassStateChanged(WINDOW_FOCUSED, isFocusWithin()));
        addEventFilter(MouseEvent.MOUSE_PRESSED, _ -> {
            if (!isFocusWithin()) {
                requestFocus();
            }
        });

        windowHeightHelper = new WindowHeightHelper(this, propertiesSlideoutTrayPane);
        // A section divider dragged past the height the sections have between them grows the
        // window, so a section can be enlarged top-down without first making room below it
        // (komet-desktop#219).
        mainContent.setGrowHandler(windowHeightHelper::growBy);
    }

    /*=========================================================================*
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     *=========================================================================*/

    /** The sections stacked in the window's content, top to bottom. */
    public ObservableList<SectionTitledPane<EntityFacade>> getSections() {
        return sections;
    }

    /**
     * Drives the toolbar's coordinates menu from the relocated FilterOptionsPopup (ike-issues#661);
     * the popup writes the window's nodeView override, which the window's KL context + areas
     * resolve through.
     *
     * @param viewProperties the window's (derived) view properties the popup edits
     * @param onViewChanged  run on each coordinate change, to refresh what follows the view
     */
    public void setupCoordinatesPopup(ViewProperties viewProperties, Runnable onViewChanged) {
        setupViewCoordinateOptionsPopup(viewProperties, FilterOptionsPopup.FILTER_TYPE.CHAPTER_WINDOW, this,
                windowControlToolbar.getCoordinatesMenuButton(), onViewChanged);
    }

    /**
     * Shows the component in the banner: its identifiers, its title and the STAMP of its latest
     * committed version, with the descriptions following the view.
     */
    public void setComponent(ViewProperties viewProperties, EntityFacade component) {
        banner.updateComponent(viewProperties, component);
    }

    /**
     * Shows in the banner the STAMP a newly created component would be committed with, read from
     * the edit coordinate: for create mode, where no committed STAMP exists yet.
     */
    public void setStampFromEditCoordinate(ViewProperties viewProperties) {
        banner.populateStampFromEditCoordinate(viewProperties);
    }

    /**
     * Puts the properties panel into the window's properties tray, starting out closed.
     *
     * @param panel      the properties panel
     * @param dragHandle the part of the panel that drags the window while the tray is open
     */
    public void setPropertiesPanel(Region panel, Node dragHandle) {
        propertiesTray = new PropertiesTray(this, propertiesSlideoutTrayPane, panel, dragHandle);
    }

    /**
     * Grows the window to fit what the properties tray requires now. Does nothing when the window
     * is tall enough already, or can't grow any further.
     *
     * @param requiredHeight the height the tray needs to show the panel's content in full
     */
    public void growToFitProperties(double requiredHeight) {
        windowHeightHelper.growToFitProperties(requiredHeight);
    }

    /**
     * Gives back the height {@link #growToFitProperties(double)} added. A window the user has resized in
     * the meantime keeps their height.
     */
    public void restorePreviousHeight() {
        windowHeightHelper.restorePreviousHeight();
    }

    /**
     * Changes the height of a window that carries a preferred height of its own by {@code delta},
     * as something in it (a collapsing section) gives up or takes height; a grown window keeps the
     * height {@link #restorePreviousHeight()} goes back to in step.
     */
    public void changeHeightBy(double delta) {
        windowHeightHelper.changeHeightBy(delta);
    }

    /**
     * Ends a running grow or restore animation right away, leaving the window at the height the
     * animation was heading to. Call it before reading or changing the window's preferred height.
     */
    public void finishHeightAnimationNow() {
        windowHeightHelper.finishAnimationNow();
    }

    /*=========================================================================*
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     *=========================================================================*/

    // -- window title
    /** The title on the toolbar's title tab. */
    public StringProperty windowTitleProperty() { return windowControlToolbar.titleProperty(); }
    public String getWindowTitle() { return windowControlToolbar.getTitle(); }
    public void setWindowTitle(String title) { windowControlToolbar.setTitle(title); }

    // -- creating
    /**
     * Whether the window frames a component that doesn't exist yet: the chrome dims and the frame
     * dashes (see :create-mode in kview.css), and the DRAFT chip and the create-mode hint show.
     */
    private final BooleanProperty creating = new SimpleBooleanProperty(this, "creating", false) {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(CREATE_MODE, get());
        }
    };
    public boolean isCreating() { return creating.get(); }
    public BooleanProperty creatingProperty() { return creating; }
    public void setCreating(boolean creating) { this.creating.set(creating); }

    // -- properties tray open
    /**
     * Whether the properties tray is slid out, by a click on the toolbar's Properties toggle or
     * by the setter; the toggle follows it either way.
     */
    private final BooleanProperty propertiesTrayOpen = new SimpleBooleanProperty(this, "propertiesTrayOpen", false) {
        @Override
        protected void invalidated() {
            if (get()) {
                propertiesTray.open();
            } else {
                propertiesTray.close();
            }
        }
    };
    public boolean isPropertiesTrayOpen() { return propertiesTrayOpen.get(); }
    public BooleanProperty propertiesTrayOpenProperty() { return propertiesTrayOpen; }
    public void setPropertiesTrayOpen(boolean open) { this.propertiesTrayOpen.set(open); }

    // -- create mode hint text
    /** What the create-mode hint under the toolbar says. */
    private final StringProperty createModeHintText = new SimpleStringProperty(this, "createModeHintText");
    public String getCreateModeHintText() { return createModeHintText.get(); }
    public StringProperty createModeHintTextProperty() { return createModeHintText; }
    public void setCreateModeHintText(String text) { this.createModeHintText.set(text); }

    // -- unpublished hint visible
    /** Whether the strip naming the changes not published yet shows under the toolbar. */
    private final BooleanProperty unpublishedHintVisible = new SimpleBooleanProperty(this, "unpublishedHintVisible", false);
    public boolean isUnpublishedHintVisible() { return unpublishedHintVisible.get(); }
    public BooleanProperty unpublishedHintVisibleProperty() { return unpublishedHintVisible; }
    public void setUnpublishedHintVisible(boolean visible) { this.unpublishedHintVisible.set(visible); }

    // -- unpublished hint text
    /** What the unpublished strip says. */
    private final StringProperty unpublishedHintText = new SimpleStringProperty(this, "unpublishedHintText");
    public String getUnpublishedHintText() { return unpublishedHintText.get(); }
    public StringProperty unpublishedHintTextProperty() { return unpublishedHintText; }
    public void setUnpublishedHintText(String text) { this.unpublishedHintText.set(text); }

    // -- unpublished discard visible
    /** Whether the unpublished strip offers its "Discard" link. */
    private final BooleanProperty unpublishedDiscardVisible = new SimpleBooleanProperty(this, "unpublishedDiscardVisible", true);
    public boolean isUnpublishedDiscardVisible() { return unpublishedDiscardVisible.get(); }
    public BooleanProperty unpublishedDiscardVisibleProperty() { return unpublishedDiscardVisible; }
    public void setUnpublishedDiscardVisible(boolean visible) { this.unpublishedDiscardVisible.set(visible); }

    // -- on show unpublished
    /** Called when the unpublished strip's "Show" link is pressed, to take the user to the first change. */
    private final ObjectProperty<Runnable> onShowUnpublished = new SimpleObjectProperty<>(this, "onShowUnpublished");
    public Runnable getOnShowUnpublished() { return onShowUnpublished.get(); }
    public ObjectProperty<Runnable> onShowUnpublishedProperty() { return onShowUnpublished; }
    public void setOnShowUnpublished(Runnable handler) { this.onShowUnpublished.set(handler); }

    // -- on discard unpublished
    /** Called when the unpublished strip's "Discard" link is pressed. */
    private final ObjectProperty<Runnable> onDiscardUnpublished = new SimpleObjectProperty<>(this, "onDiscardUnpublished");
    public Runnable getOnDiscardUnpublished() { return onDiscardUnpublished.get(); }
    public ObjectProperty<Runnable> onDiscardUnpublishedProperty() { return onDiscardUnpublished; }
    public void setOnDiscardUnpublished(Runnable handler) { this.onDiscardUnpublished.set(handler); }

    // -- on close
    /** Called when the toolbar's close button is pressed. */
    public Runnable getOnClose() { return windowControlToolbar.getOnCloseAction(); }
    public void setOnClose(Runnable handler) { windowControlToolbar.setOnCloseAction(handler); }

    // -- on publish
    /** Called when the toolbar's Publish button is pressed. */
    public Runnable getOnPublish() { return windowControlToolbar.getOnPublishAction(); }
    public void setOnPublish(Runnable handler) { windowControlToolbar.setOnPublishAction(handler); }

    // -- on field defaults
    /** Called when the toolbar's field-defaults button is pressed. */
    public ObjectProperty<Runnable> onFieldDefaultsProperty() { return windowControlToolbar.onFieldDefaultsActionProperty(); }
    public Runnable getOnFieldDefaults() { return windowControlToolbar.getOnFieldDefaultsAction(); }
    public void setOnFieldDefaults(Runnable handler) { windowControlToolbar.setOnFieldDefaultsAction(handler); }

    // -- publish visible
    /** Whether the toolbar shows its Publish button. */
    public BooleanProperty publishVisibleProperty() { return windowControlToolbar.publishVisibleProperty(); }
    public boolean isPublishVisible() { return windowControlToolbar.isPublishVisible(); }
    public void setPublishVisible(boolean visible) { windowControlToolbar.setPublishVisible(visible); }

    // -- publish disable
    /** Whether the toolbar's Publish button is disabled. */
    public BooleanProperty publishDisableProperty() { return windowControlToolbar.publishDisableProperty(); }
    public boolean isPublishDisable() { return windowControlToolbar.isPublishDisable(); }
    public void setPublishDisable(boolean disable) { windowControlToolbar.setPublishDisable(disable); }

    // -- publish tooltip
    /** The tooltip on the toolbar's Publish button. */
    public StringProperty publishTooltipProperty() { return windowControlToolbar.publishTooltipProperty(); }
    public String getPublishTooltip() { return windowControlToolbar.getPublishTooltip(); }
    public void setPublishTooltip(String tooltip) { windowControlToolbar.setPublishTooltip(tooltip); }

    // -- field defaults visible
    /** Whether the toolbar shows its field-defaults button. */
    public BooleanProperty fieldDefaultsVisibleProperty() { return windowControlToolbar.fieldDefaultsVisibleProperty(); }
    public boolean isFieldDefaultsVisible() { return windowControlToolbar.isFieldDefaultsVisible(); }
    public void setFieldDefaultsVisible(boolean visible) { windowControlToolbar.setFieldDefaultsVisible(visible); }

    // -- coordinate visible
    /** Whether the toolbar shows its coordinates menu. */
    public BooleanProperty coordinateVisibleProperty() { return windowControlToolbar.coordinateVisibleProperty(); }
    public boolean isCoordinateVisible() { return windowControlToolbar.isCoordinateVisible(); }
    public void setCoordinateVisible(boolean visible) { windowControlToolbar.setCoordinateVisible(visible); }

    // -- timeline visible
    /** Whether the toolbar shows its timeline toggle. */
    public BooleanProperty timelineVisibleProperty() { return windowControlToolbar.timelineVisibleProperty(); }
    public boolean isTimelineVisible() { return windowControlToolbar.isTimelineVisible(); }
    public void setTimelineVisible(boolean visible) { windowControlToolbar.setTimelineVisible(visible); }

    /*=========================================================================*
     *                                                                         *
     * Private Implementation                                                  *
     *                                                                         *
     *=========================================================================*/

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
        createModeHintLabel.setGraphic(hintIcon());
        return createModeHintLabel;
    }

    /**
     * Strip naming the changes that are saved but not published yet, with a link to the first of
     * them and one that discards them — the create-mode strip's styling, on the same row layout (see .unpublished-hint in
     * kview.css). Shown while the window has such changes (see unpublishedHintVisible).
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
        Bindings.bindContent(mainContent.getItems(), sections);
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
}
