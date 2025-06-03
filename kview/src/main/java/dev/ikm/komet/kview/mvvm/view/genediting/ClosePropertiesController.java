package dev.ikm.komet.kview.mvvm.view.genediting;

import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.kview.events.genediting.PropertyPanelEvent;
import dev.ikm.komet.kview.mvvm.viewmodel.ClosePropertiesViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.carlfx.cognitive.loader.InjectViewModel;

import static dev.ikm.komet.kview.events.genediting.PropertyPanelEvent.CLOSE_PANEL;

/**
 * controller for the confirmation screen post save on the
 * general editing/authoring bump out screen
 */
public class ClosePropertiesController {

    /**
     * view model to store the event topic
     */
//    @InjectViewModel
//    private SimpleViewModel genEditingViewModel;

    /**
     * button to close the properties bump out
     */
    @FXML
    private Button closePropsButton;

    /**
     * text heading at the top of this pane
     */
    @FXML
    private Label headingText;

    @FXML
    private Label messageText;

    @InjectViewModel
    private ClosePropertiesViewModel viewModel;

    /**
     * action fired by closing the properties bump out
     * @param event property panel event -> close panel
     */
    @FXML
    private void closeProperties(ActionEvent event) {
        EvtBusFactory.getDefaultEvtBus().publish(viewModel.getNotifiicationTopic(), new PropertyPanelEvent(event.getSource(), CLOSE_PANEL));
    }

    @FXML
    public void initialize() {
        headingText.textProperty().bind(viewModel.titleProperty());
        messageText.textProperty().bind(viewModel.messageProperty());
    }

}
