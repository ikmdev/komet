package dev.ikm.komet.kview.mvvm.viewmodel;

import dev.ikm.komet.framework.controls.TimeUtils;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.ClosePropertiesPanelEvent;
import dev.ikm.komet.kview.mvvm.view.genediting.ConfirmationDialogController;
import dev.ikm.tinkar.component.Stamp;
import dev.ikm.tinkar.composer.Composer;
import dev.ikm.tinkar.composer.Session;
import dev.ikm.tinkar.composer.assembler.ConceptAssembler;
import dev.ikm.tinkar.composer.assembler.PatternAssemblerConsumer;
import dev.ikm.tinkar.composer.template.FullyQualifiedNameConsumer;
import dev.ikm.tinkar.composer.template.USDialect;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.FieldDefinitionRecord;
import dev.ikm.tinkar.entity.PatternVersionRecord;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.Subscriber;
import dev.ikm.tinkar.terms.ComponentWithNid;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import org.carlfx.cognitive.validator.ValidationResult;
import org.carlfx.cognitive.viewmodel.ViewModel;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import java.util.*;

import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.fetchDescendentsOfConcept;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampFormViewModelBase.StampProperties.*;

import static dev.ikm.tinkar.terms.TinkarTerm.ACCEPTABLE;

public class StampAddFormViewModel extends StampFormViewModelBase {
    /**
     * Provide the standard Confirm Clear dialog title for use in other classes
     */
    public static final String CONFIRM_RESET_TITLE = "Confirm Reset Form";
    /**
     * Provide the standard Confirm Clear dialog message for use in other classes
     */
    public static final String CONFIRM_RESET_MESSAGE =  "Are you sure you want to reset the form? All entered data will be lost.";

    private EntityFacade entityFacade;
    private UUID topic;

    private Subscriber<ClosePropertiesPanelEvent> closePropertiesPanelEventSubscriber;

