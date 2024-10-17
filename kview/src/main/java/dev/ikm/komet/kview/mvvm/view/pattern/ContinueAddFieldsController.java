package dev.ikm.komet.kview.mvvm.view.pattern;

import static dev.ikm.komet.kview.events.pattern.PropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent.SHOW_ADD_FIELDS;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.PATTERN_TOPIC;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.kview.events.pattern.PropertyPanelEvent;
import dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternPropertiesViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.carlfx.cognitive.loader.InjectViewModel;

import java.util.UUID;

public class ContinueAddFieldsController {

    @FXML
    private Label confirmationTitle;

    @FXML
    private Button addFieldButton;

    @FXML
    private Button closePanelButton;

    @InjectViewModel
    private PatternPropertiesViewModel patternPropertiesViewModel;

    /**
     * when adding a field, the user is directed to a confirmation screen
     * to add more fields
     * @param actionEvent
     */
    @FXML
    private void addField(ActionEvent actionEvent) {
        actionEvent.consume();
        EvtBusFactory.getDefaultEvtBus().publish(getPatternTopic(),
                new ShowPatternFormInBumpOutEvent(actionEvent.getSource(), SHOW_ADD_FIELDS));
    }

    /**
     * close properties panel
     * @param actionEvent
     */
    @FXML
    private void closeProperties(ActionEvent actionEvent) {
        actionEvent.consume();
        EvtBusFactory.getDefaultEvtBus().publish(getPatternTopic(),
                new PropertyPanelEvent(actionEvent.getSource(), CLOSE_PANEL));
    }

    private UUID getPatternTopic() {
        return patternPropertiesViewModel.getPropertyValue(PATTERN_TOPIC);
    }

}
