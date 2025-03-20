package dev.ikm.komet.kview.mvvm.view.genediting;

import static dev.ikm.komet.kview.events.genediting.PropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.WINDOW_TOPIC;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.kview.events.genediting.PropertyPanelEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.viewmodel.SimpleViewModel;

/**
 * controller for the confirmation screen post save on the
 * general editing/authoring bump out screen
 */
public class ClosePropertiesController {

    /**
     * view model to store the event topic
     */
    @InjectViewModel
    private SimpleViewModel propertiesViewModel;

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


    /**
     * action fired by closing the properties bump out
     * @param event property panel event -> close panel
     */
    @FXML
    private void closeProperties(ActionEvent event) {
        EvtBusFactory.getDefaultEvtBus().publish(propertiesViewModel.getPropertyValue(WINDOW_TOPIC), new PropertyPanelEvent(event.getSource(), CLOSE_PANEL));
    }

    /**
     * set the heading text
     * @param text the text to set
     */
    public void setHeadingText(String text) {
        headingText.setText(text);
    }
}
