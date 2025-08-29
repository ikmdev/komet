package dev.ikm.komet.kview.mvvm.view.properties;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.ClosePropertiesPanelEvent;
import dev.ikm.komet.kview.events.CreateConceptEvent;
import dev.ikm.komet.kview.mvvm.model.DescrName;
import dev.ikm.komet.kview.mvvm.model.DescriptionFormType;
import dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.OtherNameViewModel;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.beans.InvalidationListener;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.viewmodel.ViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.fetchDescendentsOfConcept;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.CASE_SIGNIFICANCE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.LANGUAGE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.MODULE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.STATUS;
import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_CASE_SIGNIFICANCE;
import static dev.ikm.tinkar.terms.TinkarTerm.LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION;

public class DescriptionEditOtherController extends DescriptionBaseController<ConceptEntity>{
    private static final Logger LOG = LoggerFactory.getLogger(DescriptionEditOtherController.class);

    @InjectViewModel
    private DescrNameViewModel otherNameViewModel;

    private DescrName editDescrName;

    public DescriptionEditOtherController(UUID conceptTopic) {
        super(DescriptionFormType.EDIT_OTHER_NAME, conceptTopic);
    }
    @Override
    protected void initializeData() {

        configureDialectVisibility(true);

        otherNameViewModel
                .setPropertyValue(NAME_TYPE, TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE)
                .setPropertyValue(STATUS, TinkarTerm.ACTIVE_STATE);

        populateDialectComboBoxes();

        // bind with viewmodel.
        nameTextField.textProperty().bindBidirectional(otherNameViewModel.getProperty(NAME_TEXT));
        moduleComboBox.valueProperty().bindBidirectional(otherNameViewModel.getProperty(MODULE));
        caseSignificanceComboBox.valueProperty().bindBidirectional(otherNameViewModel.getProperty(CASE_SIGNIFICANCE));
        statusComboBox.valueProperty().bindBidirectional(otherNameViewModel.getProperty(STATUS));
        languageComboBox.valueProperty().bindBidirectional(otherNameViewModel.getProperty(LANGUAGE));

        InvalidationListener invalidationListener = obs -> validateForm();

        nameTextField.textProperty().addListener(invalidationListener);
        moduleComboBox.valueProperty().addListener(invalidationListener);
        caseSignificanceComboBox.valueProperty().addListener(invalidationListener);
        statusComboBox.valueProperty().addListener(invalidationListener);
        languageComboBox.valueProperty().addListener(invalidationListener);
        validateForm();

    }

    @Override
    protected void onCancel() {
        close(cancelButton);
    }

    @Override
    protected void onSubmit() {
        otherNameViewModel.save();

        if (!otherNameViewModel.hasNoErrorMsgs()) {
            otherNameViewModel.getValidationMessages().stream().forEach(msg -> LOG.error("Validation error " + msg));
            return;
        }

        otherNameViewModel.setPropertyValue(IS_SUBMITTED, true);


        LOG.info("Ready to update to the concept view model: " + otherNameViewModel);

        if(this.publicId != null) { //This if blocked is called when editing the exiting concept.
            otherNameViewModel.updateOtherName(this.publicId);
        }else{  // This block is called when editing the while creating the concept.
            otherNameViewModel.updateData(editDescrName);
            eventBus.publish(conceptTopic, new CreateConceptEvent(this,
                    CreateConceptEvent.EDIT_OTHER_NAME, editDescrName));
        }
        eventBus.publish(conceptTopic, new ClosePropertiesPanelEvent(submitButton,
                ClosePropertiesPanelEvent.CLOSE_PROPERTIES));
    }

    @Override
    public DescrNameViewModel getViewModel() { // TODO why diverge here from DescriptionAddOtherController with return type
        return otherNameViewModel;
    }

    @Override
    public void updateView() {

    }

    @Override
    public void clearView() {
        caseSignificanceComboBox.getItems().clear();
        statusComboBox.getItems().clear();
        moduleComboBox.getItems().clear();
        languageComboBox.getItems().clear();
    }

    public void updateModel(final ViewProperties viewProperties, EntityFacade entityFacade) {
        this.viewProperties = viewProperties;
        this.entityFacade = entityFacade;
    }

    /**
     * This method prepopulates and sets up the form in edit mode.
     * @param descrName model values that need to be prepopulated.
     */
    public void setConceptAndPopulateForm(DescrName descrName) {
        editDescrName = descrName;
        setupComboBoxEdit(moduleComboBox, fetchDescendentsOfConcept(getViewProperties(), TinkarTerm.MODULE.publicId()));
        setupComboBoxEdit(statusComboBox, fetchDescendentsOfConcept(getViewProperties(), TinkarTerm.STATUS_VALUE.publicId()));
        setupComboBoxEdit(caseSignificanceComboBox, otherNameViewModel.findAllCaseSignificants(getViewProperties()));
        setupComboBoxEdit(languageComboBox, fetchDescendentsOfConcept(getViewProperties(), TinkarTerm.LANGUAGE.publicId()));
        otherNameViewModel.setPropertyValue(NAME_TEXT, descrName.getNameText())
                .setPropertyValue(CASE_SIGNIFICANCE, descrName.getCaseSignificance())
                .setPropertyValue(STATUS, descrName.getStatus())
                .setPropertyValue(MODULE, descrName.getModule())
                .setPropertyValue(LANGUAGE, descrName.getLanguage());
    }

