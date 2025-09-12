package dev.ikm.komet.kview.klwindows.pattern;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.klwindows.AbstractEntityChapterKlWindow;
import dev.ikm.komet.kview.klwindows.EntityKlWindowType;
import dev.ikm.komet.kview.klwindows.EntityKlWindowTypes;
import dev.ikm.komet.kview.mvvm.view.pattern.PatternDetailsController;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel;
import dev.ikm.komet.kview.state.pattern.PatternDetailsPattern;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import org.carlfx.axonic.StateMachine;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;

import java.util.Optional;
import java.util.UUID;

import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.fetchDescendentsOfConcept;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.*;

/**
 * The General Editing Chapter window showing semantic details on the Journal Window's surface as a JavaFX Pane
 * having the ability to resize, drag, close.
 */
public class PatternKlWindow extends AbstractEntityChapterKlWindow {

    private final JFXNode<Pane, PatternDetailsController> jfxNode;

    /**
     * Constructs a new pattern window for a specific semantic entity.
     *
     * @param journalTopic   the UUID representing the journal topic the owning Journal Window uses to communicate events.
     * @param entityFacade   entity facade when not null usually this will load and display the current details.
     * @param viewProperties view properties is access to view calculators to query data.
     * @param preferences    komet preferences assists on reading and writing data to preferences user.home/Solor/database_folder/preferences.
     */
    public PatternKlWindow(UUID journalTopic, EntityFacade entityFacade,
                           ViewProperties viewProperties, KometPreferences preferences) {
        super(journalTopic, entityFacade, viewProperties, preferences);

        String mode;
        if (entityFacade != null) {
            mode = EDIT;
        } else {
            mode = CREATE;
        }

        // Prefetch modules and paths for view to populate radio buttons in form. Populate from database
        StateMachine patternSM = StateMachine.create(new PatternDetailsPattern());
        Config patternConfig = new Config(PatternDetailsController.class.getResource("pattern-details.fxml"))
                .updateViewModel("patternViewModel", (PatternViewModel patternViewModel) ->
                        patternViewModel.setPropertyValue(VIEW_PROPERTIES, viewProperties)
                                .setPropertyValue(MODE, mode)
                                .setPropertyValue(PATTERN_TOPIC, getWindowTopic())
                                .setPropertyValue(STATE_MACHINE, patternSM)
                                .setPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC, journalTopic)
                                .setPropertyValue(PATTERN, entityFacade));

        // Create pattern window
        jfxNode = FXMLMvvmLoader.make(patternConfig);
        Optional<PatternViewModel> optPatternViewModel = jfxNode.getViewModel("patternViewModel");
        // Here we load the Pattern Data.
        optPatternViewModel.ifPresent(PatternViewModel::loadPatternValues);

        // Getting the concept window pane
        paneWindow = jfxNode.node();

        // Calls the remove method to remove and concepts that were closed by the user.
        jfxNode.controller().setOnClosePatternWindow(windowEvent -> {
            getOnClose().ifPresent(Runnable::run);
            // TODO more clean up such as view models and listeners just in case (memory).
        });
    }

    /**
     * Called when the window is shown.
     */
    @Override
    public void onShown() {
        Platform.runLater( () -> jfxNode.controller().putTitlePanesArrowOnRight());
    }

    @Override
    public EntityKlWindowType getWindowType() {
        return EntityKlWindowTypes.PATTERN;
    }

    @Override
    protected boolean isPropertyPanelOpen() {
        return jfxNode.controller().isPropertiesPanelOpen();
    }

    @Override
    protected void setPropertyPanelOpen(boolean isOpen) {
        jfxNode.controller().setPropertiesPanelOpen(isOpen);
    }
}