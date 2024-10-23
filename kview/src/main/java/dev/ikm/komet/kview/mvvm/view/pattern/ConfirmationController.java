package dev.ikm.komet.kview.mvvm.view.pattern;

import static dev.ikm.komet.kview.events.pattern.PropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.PATTERN_TOPIC;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.kview.events.pattern.PropertyPanelEvent;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternPropertiesViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.carlfx.cognitive.loader.InjectViewModel;

import java.util.UUID;

public class ConfirmationController {

    @FXML
    private Label confirmationTitle;

    @FXML
    private Label confirmationMessage;

    @InjectViewModel
    private PatternPropertiesViewModel patternPropertiesViewModel;

    @FXML
    public void closeProperties(ActionEvent actionEvent) {
        EvtBusFactory.getDefaultEvtBus().publish(getPatternTopic(),
                new PropertyPanelEvent(actionEvent.getSource(), CLOSE_PANEL));
    }

    private UUID getPatternTopic() {
        return patternPropertiesViewModel.getPropertyValue(PATTERN_TOPIC);
    }

    /**
     * update the verbiage on the confirmation screen to show
     * a Pattern Definition Added confirmation message
     */
    public void showDefinitionAdded() {
        confirmationTitle.setText("Pattern Definition Added");
        confirmationMessage.setText("");
    }

    /**
     * update the verbiage on the confirmation screen to show
     * a Fully Qualified Name Added confirmation message
     */
    public void showFqnAdded() {
        confirmationTitle.setText("Fully Qualified Name Added");
        confirmationMessage.setText("");
    }

    /**
     * update the verbiage on the confirmation screen to show
     * an Other Name Added confirmation message
     */
    public void showOtherNameAdded() {
        confirmationTitle.setText("Other Name Added");
        confirmationMessage.setText("");
    }

    /**
     * update the verbiage on the confirmation screen to show
     * a Continue editing Fields? confirmation message
     */
    public void showContinueEditingFields() {
        confirmationTitle.setText("Continue editing Fields?");
        confirmationMessage.setText("");
    }
}
