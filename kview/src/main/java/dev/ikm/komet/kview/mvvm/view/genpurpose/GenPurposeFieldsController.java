/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.kview.mvvm.view.genpurpose;

import dev.ikm.komet.framework.observable.ObservableComposer;
import dev.ikm.komet.framework.observable.ObservableConcept;
import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.framework.observable.ObservableEntityHandle;
import dev.ikm.komet.framework.observable.ObservableEntitySnapshot;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservablePattern;
import dev.ikm.komet.framework.observable.ObservableSemantic;
import dev.ikm.komet.framework.observable.ObservableSemanticSnapshot;
import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.observable.ObservableVersion;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.Toast;
import dev.ikm.komet.kview.events.pattern.PatternSavedEvent;
import dev.ikm.komet.kview.mvvm.view.genediting.ConfirmationDialogController;
import dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.FormMode;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.version.field.KlField;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.Field;
import dev.ikm.tinkar.entity.FieldRecord;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.events.EntityVersionChangeEvent;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.Subscriber;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityBinding;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static dev.ikm.komet.kview.events.EventTopics.SAVE_PATTERN_TOPIC;
import static dev.ikm.komet.kview.klfields.KlFieldHelper.createDefaultFieldValues;
import static dev.ikm.komet.kview.klfields.KlFieldHelper.createEditableKlField;
import static dev.ikm.komet.kview.klfields.KlFieldHelper.retrieveCommittedLatestVersion;
import static dev.ikm.komet.kview.mvvm.view.journal.JournalController.toast;
import static dev.ikm.tinkar.events.FrameworkTopics.VERSION_CHANGED_TOPIC;

public class GenPurposeFieldsController {

    private static final Logger LOG = LoggerFactory.getLogger(GenPurposeFieldsController.class);

    /** The form title while editing one of the section pattern's semantics. */
    private static final String DEFAULT_FORM_TITLE = "Pattern Fields";

    private static final String CONFIRM_CLEAR_TITLE = "Confirm Clear Form";
    private static final String CONFIRM_CLEAR_MESSAGE = "Are you sure you want to clear the form? All entered data will be lost.";

    private final GenPurposeFieldsView view;

    /**
     * What the window is editing: the form follows its mode, edits on its coordinate and, unless
     * editing through {@link #formComposer}, in its composer.
     */
    private final WindowEditSession session;

    // ObservableComposer integration for proper transaction management
    private ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> semanticEditor;
    private ObservableSemanticVersion.Editable editableVersion;
    private ObservableStamp currentEditStamp;

    private final List<KlField> klFields = new ArrayList<>();

    ObservableEntityHandle observableEntityHandle;

    ObservableEntitySnapshot observableEntitySnapshot;

    Subscriber<EntityVersionChangeEvent> entityVersionChangeEventSubscriber;

    private boolean reloadPatternNavigator;

    private SemanticEntity<SemanticEntityVersion> currentEditingSemantic;

    /**
     * The KL Editor model of the pattern the semantic being edited is edited as. Its fields carry how
     * the form's fields were authored to behave, e.g. whether they can still be edited in edit mode.
     */
    private EditorPatternModel editorPatternModel;

    /**
     * The composer the semantic being edited is composed through when it is not the window's own —
     * a pattern's defaults semantic commits in its own module (see
     * {@link GenPurposePropertiesController#showDefaultsForm}); null while editing through the
     * window's composer.
     */
    private ObservableComposer formComposer;

    /**
     * Whether this form instance is the properties panel's DEFAULTS tab, editing the pattern's
     * defaults semantic (see {@link GenPurposePropertiesController#showDefaultsForm}), rather
     * than its ADD/EDIT tab editing the section patterns' semantics.
     */
    private final boolean isDefaultsForm;

    /**
     * Where a submitted semantic goes once its edited values are saved: the window, which
     * refreshes the details area, decides whether it commits now and answers what became of it.
     * Not used by the defaults form, which commits through its own composer.
     */
    private Function<SemanticEntity<SemanticEntityVersion>, SubmitOutcome> onSubmitted;

