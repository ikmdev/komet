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

import dev.ikm.komet.kview.events.genediting.GenEditingEvent;
import dev.ikm.komet.kview.events.genpurpose.KLPropertyPanelEvent;
import dev.ikm.komet.kview.fxutils.CssHelper;
import dev.ikm.komet.kview.mvvm.view.confirmation.ConfirmationPaneController;
import dev.ikm.komet.kview.mvvm.view.genediting.SemanticFieldsController;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.PropertiesTabsControl;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.PropertiesTabsControl.Tab;
import dev.ikm.komet.kview.mvvm.viewmodel.ConfirmationPaneViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.GenPurposeViewModel;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.Subscriber;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;
import org.carlfx.cognitive.loader.NamedVm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static dev.ikm.komet.kview.mvvm.view.confirmation.ConfirmationPaneController.CONFIRMATION_PANE_FXML_URL;
import static dev.ikm.komet.kview.mvvm.view.confirmation.ConfirmationPaneController.CONFIRMATION_VIEW_MODEL;
import static dev.ikm.komet.kview.mvvm.viewmodel.ConfirmationPaneViewModel.ConfirmationPropertyName.CLOSE_CONFIRMATION_PANEL;
import static dev.ikm.komet.kview.mvvm.viewmodel.ConfirmationPaneViewModel.ConfirmationPropertyName.CONFIRMATION_MESSAGE;
import static dev.ikm.komet.kview.mvvm.viewmodel.ConfirmationPaneViewModel.ConfirmationPropertyName.CONFIRMATION_TITLE;
import static dev.ikm.komet.kview.mvvm.viewmodel.ViewModelKey.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.ViewModelKey.WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.ViewModelKey.FIELD_INDEX;

public class GenPurposePropertiesController {
    private static final Logger LOG = LoggerFactory.getLogger(GenPurposePropertiesController.class);

    private final BorderPane propertiesPane = new BorderPane();

    private final PropertiesTabsControl propertiesTabs = new PropertiesTabsControl();

    private final BorderPane contentBorderPane = new BorderPane();

    /**
     * The height the panel needs to show its current content in full, without its form having to
     * scroll. See {@link #requiredHeightProperty()}.
     */
    private final ReadOnlyDoubleWrapper requiredHeight = new ReadOnlyDoubleWrapper();

    /**
     * Show the current edit window.
     */
    public enum PaneProperties {
        PROPERTY_PANE_OPEN,
    }

    private Pane closePropsPane;

    private final GenPurposeViewModel genPurposeViewModel;

    private Subscriber<KLPropertyPanelEvent> showPanelSubscriber;

    private Subscriber<GenEditingEvent> genEditingEventSubscriber;

    private JFXNode<Pane, SemanticFieldsController> editFieldsJfxNode;

    /**
     * The DEFAULTS tab's form — a second instance of the edit-fields form, editing the pattern's
     * defaults semantic (standard Pattern window only).
     */
    private JFXNode<Pane, GenPurposeFieldsController> defaultsFieldsJfxNode;

    public GenPurposePropertiesController(GenPurposeViewModel genPurposeViewModel) {
        this.genPurposeViewModel = genPurposeViewModel;

        buildView();

        setupShowingPanelHandlers();
    }

    private void buildView() {
        propertiesTabs.getTabs().setAll(Tab.ADD_EDIT, Tab.COMMENTS);
        propertiesTabs.setSelectedTab(Tab.ADD_EDIT);

        contentBorderPane.getStyleClass().add("properties-tab-container-content");

        // "properties-tab-outer-container" carries the gen purpose window's left divider border
        // and bottom padding (see kview.css); the historical inner/outer nesting is collapsed
        // into this single pane.
        propertiesPane.getStyleClass().addAll("properties-tab-outer-container", "properties-tab-container");
        propertiesPane.getStylesheets().add(CssHelper.defaultStyleSheet());
        propertiesPane.setMinHeight(300);
        propertiesPane.setPrefWidth(518);
        propertiesPane.setTop(propertiesTabs);
        BorderPane.setAlignment(propertiesTabs, Pos.CENTER);
        propertiesPane.setCenter(contentBorderPane);

        // What the content needs changes with the panel shown and, for a form, with its fields.
        // Both end in a layout pass of the content, so measure as that pass reaches it — the
        // pulse's CSS pass has styled the new nodes by then, so their preferred heights are reliable.
        contentBorderPane.needsLayoutProperty().subscribe(needsLayout -> {
            if (!needsLayout) {
                requiredHeight.set(computeRequiredHeight());
            }
        });
    }

    /**
     * The panel's own preferred height is bound to the tray's height (the panel fills whatever
     * height the window gives it), so what its content asks for is added up here instead.
     */
    private double computeRequiredHeight() {
        final double contentWidth = contentBorderPane.getWidth() > 0 ? contentBorderPane.getWidth() : -1;
        return propertiesPane.snappedTopInset()
                + propertiesTabs.prefHeight(-1)
                + contentBorderPane.prefHeight(contentWidth)
                + propertiesPane.snappedBottomInset();
    }

