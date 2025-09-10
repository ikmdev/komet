package dev.ikm.komet.kview.mvvm.viewmodel;

import dev.ikm.komet.framework.controls.TimeUtils;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.Toast;
import dev.ikm.komet.kview.events.ClosePropertiesPanelEvent;
import dev.ikm.komet.kview.mvvm.view.genediting.ConfirmationDialogController;
import dev.ikm.komet.kview.mvvm.view.journal.JournalController;
import dev.ikm.tinkar.component.Stamp;
import dev.ikm.tinkar.composer.Composer;
import dev.ikm.tinkar.composer.Session;
import dev.ikm.tinkar.composer.assembler.ConceptAssembler;
import dev.ikm.tinkar.composer.assembler.PatternAssemblerConsumer;
import dev.ikm.tinkar.composer.assembler.SemanticAssemblerConsumer;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternVersionRecord;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.Subscriber;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

import java.util.*;

import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.fetchDescendentsOfConcept;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampFormViewModelBase.StampProperties.*;

public class StampAddFormViewModel extends StampFormViewModelBase {
    /**
     * Provide the standard Confirm Clear dialog title for use in other classes
     */
    public static final String CONFIRM_RESET_TITLE = "Confirm Reset Form";
    /**
     * Provide the standard Confirm Clear dialog message for use in other classes
     */
    public static final String CONFIRM_RESET_MESSAGE =  "Are you sure you want to reset the form? All entered data will be lost.";

    private Subscriber<ClosePropertiesPanelEvent> closePropertiesPanelEventSubscriber;

    public StampAddFormViewModel(StampType stampType) {
        super(stampType);

        // Add Properties
        addProperty(CURRENT_STAMP, (Stamp) null);

        addProperty(CLEAR_RESET_BUTTON_TEXT, "RESET");
        addProperty(SUBMIT_BUTTON_TEXT, "SUBMIT");
    }

    public void init(EntityFacade entity, UUID topic, ViewProperties viewProperties) {
        // entityFocusProperty from DetailsNode often calls init with a null entity.
        if (entity == null || entity == this.entityFacade) {
            return; // null entity or the entity hasn't changed
        } else {
            this.entityFacade = entity;
        }

        this.viewProperties = viewProperties;
        this.topic = topic;

        // listen to events that the properties panel is going to be closed
        closePropertiesPanelEventSubscriber = evt -> onPropertiesPanelClose();
        EvtBusFactory.getDefaultEvtBus().subscribe(topic, ClosePropertiesPanelEvent.class, closePropertiesPanelEventSubscriber);

        // initialize observable lists
        Set<ConceptEntity> modules = fetchDescendentsOfConcept(viewProperties, TinkarTerm.MODULE.publicId());
        Set<ConceptEntity> paths = fetchDescendentsOfConcept(viewProperties, TinkarTerm.PATH.publicId());

        if (getObservableList(MODULES).isEmpty()) {
            setPropertyValues(MODULES, modules);
        }
        if (getObservableList(PATHS).isEmpty()) {
            setPropertyValues(PATHS, paths);
        }
        if (getObservableList(STATUSES).isEmpty()) {
            setPropertyValues(STATUSES, List.of(State.values()));
        }

        loadStamp();
        loadStampValuesFromDB();

        setPropertyValue(FORM_TIME_TEXT, TimeUtils.toDateString(getPropertyValue(TIME)));

        save(true);
    }

    private void onPropertiesPanelClose() {
        reset();
    }

    private void loadStamp() {
        EntityVersion latestVersion = viewProperties.calculator().latest(entityFacade).get();
        StampEntity stampEntity = latestVersion.stamp();

        setPropertyValue(CURRENT_STAMP, stampEntity);
    }

    private void loadStampValuesFromDB() {
        StampEntity stampEntity = getPropertyValue(StampProperties.CURRENT_STAMP);

        setPropertyValue(STATUS, stampEntity.state());
        setPropertyValue(TIME, stampEntity.time());
        setPropertyValue(AUTHOR, stampEntity.author());
        setPropertyValue(MODULE, stampEntity.module());
        setPropertyValue(PATH, stampEntity.path());
    }

    @Override
    protected boolean updateIsStampValuesChanged() {
        StampEntity stampEntity = getPropertyValue(CURRENT_STAMP);

        boolean same = stampEntity.state() == getPropertyValue(STATUS)
                && stampEntity.path() == getPropertyValue(PATH)
                && stampEntity.module() == getPropertyValue(MODULE);

        setPropertyValue(IS_STAMP_VALUES_THE_SAME_OR_EMPTY, same);

        if (same) {
            setPropertyValue(FORM_TITLE, "Latest " + stampType.getTextDescription() + " Version");
            setPropertyValue(FORM_TIME_TEXT, TimeUtils.toDateString(getPropertyValue(TIME)));
            setPropertyValue(AUTHOR, stampEntity.author());
        } else {
            setPropertyValue(FORM_TITLE, "New " + stampType.getTextDescription() + " Version");
            setPropertyValue(FORM_TIME_TEXT, "Uncommitted");
            ConceptFacade authorConcept = viewProperties.nodeView().editCoordinate().getAuthorForChanges();
            setPropertyValue(AUTHOR, authorConcept);
        }

        return same;
    }