    public void setConceptAndPopulateForm(PublicId publicId) {
        editDescrName = null;
        this.publicId = publicId;
        ViewCalculator viewCalculator = viewProperties.calculator();
        int nid = EntityService.get().nidForPublicId(publicId);

        // this is the Other Name
        Latest<SemanticEntityVersion> latestEntityVersion = viewCalculator.latest(nid);
        latestEntityVersion.ifPresent(semanticEntityVersion -> {
            StampEntity stampEntity = latestEntityVersion.get().stamp();

            // populate the other name text field (e.g. 'Chronic lung disease')
            String otherName = viewCalculator.getDescriptionText(nid).get();
            this.nameTextField.setText(otherName);

            Entity<? extends EntityVersion> moduleEntity = EntityService.get().getEntityFast(TinkarTerm.MODULE);
            IntIdSet moduleDescendents = viewProperties.parentView().calculator().descendentsOf(moduleEntity.nid());

            // get all descendant modules
            Set<ConceptEntity> allModules =
                    moduleDescendents.intStream()
                            .mapToObj(moduleNid -> (ConceptEntity) Entity.getFast(moduleNid))
                            .collect(Collectors.toSet());
            setupComboBoxEdit(moduleComboBox, allModules);

            // populate the current module and select it (e.g. 'SNOMED CT core module')
            findByNid(moduleComboBox.getItems(), stampEntity.moduleNid())
                    .ifPresent(concept -> otherNameViewModel.setPropertyValue(MODULE, concept));

            // get all statuses
            IntIdSet statusDescendents = viewProperties.parentView().calculator().descendentsOf(TinkarTerm.STATUS_VALUE.nid());
            Set<ConceptEntity> allStatuses = statusDescendents.intStream()
                    .mapToObj(statusNid -> (ConceptEntity) Entity.getFast(statusNid))
                    .collect(Collectors.toSet());
            setupComboBoxEdit(statusComboBox, allStatuses);

            // populate the current status (ACTIVE | INACTIVE) and select it
            findByNid(statusComboBox.getItems(), stampEntity.stateNid())
                    .ifPresent(concept -> otherNameViewModel.setPropertyValue(STATUS, concept));

            // populate all case significance choices
            IntIdSet caseSenseDescendents = viewProperties.parentView().calculator().descendentsOf(TinkarTerm.DESCRIPTION_CASE_SIGNIFICANCE.nid());
            Set<ConceptEntity> allCaseDescendents = caseSenseDescendents.intStream()
                    .mapToObj(caseNid -> (ConceptEntity) Entity.getFast(caseNid))
                    .collect(Collectors.toSet());
            setupComboBoxEdit(caseSignificanceComboBox, allCaseDescendents);

            // get case concept's case sensitivity (e.g. 'Case insensitive')
            PatternEntity<PatternEntityVersion> patternEntity = latestEntityVersion.get().pattern();
            PatternEntityVersion patternEntityVersion = viewCalculator.latest(patternEntity).get();
            int indexCaseSig = patternEntityVersion.indexForMeaning(DESCRIPTION_CASE_SIGNIFICANCE);
            EntityFacade caseSigConceptFacade = (EntityFacade) latestEntityVersion.get().fieldValues().get(indexCaseSig);
            findByNid(caseSignificanceComboBox.getItems(), caseSigConceptFacade.nid())
                    .ifPresent(concept -> otherNameViewModel.setPropertyValue(CASE_SIGNIFICANCE, concept));

            // get all available languages
            IntIdSet languageDescendents = viewProperties.parentView().calculator().descendentsOf(TinkarTerm.LANGUAGE.nid());
            Set<ConceptEntity> allLangs = languageDescendents.intStream()
                    .mapToObj(langNid -> (ConceptEntity) Entity.getFast(langNid))
                    .collect(Collectors.toSet());
            setupComboBoxEdit(languageComboBox, allLangs);

            // get the language (e.g. 'English language')
            int indexLang = patternEntityVersion.indexForMeaning(LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION);
            ConceptFacade langConceptFacade = (ConceptFacade) latestEntityVersion.get().fieldValues().get(indexLang);
            findByNid(languageComboBox.getItems(), langConceptFacade.nid())
                    .ifPresent(concept -> otherNameViewModel.setPropertyValue(LANGUAGE, concept));

            //initial state of edit screen, the submit button should be disabled
            submitButton.setDisable(true);

            LOG.info(publicId.toString());
        });
    }

    private Optional<ConceptEntity> findByNid(List<ConceptEntity> items, int nid) {

        Optional<ConceptEntity> conceptOption = items.stream().parallel()
                .filter(item -> (item.nid() == nid)).findAny();

        return conceptOption;
    }
}
