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
package dev.ikm.komet.kview.klwindows.concept;

import dev.ikm.komet.framework.KometNodeFactory;
import dev.ikm.komet.framework.activity.ActivityStream;
import dev.ikm.komet.framework.activity.ActivityStreamOption;
import dev.ikm.komet.framework.activity.ActivityStreams;
import dev.ikm.komet.framework.controls.EntityLabelWithDragAndDrop;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.klwindows.AbstractEntityChapterKlWindow;
import dev.ikm.komet.kview.klwindows.EntityKlWindowState;
import dev.ikm.komet.kview.klwindows.EntityKlWindowType;
import dev.ikm.komet.kview.klwindows.EntityKlWindowTypes;
import dev.ikm.komet.kview.mvvm.view.concept.ConceptController;
import dev.ikm.komet.kview.mvvm.view.concept.ConceptNode;
import dev.ikm.komet.kview.mvvm.view.concept.ConceptNodeFactory;
import dev.ikm.komet.kview.mvvm.viewmodel.ConceptViewModelNext;
import dev.ikm.komet.kview.mvvm.viewmodel.ConceptViewModelNext.ConceptPropertyKeys;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.flow.FlowSubscriber;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;
import org.carlfx.cognitive.loader.NamedVm;
import org.eclipse.collections.api.factory.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static dev.ikm.komet.kview.fxutils.CssHelper.defaultStyleSheet;
import static dev.ikm.komet.kview.mvvm.viewmodel.ConceptViewModel.CURRENT_ENTITY;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CREATE;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.MODE;

/**
 * A specialized Komet window for creating or editing concept entities.
 * <p>
 * This class extends {@link AbstractEntityChapterKlWindow} and incorporates a {@link ConceptNode}
 * for viewing or modifying concept details. It leverages the activity stream framework
 * to broadcast and receive updates about concept changes.
 */
public class ConceptKlWindow extends AbstractEntityChapterKlWindow {
    private static final Logger LOG = LoggerFactory.getLogger(ConceptKlWindow.class);

    private final ConceptNode conceptNode;
    private final PublicIdStringKey<ActivityStream> detailsActivityStreamKey;

    private final JFXNode<BorderPane, ConceptController> conceptJFXNode;
    private final ConceptViewModelNext conceptViewModelNext =  new ConceptViewModelNext();

