package dev.ikm.komet.kview.mvvm.viewmodel.stamp;

import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.fetchDescendentsOfConcept;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.AUTHOR;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.FORM_TIME_TEXT;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.FORM_TITLE;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.IS_CONFIRMED_OR_SUBMITTED;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.IS_STAMP_VALUES_THE_SAME_OR_EMPTY;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.MODULE;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.MODULES;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.PATH;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.PATHS;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.STATUS;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.STATUSES;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.TIME;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.ClosePropertiesPanelEvent;
import dev.ikm.komet.kview.mvvm.view.genediting.ConfirmationDialogController;
import dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.terms.ComponentWithNid;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import org.carlfx.cognitive.validator.ValidationResult;
import org.carlfx.cognitive.viewmodel.ViewModel;

import java.util.*;
import java.util.stream.*;

public abstract class StampFormViewModelBase extends FormViewModel {
    protected EntityFacade entityFacade;
    protected UUID topic;
    protected ObservableView observableView;

    /**
     * @deprecated Use {@link #getObservableView()} instead. Retained for callers
     *             that still pass {@code ViewProperties}; bridges via {@code nodeView()}.
     */
    @Deprecated(forRemoval = true)
    protected ViewProperties viewProperties;

    protected Type type;

    public StampFormViewModelBase(Type type) {
        this.type = type;

        // Stamp Properties
        addProperty(STATUS, (State) null);
        addProperty(TIME, 0L);
        addProperty(AUTHOR, (ComponentWithNid) null);
        addProperty(MODULE, (ComponentWithNid) null);
        addProperty(PATH, (ComponentWithNid) null);

        addProperty(IS_STAMP_VALUES_THE_SAME_OR_EMPTY, true);
        addValidator(IS_STAMP_VALUES_THE_SAME_OR_EMPTY, "Validator Property", (ValidationResult vr, ViewModel vm) -> {
            boolean same = updateIsStampValuesChanged();
            if (same) {
                // if UI’s stamp is the same as the previous stamp than it is invalid.
                vr.error("Cannot submit stamp because the data is the same.");
            }
        });

        addProperty(IS_CONFIRMED_OR_SUBMITTED, false);

        addProperty(MODULES, Collections.emptyList(), true);
        addProperty(PATHS, Collections.emptyList(), true);
        addProperty(STATUSES, Collections.emptyList(), true);

        addProperty(FORM_TITLE, "");
        addProperty(FORM_TIME_TEXT, "");

        // run validators when the following properties change.
        doOnChange(this::validate, STATUS, MODULE, PATH);
    }

    protected abstract boolean updateIsStampValuesChanged();

    /**
     * Populate default values for the form. This is called when the form is first opened, and can be
     * used to set default values for the form fields. Assumes that the observable view has been set and
     * module and path entities have been fetched.
     */
    public void populateDefaults() {
        setPropertyValue(StampFormViewModelBase.Properties.STATUS, State.ACTIVE);
        // module
        // @TODO Revisit TinkarTerms bindings file because the defaultModuleProperty is returning a module that isn't part of the list of modules available.
        //       int moduleNid = getObservableView().editCoordinate().defaultModuleProperty().get().nid();
        int moduleNid = TinkarTerm.DEVELOPMENT_MODULE.nid();
        List<ComponentWithNid> moduleEntities = getObservableList(StampFormViewModelBase.Properties.MODULES);
        ComponentWithNid module = lookupByNid(moduleNid, moduleEntities);
        setPropertyValue(StampFormViewModelBase.Properties.MODULE, module);

        // Path
        int pathNid = getObservableView().editCoordinate().defaultPathProperty().get().nid();
        List<ComponentWithNid> pathEntities = getObservableList(StampFormViewModelBase.Properties.PATHS);
        ComponentWithNid path = lookupByNid(pathNid, pathEntities);
        setPropertyValue(StampFormViewModelBase.Properties.PATH, path);

    }

    private ComponentWithNid lookupByNid(int nid, List<ComponentWithNid> entities) {
        return entities.stream()
                .filter(entity ->
                        entity.nid() == nid)
                .findFirst().orElse(null);
    }

