package dev.ikm.komet.kview.mvvm.viewmodel;

import dev.ikm.komet.framework.controls.TimeUtils;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.ClosePropertiesPanelEvent;
import dev.ikm.komet.kview.mvvm.view.genediting.ConfirmationDialogController;
import dev.ikm.tinkar.component.Stamp;
import dev.ikm.tinkar.composer.Composer;
import dev.ikm.tinkar.composer.Session;
import dev.ikm.tinkar.composer.assembler.ConceptAssembler;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.Subscriber;
import dev.ikm.tinkar.terms.ComponentWithNid;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import org.carlfx.cognitive.validator.ValidationResult;
import org.carlfx.cognitive.viewmodel.ViewModel;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import java.util.*;

import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.fetchDescendentsOfConcept;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.AUTHOR;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.CURRENT_STAMP;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.FORM_TITLE;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.IS_STAMP_VALUES_THE_SAME;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.MODULE;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.MODULES;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.PATH;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.PATHS;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.STATUS;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.STATUSES;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.TIME;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.TIME_TEXT;

public class StampViewModel2 extends FormViewModel {
    public static final String INITIAL_FORM_TITLE = "Latest Concept Version";
    /**
     * Provide the standard Confirm Clear dialog title for use in other classes
     */
    public static final String CONFIRM_RESET_TITLE = "Confirm Reset Form";
    /**
     * Provide the standard Confirm Clear dialog message for use in other classes
     */
    public static final String CONFIRM_RESET_MESSAGE =  "Are you sure you want to reset the form? All entered data will be lost.";

    private EntityFacade entityFacade;
    private ViewProperties viewProperties;
    private UUID topic;

    private Subscriber<ClosePropertiesPanelEvent> closePropertiesPanelEventSubscriber;

    public enum StampProperties {
        CURRENT_STAMP,                  // The current stamp

        STATUS,                         // User selected Status
        AUTHOR,                         // Author of the Stamp
        MODULE,                         // User selected Module
        PATH,                           // User selected Path
        TIME,                           // Time of the Stamp

        IS_STAMP_VALUES_THE_SAME,       // Are the Stamp values in the properties the same as of the current Stamp

        STATUSES,
        MODULES,
        PATHS,

        FORM_TITLE,
        TIME_TEXT
    }

    public StampViewModel2() {
        // Add Properties
        addProperty(CURRENT_STAMP, (Stamp) null);
        addProperty(STATUS, State.ACTIVE);
        addProperty(AUTHOR, (ComponentWithNid) null);
        addProperty(MODULE, (ComponentWithNid) null);
        addProperty(PATH, (ComponentWithNid) null);
        addProperty(TIME, 0L);

        addProperty(IS_STAMP_VALUES_THE_SAME, true);
        addValidator(IS_STAMP_VALUES_THE_SAME, "Validator Property", (ValidationResult vr, ViewModel vm) -> {
            boolean same = updateIsStampValuesChanged();
            if (same) {
                // if UI’s stamp is the same as the previous stamp than it is invalid.
                vr.error("Cannot submit stamp because the data is the same.");
            }
        });

        addProperty(MODULES, Collections.emptyList(), true);
        addProperty(PATHS, Collections.emptyList(), true);
        addProperty(STATUSES, Collections.emptyList(), true);

        addProperty(FORM_TITLE, INITIAL_FORM_TITLE);
        addProperty(TIME_TEXT, "");

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
        setPropertyValue(AUTHOR, stampEntity.author());
        setPropertyValue(MODULE, module);
        setPropertyValue(PATH, path);
        setPropertyValue(TIME, stampEntity.time());
    }

    private boolean updateIsStampValuesChanged() {
        StampEntity stampEntity = getPropertyValue(CURRENT_STAMP);

        boolean same = stampEntity.state() == getPropertyValue(STATUS)
                && stampEntity.path() == getPropertyValue(PATH)
                && stampEntity.module() == getPropertyValue(MODULE);

        setPropertyValue(IS_STAMP_VALUES_THE_SAME, same);

        if (same) {
            setPropertyValue(FORM_TITLE, INITIAL_FORM_TITLE);
            setPropertyValue(TIME_TEXT, TimeUtils.toDateString(getPropertyValue(TIME)));
        } else {
            setPropertyValue(FORM_TITLE, "New Concept Version");
            setPropertyValue(TIME_TEXT, "Uncommitted");
        }

        return same;
    }

    public void cancel() {
        EvtBusFactory.getDefaultEvtBus().publish(topic, new ClosePropertiesPanelEvent("StampViewModel2 cancel()",
                ClosePropertiesPanelEvent.CLOSE_PROPERTIES));
        reset();
    }

    public void resetForm(ActionEvent actionEvent) {
        ConfirmationDialogController.showConfirmationDialog((Node) actionEvent.getSource(), CONFIRM_RESET_TITLE, CONFIRM_RESET_MESSAGE)
            .thenAccept(confirmed -> {
                if (confirmed) {
                    reset();
                }
            });
    }

    @Override
    public StampViewModel2 save() {
        super.save();

        if (invalidProperty().get()) {
            // Validation error so returning and not going to run the code to save to DB
            return this;
        }

        // -----------  Get values from the UI form ------------
        State status = getValue(STATUS);
        EntityFacade module = getValue(MODULE);
        EntityFacade path = getValue(PATH);


        // -----------  Save stamp on the Database --------------


        // ------ ObservableStamp version

//        EntityVersion latestVersion = viewProperties.calculator().latest(entityFacade).get(); // Concept Version
//        StampEntity stampEntity = latestVersion.stamp();  // Stamp for ConceptVersion.
//
//        ObservableStamp observableStamp = ObservableEntity.get(stampEntity.nid()); //ObservableStamp for ConceptVersion.
//        ObservableStampVersion observableStampVersion = observableStamp.lastVersion(); // Concept STAMP
//
//        Transaction transaction = Transaction.make();
//
//        StampEntity stampEntity2 = transaction.getStamp(status, stampEntity.authorNid(), module.nid(), path.nid()); // create an uncomitted stamp for records in transaction
//
//        observableStampVersion.stateProperty().set(status);
//        observableStampVersion.timeProperty().set(stampEntity2.time());
//        observableStampVersion.authorProperty().set(stampEntity.author());
//        observableStampVersion.moduleProperty().set((ConceptFacade) module);
//        observableStampVersion.pathProperty().set((ConceptFacade) path);
//
//        transaction.commit();


        // ------- Composer version

        Composer composer = new Composer("Save new STAMP in Concept");

        Session session = composer.open(status, TinkarTerm.USER, module.toProxy(), path.toProxy());

        session.compose((ConceptAssembler conceptAssembler) -> {
            conceptAssembler.concept(entityFacade.toProxy());
        });

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
