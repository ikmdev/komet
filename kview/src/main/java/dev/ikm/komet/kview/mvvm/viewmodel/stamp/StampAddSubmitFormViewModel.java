package dev.ikm.komet.kview.mvvm.viewmodel.stamp;

import dev.ikm.komet.framework.observable.*;
import dev.ikm.komet.kview.controls.Toast;
import dev.ikm.komet.kview.mvvm.view.journal.JournalController;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.State;

import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.*;

public class StampAddSubmitFormViewModel extends StampAddFormViewModelBase {

    public StampAddSubmitFormViewModel(Type type) {
        super(type);

        addProperty(SUBMIT_BUTTON_TEXT, "SUBMIT");
    }

    protected void showSucessToast() {
        State status = getValue(STATUS);
        ObservableConcept module = StampProperties.MODULE.getFrom(this);
        ObservableConcept path = StampProperties.PATH.getFrom(this);

        String statusString = getViewProperties().calculator().getDescriptionTextOrNid(status.nid());
        String moduleString = getViewProperties().calculator().getDescriptionTextOrNid(module.nid());
        String pathString = getViewProperties().calculator().getDescriptionTextOrNid(path.nid());

        String submitMessage = "New " + type.getTextDescription() + " version created (" + statusString +
                ", " + moduleString + ", " + pathString + ")";

        JournalController.toast()
                .show(
                        Toast.Status.SUCCESS,
                        submitMessage
                );
    }

    @Override
    public StampAddSubmitFormViewModel save() {
        super.save();

        if (invalidProperty().get()) {
            // Validation error so returning and not going to run the code to save to DB
            return this;
        }

        // -----------  Get type-safe canonical observable entities from properties ------------
        State status = getValue(STATUS);
        ObservableConcept module = StampProperties.MODULE.getFrom(this);
        ObservableConcept path = StampProperties.PATH.getFrom(this);
        ObservableConcept author = StampProperties.AUTHOR.getFrom(this);

        // -----------  Create ObservableComposer for STAMP management --------------
        ObservableComposer composer = ObservableComposer.create(
                getViewProperties().calculator().stampCalculator(),
                status,
                author,
                module,
                path,
                "Save new STAMP in Component"
        );

        // -----------  Get the observable entity and create editable version using sealed pattern --------------
        // Get observable entity using ObservableEntityHandle (type-safe)
        ObservableEntity<?> observableEntity = ObservableEntityHandle.get(entityFacade.nid()).expectEntity();

        // Use sealed class pattern with switch expression for exhaustive type checking
        // Compiler guarantees all ObservableEntity subtypes are handled
        switch (observableEntity) {
            case ObservableConcept concept -> {
                // Create editable concept version through ObservableComposer unified API
                ObservableComposer.EntityComposer<ObservableEditableConceptVersion, ObservableConcept> editor =
                        composer.composeConcept(concept.publicId());

                // Save creates uncommitted version with new stamp coordinates
                editor.save();
            }
            case ObservablePattern pattern -> {
                // Create editable pattern version through ObservableComposer unified API
                ObservableComposer.EntityComposer<ObservableEditablePatternVersion, ObservablePattern> editor =
                        composer.composePattern(pattern.publicId());

                // Save creates uncommitted version with new stamp coordinates
                editor.save();
            }
            case ObservableSemantic semantic -> {
                // Create editable semantic version through ObservableComposer unified API
                // Get referenced component and pattern from the semantic
                ObservableEntity referencedComponent = ObservableEntityHandle.get(semantic.referencedComponentNid()).expectEntity();
                ObservablePattern patternFacade = ObservableEntityHandle.get(semantic.patternNid()).expectPattern();
                ObservableComposer.EntityComposer<ObservableEditableSemanticVersion, ObservableSemantic> editor =
                        composer.composeSemantic(semantic.publicId(), referencedComponent, patternFacade);

                // Save creates uncommitted version with new stamp coordinates
                editor.save();
            }
            case ObservableStamp stamp -> {
                // Cannot add new STAMP version to STAMP entity - not a valid operation
                throw new RuntimeException("Cannot add new STAMP version to STAMP entity - STAMP entities do not support versioning");
            }
        }

        // Commit the transaction to finalize the stamp timestamps
        composer.commit();

        // Load the new STAMP and store the new initial values
        loadStamp();
        loadStampValuesFromDB();
        save(true);
        updateIsStampValuesChanged();

        return this;
    }
}
