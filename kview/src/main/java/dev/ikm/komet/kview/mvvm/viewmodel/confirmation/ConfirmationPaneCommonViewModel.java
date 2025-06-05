package dev.ikm.komet.kview.mvvm.viewmodel.confirmation;

import dev.ikm.komet.framework.events.Evt;
import dev.ikm.komet.framework.events.EvtBusFactory;
import javafx.beans.property.StringProperty;
import org.carlfx.cognitive.viewmodel.SimpleViewModel;
import org.eclipse.jgit.util.StringUtils;

import java.util.UUID;

/**
 * The view model for the confirmation pane.  This view model manages the text that is displayed
 * on the confirmation pane, and the notification topic and event that is sent when the close
 * button is pressed.
 */
public class ConfirmationPaneCommonViewModel extends SimpleViewModel {

    public enum Properties {
        NOTIFICATION_TOPIC,
        NOTIFICATION_EVENT,
        CONFIRMATION_TITLE,
        CONFIRMATION_MESSAGE
    }

    public ConfirmationPaneCommonViewModel() {
        addProperty(Properties.NOTIFICATION_TOPIC, (UUID) null);
        addProperty(Properties.NOTIFICATION_EVENT, (Evt) null);
        addProperty(Properties.CONFIRMATION_TITLE, (String) null);
        addProperty(Properties.CONFIRMATION_MESSAGE, (String) null);
    }

    public StringProperty getTitleProperty() {
        return getProperty(Properties.CONFIRMATION_TITLE);
    }

    public StringProperty getMessageProperty() {
        return getProperty(Properties.CONFIRMATION_MESSAGE);
    }

    public String getTitle() {
        return getPropertyValue(Properties.CONFIRMATION_TITLE);
    }

    public void setTitle(String title) {
        if (!StringUtils.isEmptyOrNull(title)) {
            setPropertyValue(Properties.CONFIRMATION_TITLE, title);
        }
    }

    public String getMessage() {
        return getPropertyValue(Properties.CONFIRMATION_MESSAGE);
    }

    public void setMessage(String message) {
        setPropertyValue(Properties.CONFIRMATION_MESSAGE, message);
    }

    public void setConfirmationText(String title, String message) {
        if (!StringUtils.isEmptyOrNull(title)) {
            setTitle(title);
            setMessage(message);
        }
    }

    public UUID getNotifiicationTopic() {
        return getPropertyValue(Properties.NOTIFICATION_TOPIC);
    }

    public void setNotificationTopic(UUID notifiicationTopic) {
        setPropertyValue(Properties.NOTIFICATION_TOPIC, notifiicationTopic);
    }

    public Evt getNotificationEvent() {
        return getPropertyValue(Properties.NOTIFICATION_EVENT);
    }

    public void setNotificationEvent(Evt notifiationEvent) {
        setPropertyValue(Properties.NOTIFICATION_EVENT, notifiationEvent);
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
            setPropertyValue(Properties.CONFIRMATION_TITLE, confirmationMessage.getConfirmationText().title());
            setPropertyValue(Properties.CONFIRMATION_MESSAGE, confirmationMessage.getConfirmationText().message());
        }
    }

}