    /**
     * Updates the form with the given entity, topic, and observable view.
     * The observable view provides edit coordinates and a view calculator for
     * resolving modules, paths, and other STAMP defaults via the scene-graph
     * based {@code KlContext} architecture.
     *
     * @param entity         the entity being edited/created
     * @param topic          the event bus topic for this window
     * @param observableView the observable view coordinate (from {@code KlContext.viewCoordinate()}
     *                       or {@code ViewProperties.nodeView()})
     */
    public final void update(EntityFacade entity, UUID topic, ObservableView observableView) {
        this.observableView = observableView;
        this.topic = topic;

        // initialize observable lists
        Set<ComponentWithNid> modules = fetchDescendentsOfConcept(observableView.calculator(), TinkarTerm.MODULE.publicId()).stream().map(conceptEntity -> (ComponentWithNid) conceptEntity).collect(Collectors.toSet());
        // add default module just in case it isn't a descendent
        modules.add(observableView.editCoordinate().defaultModuleProperty().get());
        Set<ComponentWithNid> paths = fetchDescendentsOfConcept(observableView.calculator(), TinkarTerm.PATH.publicId()).stream().map(conceptEntity -> (ComponentWithNid) conceptEntity).collect(Collectors.toSet());

        if (getObservableList(MODULES).isEmpty()) {
            setPropertyValues(MODULES, modules);
        }
        if (getObservableList(PATHS).isEmpty()) {
            setPropertyValues(PATHS, paths);
        }
        if (getObservableList(STATUSES).isEmpty()) {
            setPropertyValues(STATUSES, List.of(State.values()));
        }

        doUpdate(entity, topic, observableView);
    }

    /**
     * @deprecated Use {@link #update(EntityFacade, UUID, ObservableView)} instead.
     */
    @Deprecated(forRemoval = true)
    public final void update(EntityFacade entity, UUID topic, ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
        update(entity, topic, viewProperties.nodeView());
    }

    protected abstract void doUpdate(EntityFacade entity, UUID topic, ObservableView observableView);

    public void resetOrClearForm(ActionEvent actionEvent) {
        ConfirmationDialogController.showConfirmationDialog(
                (Node) actionEvent.getSource(),
                getClearOrResetConfirmationTitle(),
                getClearOrResetConfirmationMsg()
            )
            .thenAccept(confirmed -> {
                if (confirmed) {
                    reset();
                }
            }
       );
    }

    public void submitOrConfirm() {
        save();
        showSucessToast();

        setPropertyValue(IS_CONFIRMED_OR_SUBMITTED, true);
        EvtBusFactory.getDefaultEvtBus().publish(topic, new ClosePropertiesPanelEvent("Stamp From View Model cancel()",
                ClosePropertiesPanelEvent.CLOSE_PROPERTIES));
    }

    public void cancel() {
        EvtBusFactory.getDefaultEvtBus().publish(topic, new ClosePropertiesPanelEvent("StampViewModel2 cancel()",
                ClosePropertiesPanelEvent.CLOSE_PROPERTIES));
        reset();
    }

    protected abstract void showSucessToast();

    /**
     * Returns the observable view coordinate for this form.
     * Prefer this over {@link #getViewProperties()}.
     */
    public ObservableView getObservableView() { return observableView; }

    /**
     * @deprecated Use {@link #getObservableView()} instead.
     */
    @Deprecated(forRemoval = true)
    public ViewProperties getViewProperties() { return viewProperties; }

    /**
     * Provide the standard Confirm Clear/Reset dialog title.
     */
    protected abstract String getClearOrResetConfirmationMsg();

    /**
     * Provide the standard Confirm Clear/Reset dialog message.
     */
    protected abstract String getClearOrResetConfirmationTitle();

    /***************************************************************************
     *                                                                         *
     * Support Classes                                                         *
     *                                                                         *
     **************************************************************************/

    public enum Properties {
        CURRENT_STAMP,                  // The current stamp

        STATUS,                         // User selected Status
        AUTHOR,                         // Author of the Stamp
        MODULE,                         // User selected Module
        PATH,                           // User selected Path
        TIME,                           // Time of the Stamp

        IS_STAMP_VALUES_THE_SAME_OR_EMPTY,       // Are the Stamp values in the properties the same as of the current Stamp.
        // Cn also indicate if the current stamp values are empty.

        IS_CONFIRMED_OR_SUBMITTED,                   // Signals whether the user has pressed confirmed on the form with valid stamp values

        STATUSES,
        MODULES,
        PATHS,

        FORM_TITLE,
        FORM_TIME_TEXT,

        CLEAR_RESET_BUTTON_TEXT,
        SUBMIT_BUTTON_TEXT
    }

    public enum Type {
        CONCEPT("Concept"),
        PATTERN("Pattern"),
        SEMANTIC("Semantic");

        private final String textDescription;

        Type(String textDescription) {
            this.textDescription = textDescription;
        }

        public String getTextDescription() { return textDescription; }
    }
}