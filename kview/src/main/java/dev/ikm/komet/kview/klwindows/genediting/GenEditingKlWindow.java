package dev.ikm.komet.kview.klwindows.genediting;

import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.FIELDS_COLLECTION;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.REF_COMPONENT;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.SEMANTIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.WINDOW_TOPIC;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.fxutils.window.WindowSupport;
import dev.ikm.komet.kview.klwindows.AbstractEntityChapterKlWindow;
import dev.ikm.komet.kview.mvvm.view.genediting.GenEditingDetailsController;
import dev.ikm.komet.layout.window.KlWindow;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;

import java.util.ArrayList;
import java.util.UUID;

/**
 * The General Editing Chapter window showing semantic details on the Journal Window's surface as a JavaFX Pane
 * having the ability to resize, drag, close.
 */
public class GenEditingKlWindow extends AbstractEntityChapterKlWindow implements KlWindow {
    private JFXNode<Pane, GenEditingDetailsController> jfxNode;

    /**
     *
     * @param journalTopic
     * @param entityFacade
     * @param desktopSurface
     * @param viewProperties
     * @param preferences
     */
    public GenEditingKlWindow(UUID journalTopic, EntityFacade entityFacade, Pane desktopSurface,  ViewProperties viewProperties, KometPreferences preferences) {
        super(journalTopic, entityFacade, viewProperties, preferences);

        SemanticEntity entity = (SemanticEntity) EntityService.get().getEntity(entityFacade.nid()).get();
        EntityFacade refComponent = EntityService.get().getEntity(entity.referencedComponentNid()).get();

        Config config = new Config(GenEditingDetailsController.class.getResource("genediting-details.fxml"))
                .updateViewModel("genEditingViewModel", genEditingViewModel ->
                        genEditingViewModel.setPropertyValue(VIEW_PROPERTIES, viewProperties)
                                .setPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC, journalTopic)
                                .setPropertyValue(WINDOW_TOPIC, UUID.randomUUID())
//                                .setPropertyValue(STAMP_VIEW_MODEL, stampViewModel)
                                .setPropertyValue(FIELDS_COLLECTION, new ArrayList<String>()) // Ordered collection of Fields
                                .setPropertyValue(REF_COMPONENT, refComponent)
                                .setPropertyValue(SEMANTIC, entityFacade));

        // create chapter window
        jfxNode = FXMLMvvmLoader.make(config);

        //Getting the concept window pane
        Pane chapterWindow = jfxNode.node();
        WindowSupport windowSupport = new WindowSupport(chapterWindow, desktopSurface);
        // Set the JavaFX Pane as the visible UI (chapter window)
        setPaneWindow(chapterWindow);

        //Calls the remove method to remove and concepts that were closed by the user.
        jfxNode.controller().setOnCloseConceptWindow(windowEvent -> {
            getOnClose().ifPresent(Runnable::run);
            // TODO more clean up such as view models and listeners just in case (memory).
        });

    }
    public void onShown() {
        jfxNode.controller().putTitlePanesArrowOnRight();
    }
    @Override
    public Pane getPaneWindow() {
        return super.getPaneWindow();
    }
    @Override
    public Scene scene() {
        return null;
    }
}
