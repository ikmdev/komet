package dev.ikm.komet.kview.mvvm.viewmodel.confirmation;

import dev.ikm.komet.framework.events.Evt;
import dev.ikm.komet.framework.events.EvtBusFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.carlfx.cognitive.viewmodel.SimpleViewModel;

import java.util.UUID;

/**
 * The view model for the confirmation pane.  This view model manages the text that is displayed
 * on the confirmation pane, and the notification topic and event that is sent when the close
 * button is pressed.
 */
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

    /**
     * Send the notification to the provided topic using the provided event.
     */
    public void sendNotification() {
        var topic = getNotifiicationTopic();
        var event = getNotificationEvent();

        if (topic != null && event != null) {
            EvtBusFactory.getDefaultEvtBus().publish(topic, event);
        }
    }

    /**
     * Set the confirmation message to display in the title and message labels.
     * @param confirmationMessage The message to display
     */
    public void setConfirmationMessage(ConfirmationMessages confirmationMessage) {
        if (confirmationMessage != null) {
            this.confirmationMessage = confirmationMessage;

            title.setValue(confirmationMessage.getConfirmationText().title());
            message.setValue(confirmationMessage.getConfirmationText().message());
        }
    }

}
