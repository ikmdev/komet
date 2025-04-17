package dev.ikm.komet.kview.mvvm.view.genediting;

import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.COMPOSER;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.PATTERN;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.REF_COMPONENT;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.SEMANTIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.SESSION;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.STAMP_VIEW_MODEL;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.AUTHOR;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.MODULE;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.PATH;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.STATUS;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.kview.mvvm.model.DataModelHelper;
import dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
    private Button submitButton;

    @InjectViewModel
    //private ValidationViewModel referenceComponentViewModel;
    private GenEditingViewModel genEditingViewModel;

    @FXML
    private void initialize() {
        // clear all reference details.
        referenceComponentVBox.setSpacing(8.0);
        referenceComponentVBox.getChildren().clear();
        submitButton.setDisable(false);
        KLComponentControl componentControl = new KLComponentControl();
        componentControl.setTitle("Reference component");
        ObjectProperty<EntityProxy> refComponentProperty = genEditingViewModel.getProperty(REF_COMPONENT);
        //refComponentProperty.bind(componentControl.entityProperty());
        componentControl.entityProperty().bindBidirectional(refComponentProperty);
        //EntityProxy entityProxy = refComponentProperty.get().toProxy();
        //componentControl.setEntity(entityProxy);
        referenceComponentVBox.getChildren().add(componentControl);
    }

    public ViewProperties getViewProperties() {
        return genEditingViewModel.getPropertyValue(VIEW_PROPERTIES);
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
        actionEvent.consume();
        if (genEditingViewModel.getPropertyValue(REF_COMPONENT) != null) {
            EntityFacade refComponent = genEditingViewModel.getPropertyValue(REF_COMPONENT);
            EntityFacade semantic = genEditingViewModel.getPropertyValue(SEMANTIC);
            EntityFacade pattern = genEditingViewModel.getPropertyValue(PATTERN);
            StampViewModel stampViewModel = genEditingViewModel.getPropertyValue(STAMP_VIEW_MODEL);

            semantic = DataModelHelper.commitSemantic(getViewProperties(),
                    pattern,
                    stampViewModel.getValue(STATUS),
                    stampViewModel.getValue(AUTHOR),
                    stampViewModel.getValue(MODULE),
                    stampViewModel.getValue(PATH),
                    semantic, refComponent.toProxy(),
                    genEditingViewModel.getPropertyValue(COMPOSER),
                    genEditingViewModel.getPropertyValue(SESSION),
                    false);
            // replacing the semantic
            genEditingViewModel.setPropertyValue(SEMANTIC, semantic.toProxy());

            //TODO override call create method
            genEditingViewModel.save();
/*
            EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC),
                    new GenEditingEvent(actionEvent.getSource(), REFERENCE_COMPONENT_CHANGED_EVENT));
*/
        }
    }
}
