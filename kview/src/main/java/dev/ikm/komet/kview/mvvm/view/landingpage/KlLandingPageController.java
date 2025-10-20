package dev.ikm.komet.kview.mvvm.view.landingpage;

import dev.ikm.komet.framework.preferences.PrefX;
import dev.ikm.komet.kview.events.CreateKLEditorWindowEvent;
import dev.ikm.tinkar.events.EvtBus;
import dev.ikm.tinkar.events.EvtBusFactory;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static dev.ikm.komet.kview.events.EventTopics.KL_TOPIC;

public class KlLandingPageController {
    private static final Logger LOG = LoggerFactory.getLogger(KlLandingPageController.class);

    private final EvtBus eventBus = EvtBusFactory.getDefaultEvtBus();

    @FXML
    public void initialize() {
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
                new CreateKLEditorWindowEvent(event.getSource(), CreateKLEditorWindowEvent.CREATE_KL_EDITOR, klWindowSettingsObjectMap));

        LOG.info("KL EDITOR WINDOW LAUNCHED");
    }
}