    /**
     * Where a cancelled semantic goes once the form has put its fields back: the window drops the
     * form's edits from its transaction — or the semantic itself, when the form was creating it.
     * Not used by the defaults form.
     */
    private Consumer<SemanticEntity<SemanticEntityVersion>> onCancelled = _ -> { };

    /** Closes the properties panel once the form is done with (submit, cancel). */
    private Runnable onCloseRequested = () -> { };

    public void setOnSubmitted(Function<SemanticEntity<SemanticEntityVersion>, SubmitOutcome> onSubmitted) {
        this.onSubmitted = onSubmitted;
    }

    public void setOnCancelled(Consumer<SemanticEntity<SemanticEntityVersion>> onCancelled) {
        this.onCancelled = onCancelled;
    }

    public void setOnCloseRequested(Runnable onCloseRequested) {
        this.onCloseRequested = onCloseRequested;
    }

    private ObservableComposer getComposer() {
        return formComposer != null ? formComposer : session.createOrGetComposer();
    }

    /**
     * @param view         the form's layout, which the controller fills and drives
     * @param session      what the window is editing: the form follows its mode and edits in its
     *                     composer unless given another
     * @param isDefaultsForm whether this instance is the DEFAULTS tab's form (see {@link #isDefaultsForm})
     */
    public GenPurposeFieldsController(GenPurposeFieldsView view, WindowEditSession session, boolean isDefaultsForm) {
        this.view = view;
        this.session = session;
        this.isDefaultsForm = isDefaultsForm;

        view.setOnCancel(this::cancel);
        view.setOnClearOrResetForm(this::clearOrResetForm);
        view.setOnSubmit(this::submit);
        view.setFieldNodeFactory(this::createFieldNode);

        // The defaults form publishes on its own button (see submit) rather than leaving them uncommitted for the
        // toolbar's Publish, so its button says what it does.
        view.setSubmitButtonText(isDefaultsForm ? "Publish" : "Submit");

        // The form's Clear/Reset button follows the window's mode.
        session.modeProperty().subscribe((mode) -> {
            if (mode == FormMode.EDIT) {
                view.setClearOrResetButtonText("Reset form");
            } else {
                view.setClearOrResetButtonText("Clear form");
            }
        });

        view.setSubmitDisabled(true); // disable submit until fields changed.

        reloadPatternNavigator = true;
    }

    /** The form's layout, to place in the properties panel. */
    public GenPurposeFieldsView getView() {
        return view;
    }

