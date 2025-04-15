package dev.ikm.komet.kview.mvvm.view.genediting;

import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.REF_COMPONENT;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.SEMANTIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.STAMP_VIEW_MODEL;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.AUTHOR;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.MODULE;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.PATH;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.STATUS;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel;
import dev.ikm.tinkar.composer.Composer;
import dev.ikm.tinkar.composer.Session;
import dev.ikm.tinkar.composer.assembler.SemanticAssembler;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.viewmodel.ValidationViewModel;
import org.carlfx.cognitive.viewmodel.ViewModel;
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

        EntityFacade refComponent = genEditingViewModel.getPropertyValue(REF_COMPONENT);
        EntityFacade semantic = genEditingViewModel.getPropertyValue(SEMANTIC);

        ViewModel stampViewModel = genEditingViewModel.getPropertyValue(STAMP_VIEW_MODEL);
        
        Composer composer = new Composer("Add reference component %s for semantic %s"
                                .formatted(refComponent.description(), semantic.description()));
        Session session = composer.open(stampViewModel.getPropertyValue(STATUS),
                                stampViewModel.getPropertyValue(AUTHOR),
                                stampViewModel.getPropertyValue(MODULE),
                                stampViewModel.getPropertyValue(PATH));
        session.compose((SemanticAssembler semanticAssembler) ->
                            semanticAssembler
                                    .semantic(semantic.toProxy())
                                    // add the reference component to the Semantic
                                    .reference(refComponent.toProxy()));

        genEditingViewModel.setPropertyValue(SEMANTIC, semantic);
    }
}