    // <Concept Node stuff for entity update>
    protected final SimpleObjectProperty<EntityFacade> entityFocusProperty = new SimpleObjectProperty<>();
    protected FlowSubscriber<Integer> invalidationSubscriber;
    protected ChangeListener<EntityFacade> entityFocusChangeListener;
    // </Concept Node>
    /**
     * Constructs a new {@code ConceptKlWindow}.
     *
     * @param journalTopic   the UUID representing the journal topic the owning Journal Window uses to communicate events.
     * @param entityFacade   entity facade when not null usually this will load and display the current details.
     * @param viewProperties view properties is access to view calculators to query data.
     * @param preferences    komet preferences assists on reading and writing data to preferences user.home/Solor/database_folder/preferences
     */
    public ConceptKlWindow(UUID journalTopic, EntityFacade entityFacade,
                           ViewProperties viewProperties, KometPreferences preferences) {
        super(journalTopic, entityFacade, viewProperties, preferences);

        final boolean isCreateMode = (entityFacade == null);

        String uniqueDetailsTopic = isCreateMode
                ? "details-%s".formatted(getWindowTopic())
                : "details-%s".formatted(entityFacade.nid());
        UUID uuid = UuidT5Generator.get(uniqueDetailsTopic);

        // Create a unique key for the details activity stream.
        this.detailsActivityStreamKey = new PublicIdStringKey<>(PublicIds.of(uuid.toString()), uniqueDetailsTopic);
        ActivityStreams.create(detailsActivityStreamKey); // TODO: we ignore the return value here?? test what we get returned to understand the actual streamKey type

        // create a unique topic for each concept detail instance
        UUID conceptTopic = UUID.randomUUID();

        // Create a ConceptViewModel with preSet Propertys

        NamedVm conceptViewModelNext = new NamedVm("conceptViewModelNext", this.conceptViewModelNext);
        conceptViewModelNext.viewModel()
                .setValue(ConceptPropertyKeys.VIEW_PROPERTIES, viewProperties)
                .setValue(ConceptPropertyKeys.THIS_IS_A_NEW_CONCEPT, isCreateMode)
                .setValue(ConceptPropertyKeys.THIS_CONCEPT_ENTITY_FACADE, entityFacade)
                .setValue(ConceptPropertyKeys.ASOCIATED_JOURNAL_WINDOW_TOPIC, journalTopic)
                .setValue(ConceptPropertyKeys.THIS_UNIQUE_CONCEPT_TOPIC, conceptTopic)
                .reset(); // make sure that View values are init

        LOG.info(conceptViewModelNext.viewModel().toString());

        Config conceptConfig = new Config(ConceptController.class.getResource(ConceptController.CONCEPT_DETAILS_VIEW_FXML_FILE)).addNamedViewModel(conceptViewModelNext);

        this.conceptJFXNode = FXMLMvvmLoader.make(conceptConfig);

        LOG.info(conceptViewModelNext.viewModel().toString());
        this.conceptJFXNode.controller().updateView();

        // Programmatically change CSS Theme
        this.conceptJFXNode.node().getStylesheets().clear();
        String styleSheet = defaultStyleSheet();
        this.conceptJFXNode.node().getStylesheets().add(styleSheet);



        // Initialize the DetailsNode with a factory.
        KometNodeFactory conceptDetailsNodeFactory = new ConceptNodeFactory();
        this.conceptNode = (ConceptNode) conceptDetailsNodeFactory.create(viewProperties.parentView(),
                detailsActivityStreamKey, // TODO: understand - we create a activity stream and we asociated the PUBLISH option to it. The two other options are subscribe / sync
                ActivityStreamOption.PUBLISH.keyForOption(),
                AlertStreams.ROOT_ALERT_STREAM_KEY,
                true,
                journalTopic);

        // Configure the details node if we are in create mode.
//        if (isCreateMode) {
//            conceptNode.getConceptDetailsViewController()
//                    .getConceptViewModel()
//                    .setPropertyValue(MODE, CREATE);
//            conceptNode.getConceptDetailsViewController().updateView();
//        }

        // TODO: wtf is this late update mechanism
        // This will refresh the Concept details, history, timeline
        conceptNode.handleActivity(Lists.immutable.of(entityFacade));

        // Getting the concept window pane
        paneWindow = this.conceptJFXNode.node();

        // Set the onClose callback for the details window.
        conceptJFXNode.controller().setOnCloseConceptWindow(
                detailsController -> {
                    ActivityStreams.delete(detailsActivityStreamKey);
                    getOnClose().ifPresent(Runnable::run);
                    // TODO more clean up such as view models and listeners just in case (memory).
                }
        );

        this.conceptViewModelNext.getViewProperties().nodeView().addListener((obs, oldViewCoord, newViewCoord) -> {
            if (newViewCoord != null) {
                LOG.info("refresh concept window when view coordinate has changed." + newViewCoord);
                //updateView(); // this was the ConceptController refresh
                this.conceptViewModelNext.reset(); // TODO: verify how this is working
            }
        });


        //conceptNode.getConceptDetailsViewController().setOnCloseConceptWindow();
    }

    /**
     * Returns the key that identifies the activity stream for concept details.
     *
     * @return a {@link PublicIdStringKey} keyed to the {@link ActivityStream} for concept details
     */
    public PublicIdStringKey<ActivityStream> getDetailsActivityStreamKey() {
        return detailsActivityStreamKey;
    }


    @Override
    public EntityKlWindowType getWindowType() {
        return EntityKlWindowTypes.CONCEPT;
    }

    @Override
    protected boolean isPropertyPanelOpen() {
        return conceptJFXNode.controller().isPropertiesPanelOpen();
    }