    /**
     * Loads the semantic into the form: one editor per field, replacing whatever semantic the form
     * was editing before.
     *
     * @param semanticEntity     the semantic to edit
     * @param editorPatternModel the KL Editor model of the pattern the semantic is edited as; null
     *                           for a semantic with no placement in the layout (a pattern's
     *                           defaults semantic), whose fields are all editable
     * @param composer           the composer to edit the semantic through; null for the window's own
     * @param formTitle          the form's title; null for the default "Pattern Fields"
     */
    public void showSemantic(SemanticEntity<SemanticEntityVersion> semanticEntity,
                             EditorPatternModel editorPatternModel,
                             ObservableComposer composer, String formTitle) {
        // Clear previous controls that might be there from previously editing another Semantic
        klFields.clear();

        observableEntityHandle = ObservableEntityHandle.get(semanticEntity.nid());

        currentEditingSemantic = semanticEntity;
        this.editorPatternModel = editorPatternModel;
        this.formComposer = composer;
        view.setFormTitle(formTitle != null ? formTitle : DEFAULT_FORM_TITLE);

        this.observableEntityHandle.ifSemantic(observableSemantic -> {
            observableEntitySnapshot = observableSemantic.getSnapshot(getViewProperties().calculator());

            //Setting author to author for change. This value will be used during auto-save
            ConceptFacade authorForChanges = getViewProperties().nodeView().editCoordinate().getAuthorForChanges();
            //TODO: setAuthorForChanges seems to be an API decision to revisit.
            if (observableEntitySnapshot instanceof ObservableSemanticSnapshot observableSemanticSnapshot) {
                observableSemanticSnapshot.getLatestVersion().get().setAuthorForChanges(authorForChanges);
            }
            loadUIData(); // And populates Nodes and Observable fields.

            // Drop the previous edit session's subscriber before registering one for this
            // semantic. Left subscribed, it would react to its own semantic being saved (e.g.
            // when the shared transaction commits all pending semantics at once) by writing
            // that semantic's field values into the fields now loaded for THIS semantic —
            // corrupting them with wrongly-typed values (ClassCastException).
            if (entityVersionChangeEventSubscriber != null) {
                EvtBusFactory.getDefaultEvtBus().unsubscribe(VERSION_CHANGED_TOPIC,
                        EntityVersionChangeEvent.class, entityVersionChangeEventSubscriber);
            }

            entityVersionChangeEventSubscriber = evt -> {
                LOG.info("Version has been updated: " + evt.getEventType());
                // get payload
                if (evt.getEntityVersion().nid() == observableSemantic.nid()
                        && evt.getEntityVersion() instanceof SemanticVersionRecord semanticVersionRecord) {
                    ImmutableList<Object> values = semanticVersionRecord.fieldValues();
                    for (int i = 0; i < values.size(); i++) {
                        ObservableField.Editable<?> editableField = getKlFields().get(i).fieldEditable();
                        // Update via editable field's cached property
                        @SuppressWarnings("unchecked")
                        ObservableField.Editable<Object> uncheckedField = (ObservableField.Editable<Object>) editableField;
                        uncheckedField.setValue(values.get(i));
                    }
                }
                if (reloadPatternNavigator && session.isCreateMode()) {
                    // refresh the pattern navigation
                    EvtBusFactory.getDefaultEvtBus().publish(SAVE_PATTERN_TOPIC,
                            new PatternSavedEvent(this, PatternSavedEvent.PATTERN_CREATION_EVENT));
                    reloadPatternNavigator = false;
                }
            };

            EvtBusFactory.getDefaultEvtBus().subscribe(VERSION_CHANGED_TOPIC,
                    EntityVersionChangeEvent.class, entityVersionChangeEventSubscriber);
        });
    }

    /**
     * Refactored to use ObservableComposer pattern for proper transaction management.
     */
    private void loadUIData() {
        // Get the observable semantic from the handle
        observableEntityHandle.ifSemantic(observableSemantic -> {
            // Create semantic editor using composer unified API
            // Get referenced component and pattern from the semantic
            ObservableEntity referencedComponent = ObservableEntityHandle.get(observableSemantic.referencedComponentNid()).expectEntity();
            ObservablePattern pattern = ObservableEntityHandle.get(observableSemantic.patternNid()).expectPattern();
            semanticEditor = getComposer().composeSemantic(observableSemantic.publicId(), referencedComponent, pattern);

            // Get editable version with cached editing capabilities
            editableVersion = semanticEditor.getEditableVersion();

            // Get the edit stamp for UI generation
            currentEditStamp = editableVersion.getEditStamp();

            ObservableList<ObservableField.Editable<?>> editables = editableVersion.getEditableFields();
            // Generate UI nodes from editable fields. A new semantic's component fields already
            // start as BLANK_CONCEPT (ObservableComposer.generateDefaultFieldValues); any other
            // value was set deliberately — e.g. seeded from a section's display filter — so no
            // create-mode blanking here.
            view.getFields().setAll(editables);
            view.setSubmitDisabled(true);
            LOG.info("Loaded UI with {} editable fields using ObservableComposer", getKlFields().size());
        });
    }

    /** Makes the editor for one of the semantic's fields; the view calls this for each field it shows. */
    private Node createFieldNode(ObservableField.Editable<?> editableField) {
        Field field = editableField.field();
        KlField<?> klField = createEditableKlField(
                (FieldRecord<?>) field,
                editableField,
                getViewProperties(),
                currentEditStamp);
        klField.doOnEditableValuePropertyChange(() -> view.setSubmitDisabled(false));
        getKlFields().add(klField);
        // A field authored in the KL Editor as not editable in edit mode is still shown in
        // the form, but can't be changed.
        klField.fxObject().setDisable(!canEditField(editableField.getObservableFeature().indexInPattern()));
        return klField.fxObject();
    }

