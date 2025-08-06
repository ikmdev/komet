package dev.ikm.komet.kview.mvvm.viewmodel;

import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.*;
import dev.ikm.komet.framework.view.*;
import dev.ikm.komet.kview.events.*;
import dev.ikm.tinkar.component.*;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.events.*;
import dev.ikm.tinkar.terms.*;
import org.carlfx.cognitive.validator.*;
import org.carlfx.cognitive.viewmodel.*;
import org.slf4j.*;

import java.util.*;

public class StampViewModel2 extends FormViewModel {
    private static final Logger LOG = LoggerFactory.getLogger(StampViewModel2.class);
    /**
     * Provide the standard Confirm Clear dialog title for use in other classes
     */
    public static final String CONFIRM_CLEAR_TITLE = "Confirm Clear Form";
    /**
     * Provide the standard Confirm Clear dialog message for use in other classes
     */
    public static final String CONFIRM_CLEAR_MESSAGE =  "Are you sure you want to clear the form? All entered data will be lost.";

    private EntityFacade entityFacade;
    private ViewProperties viewProperties;
    private UUID topic;

    private Subscriber<ClosePropertiesPanelEvent> closePropertiesPanelEventSubscriber;

    public enum StampProperties {
        CURRENT_STAMP,                  // The current stamp

        STATUS,                         // User selected Status
        MODULE,                         // User selected Module
        PATH,                           // User selected Path

        IS_STAMP_VALUES_THE_SAME,       // Are the Stamp values in the properties the same as of the current Stamp

        STATUSES,
        MODULES,
        PATHS
    }

    public StampViewModel2() {
        super();
        addProperty(CURRENT_STAMP, (Stamp) null);
        addProperty(STATUS, State.ACTIVE);
        addProperty(MODULE, (ComponentWithNid) null);
        addProperty(PATH, (ComponentWithNid) null);

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

        // run validators when the following properties change.
        doOnChange(this::validate, STATUS, MODULE, PATH);
    }

    public void init(EntityFacade entity, UUID topic, ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
        this.topic = topic;

        // listen to events that the properties panel is going to be closed
        closePropertiesPanelEventSubscriber = evt -> onPropertiesPanelClose();
        EvtBusFactory.getDefaultEvtBus().subscribe(topic, ClosePropertiesPanelEvent.class, closePropertiesPanelEventSubscriber);

        // TODO: Remove the entityFocusProperty from DetailsNode it often calls init with a null entity.
//        if (entity == null
//                || (entity != null
//                    && this.entityFacade != null
//                    && entity.nid() == this.entityFacade.nid())) {
        if (entity == null) {
            return;
        } else {
            this.entityFacade = entity;
        }

        // initialize observable lists
        Set<ConceptEntity> modules = fetchDescendentsOfConcept(viewProperties, TinkarTerm.MODULE.publicId());
        Set<ConceptEntity> paths = fetchDescendentsOfConcept(viewProperties, TinkarTerm.PATH.publicId());

        // populate sets which are bound to the combo boxes.
        setPropertyValues(MODULES, modules);
        setPropertyValues(PATHS, paths);
        setPropertyValues(STATUSES, List.of(State.values()));

        loadStamp();

        // Obtain current module and path concept objects from the modules set and paths set respectively.
        // NOTE: value property must be the same object in the combobox items.
        loadStampValuesFromDB(modules, paths); // MODULE

        save(true);
        LOG.info("StampViewModel2 init complete");
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
        setPropertyValue(MODULE, module);
        setPropertyValue(PATH, path);
    }

    private boolean updateIsStampValuesChanged() {
        StampEntity stampEntity = getPropertyValue(StampProperties.CURRENT_STAMP);

        boolean same = stampEntity.state() == getPropertyValue(STATUS)
                && stampEntity.path() == getPropertyValue(PATH)
                && stampEntity.module() == getPropertyValue(MODULE);

        setPropertyValue(IS_STAMP_VALUES_THE_SAME, same);

        return same;
    }

    public void cancel() {
        EvtBusFactory.getDefaultEvtBus().publish(topic, new ClosePropertiesPanelEvent("StampViewModel2 cancel()",
                ClosePropertiesPanelEvent.CLOSE_PROPERTIES));
        reset();
    }

    @Override
    public StampViewModel2 save(boolean force) {
        super.save(force);
        return this;
    }

    public ViewProperties getViewProperties() {
        return viewProperties;
    }
}
