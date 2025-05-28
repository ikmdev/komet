package dev.ikm.komet.kview.mvvm.view.genediting;

import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.kview.events.genediting.PropertyPanelEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.viewmodel.SimpleViewModel;

import static dev.ikm.komet.kview.events.genediting.PropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.WINDOW_TOPIC;

/**
 * controller for the confirmation screen post save on the
 * general editing/authoring bump out screen
 */
public class ClosePropertiesController {

    /**
     * view model to store the event topic
     */
    @InjectViewModel
    private SimpleViewModel genEditingViewModel;

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
    private Label subtextLine1;

    @FXML
    private Label subtextLine2;

    private String headingTextDefault;
    private String subtextLine1Default;
    private String subtextLine2Default;

    /**
     * action fired by closing the properties bump out
     * @param event property panel event -> close panel
     */
    @FXML
    private void closeProperties(ActionEvent event) {
        EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC), new PropertyPanelEvent(event.getSource(), CLOSE_PANEL));
    }

    @FXML
    public void initialize() {
        headingTextDefault = headingText.getText();
        subtextLine1Default = subtextLine1.getText();
        subtextLine2Default = subtextLine2.getText();
    }

    /**
     * set the heading text
     * @param text the text to set
     */
    public void setHeadingText(String text) {
        headingText.setText(text);
    }

    /**
     * set the first line of the subtext
     * @param text
     */
    public void setSubtextLine1(String text) {
        subtextLine1.setText(text);
    }

    /**
     * set the second line of the subtext
     * @param text
     */
    public void setSubtextLine2(String text) {
        subtextLine2.setText(text);
    }

    public void showNoSelectionMadeToEditSemanticElement() {
        headingText.setText("No Selection Made");
        subtextLine1.setText("Make a selection in the view");
        subtextLine2.setText("to edit the Semantic Element");
    }

    public void showSemanticDetailsAdded() {
        headingText.setText(headingTextDefault);
        subtextLine1.setText(subtextLine1Default);
        subtextLine2.setText(subtextLine2Default);
    }

    public void showSemanticDetailsChanged() {
        headingText.setText("Semantic Details Changed");
        subtextLine1.setText(subtextLine1Default);
        subtextLine2.setText(subtextLine2Default);
    }

}
