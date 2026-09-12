package dev.ikm.komet.kview.klwindows.genpurpose;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.klwindows.AbstractEntityChapterKlWindow;
import dev.ikm.komet.kview.klwindows.EntityKlWindowState;
import dev.ikm.komet.kview.klwindows.EntityKlWindowType;
import dev.ikm.komet.kview.klwindows.EntityKlWindowTypes;
import dev.ikm.komet.kview.mvvm.view.concept.ConceptNode;
import dev.ikm.komet.kview.mvvm.view.genpurpose.GenPurposeDetailsController;
import dev.ikm.komet.kview.mvvm.view.genpurpose.GenPurposeWindowView;
import dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.FormMode;
import dev.ikm.komet.kview.mvvm.viewmodel.GenPurposeViewModel;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;

import static dev.ikm.komet.kview.mvvm.viewmodel.ViewModelKey.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.ViewModelKey.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.ViewModelKey.FIELDS_COLLECTION;
import static dev.ikm.komet.kview.mvvm.viewmodel.ViewModelKey.REF_COMPONENT;
import static dev.ikm.komet.kview.mvvm.viewmodel.ViewModelKey.WINDOW_TOPIC;

public class GenPurposeKLWindow extends AbstractEntityChapterKlWindow {

    /** Preference key holding the title of the KL-editor window definition this window is built from. */
    public static final String KL_EDITOR_WINDOW_TITLE = "KL_EDITOR_WINDOW_TITLE";

    /**
     * Preference key holding the kl-editor-app folder the definition lives in:
     * {@code user-windows} or {@code standard-windows}.
     */
    public static final String KL_EDITOR_WINDOW_DIR = "KL_EDITOR_WINDOW_DIR";

    /**
     * The {@code kl-editor-app/{user,standard}-windows/<title>} node of the KL-editor window
     * definition this window is built from. Persisted by {@link #captureAdditionalState} so the
     * definition can be re-resolved when the journal is reopened (komet-desktop#20).
     */
    private final KometPreferences klEditorWindowPreferences;

    private final GenPurposeViewModel genPurposeViewModel;

    private final GenPurposeDetailsController controller;

    /**
     * Constructs a general-purpose KL window built from a KL-editor window definition.
     *
     * @param journalTopic              the UUID representing the journal topic the owning Journal Window uses to communicate events.
     * @param entityFacade              entity facade when not null usually this will load and display the current details.
     * @param viewProperties            view properties is access to view calculators to query data.
     * @param preferences               komet preferences assists on reading and writing data to preferences user.home/Solor/database_folder/preferences
     * @param klEditorWindowPreferences the {@code kl-editor-app/{user,standard}-windows/<title>} node of the
     *                                  KL-editor window definition (sections, patterns, fields) to build the window from
     */
    public GenPurposeKLWindow(UUID journalTopic, EntityFacade entityFacade,
                              ViewProperties viewProperties, KometPreferences preferences,
                              KometPreferences klEditorWindowPreferences) {
        super(journalTopic, entityFacade, viewProperties, preferences);
        this.klEditorWindowPreferences = klEditorWindowPreferences;

        // No component to open means the window was launched from the Journal's "+" button to create a
        // new one; a supplied component means it was opened ("Open as ...") to edit that component.
        final FormMode mode = entityFacade == null ? FormMode.CREATE : FormMode.EDIT;

        genPurposeViewModel = new GenPurposeViewModel();
        genPurposeViewModel.setPropertyValue(VIEW_PROPERTIES, getViewProperties())
                .setPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC, journalTopic)
                .setPropertyValue(WINDOW_TOPIC, getWindowTopic())
                .setPropertyValue(FIELDS_COLLECTION, new ArrayList<String>()) // Ordered collection of Fields
                .setPropertyValue(REF_COMPONENT, entityFacade);
        genPurposeViewModel.setMode(mode);

        // The view is the window's root pane. The window's KL ViewContext goes on it before the
        // controller builds the definition's sections, whose KL areas resolve that context (#660).
        GenPurposeWindowView view = new GenPurposeWindowView();
        paneWindow = view;
        establishViewContext();

        // Wires the window's behavior onto the view for the definition, against the window's
        // derived coordinate (#660), not the raw journal one.
        controller = new GenPurposeDetailsController(view, genPurposeViewModel, klEditorWindowPreferences,
                getViewProperties());

        // Calls the remove method to remove and concepts that were closed by the user.
        controller.setOnCloseConceptWindow(windowEvent -> {
            getOnClose().ifPresent(Runnable::run);
            // TODO more clean up such as view models and listeners just in case (memory).
        });

        // tracks viewModel changes and save them into preferences
        listenToEntityChanges();
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
        // Persist which KL-editor window definition this window is built from (title + folder), so the
        // factory can re-resolve it on restore (komet-desktop#20).
        Path path = Paths.get(klEditorWindowPreferences.absolutePath());
        state.addProperty(KL_EDITOR_WINDOW_TITLE, path.getFileName().toString());
        state.addProperty(KL_EDITOR_WINDOW_DIR, path.getParent().getFileName().toString());
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
        // Listen to semantic changes (caused by a newly commited semantic)
        ObjectProperty<EntityFacade> refComponentProperty = genPurposeViewModel.getProperty(REF_COMPONENT);
        refComponentProperty.subscribe((eF) -> {
            this.setEntityFacade(eF);
            // save to preference
            this.save(); // call captureAdditionalState of AbstractEntityChapterKLWindow
        });
    }
}
