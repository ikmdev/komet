package dev.ikm.komet.kview.mvvm.view.genediting;

import static dev.ikm.komet.kview.events.genediting.GenEditingEvent.CONFIRM_REFERENCE_COMPONENT;
import static dev.ikm.komet.kview.events.genediting.PropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.PATTERN;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.REF_COMPONENT;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.SEMANTIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.STAMP_VIEW_MODEL;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.WINDOW_TOPIC;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.AUTHOR;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.MODULE;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.PATH;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.STATUS;
import static dev.ikm.tinkar.terms.TinkarTerm.INTEGER_FIELD;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.framework.observable.ObservablePatternSnapshot;
import dev.ikm.komet.framework.observable.ObservablePatternVersion;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.kview.events.genediting.GenEditingEvent;
import dev.ikm.komet.kview.events.genediting.PropertyPanelEvent;
import dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.RecordListBuilder;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.SemanticRecordBuilder;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.entity.SemanticVersionRecordBuilder;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

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
        klComponentControl.entityProperty().bindBidirectional(refComponentProperty);
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
        EntityFacade semantic = createUncommitedSemanticRecord();
        EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC), new GenEditingEvent(actionEvent.getSource(), CONFIRM_REFERENCE_COMPONENT));
        EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC), new PropertyPanelEvent(actionEvent.getSource(), CLOSE_PANEL));
        genEditingViewModel.setPropertyValue(SEMANTIC, semantic);
        actionEvent.consume();
    }

    private EntityFacade createUncommitedSemanticRecord() {
        RecordListBuilder<SemanticVersionRecord> versions = RecordListBuilder.make();
        UUID semanticUUID = UUID.randomUUID();
        EntityFacade patternFacade = genEditingViewModel.getPropertyValue(PATTERN);
        EntityProxy referencedComponent = klComponentControl.entityProperty().get();

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
        System.out.println(" SEMANTIC RECORD " + semanticRecord.nid());

        State state = stampViewModel.getPropertyValue(STATUS);
        int authorNid = ((EntityProxy.Concept) stampViewModel.getPropertyValue(AUTHOR)).nid();
        int moduleNid = ((ConceptEntity)  stampViewModel.getPropertyValue(MODULE)).nid();
        int pathNid = ((ConceptEntity)  stampViewModel.getPropertyValue(PATH)).nid();
        StampEntity stampEntity = transaction.getStampForEntities(state, authorNid, moduleNid, pathNid, semanticRecord);

        ImmutableList<Object> fieldValues = createDefaultFieldValues(patternFacade);
        versions.add(SemanticVersionRecordBuilder.builder()
                .chronology(semanticRecord)
                .stampNid(stampEntity.nid())
                .fieldValues(fieldValues)
                .build());

        //Rebuild the Semantic with the now populated version data
        SemanticEntity<? extends SemanticEntityVersion> semanticEntity = SemanticRecordBuilder
                .builder(semanticRecord)
                .versions(versions.toImmutable()).build();

        System.out.println(" SEMANTIC Entity " + semanticEntity.nid());
        Entity.provider().putEntity(semanticEntity);
        return semanticEntity;

    }
    public static final EntityProxy.Concept BLANK_CONCEPT =
            EntityProxy.Concept.make("", UUID.randomUUID());
    private ImmutableList<Object> createDefaultFieldValues(EntityFacade patternFacade) {
        ObservableEntity observableEntity = ObservableEntity.get(patternFacade.nid());
        ObservablePatternSnapshot observablePatternSnapshot = (ObservablePatternSnapshot) observableEntity.getSnapshot(getViewProperties().calculator());
        ObservablePatternVersion observablePatternVersion = observablePatternSnapshot.getLatestVersion().get();
        MutableList<Object> fieldsValues = Lists.mutable.ofInitialCapacity(observablePatternVersion.fieldDefinitions().size());
        observablePatternVersion.fieldDefinitions().forEach(f -> {
            if (f.dataTypeNid() == TinkarTerm.COMPONENT_FIELD.nid()) {
                fieldsValues.add(BLANK_CONCEPT);
            } else if (f.dataTypeNid() == TinkarTerm.STRING_FIELD.nid()
                    || f.dataTypeNid() == TinkarTerm.STRING.nid()) {
                fieldsValues.add("");
            } else if (f.dataTypeNid() == INTEGER_FIELD.nid()) {
                fieldsValues.add(0);
            } else if (f.dataTypeNid() == TinkarTerm.FLOAT_FIELD.nid()) {
                fieldsValues.add(0.0F);
            } else if (f.dataTypeNid() == TinkarTerm.BOOLEAN_FIELD.nid()) {
                fieldsValues.add(false);
            } else if (f.dataTypeNid() == TinkarTerm.COMPONENT_ID_LIST_FIELD.nid()) {
                fieldsValues.add(IntIds.list.empty());
            } else if (f.dataTypeNid() == TinkarTerm.COMPONENT_ID_SET_FIELD.nid()) {
                fieldsValues.add(IntIds.set.empty());
            }
        });
        return fieldsValues.toImmutable();
    }

}
