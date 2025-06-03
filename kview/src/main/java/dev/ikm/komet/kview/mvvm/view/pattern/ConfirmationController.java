package dev.ikm.komet.kview.mvvm.view.pattern;

import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.kview.events.pattern.PropertyPanelEvent;
import dev.ikm.komet.kview.mvvm.viewmodel.ConfirmationViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.carlfx.cognitive.loader.InjectViewModel;

import static dev.ikm.komet.kview.events.pattern.PropertyPanelEvent.CLOSE_PANEL;

public class ConfirmationController {

    @FXML
    private Label confirmationTitle;

    @FXML
    private Label confirmationMessage;

    @InjectViewModel
    private ConfirmationViewModel viewModel;

    @FXML
    public void closeProperties(ActionEvent actionEvent) {
        EvtBusFactory.getDefaultEvtBus().publish(viewModel.getNotifiicationTopic(),
                new PropertyPanelEvent(actionEvent.getSource(), CLOSE_PANEL));
    }

    @FXML
    public void initialize() {
        confirmationTitle.textProperty().bind(viewModel.titleProperty());
        confirmationMessage.textProperty().bind(viewModel.messageProperty());
    }

}
