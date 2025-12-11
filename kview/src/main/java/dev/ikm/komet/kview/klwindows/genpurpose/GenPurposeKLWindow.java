package dev.ikm.komet.kview.klwindows.genpurpose;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.klwindows.AbstractEntityChapterKlWindow;
import dev.ikm.komet.kview.klwindows.EntityKlWindowType;
import dev.ikm.komet.kview.klwindows.EntityKlWindowTypes;
import dev.ikm.komet.kview.mvvm.view.concept.ConceptNode;
import dev.ikm.komet.kview.mvvm.view.genpurpose.GenPurposeDetailsController;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;

import java.util.ArrayList;
import java.util.UUID;

import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.FIELDS_COLLECTION;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.REF_COMPONENT;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.SEMANTIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.PATTERN;

public class GenPurposeKLWindow extends AbstractEntityChapterKlWindow {
    private final JFXNode<Pane, GenPurposeDetailsController> jfxNode;

    /**
     * Constructs a new {@code ConceptKlWindow}.
     *
     * @param journalTopic   the UUID representing the journal topic the owning Journal Window uses to communicate events.
     * @param entityFacade   entity facade when not null usually this will load and display the current details.
     * @param viewProperties view properties is access to view calculators to query data.
     * @param preferences    komet preferences assists on reading and writing data to preferences user.home/Solor/database_folder/preferences
     */
    public GenPurposeKLWindow(UUID journalTopic, EntityFacade entityFacade,
                           ViewProperties viewProperties, KometPreferences preferences) {
        super(journalTopic, entityFacade, viewProperties, preferences);

        // Prefetch modules and paths for view to populate radio buttons in form. Populate from database
        Config patternConfig = new Config(GenPurposeDetailsController.class.getResource("genpurpose-details.fxml"))
                .updateViewModel("genPurposeViewModel", genPurposeViewModel ->
                        genPurposeViewModel.setPropertyValue(VIEW_PROPERTIES, viewProperties)
                                .setPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC, journalTopic)
                                .setPropertyValue(WINDOW_TOPIC, getWindowTopic())
//                                .setPropertyValue(STAMP_VIEW_MODEL, stampViewModel)
                                .setPropertyValue(FIELDS_COLLECTION, new ArrayList<String>()) // Ordered collection of Fields
                                .setPropertyValue(REF_COMPONENT, entityFacade)
//                                .setPropertyValue(SEMANTIC, semanticComponent)
//                                .setPropertyValue(PATTERN, patternFacade))
                );

        // Create pattern window
        jfxNode = FXMLMvvmLoader.make(patternConfig);

        // Getting the concept window pane
        paneWindow = jfxNode.node();

        // Calls the remove method to remove and concepts that were closed by the user.
        jfxNode.controller().setOnCloseConceptWindow(windowEvent -> {
            getOnClose().ifPresent(Runnable::run);
            // TODO more clean up such as view models and listeners just in case (memory).
        });

        jfxNode.controller().init(preferences, viewProperties);
    }

    /**
     * Returns the {@link ConceptNode} associated with this window.
     *
     * @return the {@link ConceptNode} used for concept viewing or editing
     */
    public Node getDetailsNode() {
        return new Label("Empty");
    }

    @Override
    public EntityKlWindowType getWindowType() {
        return EntityKlWindowTypes.GEN_PURPOSE_KL;
    }

    @Override
    protected boolean isPropertyPanelOpen() {
//        return conceptNode.getConceptDetailsViewController().isPropertiesPanelOpen();
        return false;
    }

    @Override
    protected void setPropertyPanelOpen(boolean isOpen) {
//        conceptNode.getConceptDetailsViewController().setPropertiesPanelOpen(isOpen);
    }

    @Override
    protected String selectedPropertyPanel() {
//        String pane = conceptNode.getPropertiesViewController().selectedView();
//        LOG.debug("saving with Concept " + pane);
//        return pane;
        return null;
    }

    @Override
    protected void setSelectedPropertyPanel(String selectedPanel) {
//        LOG.debug("restoring pane with "+ selectedPanel);
//        conceptNode.getPropertiesViewController().restoreSelectedView(selectedPanel);
    }

    private void listenToEntityChanges() {
//        ViewModel conceptViewModel = conceptNode.getConceptDetailsViewController().getConceptViewModel();
//        ObjectProperty<EntityFacade> conceptProperty = conceptViewModel.getProperty(CURRENT_ENTITY);
//        conceptProperty.subscribe( entityFacade -> {
//            this.setEntityFacade(entityFacade);
//            // save to preference
//            this.save(); // call captureAdditionalState of AbstractEntityChapterKLWindow
//        });
    }
}