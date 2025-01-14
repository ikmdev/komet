package dev.ikm.komet.kview.klwindows.pattern;

import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CREATE;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.EDIT;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.MODE;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.PATTERN;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.PATTERN_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.STAMP_VIEW_MODEL;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.STATE_MACHINE;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel.MODULES_PROPERTY;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel.PATHS_PROPERTY;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.AUTHOR;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.MODULE;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.PATH;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.STATUS;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.TIME;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.fxutils.window.WindowSupport;
import dev.ikm.komet.kview.klwindows.AbstractEntityChapterKlWindow;
import dev.ikm.komet.kview.mvvm.view.pattern.PatternDetailsController;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel;
import dev.ikm.komet.kview.state.pattern.PatternDetailsPattern;
import dev.ikm.komet.layout.window.KlWindow;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import org.carlfx.axonic.StateMachine;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;

import java.util.Optional;
import java.util.UUID;

/**
 * The General Editing Chapter window showing semantic details on the Journal Window's surface as a JavaFX Pane
 * having the ability to resize, drag, close.
 */
public class PatternKlWindow extends AbstractEntityChapterKlWindow implements KlWindow {
    private JFXNode<Pane, PatternDetailsController> jfxNode;

    /**
     *
     * @param journalTopic
     * @param entityFacade
     * @param desktopSurface
     * @param viewProperties
     * @param preferences
     */
    public PatternKlWindow(UUID journalTopic, EntityFacade entityFacade, Pane desktopSurface, ViewProperties viewProperties, KometPreferences preferences) {
        super(journalTopic, entityFacade, viewProperties, preferences);
        //initialize stampsViewModel with basic data.
        StampViewModel stampViewModel = new StampViewModel();
        stampViewModel.setPropertyValue(PATHS_PROPERTY, stampViewModel.findAllPaths(viewProperties), true)
                .setPropertyValue(MODULES_PROPERTY, stampViewModel.findAllModules(viewProperties), true);

        String mode;
        if(entityFacade != null){
            mode = EDIT;
            Entity patternEntity = EntityService.get().getEntity(entityFacade.nid()).get();
            // populate STAMP values
            Latest<EntityVersion> patternStamp = viewProperties.calculator().stampCalculator().latest(patternEntity);
            stampViewModel.setPropertyValue(STATUS, patternStamp.get().stamp().state())
                    .setPropertyValue(TIME, patternStamp.get().stamp().time())
                    .setPropertyValue(AUTHOR, TinkarTerm.USER)
                    .setPropertyValue(MODULE, patternStamp.get().stamp().module())
                    .setPropertyValue(PATH, patternStamp.get().stamp().path())
            ;
        } else {
            mode = CREATE;
        }
        stampViewModel.setPropertyValue(MODE, mode);
        // Prefetch modules and paths for view to populate radio buttons in form. Populate from database
        StateMachine patternSM = StateMachine.create(new PatternDetailsPattern());
        Config patternConfig = new Config(PatternDetailsController.class.getResource("pattern-details.fxml"))
                .updateViewModel("patternViewModel", (PatternViewModel patternViewModel) -> {
                    patternViewModel.setPropertyValue(VIEW_PROPERTIES, viewProperties)
                            .setPropertyValue(MODE, mode)
                            .setPropertyValue(STAMP_VIEW_MODEL, stampViewModel)
                            .setPropertyValue(PATTERN_TOPIC, UUID.randomUUID())
                            .setPropertyValue(STATE_MACHINE, patternSM)
                            .setPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC, journalTopic)
                            .setPropertyValue(PATTERN, entityFacade);
                });

        // create pattern window
        jfxNode = FXMLMvvmLoader.make(patternConfig);
        Optional<PatternViewModel> optPatternViewModel = jfxNode.getViewModel("patternViewModel");
        //Here we load the Pattern Data.
        optPatternViewModel.ifPresent(patternViewModel -> patternViewModel.loadPatternValues());

        //Getting the concept window pane
        setPaneWindow(jfxNode.node());

        WindowSupport windowSupport = new WindowSupport(getPaneWindow(), desktopSurface);

        //Calls the remove method to remove and concepts that were closed by the user.
        jfxNode.controller().setOnCloseConceptWindow(windowEvent -> {
            // TODO more clean up such as view models and listeners just in case (memory).
            getOnClose().ifPresent(Runnable::run);
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