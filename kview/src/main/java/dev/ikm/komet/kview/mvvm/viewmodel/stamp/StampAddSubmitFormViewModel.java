package dev.ikm.komet.kview.mvvm.viewmodel.stamp;

import dev.ikm.komet.kview.controls.Toast;
import dev.ikm.komet.kview.mvvm.view.journal.JournalController;
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
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;

import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.StampProperties.*;

public class StampAddSubmitFormViewModel extends StampAddFormViewModelBase {

    public StampAddSubmitFormViewModel(StampType stampType) {
        super(stampType);

        addProperty(SUBMIT_BUTTON_TEXT, "SUBMIT");
    }

    protected void showSucessToast() {
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
    public StampAddSubmitFormViewModel save() {
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
}
