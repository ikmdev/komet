package dev.ikm.komet.kview.mvvm.view.genediting;

import static dev.ikm.komet.kview.events.genediting.GenEditingEvent.CONFIRM_REFERENCE_COMPONENT;
import static dev.ikm.komet.kview.events.genediting.PropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.REF_COMPONENT;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.WINDOW_TOPIC;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.kview.events.genediting.GenEditingEvent;
import dev.ikm.komet.kview.events.genediting.PropertyPanelEvent;
import dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReferenceComponentController {

    private static final Logger LOG = LoggerFactory.getLogger(ReferenceComponentController.class);

    @FXML
    private VBox referenceComponentVBox;
    @FXML
    private Button cancelButton;

    @FXML
    private Button clearFormButton;

    @FXML
    private Button confirmButton;

    @InjectViewModel
    private GenEditingViewModel genEditingViewModel;

    private KLComponentControl klComponentControl;

    @FXML
    private void initialize() {
        // clear all reference details.
        referenceComponentVBox.setSpacing(8.0);
        referenceComponentVBox.getChildren().clear();
        confirmButton.setDisable(true);
        klComponentControl = new KLComponentControl();
        klComponentControl.setTitle("Reference component");
        ObjectProperty<EntityProxy> refComponentProperty = genEditingViewModel.getProperty(REF_COMPONENT);
        //refComponentProperty.bind(componentControl.entityProperty());
        klComponentControl.entityProperty().bindBidirectional(refComponentProperty);
        //EntityProxy entityProxy = refComponentProperty.get().toProxy();
        //componentControl.setEntity(entityProxy);
        referenceComponentVBox.getChildren().add(klComponentControl);

        confirmButton.disableProperty().bind(klComponentControl.entityProperty().isNull());


        //TODO Confirm if its necessary to show a message to end-user about restrictions on reference component.
        Label label = new Label(" Note: Reference component cannot be changed once confirmed. \\n If you confirm and then decide to change the reference component,\\n you will have to recreate a new semantic.");
        referenceComponentVBox.getChildren().add(label);
    }

    public ViewProperties getViewProperties() {
        return genEditingViewModel.getPropertyValue(VIEW_PROPERTIES);
    }

    @FXML
    private void cancel(ActionEvent actionEvent) {
        klComponentControl.entityProperty().set(null);
        EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC), new PropertyPanelEvent(actionEvent.getSource(), CLOSE_PANEL));
        actionEvent.consume();
        // if previous state was closed cancel will close properties bump out.
        // else show
    }

    @FXML
    private void clearForm(ActionEvent actionEvent) {
        klComponentControl.entityProperty().set(null);
        actionEvent.consume();
    }

    @FXML
    public void confirm(ActionEvent actionEvent) {
        EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC), new GenEditingEvent(actionEvent.getSource(), CONFIRM_REFERENCE_COMPONENT));
        EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC), new PropertyPanelEvent(actionEvent.getSource(), CLOSE_PANEL));
        actionEvent.consume();
    }
}
