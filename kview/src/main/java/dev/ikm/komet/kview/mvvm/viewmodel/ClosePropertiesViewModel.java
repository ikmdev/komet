package dev.ikm.komet.kview.mvvm.viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.carlfx.cognitive.viewmodel.SimpleViewModel;

import java.util.UUID;

public class ClosePropertiesViewModel  extends SimpleViewModel {

    // properties
    public static final String NOTIFICATION_TOPIC = "notificationTopic";

    private StringProperty title = new SimpleStringProperty();
    private StringProperty message = new SimpleStringProperty();
    private ConfirmationMessages confirmationMessage;

    public ClosePropertiesViewModel() {
        addProperty(NOTIFICATION_TOPIC, (UUID) null);
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

    public void sendNotification() {

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
