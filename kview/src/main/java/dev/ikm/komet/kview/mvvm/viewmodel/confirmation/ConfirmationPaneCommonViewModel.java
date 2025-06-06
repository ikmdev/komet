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

    /**
     * The properties of the view model.
     * <br>
     * Because the notification topic and event are NOT used in the view controller, they technically
     * don't need to be properties of the view model.  They could be instance variables instead.
     */
    public enum Properties {
        // not used in the view
        NOTIFICATION_TOPIC,
        // not used in the view
        NOTIFICATION_EVENT,

        CONFIRMATION_TITLE,
        CONFIRMATION_MESSAGE
    }

    /**
     * Initialize the SimpleViewModel with the properties.
     */
    public ConfirmationPaneCommonViewModel() {
        addProperty(Properties.NOTIFICATION_TOPIC, (UUID) null);
        addProperty(Properties.NOTIFICATION_EVENT, (Evt) null);
        addProperty(Properties.CONFIRMATION_TITLE, (String) null);
        addProperty(Properties.CONFIRMATION_MESSAGE, (String) null);
    }

    /**
     * Convenience method to get the title StringProperty.  Use this method to bind the view's
     * title to the property in the view model.
     * @return StringProperty of the confirmation title
     */
    public StringProperty getTitleProperty() {
        return getProperty(Properties.CONFIRMATION_TITLE);
    }

    /**
     * Convenience method to get the message StringProperty.  Use this method to bind the view's
     * message to the property in the view model.
     * @return StringProperty of the confirmation message
     */
    public StringProperty getMessageProperty() {
        return getProperty(Properties.CONFIRMATION_MESSAGE);
    }

    /**
     * Convenience method to get the title property value.
     * @return The title property value
     */
    public String getTitle() {
        return getPropertyValue(Properties.CONFIRMATION_TITLE);
    }

    /**
     * Convenience method to set the title property value.  This method only accepts non-null
     * values as the title is required for the confirmation pane.  The title property value is updated,
     * causing the title view display to change.
     * @param title The title String to display, which must be non-null
     */
    public void setTitle(String title) {
        if (!StringUtils.isEmptyOrNull(title)) {
            setPropertyValue(Properties.CONFIRMATION_TITLE, title);
        }
    }

    /**
     * Convenience method to get the message property value.
     * @return The message property value
     */
    public String getMessage() {
        return getPropertyValue(Properties.CONFIRMATION_MESSAGE);
    }

    /**
     * Convenience method to set the message property value.  The message property value is updated,
     * causing the message view display to change.
     * @param message The message String to display
     */
    public void setMessage(String message) {
        setPropertyValue(Properties.CONFIRMATION_MESSAGE, message);
    }

    /**
     * A convenience method to set both the title and message property values.   This method only accepts non-null
     * title value as the title is required for the confirmation pane.  If the title provided is non-null the title
     * and message property values are updated, and the view components that are bound to the properties are updated.
     * @param title The title String to display, which must be non-null
     * @param message The message String to display
     */
    public void setConfirmationText(String title, String message) {
        if (!StringUtils.isEmptyOrNull(title)) {
            setTitle(title);
            setMessage(message);
        }
    }

    /**
     * Convenience method that provides the notification topic property value.  The notification
     * topic is used to send an event on the event bus.
     * @return The UUID for the notification topic
     */
    public UUID getNotifiicationTopic() {
        return getPropertyValue(Properties.NOTIFICATION_TOPIC);
    }

    /**
     * Convenience method to set the notification topic property value.  The notification
     *      * topic is used to send an event on the event bus.
     * @param notifiicationTopic
     */
    public void setNotificationTopic(UUID notifiicationTopic) {
        setPropertyValue(Properties.NOTIFICATION_TOPIC, notifiicationTopic);
    }

    /**
     * Convenience method that provides the notification event property value.  The notification
     * event is sent to the notification topic on the event bus.
     * @return The Evt object for the notification event
     */
    public Evt getNotificationEvent() {
        return getPropertyValue(Properties.NOTIFICATION_EVENT);
    }

    /**
     * Convenience method to set the notification event property value.  The notification
     * event is sent to the notification topic on the event bus.
     * @param notifiationEvent
     */
    public void setNotificationEvent(Evt notifiationEvent) {
        setPropertyValue(Properties.NOTIFICATION_EVENT, notifiationEvent);
    }

    /**
     * Convenience method to set both the notification topic and event.  Both of these values are needed to send
     * an event on the event bus when the CLOSE PROPERTIES PANE button is pressed.
     * @param notificationTopic
     * @param notifiationEvent
     */
    public void setNotificationTopicAndEvent(UUID notificationTopic, Evt notifiationEvent) {
        if (notificationTopic != null && notifiationEvent != null) {
            setPropertyValue(Properties.NOTIFICATION_TOPIC, notificationTopic);
            setPropertyValue(Properties.NOTIFICATION_EVENT, notifiationEvent);
        }
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
     * Set the confirmation message to display in the title and message labels.  This method uses pre-defined,
     * well known, titles and messages that are defined in a convenient enum ConfirmationMessages.
     * @param confirmationMessage The message to display
     */
    public void setConfirmationMessage(ConfirmationMessages confirmationMessage) {
        if (confirmationMessage != null) {
            setPropertyValue(Properties.CONFIRMATION_TITLE, confirmationMessage.getConfirmationText().title());
            setPropertyValue(Properties.CONFIRMATION_MESSAGE, confirmationMessage.getConfirmationText().message());
        }
    }

}
