package dev.ikm.komet.kview.mvvm.viewmodel;

import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.fetchDescendentsOfConcept;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampFormViewModel.StampProperties.AUTHOR;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampFormViewModel.StampProperties.CURRENT_STAMP;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampFormViewModel.StampProperties.FORM_TITLE;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampFormViewModel.StampProperties.IS_STAMP_VALUES_THE_SAME;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampFormViewModel.StampProperties.MODULE;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampFormViewModel.StampProperties.MODULES;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampFormViewModel.StampProperties.PATH;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampFormViewModel.StampProperties.PATHS;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampFormViewModel.StampProperties.STAMP_TYPE;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampFormViewModel.StampProperties.STATUS;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampFormViewModel.StampProperties.STATUSES;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampFormViewModel.StampProperties.TIME;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampFormViewModel.StampProperties.TIME_TEXT;
import dev.ikm.komet.framework.controls.TimeUtils;
import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.framework.observable.ObservableEntitySnapshot;
import dev.ikm.komet.framework.observable.ObservableVersion;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.ClosePropertiesPanelEvent;
import dev.ikm.komet.kview.mvvm.view.genediting.ConfirmationDialogController;
import dev.ikm.tinkar.component.Stamp;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.events.EntityVersionChangeEvent;
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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class StampFormViewModel extends FormViewModel {
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
    private Subscriber<EntityVersionChangeEvent> entityVersionChangeEventSubscriber;

    private Subscriber<ClosePropertiesPanelEvent> closePropertiesPanelEventSubscriber;

    public enum StampProperties {
        CURRENT_STAMP,                  // The current stamp

        STATUS,                         // User selected Status
        AUTHOR,                         // Author of the Stamp
        MODULE,                         // User selected Module
        PATH,                           // User selected Path
        TIME,                           // Time of the Stamp

        STAMP_TYPE,

        IS_STAMP_VALUES_THE_SAME,       // Are the Stamp values in the properties the same as of the current Stamp

        STATUSES,
        MODULES,
        PATHS,

        FORM_TITLE,
        TIME_TEXT
    }

    public enum StampType {
        CONCEPT("Concept"),
        PATTERN("Pattern"),
        SEMANTIC("Semantic");

        private final String textDescription;

        StampType(String textDescription) {
            this.textDescription = textDescription;
        }

        public String getTextDescription() { return textDescription; }
    }

    public StampFormViewModel() {
        // Add Properties
        addProperty(CURRENT_STAMP, (Stamp) null);
        addProperty(STATUS, State.ACTIVE);
        addProperty(AUTHOR, (ComponentWithNid) null);
        addProperty(MODULE, (ComponentWithNid) null);
        addProperty(PATH, (ComponentWithNid) null);
        addProperty(TIME, 0L);
        addProperty(STAMP_TYPE, (StampType) null);

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

        addProperty(FORM_TITLE, "");
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
        StampEntity stampEntity = EntityService.get().getEntityFast(entityFacade.nid()).versions().getLastOptional().get().stamp();
        setPropertyValue(CURRENT_STAMP, stampEntity);
    }

    private void loadStampValuesFromDB(Set<ConceptEntity> modules, Set<ConceptEntity> paths) {
        StampEntity stampEntity = getPropertyValue(StampProperties.CURRENT_STAMP);
//        ObservableEntity stampObservableEntity = ObservableEntity.get(stampEntity.nid());
//        if(stampObservableEntity instanceof ObservableStamp observableStamp){
//            ObservableStampVersion observableStampVersion = observableStamp.lastVersion();
//            State state = observableStampVersion.state();
//            long time = observableStampVersion.time();
//            ConceptFacade author = observableStampVersion.author();
//            ConceptFacade module = observableStampVersion.module();
//            ConceptFacade path = observableStampVersion.path();
//            setPropertyValue(STATUS, state);
//            setPropertyValue(TIME, time);
//            setPropertyValue(AUTHOR, author);
//            setPropertyValue(MODULE, module);
//            setPropertyValue(PATH, path);
//            modules.stream().filter( m -> m.nid() == module.nid()).findFirst().orElse(null);
//            paths.stream().filter( m -> m.nid() == path.nid()).findFirst().orElse(null);
//        }
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

        setPropertyValue(IS_STAMP_VALUES_THE_SAME, same);

        StampType stampType = getPropertyValue(STAMP_TYPE);

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

    public void resetForm(ActionEvent actionEvent) {
        ConfirmationDialogController.showConfirmationDialog((Node) actionEvent.getSource(), CONFIRM_RESET_TITLE, CONFIRM_RESET_MESSAGE)
            .thenAccept(confirmed -> {
                if (confirmed) {
                    reset();
                }
            });
    }

    @Override
    public StampFormViewModel save() {
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
        Transaction transaction = Transaction.make();
        StampEntity stampEntity = transaction.getStampForEntities(status, author.nid(), module.nid(), path.nid(), entityFacade);

        ObservableEntity observableEntity = ObservableEntity.get(entityFacade.nid());
        ObservableEntitySnapshot observableEntitySnapshot = observableEntity.getSnapshot(viewProperties.calculator());
        observableEntitySnapshot.getLatestVersion().ifPresent(latestVersion -> {
            if (latestVersion instanceof ObservableVersion observableVersion) {
                observableVersion.versionProperty().set(observableVersion.updateStampNid(stampEntity.nid()));
            }
        });
        int stampCount = transaction.commit();
        System.out.println(" TOTAL STAMPS UPDATED " + stampCount);

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
