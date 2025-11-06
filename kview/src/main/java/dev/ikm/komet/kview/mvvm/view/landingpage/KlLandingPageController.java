package dev.ikm.komet.kview.mvvm.view.landingpage;

import dev.ikm.komet.framework.preferences.PrefX;
import dev.ikm.komet.kview.events.CreateKLEditorWindowEvent;
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

import java.util.List;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.kview.events.EventTopics.KL_TOPIC;
import static dev.ikm.komet.preferences.KLEditorPreferences.KL_EDITOR_APP;
import static dev.ikm.komet.preferences.KLEditorPreferences.KL_EDITOR_WINDOWS;

public class KlLandingPageController {
    private static final Logger LOG = LoggerFactory.getLogger(KlLandingPageController.class);

    private final EvtBus eventBus = EvtBusFactory.getDefaultEvtBus();

    @FXML
    private FlowPane customViewsContainer;

    @FXML
    public void initialize() {
        // Load the preferences for the KLEditor Landing page
        loadPreferencesForKLLandingPage();
    }

    @FXML
    private void mousePressedOnCreateEditableLayout(MouseEvent mouseEvent) {
        createNewKLEditorWindow(mouseEvent);
    }

    private void createNewKLEditorWindow(Event event) {
        // publish the event that the new KLEditor Window button was pressed
        final PrefX klWindowSettingsObjectMap = PrefX.create();
        // final UUID klEditorTopic = UUID.randomUUID();
        eventBus.publish(KL_TOPIC,
                new CreateKLEditorWindowEvent(event.getSource(), CreateKLEditorWindowEvent.CREATE_KL_WINDOW, klWindowSettingsObjectMap, null));

        // Subscribe to new KL Windows being created
        Subscriber<KLEditorWindowCreatedOrRemovedEvent> windowCreatedSubscriber = evt -> {
            if (evt.getEventType() == KLEditorWindowCreatedOrRemovedEvent.KL_EDITOR_WINDOW_CREATED) {
                createAndAddCard(evt.getWindowTitle());
            }
        };
        eventBus.subscribe(KL_TOPIC, KLEditorWindowCreatedOrRemovedEvent.class, windowCreatedSubscriber);

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
        final KometPreferences klEditorAppPreferences = appPreferences.node(KL_EDITOR_APP);

        List<String> editorWindows = klEditorAppPreferences.getList(KL_EDITOR_WINDOWS);

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

        customViewsContainer.getChildren().add(card);
    }

    private void onDeleteCard(KLLandingPageCardControl card) {
        customViewsContainer.getChildren().remove(card);

        String windowTitle = card.getTitle();

        // Remove from preferences
        final KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        final KometPreferences klEditorAppPreferences = appPreferences.node(KL_EDITOR_APP);

        List<String> editorWindows = klEditorAppPreferences.getList(KL_EDITOR_WINDOWS);
        editorWindows.remove(windowTitle);

        klEditorAppPreferences.putList(KL_EDITOR_WINDOWS, editorWindows);

        try {
            klEditorAppPreferences.flush();
        } catch (BackingStoreException e) {
            LOG.error("Error deleting  KL Editor Window from preferences", e);
        }

        // fire card remove event
        eventBus.publish(KL_TOPIC,
                new KLEditorWindowCreatedOrRemovedEvent(card, KLEditorWindowCreatedOrRemovedEvent.KL_EDITOR_WINDOW_REMOVED, windowTitle));

    }
}
