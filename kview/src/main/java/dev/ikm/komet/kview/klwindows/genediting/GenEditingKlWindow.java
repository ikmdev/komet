package dev.ikm.komet.kview.klwindows.genediting;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.klwindows.AbstractEntityChapterKlWindow;
import dev.ikm.komet.kview.mvvm.view.genediting.GenEditingDetailsController;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import javafx.scene.layout.Pane;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;

import java.util.ArrayList;
import java.util.UUID;

import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.*;

/**
 * The General Editing Chapter window showing semantic details on the Journal Window's surface as a JavaFX Pane
 * having the ability to resize, drag, close.
 */
public class GenEditingKlWindow extends AbstractEntityChapterKlWindow {

    /**
     * Root container for the FXML UI and its controller.
     */
    private JFXNode<Pane, GenEditingDetailsController> jfxNode;

    private UUID windowTopic;

    /**
     * Constructs a new editing window for a specific semantic entity.
     *
     * @param journalTopic   the UUID representing the journal topic the owning Journal Window uses to communicate events.
     * @param entityFacade   the semantic when in edit mode, the pattern when in create mode
     * @param viewProperties view properties is access to view calculators to query data.
     * @param preferences    komet preferences assists on reading and writing data to preferences user.home/Solor/database_folder/preferences
     */
    public GenEditingKlWindow(UUID journalTopic, EntityFacade entityFacade, ViewProperties viewProperties, KometPreferences preferences) {
        super(journalTopic, entityFacade, viewProperties, preferences);

        EntityFacade refComponent;
        EntityFacade semanticComponent;
        PatternFacade patternFacade;
        if (entityFacade instanceof PatternFacade) {
            patternFacade = (PatternFacade) entityFacade;
            refComponent = null;
            semanticComponent = null;
        } else {
            patternFacade = null;
            semanticComponent = entityFacade;
            SemanticEntity entity = (SemanticEntity) EntityService.get().getEntity(entityFacade.nid()).get();
            refComponent = EntityService.get().getEntity(entity.referencedComponentNid()).get();
        }
        windowTopic = UUID.randomUUID();
        Config config = new Config(GenEditingDetailsController.class.getResource("genediting-details.fxml"))
                .updateViewModel("genEditingViewModel", genEditingViewModel ->
                        genEditingViewModel.setPropertyValue(VIEW_PROPERTIES, viewProperties)
                                .setPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC, journalTopic)
                                .setPropertyValue(WINDOW_TOPIC, windowTopic)
//                                .setPropertyValue(STAMP_VIEW_MODEL, stampViewModel)
                                .setPropertyValue(FIELDS_COLLECTION, new ArrayList<String>()) // Ordered collection of Fields
                                .setPropertyValue(REF_COMPONENT, refComponent)
                                .setPropertyValue(SEMANTIC, semanticComponent)
                                .setPropertyValue(PATTERN, patternFacade));

        // Create chapter window
        jfxNode = FXMLMvvmLoader.make(config);

        // Getting the concept window pane
        this.paneWindow = jfxNode.node();

        // Calls the remove method to remove and concepts that were closed by the user.
        jfxNode.controller().setOnCloseConceptWindow(windowEvent -> {
            getOnClose().ifPresent(Runnable::run);
            // TODO more clean up such as view models and listeners just in case (memory).
        });
    }

    /**
     * Called when the window is shown.
     */
    public void onShown() {
        jfxNode.controller().putTitlePanesArrowOnRight();
    }

    public UUID getWindowTopic() {
        return windowTopic;
    }
}