    /**
     * Whether the field at the given index in the pattern can be edited in the form right now. While
     * the window is in create mode, and for a semantic that is still being created (no committed
     * version yet), every field can — its values have to be filled in somewhere. Once the window is
     * in edit mode a committed semantic's field can only be edited if it was authored as editable in
     * the KL Editor (authored per pattern, so it covers fields not shown in the layout too).
     */
    private boolean canEditField(int fieldIndex) {
        if (session.isCreateMode()
                || currentEditingSemantic.versions().stream().allMatch(SemanticEntityVersion::uncommitted)) {
            return true;
        }
        // A semantic with no placement in the layout (a pattern's defaults semantic) has no
        // authored restrictions.
        return editorPatternModel == null || editorPatternModel.isFieldEditable(fieldIndex);
    }

    public ViewProperties getViewProperties() {
        return session.getViewProperties();
    }

    /**
     * Cancels the form: what was edited since it opened is dropped. The fields go back to the
     * version the form opened on — the editable version is canonical per stamp, so the next form
     * on this semantic would otherwise come back showing the edits just cancelled — and the window
     * drops the edits from its transaction, or the semantic itself when the form was creating it.
     */
    private void cancel() {
        revertFieldValues();
        onCancelled.accept(currentEditingSemantic);
        onCloseRequested.run();
    }

    /**
     * Puts the fields back to the version the form opened on: the latest version in the view when
     * the semantic was loaded, published or not — unlike Reset form, which goes back to the latest
     * published version.
     */
    private void revertFieldValues() {
        ObservableVersion openedVersion = (ObservableVersion) observableEntitySnapshot.getLatestVersion().get();
        setFieldValuesFrom(openedVersion.getVersionRecord());
    }

    private void clearOrResetForm() {
        // if create mode display the confirm clear dialog
        if (session.isCreateMode()) {
            ConfirmationDialogController.showConfirmationDialog(view, CONFIRM_CLEAR_TITLE, CONFIRM_CLEAR_MESSAGE)
                    .thenAccept(confirmed -> {
                        if (confirmed) {
                            doTheClearOrResetForm();
                        }
                    });
        } else {
            ConfirmationDialogController.showConfirmationDialog(view,
                            "Confirm Reset Form",
                            "Your changes will be lost if you reset the form. Are you sure you want to continue?")
                    .thenAccept(confirmed -> {
                        if (confirmed) {
                            doTheClearOrResetForm();
                        }
                    });
        }
    }

    private void doTheClearOrResetForm() {
        Latest<EntityVersion> latestCommitted = retrieveCommittedLatestVersion(observableEntitySnapshot);
        latestCommitted.ifPresentOrElse(this::setFieldValuesFrom, this::clearField);
        view.setClearOrResetDisabled(true);
    }

    /**
     * Clears the fields in create mode
     */
    private void clearField() {
        observableEntityHandle.ifPresent(observableEntity -> {
            EntityFacade patternForEntity = switch (observableEntity) {
                case ObservableConcept concept -> EntityBinding.Concept.pattern();
                case ObservablePattern pattern -> EntityBinding.Pattern.pattern();
                case ObservableSemantic semantic -> semantic.pattern();
                case ObservableStamp stamp -> EntityBinding.Stamp.pattern();
            };
            ImmutableList<Object> fieldValues = createDefaultFieldValues(patternForEntity, getViewProperties());
            for (int i = 0; i < fieldValues.size(); i++) {
                ObservableField.Editable<?> editableField = getKlFields().get(i).fieldEditable();
                // Use setValue() to update via editable field
                @SuppressWarnings("unchecked")
                ObservableField.Editable<Object> uncheckedField = (ObservableField.Editable<Object>) editableField;
                uncheckedField.setValue(fieldValues.get(i));
            }
        });
    }

