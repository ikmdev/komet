package dev.ikm.komet.kview.mvvm.view.landingpage;

import dev.ikm.komet.framework.preferences.PrefX;
import dev.ikm.komet.kview.events.CreateKLEditorWindowEvent;
import dev.ikm.komet.kview.events.KLEditorWindowCreatedEvent;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import dev.ikm.tinkar.events.EvtBus;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.Subscriber;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

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
        final UUID klEditorTopic = UUID.randomUUID();

        eventBus.publish(KL_TOPIC,
                new CreateKLEditorWindowEvent(event.getSource(), CreateKLEditorWindowEvent.CREATE_KL_WINDOW, klWindowSettingsObjectMap));

        Subscriber<KLEditorWindowCreatedEvent> windowCreatedSubscriber = evt -> {
            createAndAddCard(evt.getWindowTitle());
        };
        eventBus.subscribe(KL_TOPIC, KLEditorWindowCreatedEvent.class, windowCreatedSubscriber);

        LOG.info("KL EDITOR WINDOW LAUNCHED");
    }

    private void loadPreferencesForKLLandingPage() {
        final KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        final KometPreferences klEditorAppPreferences = appPreferences.node(KL_EDITOR_APP);

        List<String> editorWindows = klEditorAppPreferences.getList(KL_EDITOR_WINDOWS);

        for (String windowTitle : editorWindows) {
            createAndAddCard(windowTitle);
        }
    }

    private void createAndAddCard(String title) {
        String titleCapitalized = title.substring(0, 1).toUpperCase() + title.substring(1);

        VBox cardMainContainer = new VBox();
        cardMainContainer.getStyleClass().add("card");

        StackPane iconContainer = new StackPane();
        iconContainer.getStyleClass().add("icon-container");
        Region icon = new Region();
        icon.getStyleClass().addAll("icon", "kl-editable-layout");
        iconContainer.getChildren().add(icon);

        HBox bottomContainer = new HBox();
        bottomContainer.getStyleClass().add("bottom-container");
        Label titleLabel = new Label(titleCapitalized);
        bottomContainer.getChildren().add(titleLabel);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        cardMainContainer.getChildren().addAll(iconContainer, bottomContainer);

        customViewsContainer.getChildren().add(cardMainContainer);
    }
}
