package dev.ikm.komet.kview.mvvm.viewmodel;

import dev.ikm.komet.framework.events.Evt;
import dev.ikm.komet.framework.events.EvtBusFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.carlfx.cognitive.viewmodel.SimpleViewModel;

import java.util.UUID;

public class ClosePropertiesViewModel  extends SimpleViewModel {

    // properties
    public static final String NOTIFICATION_TOPIC = "notificationTopic";
    public static final String NOTIFICATION_EVENT = "notificationEvent";

    private StringProperty title = new SimpleStringProperty();
    private StringProperty message = new SimpleStringProperty();
    private ConfirmationMessages confirmationMessage;

    public ClosePropertiesViewModel() {
        addProperty(NOTIFICATION_TOPIC, (UUID) null);
        addProperty(NOTIFICATION_EVENT, (Evt) null);
    }

    public void setConfirmationMessage(ConfirmationMessages confirmationMessage) {
        this.confirmationMessage = confirmationMessage;

        title.setValue(confirmationMessage.getConfirmationText().title());
        message.setValue(confirmationMessage.getConfirmationText().message());
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

    public record ConfirmationText(String title, String message) {}

    public enum ConfirmationMessages {

        NO_SELECTION_MADE_SEMANTIC(new ConfirmationText("No Selection Made", "Make a selection in the view to edit the Semantic.")),
        SEMANTIC_DETAILS_ADDED(new ConfirmationText("Semantic Details Added", "Make a selection in the view to edit the Semantic."));

        private ConfirmationText confirmationText;

        private ConfirmationMessages(ConfirmationText confirmationText) {
            this.confirmationText = confirmationText;
        }

        ConfirmationText getConfirmationText() {
            return confirmationText;
        }

    }

}