    /**
     * Sets every field editor to the value the version holds for it: what Reset form does with
     * the latest published version and Cancel with the version the form opened on. Nothing is
     * saved; the editable fields behind the form take the values.
     *
     * @param entityVersion the semantic version whose field values the form takes
     */
    private void setFieldValuesFrom(EntityVersion entityVersion) {
        if (entityVersion instanceof SemanticEntityVersion semanticEntityVersion) {
            for (int i = 0; i < semanticEntityVersion.fieldValues().size(); i++) {
                Object object = semanticEntityVersion.fieldValues().get(i);
                ObservableField.Editable<?> editableField = getKlFields().get(i).fieldEditable();
                // Use setValue() to update via editable field
                @SuppressWarnings("unchecked")
                ObservableField.Editable<Object> uncheckedField = (ObservableField.Editable<Object>) editableField;
                uncheckedField.setValue(object);
            }
        }
    }

    /**
     * Returns KlFields used to rebind {@link ObservableField.Editable} objects.
     *
     * @return Returns klFields used to rebind {@link ObservableField.Editable} objects.
     */
    private List<KlField> getKlFields() {
        return klFields;
    }

    /**
     * Refactored submit using ObservableComposer pattern.
     * Saves editable version and commits the transaction.
     */
    private void submit() {
        view.setSubmitDisabled(true);
        view.focusCancelButton();

        try {
            try {
                LOG.info("Committed semantic changes successfully ");
                // Refresh observable handles and snapshots
                if (observableEntityHandle.isPresent()) {
                    observableEntitySnapshot = observableEntityHandle.expectEntity()
                            .getSnapshot(getViewProperties().calculator());
                }

                // Persist the edited field values as an uncommitted version. The window may not
                // commit right away — in the Publish-flow window it stays uncommitted until the toolbar's
                // Publish button, and otherwise create mode defers until every required pattern
                // is satisfied — and the details area re-renders from the stored version, so the
                // values must be saved, not left pending in the editable overlay.
                semanticEditor.save();

                if (isDefaultsForm) {
                    // The pattern's field defaults publish right here: they are not part of the
                    // window's own uncommitted changes, and they only take effect once published (see
                    // PatternFieldDefaults.defaultsSemanticVersion), so there is nothing to gain
                    // from leaving them uncommitted until the toolbar's Publish button.
                    getComposer().commit();
                    onCloseRequested.run();
                    toast().show(Toast.Status.SUCCESS, "Field defaults published");
                    return;
                }

                // Hand the saved semantic to the window, which refreshes the details area and
                // decides whether it commits now.
                SubmitOutcome outcome = onSubmitted.apply(currentEditingSemantic);

                // Submitting finishes the edit form, so close the properties bumpout.
                onCloseRequested.run();

                switch (outcome) {
                    case UNCOMMITTED_UNTIL_PUBLISHED -> {
                        // Point at the Publish button as the remaining step (in create mode the
                        // window's create-mode hint already says so).
                        if (!session.isCreateMode()) {
                            toast().show(Toast.Status.SUCCESS, "Changes saved - hit Publish to apply them");
                        }
                    }
                    // Creation is deferred while required patterns still miss a semantic — the
                    // window's create-mode hint covers that.
                    case UNCOMMITTED_UNTIL_REQUIREMENTS_MET -> { }
                    case COMMITTED -> toast().show(Toast.Status.SUCCESS, "Semantic Details Edited Successfully");
                    case COMPONENT_CREATED -> toast().show(Toast.Status.SUCCESS, "Concept created");
                }
            } catch (Exception e) {
                LOG.error("Error committing semantic changes", e);
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR,
                            "Failed to commit changes: " + e.getMessage(),
                            ButtonType.OK);
                    alert.setHeaderText("Commit Failed");
                    alert.showAndWait();
                });
            }

        } catch (Exception e) {
            LOG.error("Error during submit", e);
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Failed to save changes: " + e.getMessage(),
                    ButtonType.OK);
            alert.setHeaderText("Save Failed");
            alert.showAndWait();
        }
    }
}