    public void cancel() {
        EvtBusFactory.getDefaultEvtBus().publish(topic, new ClosePropertiesPanelEvent("StampViewModel2 cancel()",
                ClosePropertiesPanelEvent.CLOSE_PROPERTIES));
        reset();
    }

    public void resetOrClearForm(ActionEvent actionEvent) {
        ConfirmationDialogController.showConfirmationDialog((Node) actionEvent.getSource(), CONFIRM_RESET_TITLE, CONFIRM_RESET_MESSAGE)
            .thenAccept(confirmed -> {
                if (confirmed) {
                    reset();
                }
            });
    }

    @Override
    public void submitOrConfirm() {
        save();
        showSucessToast();
        setPropertyValue(IS_CONFIRMED_OR_SUBMITTED, true);
        EvtBusFactory.getDefaultEvtBus().publish(topic, new ClosePropertiesPanelEvent("StampViewModel2 cancel()",
                ClosePropertiesPanelEvent.CLOSE_PROPERTIES));
    }

    private void showSucessToast() {
        State status = getValue(STATUS);
        EntityFacade module = getValue(MODULE);
        EntityFacade path = getValue(PATH);

        String statusString = getViewProperties().calculator().getDescriptionTextOrNid(status.nid());
        String moduleString = getViewProperties().calculator().getDescriptionTextOrNid(module.nid());
        String pathString = getViewProperties().calculator().getDescriptionTextOrNid(path.nid());

        String submitMessage = "New " + stampType.getTextDescription() + " version created (" + statusString +
                ", " + moduleString + ", " + pathString + ")";

        JournalController.toast()
                .show(
                        Toast.Status.SUCCESS,
                        submitMessage
                );
    }

    @Override
    public StampAddFormViewModel save() {
        super.save();

        if (invalidProperty().get()) {
            // Validation error so returning and not going to run the code to save to DB
            return this;
        }

        // -----------  Get values from the UI form ------------
        State status = getValue(STATUS);
        EntityFacade module = getValue(MODULE);
        EntityFacade path = getValue(PATH);
        EntityFacade author = viewProperties.nodeView().editCoordinate().getAuthorForChanges();

        // -----------  Save stamp on the Database --------------
        Composer composer = new Composer("Save new STAMP in Component");

        Session session = composer.open(status, author.toProxy(), module.toProxy(), path.toProxy());

        switch (stampType) {
            case CONCEPT -> {
                session.compose((ConceptAssembler conceptAssembler) -> {
                    conceptAssembler.concept(entityFacade.toProxy());
                });
            }
            // TODO: implement better handing of empty latestEntityVersion
            case PATTERN -> {
                Latest<EntityVersion> latestEntityVersion = viewProperties.calculator().latest(entityFacade);
                EntityVersion entityVersion = latestEntityVersion.get();
                PatternVersionRecord patternVersionRecord = (PatternVersionRecord) entityVersion;

                session.compose((PatternAssemblerConsumer) patternAssembler -> { patternAssembler
                    .pattern(entityFacade.toProxy())
                    .meaning(patternVersionRecord.semanticMeaning().toProxy())
                    .purpose(patternVersionRecord.semanticPurpose().toProxy());

                    // Add the field definitions
                    ((PatternVersionRecord) entityVersion).fieldDefinitions().forEach(fieldDefinitionRecord -> {
                        ConceptEntity fieldMeaning = fieldDefinitionRecord.meaning();
                        ConceptEntity fieldPurpose = fieldDefinitionRecord.purpose();
                        ConceptEntity fieldDataType = fieldDefinitionRecord.dataType();
                        patternAssembler.fieldDefinition(fieldMeaning.toProxy(), fieldPurpose.toProxy(), fieldDataType.toProxy());
                    });
                });
            }
            case SEMANTIC -> {
                Latest<EntityVersion> latestEntityVersion = viewProperties.calculator().latest(entityFacade);
                EntityVersion entityVersion = latestEntityVersion.get();
                SemanticVersionRecord semanticVersionRecord = (SemanticVersionRecord) entityVersion;

                session.compose((SemanticAssemblerConsumer)  semanticAssembler -> semanticAssembler
                        .semantic(entityFacade.toProxy())
                        .pattern(semanticVersionRecord.pattern().toProxy())
                        .reference(semanticVersionRecord.referencedComponent().toProxy())
                        .fieldValues(vals -> vals.withAll(semanticVersionRecord.fieldValues()))
                );
            }
            default -> throw new RuntimeException("Stamp Type " + stampType + " not supported");
        }

        composer.commitSession(session);


        // Load the new STAMP and store the new initial values
        loadStamp();
        loadStampValuesFromDB();
        save(true);
        updateIsStampValuesChanged();

        return this;
    }

    public ViewProperties getViewProperties() {
        return viewProperties;
    }
}
