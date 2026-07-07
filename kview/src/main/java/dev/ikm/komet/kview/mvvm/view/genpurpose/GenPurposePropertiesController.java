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

import dev.ikm.komet.kview.events.StampEvent;
import dev.ikm.komet.kview.events.genediting.GenEditingEvent;
import dev.ikm.komet.kview.events.genpurpose.KLPropertyPanelEvent;
import dev.ikm.komet.kview.fxutils.CssHelper;
import dev.ikm.komet.kview.mvvm.view.common.StampFormController;
import dev.ikm.komet.kview.mvvm.view.confirmation.ConfirmationPaneController;
import dev.ikm.komet.kview.mvvm.view.genediting.ReferenceComponentController;
import dev.ikm.komet.kview.mvvm.view.genediting.SemanticFieldsController;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.PropertiesTabsControl;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.PropertiesTabsControl.Tab;
import dev.ikm.komet.kview.mvvm.viewmodel.ConfirmationPaneViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.FormMode;
import dev.ikm.komet.kview.mvvm.viewmodel.GenPurposeViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampAddSubmitFormViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampCreateFormViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.Subscriber;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.beans.property.BooleanProperty;
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
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Type.CONCEPT;
import static dev.ikm.komet.kview.mvvm.viewmodel.ViewModelKey.FIELD_INDEX;
import static dev.ikm.komet.kview.mvvm.viewmodel.ViewModelKey.REF_COMPONENT;
import static dev.ikm.komet.kview.events.StampEvent.ADD_STAMP;
import static dev.ikm.komet.kview.events.StampEvent.CREATE_STAMP;

public class GenPurposePropertiesController {
    private static final Logger LOG = LoggerFactory.getLogger(GenPurposePropertiesController.class);

    private final BorderPane propertiesPane = new BorderPane();

    private final PropertiesTabsControl propertiesTabs = new PropertiesTabsControl();

    private final BorderPane contentBorderPane = new BorderPane();

    /**
     * Show the current edit window.
     */
    public enum PaneProperties {
        PROPERTY_PANE_OPEN,
    }

    private Pane closePropsPane;

    private StampAddSubmitFormViewModel stampAddSubmitFormViewModel;

    private StampCreateFormViewModel stampCreateFormViewModel;

    private JFXNode<Pane, StampFormController> stampJFXNode;

    private final GenPurposeViewModel genPurposeViewModel;

    private Subscriber<KLPropertyPanelEvent> showPanelSubscriber;

    private Subscriber<GenEditingEvent> genEditingEventSubscriber;

    private Subscriber<StampEvent> addStampSubscriber;

    private Subscriber<StampEvent> createStampSubscriber;

    private JFXNode<Pane, SemanticFieldsController> editFieldsJfxNode;

    public GenPurposePropertiesController(GenPurposeViewModel genPurposeViewModel) {
        this.genPurposeViewModel = genPurposeViewModel;

        // The header STAMP belongs to the window's reference component, which is a Concept.
        this.stampAddSubmitFormViewModel = new StampAddSubmitFormViewModel(CONCEPT);
        this.stampCreateFormViewModel = new StampCreateFormViewModel(CONCEPT);

        buildView();

        setupShowingStampForm();
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
    }

    private void setupShowingStampForm() {
        // Load Stamp add View Panel (FXML & Controller)
        Config stampConfig = new Config(StampFormController.class.getResource(StampFormController.STAMP_FORM_FXML_FILE));
        stampJFXNode = FXMLMvvmLoader.make(stampConfig);

        // -- add stamp: edit the STAMP of the window's reference component (which always exists in a
        //    Knowledge Layout window), adding a new committed version. There is no CREATE flow here
        //    because the reference component is never created from this window.
        addStampSubscriber = evt -> {
            if (evt.getEventType() == ADD_STAMP) {
                EntityFacade refComponent = genPurposeViewModel.getPropertyValue(REF_COMPONENT);
                if (refComponent == null) {
                    return;
                }
                stampJFXNode.controller().init(stampAddSubmitFormViewModel);
                stampAddSubmitFormViewModel.update(refComponent,
                        genPurposeViewModel.getPropertyValue(WINDOW_TOPIC), genPurposeViewModel.getViewProperties());

                contentBorderPane.setCenter(stampJFXNode.node());

                propertiesTabs.setSelectedTab(Tab.ADD_EDIT);
            }
        };
        EvtBusFactory.getDefaultEvtBus().subscribe(genPurposeViewModel.getPropertyValue(WINDOW_TOPIC), StampEvent.class, addStampSubscriber);

        // -- create stamp: open an editable STAMP form for the new component being created. Create mode has
        //    no reference component, so the form starts from default values rather than an existing STAMP.
        //    update() first so the view properties and module/path lists are populated before defaults.
        createStampSubscriber = evt -> {
            if (evt.getEventType() == CREATE_STAMP) {
                stampCreateFormViewModel.update(genPurposeViewModel.getPropertyValue(REF_COMPONENT),
                        genPurposeViewModel.getPropertyValue(WINDOW_TOPIC), genPurposeViewModel.getViewProperties());
                stampCreateFormViewModel.populateDefaults();
                stampJFXNode.controller().init(stampCreateFormViewModel);

                contentBorderPane.setCenter(stampJFXNode.node());

                propertiesTabs.setSelectedTab(Tab.ADD_EDIT);
            }
        };
        EvtBusFactory.getDefaultEvtBus().subscribe(genPurposeViewModel.getPropertyValue(WINDOW_TOPIC), StampEvent.class, createStampSubscriber);
    }

    private void setupShowingPanelHandlers() {
        Config config = new Config(this.getClass().getResource("genpurpose-edit-fields.fxml"))
            .addNamedViewModel(new NamedVm("genPurposeViewModel", genPurposeViewModel));

        editFieldsJfxNode = FXMLMvvmLoader.make(config);

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
            propertiesTabs.setSelectedTab(Tab.ADD_EDIT);

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
     * Returns the properties tabs control to be used as a draggable region.
     * @return The control containing the property tabs
     */
    public PropertiesTabsControl getPropertiesTabs() {
        return propertiesTabs;
    }

    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/

    /**
     * The STAMP form view model used to edit the reference component's STAMP. The details controller
     * reads this to refresh the header STAMP once the user submits a new version.
     */
    public StampFormViewModelBase getStampFormViewModel() {
        return genPurposeViewModel.getMode() == FormMode.CREATE ? stampCreateFormViewModel : stampAddSubmitFormViewModel;
    }
}
