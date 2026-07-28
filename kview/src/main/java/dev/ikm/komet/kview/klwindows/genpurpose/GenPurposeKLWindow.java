package dev.ikm.komet.kview.klwindows.genpurpose;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.klwindows.AbstractEntityChapterKlWindow;
import dev.ikm.komet.kview.klwindows.EntityKlWindowState;
import dev.ikm.komet.kview.klwindows.EntityKlWindowType;
import dev.ikm.komet.kview.klwindows.EntityKlWindowTypes;
import dev.ikm.komet.kview.mvvm.view.concept.ConceptNode;
import dev.ikm.komet.kview.mvvm.view.genpurpose.GenPurposeDetailsController;
import dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.FormMode;
import dev.ikm.komet.kview.mvvm.viewmodel.GenPurposeViewModel;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;
import org.carlfx.cognitive.viewmodel.ViewModel;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Optional;

import static dev.ikm.komet.kview.mvvm.viewmodel.ViewModelKey.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.ViewModelKey.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.ViewModelKey.FIELDS_COLLECTION;
import static dev.ikm.komet.kview.mvvm.viewmodel.ViewModelKey.REF_COMPONENT;
import static dev.ikm.komet.kview.mvvm.viewmodel.ViewModelKey.SEMANTIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.ViewModelKey.WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.ViewModelKey.PATTERN;

public class GenPurposeKLWindow extends AbstractEntityChapterKlWindow {

    /** Preference key holding the title of the KL-editor window definition this window realizes. */
    public static final String KL_EDITOR_WINDOW_TITLE = "KL_EDITOR_WINDOW_TITLE";

    /**
     * Preference key holding the kl-editor-app folder the definition lives in:
     * {@code user-windows} or {@code standard-windows}.
     */
    public static final String KL_EDITOR_WINDOW_DIR = "KL_EDITOR_WINDOW_DIR";

    private final JFXNode<Pane, GenPurposeDetailsController> jfxNode;

    /**
     * The {@code kl-editor-app/{user,standard}-windows/<title>} node of the KL-editor window
     * definition this window realizes, wired in by {@link #initKLWindowPreferences}. Persisted by
     * {@link #captureAdditionalState} so the definition can be re-resolved and re-initialized when
     * the journal is reopened (komet-desktop#20).
     */
    private KometPreferences klEditorWindowPreferences;

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

        // No component to open means the window was launched from the Journal's "+" button to create a
        // new one; a supplied component means it was opened ("Open as ...") to edit that component.
        final FormMode mode = entityFacade == null ? FormMode.CREATE : FormMode.EDIT;

        // Prefetch modules and paths for view to populate radio buttons in form. Populate from database
        Config patternConfig = new Config(GenPurposeDetailsController.class.getResource("genpurpose-details.fxml"))
                .updateViewModel("genPurposeViewModel", (GenPurposeViewModel genPurposeViewModel) -> {
                    genPurposeViewModel.setPropertyValue(VIEW_PROPERTIES, getViewProperties())
                            .setPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC, journalTopic)
                            .setPropertyValue(WINDOW_TOPIC, getWindowTopic())
//                                .setPropertyValue(STAMP_VIEW_MODEL, stampViewModel)
                            .setPropertyValue(FIELDS_COLLECTION, new ArrayList<String>()) // Ordered collection of Fields
                            .setPropertyValue(REF_COMPONENT, entityFacade)
//                                .setPropertyValue(SEMANTIC, semanticComponent)
//                                .setPropertyValue(PATTERN, patternFacade))
                    ;
                    genPurposeViewModel.setMode(mode);
                });

        // Create pattern window
        jfxNode = FXMLMvvmLoader.make(patternConfig);

        // Getting the concept window pane
        paneWindow = jfxNode.node();

        // Establish the window's KL ViewContext on the root pane now that paneWindow exists (#660).
        establishViewContext();

        // Calls the remove method to remove and concepts that were closed by the user.
        jfxNode.controller().setOnCloseConceptWindow(windowEvent -> {
            getOnClose().ifPresent(Runnable::run);
            // TODO more clean up such as view models and listeners just in case (memory).
        });

        // tracks viewModel changes and save them into preferences
        listenToEntityChanges();
    }

    public void initKLWindowPreferences(KometPreferences klWindowPreferences, ViewProperties viewProperties) {
        this.klEditorWindowPreferences = klWindowPreferences;
        // Initialize the controller with the window's derived coordinate (#660), not the raw arg.
        jfxNode.controller().init(klWindowPreferences, getViewProperties());
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
    protected void captureAdditionalState(EntityKlWindowState state) {
        super.captureAdditionalState(state);
        // Persist which KL-editor window definition this window realizes (title + folder), so the
        // factory can re-resolve and re-initialize it on restore (komet-desktop#20).
        if (klEditorWindowPreferences != null) {
            Path path = Paths.get(klEditorWindowPreferences.absolutePath());
            state.addProperty(KL_EDITOR_WINDOW_TITLE, path.getFileName().toString());
            state.addProperty(KL_EDITOR_WINDOW_DIR, path.getParent().getFileName().toString());
        }
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
        Optional<ViewModel> optGenPurposeViewModel = jfxNode.getViewModel("genPurposeViewModel");
        optGenPurposeViewModel.ifPresent(( vm) -> {
            // Listen to semantic changes (caused by a newly commited semantic)
            ObjectProperty<EntityFacade> refComponentProperty = vm.getProperty(REF_COMPONENT);
            refComponentProperty.subscribe((eF) -> {
                this.setEntityFacade(eF);
                // save to preference
                this.save(); // call captureAdditionalState of AbstractEntityChapterKLWindow
            });

        });
    }
}