    private void setupShowingPanelHandlers() {
        Config config = new Config(this.getClass().getResource("genpurpose-edit-fields.fxml"))
            .addNamedViewModel(new NamedVm("genPurposeViewModel", genPurposeViewModel));

        editFieldsJfxNode = FXMLMvvmLoader.make(config);

        // The DEFAULTS tab gets its own form instance, so editing the pattern's field defaults
        // never disturbs a semantic edit in progress on the ADD/EDIT tab.
        Config defaultsConfig = new Config(this.getClass().getResource("genpurpose-edit-fields.fxml"))
            .addNamedViewModel(new NamedVm("genPurposeViewModel", genPurposeViewModel));
        defaultsFieldsJfxNode = FXMLMvvmLoader.make(defaultsConfig);
        defaultsFieldsJfxNode.controller().setDefaultsForm(true);

        JFXNode<Pane, ConfirmationPaneController> closePropsJfxNode = FXMLMvvmLoader.make(CONFIRMATION_PANE_FXML_URL);
        closePropsPane = closePropsJfxNode.node();

        Optional<ConfirmationPaneViewModel> confirmationPaneViewModelOpt = closePropsJfxNode.getViewModel(CONFIRMATION_VIEW_MODEL);
        ConfirmationPaneViewModel confirmationPaneViewModel = confirmationPaneViewModelOpt.get();

        BooleanProperty closeConfPanelProp = confirmationPaneViewModel.getBooleanProperty(CLOSE_CONFIRMATION_PANEL);
        closeConfPanelProp.subscribe(closeIt -> {
            if (closeIt) {
                EvtBusFactory.getDefaultEvtBus().publish(genPurposeViewModel.getPropertyValue(WINDOW_TOPIC),
                        new KLPropertyPanelEvent(closePropsPane, KLPropertyPanelEvent.CLOSE_PANEL));

                confirmationPaneViewModel.reset();
            }
        });

        genEditingEventSubscriber = evt -> {
            LOG.info("Publish event type: " + evt.getEventType());

            // "Semantic Details Added" is displayed when form values are Submitted when in CREATE mode
            // "Semantic Details Changed" is displayed when form values are Submitted when in EDIT mode

            confirmationPaneViewModel.setPropertyValue(CONFIRMATION_TITLE, "Semantic Details Added");
            confirmationPaneViewModel.setPropertyValue(CONFIRMATION_MESSAGE, "Make a selection in the view to edit the Semantic.");

            contentBorderPane.setCenter(closePropsPane);
        };
        EvtBusFactory.getDefaultEvtBus().subscribe(genPurposeViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC),
                GenEditingEvent.class, genEditingEventSubscriber);

        showPanelSubscriber = evt -> {
            LOG.info("Show Panel by event type: " + evt.getEventType());

            if (evt.getEventType() == KLPropertyPanelEvent.SHOW_PATTERN_FIELD_DEFAULTS) {
                // The window loaded the defaults form through this event (the DEFAULTS tab was
                // selected, or the toolbar's field-defaults button pressed): show it under its tab.
                propertiesTabs.setSelectedTab(Tab.DEFAULTS);
                contentBorderPane.setCenter(defaultsFieldsJfxNode.node());
                return;
            }

            // Every other panel event belongs to the ADD/EDIT tab. (OPEN_PANEL and CLOSE_PANEL
            // reach here too; they must not move the selection away from the DEFAULTS tab.)
            if (evt.getEventType() != KLPropertyPanelEvent.OPEN_PANEL
                    && evt.getEventType() != KLPropertyPanelEvent.CLOSE_PANEL) {
                propertiesTabs.setSelectedTab(Tab.ADD_EDIT);
            }

            if (evt.getEventType() == KLPropertyPanelEvent.SHOW_EDIT_SEMANTIC_FIELDS) {
                genPurposeViewModel.setPropertyValue(FIELD_INDEX, -1);
                contentBorderPane.setCenter(editFieldsJfxNode.node());
            } else if (evt.getEventType() == KLPropertyPanelEvent.NO_SELECTION_MADE_PANEL) {
                // change the heading on the top of the panel
                genPurposeViewModel.setPropertyValue(FIELD_INDEX, -1);

                confirmationPaneViewModel.setPropertyValue(CONFIRMATION_TITLE, "No Selection Made");
                confirmationPaneViewModel.setPropertyValue(CONFIRMATION_MESSAGE, "Make a selection in the view to edit the Semantic.");

                contentBorderPane.setCenter(closePropsPane);
            }
        };
        EvtBusFactory.getDefaultEvtBus().subscribe(genPurposeViewModel.getPropertyValue(WINDOW_TOPIC),
                KLPropertyPanelEvent.class, showPanelSubscriber);
    }

    /**
     * Returns the root node of the properties panel, to be attached to the window's slideout tray.
     */
    public BorderPane getNode() {
        return propertiesPane;
    }

    /**
     * The height the panel needs to show its current content in full, without its form having to
     * scroll. Updated as the panel shown changes and as a form's fields are loaded.
     */
    public ReadOnlyDoubleProperty requiredHeightProperty() {
        return requiredHeight.getReadOnlyProperty();
    }

    /**
     * Measures {@link #requiredHeightProperty()} right away, for content set in the current pulse
     * that the next layout pass hasn't measured yet.
     */
    public void refreshRequiredHeight() {
        propertiesPane.applyCss();
        requiredHeight.set(computeRequiredHeight());
    }

    /**
     * Returns the properties tabs control to be used as a draggable region.
     * @return The control containing the property tabs
     */
    public PropertiesTabsControl getPropertiesTabs() {
        return propertiesTabs;
    }

}