    @Override
    protected void setPropertyPanelOpen(boolean isOpen) {
        conceptJFXNode.controller().setPropertiesPanelOpen(isOpen);
    }

    @Override
    protected String selectedPropertyPanel() {
        // TODO: get viewModel via Concept node and save / restore property window that way
        //String pane = conceptNode.getPropertiesViewController().selectedView();
       //LOG.debug("saving with Concept " + pane);
        //return pane;
        return "TODO";
    }

    @Override
    protected void setSelectedPropertyPanel(String selectedPanel) {
        // TODO: get viewModel via Concept node and save / restore property window that way
        LOG.debug("restoring pane with "+ selectedPanel);
        //conceptNode.getPropertiesViewController().restoreSelectedView(selectedPanel);
    }

    // For ConceptNode in the case that Concept should not be displayed on the journalView
    public BorderPane getConceptBorderPane() {
        return this.conceptJFXNode.node();
    }

    public ConceptController getConceptController() {
        return this.conceptJFXNode.controller();
    }

    private void listenOnEntityFacadeUpdate() {
        // remove later when closing
        this.entityFocusChangeListener = (observable, oldEntityFacade, newEntityFacade) -> {
            if (newEntityFacade != null) {

                if (newEntityFacade == oldEntityFacade) {
                    LOG.info("WE GOT UPDATE ON ENTITY FACADE WITHOUT ANYTHING CHANGING!");
                }

                // TODO: what was ConceptNode title and tooltip used for
                //titleProperty.set(viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(newEntityFacade));
                //toolTipTextProperty.set(viewProperties.calculator().getFullyQualifiedDescriptionTextWithFallbackOrNid(newEntityFacade));

                // forceupdating the Model with new state
                conceptViewModelNext.setValue(ConceptPropertyKeys.THIS_CONCEPT_ENTITY_FACADE, newEntityFacade);


                // Populate Detail View
//                if (getConceptDetailsViewController() != null) {
//                    getConceptDetailsViewController()
//                            .getConceptViewModel()
//                            .setPropertyValue(CURRENT_ENTITY, newEntityFacade);
//                    getConceptDetailsViewController().updateView();
//                }

                // Populate Properties View
                //
//                if (getPropertiesViewController() != null) {
//                    getPropertiesViewController().updateModel(viewProperties, newEntityFacade);
//                    getPropertiesViewController().updateView();
//                }

                // Populate Timeline View //TODO: dont forget abotu timeline reset here
//                if (getTimelineViewController() != null) {
//                    getTimelineViewController().resetConfigPathAndModules();
//                    getTimelineViewController().updateModel(viewProperties, newEntityFacade);
//                    getTimelineViewController().updateView();
//                }

            } else {
                // Show a blank view (nothing selected)
                //titleProperty.set(EntityLabelWithDragAndDrop.EMPTY_TEXT);
                //toolTipTextProperty.set(EntityLabelWithDragAndDrop.EMPTY_TEXT);
                conceptViewModelNext.setValue(ConceptPropertyKeys.THIS_CONCEPT_ENTITY_FACADE, newEntityFacade);
                //getConceptDetailsViewController().clearView();
                //getPropertiesViewController().clearView(); // does nothing currently
                // getPropertiesViewController().updateModel(viewProperties, newEntityFacade); // TODO: updates history/hirarchy controller
            }

        };

        // When a new entity is selected populate the view. An entity has been selected upstream (activity stream)
        this.entityFocusProperty.addListener(this.entityFocusChangeListener);

        // If database updates the underlying entity, this will do a force update of the UI.
        this.invalidationSubscriber = new FlowSubscriber<>(nid -> {
            if (entityFocusProperty.get() != null && entityFocusProperty.get().nid() == nid) {
                // component has changed, need to update.
                Platform.runLater(() -> entityFocusProperty.set(null));
                Platform.runLater(() -> entityFocusProperty.set(Entity.provider().getEntityFast(nid)));
            }
        });

        // Register to the Entity Service
        Entity.provider().addSubscriberWithWeakReference(this.invalidationSubscriber);
    }

}
