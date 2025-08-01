package dev.ikm.komet.kview.mvvm.view.genediting;

import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.kview.controls.KLComponentControlFactory;
import dev.ikm.komet.kview.events.genediting.GenEditingEvent;
import dev.ikm.komet.kview.events.genediting.PropertyPanelEvent;
import dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.State;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static dev.ikm.komet.kview.events.genediting.GenEditingEvent.CONFIRM_REFERENCE_COMPONENT;
import static dev.ikm.komet.kview.events.genediting.PropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.kview.klfields.KlFieldHelper.createDefaultFieldValues;
import static dev.ikm.komet.kview.mvvm.view.genediting.SemanticFieldsController.CONFIRM_CLEAR_MESSAGE;
import static dev.ikm.komet.kview.mvvm.view.genediting.SemanticFieldsController.CONFIRM_CLEAR_TITLE;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.*;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.*;

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
        klComponentControl = KLComponentControlFactory.createTypeAheadComponentControl(genEditingViewModel.getViewProperties().calculator());
        klComponentControl.setTitle("Reference component");
        ObjectProperty<EntityProxy> refComponentProperty = genEditingViewModel.getProperty(REF_COMPONENT);
        klComponentControl.entityProperty().bindBidirectional(refComponentProperty);
        referenceComponentVBox.getChildren().add(klComponentControl);
        confirmButton.disableProperty().bind(refComponentProperty.isNull());
        //TODO Confirm if its necessary to show a message to end-user about restrictions on reference component.
        Label label = new Label(" Note: Reference component cannot be changed once confirmed. If you confirm and then " +
                "decide to change the reference component, you will have to recreate a new semantic.");
        label.setWrapText(true);
        label.maxWidthProperty().bind(referenceComponentVBox.widthProperty());
        referenceComponentVBox.getChildren().add(label);
    }

    public ViewProperties getViewProperties() {
        return genEditingViewModel.getPropertyValue(VIEW_PROPERTIES);
    }

    @FXML
    private void cancel(ActionEvent actionEvent) {
        klComponentControl.entityProperty().set(null);
        EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC), new PropertyPanelEvent(actionEvent.getSource(), CLOSE_PANEL));
        actionEvent.consume();
        // if previous state was closed cancel will close properties bump out.
        // else show
    }

    @FXML
    private void clearForm(ActionEvent actionEvent) {
        if (klComponentControl.entityProperty().get() != null) {
            ConfirmationDialogController.showConfirmationDialog(this.cancelButton, CONFIRM_CLEAR_TITLE, CONFIRM_CLEAR_MESSAGE)
                    .thenAccept(confirmed -> {
                        if (confirmed) {
                            klComponentControl.entityProperty().set(null);
                        }
                    });
        }

        actionEvent.consume();
    }

    @FXML
    public void confirm(ActionEvent actionEvent) {
        EntityFacade semantic = createUncommitedSemanticRecord();
        genEditingViewModel.setPropertyValue(SEMANTIC, semantic);
        EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC), new GenEditingEvent(actionEvent.getSource(), CONFIRM_REFERENCE_COMPONENT));
        EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC), new PropertyPanelEvent(actionEvent.getSource(), CLOSE_PANEL));
        actionEvent.consume();
    }

    private EntityFacade createUncommitedSemanticRecord() {
        RecordListBuilder<SemanticVersionRecord> versions = RecordListBuilder.make();
        UUID semanticUUID = UUID.randomUUID();
        EntityFacade patternFacade = genEditingViewModel.getPropertyValue(PATTERN);
        EntityProxy referencedComponent = genEditingViewModel.getPropertyValue(REF_COMPONENT);

        SemanticRecord semanticRecord = SemanticRecordBuilder.builder()
                .nid(EntityService.get().nidForUuids(semanticUUID))
                .leastSignificantBits(semanticUUID.getLeastSignificantBits())
                .mostSignificantBits(semanticUUID.getMostSignificantBits())
                .additionalUuidLongs(null)
                .patternNid(patternFacade.nid())
                .referencedComponentNid(referencedComponent.nid())
                .versions(versions.toImmutable())
                .build();

        StampViewModel stampViewModel = genEditingViewModel.getPropertyValue(STAMP_VIEW_MODEL);
        Transaction transaction = Transaction.make("Transaction For "+semanticRecord.nid());

        State state = stampViewModel.getPropertyValue(STATUS);
        int authorNid = ((EntityProxy.Concept) stampViewModel.getPropertyValue(AUTHOR)).nid();
        int moduleNid = ((ConceptEntity)  stampViewModel.getPropertyValue(MODULE)).nid();
        int pathNid = ((ConceptEntity)  stampViewModel.getPropertyValue(PATH)).nid();
        StampEntity stampEntity = transaction.getStampForEntities(state, authorNid, moduleNid, pathNid, semanticRecord);

        ImmutableList<Object> fieldValues = createDefaultFieldValues(patternFacade, getViewProperties());
        versions.add(SemanticVersionRecordBuilder.builder()
                .chronology(semanticRecord)
                .stampNid(stampEntity.nid())
                .fieldValues(fieldValues)
                .build());

        //Rebuild the Semantic with the now populated version data
        SemanticEntity<? extends SemanticEntityVersion> semanticEntity = SemanticRecordBuilder
                .builder(semanticRecord)
                .versions(versions.toImmutable()).build();

        Entity.provider().putEntity(semanticEntity);
        return semanticEntity;
    }

}
