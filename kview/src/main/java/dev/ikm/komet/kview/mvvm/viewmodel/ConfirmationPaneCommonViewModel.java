package dev.ikm.komet.kview.mvvm.viewmodel;

import dev.ikm.komet.framework.events.Evt;
import dev.ikm.komet.framework.events.EvtBusFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.carlfx.cognitive.viewmodel.SimpleViewModel;

import java.util.UUID;

public class ConfirmationPaneCommonViewModel extends SimpleViewModel {

    // properties
    public static final String NOTIFICATION_TOPIC = "notificationTopic";
    public static final String NOTIFICATION_EVENT = "notificationEvent";

    private StringProperty title = new SimpleStringProperty();
    private StringProperty message = new SimpleStringProperty();
    private ConfirmationMessages confirmationMessage;

    public ConfirmationPaneCommonViewModel() {
        addProperty(NOTIFICATION_TOPIC, (UUID) null);
        addProperty(NOTIFICATION_EVENT, (Evt) null);
    }

    public StringProperty titleProperty() {
        return title;
    }

    public StringProperty messageProperty() {
        return message;
    }

    public UUID getNotifiicationTopic() {
        return getPropertyValue(NOTIFICATION_TOPIC);
    }

    public void setNotificationTopic(UUID notifiicationTopic) {
        setPropertyValue(NOTIFICATION_TOPIC, notifiicationTopic);
    }

    public Evt getNotificationEvent() {
        return getPropertyValue(NOTIFICATION_EVENT);
    }

    public void setNotificationEvent(Evt notifiationEvent) {
        setPropertyValue(NOTIFICATION_EVENT, notifiationEvent);
    }

    public void sendNotification() {
        EvtBusFactory.getDefaultEvtBus().publish(getNotifiicationTopic(), getNotificationEvent());
    }

    public void setConfirmationMessage(ConfirmationMessages confirmationMessage) {
        if (confirmationMessage != null) {
            this.confirmationMessage = confirmationMessage;

            title.setValue(confirmationMessage.getConfirmationText().title());
            message.setValue(confirmationMessage.getConfirmationText().message());
        }
    }

    public record ConfirmationText(String title, String message) {}

    private static final String SEMANTIC_DEFAULT_MESSAGE = "Make a selection in the view to edit the Semantic.";
    private static final String PATTERN_DEFAULT_MESSAGE = "Make a selection in the view to edit the Pattern.";

    public enum ConfirmationMessages {

        NO_SELECTION_MADE_SEMANTIC(new ConfirmationText("No Selection Made", SEMANTIC_DEFAULT_MESSAGE)),
        SEMANTIC_DETAILS_ADDED(new ConfirmationText("Semantic Details Added", SEMANTIC_DEFAULT_MESSAGE)),

        NO_SELECTION_MADE_PATTERN(new ConfirmationText("No Selection Made", PATTERN_DEFAULT_MESSAGE)),
        PATTERN_DEFINITION_ADDED(new ConfirmationText("Pattern Definition Added", "")),
        FULLY_QUALIFIED_NAME_ADDED(new ConfirmationText("Fully Qualified Name Added", "")),
        OTHER_NAME_ADDED(new ConfirmationText("Other Name Added", "")),
        DEFINITION_ADDED(new ConfirmationText("Definition Added", "")),
        CONTINUE_EDITING_FIELDS(new ConfirmationText("Continue Editing Fields?", ""));

        private ConfirmationText confirmationText;

        private ConfirmationMessages(ConfirmationText confirmationText) {
            this.confirmationText = confirmationText;
        }

        ConfirmationText getConfirmationText() {
            return confirmationText;
        }

    }

}
