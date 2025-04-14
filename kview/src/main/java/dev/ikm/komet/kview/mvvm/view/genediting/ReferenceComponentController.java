package dev.ikm.komet.kview.mvvm.view.genediting;

import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.REF_COMPONENT;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.viewmodel.ValidationViewModel;
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
    private Button submitButton;

    @InjectViewModel
    private ValidationViewModel referenceComponentViewModel;

    @FXML
    private void initialize() {
        // clear all reference details.
        referenceComponentVBox.setSpacing(8.0);
        referenceComponentVBox.getChildren().clear();
        submitButton.setDisable(false);
        KLComponentControl componentControl = new KLComponentControl();
        componentControl.setTitle("Reference component");
        ObjectProperty<EntityProxy> refComponentProperty = referenceComponentViewModel.getProperty(REF_COMPONENT);
        refComponentProperty.bind(componentControl.entityProperty());
        componentControl.setEntity(refComponentProperty.getValue());
        referenceComponentVBox.getChildren().add(componentControl);
    }

    public ViewProperties getViewProperties() {
        return referenceComponentViewModel.getPropertyValue(VIEW_PROPERTIES);
    }

    @FXML
    private void cancel(ActionEvent actionEvent) {
        actionEvent.consume();
        // if previous state was closed cancel will close properties bump out.
        // else show
    }

    @FXML
    private void clearForm(ActionEvent actionEvent) {
        actionEvent.consume();
    }

    @FXML
    public void submit(ActionEvent actionEvent) {

    }
}