    public StampAddFormViewModel(StampType stampType) {
        super(stampType);

        // Add Properties
        addProperty(CURRENT_STAMP, (Stamp) null);
        addProperty(STATUS, State.ACTIVE);
        addProperty(AUTHOR, (ComponentWithNid) null);
        addProperty(MODULE, (ComponentWithNid) null);
        addProperty(PATH, (ComponentWithNid) null);
        addProperty(TIME, 0L);

        addProperty(IS_STAMP_VALUES_THE_SAME_OR_EMPTY, true);
        addValidator(IS_STAMP_VALUES_THE_SAME_OR_EMPTY, "Validator Property", (ValidationResult vr, ViewModel vm) -> {
            boolean same = updateIsStampValuesChanged();
            if (same) {
                // if UI’s stamp is the same as the previous stamp than it is invalid.
                vr.error("Cannot submit stamp because the data is the same.");
            }
        });

        addProperty(MODULES, Collections.emptyList(), true);
        addProperty(PATHS, Collections.emptyList(), true);
        addProperty(STATUSES, Collections.emptyList(), true);

        addProperty(FORM_TITLE, "");
        addProperty(TIME_TEXT, "");

        addProperty(CLEAR_RESET_BUTTON_TEXT, "RESET");
        addProperty(SUBMIT_BUTTON_TEXT, "SUBMIT");

        // run validators when the following properties change.
        doOnChange(this::validate, STATUS, MODULE, PATH);
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

        // Obtain current module and path concept objects from the modules set and paths set respectively.
        // NOTE: value property must be the same object in the combobox items.
        loadStampValuesFromDB(modules, paths); // MODULE

        setPropertyValue(TIME_TEXT, TimeUtils.toDateString(getPropertyValue(TIME)));

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

    private void loadStampValuesFromDB(Set<ConceptEntity> modules, Set<ConceptEntity> paths) {
        StampEntity stampEntity = getPropertyValue(StampProperties.CURRENT_STAMP);

        // Choose one item from the Sets as the module and path. Items will use .equals(). STATUS property value is an Enum.
        ConceptEntity module = modules.stream().filter( m -> m.nid() == stampEntity.moduleNid()).findFirst().orElse(null);
        ConceptEntity path = paths.stream().filter( m -> m.nid() == stampEntity.pathNid()).findFirst().orElse(null);

        setPropertyValue(STATUS, stampEntity.state());
        setPropertyValue(TIME, stampEntity.time());
        setPropertyValue(AUTHOR, stampEntity.author());
        setPropertyValue(MODULE, module);
        setPropertyValue(PATH, path);
    }

    private boolean updateIsStampValuesChanged() {
        StampEntity stampEntity = getPropertyValue(CURRENT_STAMP);

        boolean same = stampEntity.state() == getPropertyValue(STATUS)
                && stampEntity.path() == getPropertyValue(PATH)
                && stampEntity.module() == getPropertyValue(MODULE);

        setPropertyValue(IS_STAMP_VALUES_THE_SAME_OR_EMPTY, same);

        if (same) {
            setPropertyValue(FORM_TITLE, "Latest " + stampType.getTextDescription() + " Version");
            setPropertyValue(TIME_TEXT, TimeUtils.toDateString(getPropertyValue(TIME)));
            setPropertyValue(AUTHOR, stampEntity.author());
        } else {
            setPropertyValue(FORM_TITLE, "New " + stampType.getTextDescription() + " Version");
            setPropertyValue(TIME_TEXT, "Uncommitted");
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
            case PATTERN -> {
                Latest<EntityVersion> latestEntityVersion = viewProperties.calculator().latest(entityFacade);
                EntityVersion entityVersion = latestEntityVersion.get();

                Entity purposeEntity = ((PatternVersionRecord) entityVersion).semanticPurpose();
                Entity meaningEntity = ((PatternVersionRecord) entityVersion).semanticMeaning();

                // FQN
                SemanticEntityVersion fqnSemanticEntityVersion = getViewProperties().calculator().languageCalculator()
                        .getFullyQualifiedDescription(entityFacade).getWithContradictions().getFirstOptional().get();
                ConceptFacade fqnLanguage = (ConceptFacade) fqnSemanticEntityVersion.fieldValues().get(0);
                String fqnString = (String) fqnSemanticEntityVersion.fieldValues().get(1);
                ConceptFacade fqnCaseSignificance = (ConceptFacade) fqnSemanticEntityVersion.fieldValues().get(2);
                // ConceptFacade fqnDescriptionType = (ConceptFacade) fqnSemanticEntityVersion.fieldValues().get(3);

                session.compose((PatternAssemblerConsumer) patternAssembler -> {
                    patternAssembler
                            .pattern(entityFacade.toProxy())
                            .meaning(meaningEntity.toProxy())
                            .purpose(purposeEntity.toProxy())
                            .attach((FullyQualifiedNameConsumer) fqn -> fqn
                                    .semantic(fqnSemanticEntityVersion.entity().toProxy())
                                    .language(fqnLanguage.toProxy())
                                    .text(fqnString)
                                    .caseSignificance(fqnCaseSignificance.toProxy())
                                    .attach(new USDialect().acceptability(ACCEPTABLE))
                            );

                    // Add the field definitions
                    ImmutableList<FieldDefinitionRecord> fieldDefinitions = ((PatternVersionRecord) entityVersion).fieldDefinitions();
                    for (int i = 0; i < fieldDefinitions.size(); i++) {
                        FieldDefinitionRecord fieldDefinitionRecord = fieldDefinitions.get(i);

                        ConceptEntity fieldMeaning = fieldDefinitionRecord.meaning();
                        ConceptEntity fieldPurpose = fieldDefinitionRecord.purpose();
                        ConceptEntity fieldDataType = fieldDefinitionRecord.dataType();
                        patternAssembler.fieldDefinition(fieldMeaning.toProxy(), fieldPurpose.toProxy(), fieldDataType.toProxy(), i);
                    }
                });
            }
            default -> throw new RuntimeException("Stamp Type " + stampType + " not supported");
        }

        composer.commitSession(session);

        // Load the new STAMP and store the new initial values
        loadStamp();
        save(true);
        updateIsStampValuesChanged();

        return this;
    }

    public ViewProperties getViewProperties() {
        return viewProperties;
    }
}
