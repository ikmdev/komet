package dev.ikm.komet.kview.mvvm.view.landingpage;

import dev.ikm.komet.framework.preferences.PrefX;
import dev.ikm.komet.kview.events.CreateKLEditorWindowEvent;
import dev.ikm.komet.layout.editor.StandardEditorWindows;
import dev.ikm.komet.kview.events.KLEditorWindowCreatedOrRemovedEvent;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import dev.ikm.tinkar.events.EvtBus;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.Subscriber;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.kview.events.EventTopics.KL_TOPIC;
import static dev.ikm.komet.preferences.KLEditorPreferences.KL_EDITOR_APP;
import static dev.ikm.komet.preferences.KLEditorPreferences.KL_EDITOR_WINDOWS;
import static dev.ikm.komet.preferences.KLEditorPreferences.KL_USER_WINDOWS_DIR;

public class KlLandingPageController {
    private static final Logger LOG = LoggerFactory.getLogger(KlLandingPageController.class);

    private final EvtBus eventBus = EvtBusFactory.getDefaultEvtBus();

    private Subscriber<KLEditorWindowCreatedOrRemovedEvent> windowCreatedSubscriber;

    private final List<KLLandingPageCardControl> landingPageCards = new ArrayList<>();

    @FXML
    private FlowPane customViewsContainer;

    @FXML
    public void initialize() {
        // Load the preferences for the KLEditor Landing page
        loadPreferencesForKLLandingPage();

        // Subscribe to KL Windows being saved. The editor fires KL_EDITOR_WINDOW_CREATED on
        // every save, including re-saves of existing windows, so only add a card when there
        // isn't one for that title yet.
        windowCreatedSubscriber = evt -> {
            if (evt.getEventType() == KLEditorWindowCreatedOrRemovedEvent.KL_EDITOR_WINDOW_CREATED
                    && !cardExists(evt.getWindowTitle())) {
                createAndAddCard(evt.getWindowTitle());
            }
        };
        eventBus.subscribe(KL_TOPIC, KLEditorWindowCreatedOrRemovedEvent.class, windowCreatedSubscriber);
    }

    private boolean cardExists(String windowTitle) {
        return landingPageCards.stream()
                .anyMatch(card -> card.getTitle().equals(windowTitle));
    }

    @FXML
    private void mousePressedOnCreateComponentLayout(MouseEvent mouseEvent) {
        createNewKLEditorWindow(mouseEvent);
    }

    @FXML
    private void mousePressedOnConceptView(MouseEvent mouseEvent) {
        loadStandardKLEditorWindow(mouseEvent, StandardEditorWindows.CONCEPT_WINDOW_2);
    }

    @FXML
    private void mousePressedOnPatternView(MouseEvent mouseEvent) {
        loadStandardKLEditorWindow(mouseEvent, StandardEditorWindows.PATTERN_WINDOW_2);
    }

    /**
     * Opens the KL Editor with a standard (application-provided) window loaded for editing.
     *
     * @param event       the mouse event from the landing page card
     * @param windowTitle the title of the standard window to load
     */
    private void loadStandardKLEditorWindow(Event event, String windowTitle) {
        final PrefX klWindowSettingsObjectMap = PrefX.create();
        eventBus.publish(KL_TOPIC,
                new CreateKLEditorWindowEvent(event.getSource(), CreateKLEditorWindowEvent.CREATE_KL_WINDOW,
                        klWindowSettingsObjectMap, windowTitle, true));
    }

    private void createNewKLEditorWindow(Event event) {
        // publish the event that the new KLEditor Window button was pressed
        final PrefX klWindowSettingsObjectMap = PrefX.create();
        // final UUID klEditorTopic = UUID.randomUUID();
        eventBus.publish(KL_TOPIC,
                new CreateKLEditorWindowEvent(event.getSource(), CreateKLEditorWindowEvent.CREATE_KL_WINDOW, klWindowSettingsObjectMap, null));

        LOG.info("KL EDITOR WINDOW LAUNCHED");
    }

    private void loadKLEditorWindow(Event event, String windowTitle) {
        // publish the event that the new KLEditor Window button was pressed
        final PrefX klWindowSettingsObjectMap = PrefX.create();
        // final UUID klEditorTopic = UUID.randomUUID();
        eventBus.publish(KL_TOPIC,
                new CreateKLEditorWindowEvent(event.getSource(), CreateKLEditorWindowEvent.CREATE_KL_WINDOW, klWindowSettingsObjectMap, windowTitle));

    }

    private void loadPreferencesForKLLandingPage() {
        final KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        final KometPreferences userWindowsPreferences = appPreferences.node(KL_EDITOR_APP).node(KL_USER_WINDOWS_DIR);

        List<String> editorWindows = userWindowsPreferences.getList(KL_EDITOR_WINDOWS);

        for (String windowTitle : editorWindows) {
            createAndAddCard(windowTitle);
        }
    }

    private void createAndAddCard(String windowTitle) {
        KLLandingPageCardControl card = new KLLandingPageCardControl();
        card.setOnMouseClicked(event -> loadKLEditorWindow(event, windowTitle));
        card.setDeletable(true);
        card.setOnDeleteAction(() -> onDeleteCard(card));
        card.setTitle(windowTitle);

        landingPageCards.add(card);
        customViewsContainer.getChildren().add(card);
    }

    private void onDeleteCard(KLLandingPageCardControl card) {
        landingPageCards.remove(card);
        customViewsContainer.getChildren().remove(card);

        String windowTitle = card.getTitle();

        // Remove from preferences
        final KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        final KometPreferences userWindowsPreferences = appPreferences.node(KL_EDITOR_APP).node(KL_USER_WINDOWS_DIR);

        List<String> editorWindows = userWindowsPreferences.getList(KL_EDITOR_WINDOWS);
        editorWindows.remove(windowTitle);

        userWindowsPreferences.putList(KL_EDITOR_WINDOWS, editorWindows);

        final KometPreferences editorWindowPreferences = userWindowsPreferences.node(windowTitle);
        try {
            editorWindowPreferences.removeNode();
        } catch (BackingStoreException e) {
            LOG.error("Error deleting KL Editor Window node from preferences", e);
        }

        try {
            editorWindowPreferences.flush();
            userWindowsPreferences.flush();
        } catch (BackingStoreException e) {
            LOG.error("Error deleting  KL Editor Window in preferences", e);
        }

        // fire card remove event
        eventBus.publish(KL_TOPIC,
                new KLEditorWindowCreatedOrRemovedEvent(card, KLEditorWindowCreatedOrRemovedEvent.KL_EDITOR_WINDOW_REMOVED, windowTitle));

    }
}
