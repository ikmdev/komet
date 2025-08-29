package dev.ikm.komet.kview.mvvm.view.properties;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.ClosePropertiesPanelEvent;
import dev.ikm.komet.kview.events.CreateConceptEvent;
import dev.ikm.komet.kview.events.EditConceptEvent;
import dev.ikm.komet.kview.mvvm.model.DescrName;
import dev.ikm.komet.kview.mvvm.model.DescriptionFormType;
import dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel;
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
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.STATUS;
import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_CASE_SIGNIFICANCE;
import static dev.ikm.tinkar.terms.TinkarTerm.LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION;

public class DescriptionEditFqnController extends DescriptionBaseController<ConceptEntity>{

    private static final Logger LOG = LoggerFactory.getLogger(DescriptionEditFqnController.class);


    @InjectViewModel
    private DescrNameViewModel fqnViewModel;

    public DescriptionEditFqnController(UUID conceptTopic) {
        super(DescriptionFormType.EDIT_FQN, conceptTopic);
    }


    @Override
    protected void initializeData() {
        titleLabel.setText("Edit Description: Fully Qualified Name");
        configureDialectVisibility(true);
        populateDialectComboBoxes();

        fqnViewModel.setPropertyValue(NAME_TYPE, TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE);

        // bind with viewmodel.
        nameTextField.textProperty().bindBidirectional(fqnViewModel.getProperty(NAME_TEXT));
        moduleComboBox.valueProperty().bindBidirectional(fqnViewModel.getProperty(MODULE));
        caseSignificanceComboBox.valueProperty().bindBidirectional(fqnViewModel.getProperty(CASE_SIGNIFICANCE));
        statusComboBox.valueProperty().bindBidirectional(fqnViewModel.getProperty(STATUS));
        languageComboBox.valueProperty().bindBidirectional(fqnViewModel.getProperty(LANGUAGE));

        InvalidationListener invalidationListener = obs -> validateForm();

        nameTextField.textProperty().addListener(invalidationListener);
        moduleComboBox.valueProperty().addListener(invalidationListener);
        caseSignificanceComboBox.valueProperty().addListener(invalidationListener);
        statusComboBox.valueProperty().addListener(invalidationListener);
        languageComboBox.valueProperty().addListener(invalidationListener);

        validateForm();
        // submitButton.setOnAction(onSubmit); // this is already linked in the fxml not sure why ?

    }

    @Override
    protected void onCancel() {
        close(cancelButton);
    }

    @Override
    protected void onSubmit() {
        fqnViewModel.save();
        // validate
        if (fqnViewModel.hasErrorMsgs()) {
            fqnViewModel.getValidationMessages().stream().forEach(msg -> LOG.error("Validation error " + msg));
            return;
        }
        fqnViewModel.setPropertyValue(IS_SUBMITTED, true);

        DescrName fqnDescrName = fqnViewModel.create();


        if(this.publicId != null) {
            // delegate the transaction logic to the view model
            fqnViewModel.updateFullyQualifiedName(this.publicId);
        }else{
            // Concept is edited before the transaction is saved. Hence the pubicId would not be generated.
            eventBus.publish(conceptTopic, new CreateConceptEvent(this, CreateConceptEvent.ADD_FQN, fqnDescrName));
        }

        LOG.info("transaction complete");
        clearView();

        // publish the event of the updated FQN
        eventBus.publish(conceptTopic, new EditConceptEvent(submitButton,
                EditConceptEvent.EDIT_FQN, fqnDescrName));

        // close the property bump out panel
        eventBus.publish(conceptTopic, new ClosePropertiesPanelEvent(submitButton,
                ClosePropertiesPanelEvent.CLOSE_PROPERTIES));
    }

    @Override
    public DescrNameViewModel getViewModel() {
        return fqnViewModel;
    }

    @Override
    public void updateView() {

    }

    @Override
    public void clearView() {
        nameTextField.setText("");
        caseSignificanceComboBox.getSelectionModel().clearSelection();
        statusComboBox.getSelectionModel().clearSelection();
        moduleComboBox.getSelectionModel().clearSelection();
        languageComboBox.getSelectionModel().clearSelection();
    }

    /**
     * This method prepopulates and sets up the form in edit mode.
     * @param descrName model values that need to be prepopulated.
     */
    public void setConceptAndPopulateForm(DescrName descrName) {
        setupComboBoxEdit(moduleComboBox, fetchDescendentsOfConcept(getViewProperties(), TinkarTerm.MODULE.publicId()));
        setupComboBoxEdit(statusComboBox, fetchDescendentsOfConcept(getViewProperties(), TinkarTerm.STATUS_VALUE.publicId()));
        setupComboBoxEdit(caseSignificanceComboBox, fqnViewModel.findAllCaseSignificants(getViewProperties()));
        setupComboBoxEdit(languageComboBox, fetchDescendentsOfConcept(getViewProperties(), TinkarTerm.LANGUAGE.publicId()));
        fqnViewModel.setPropertyValue(NAME_TEXT, descrName.getNameText())
                .setPropertyValue(CASE_SIGNIFICANCE, descrName.getCaseSignificance())
                .setPropertyValue(STATUS, descrName.getStatus())
                .setPropertyValue(MODULE, descrName.getModule())
                .setPropertyValue(LANGUAGE, descrName.getLanguage());
    }

    public void setConceptAndPopulateForm(PublicId publicId) {
        this.publicId = publicId;

        ViewCalculator viewCalculator = viewProperties.calculator();

        int nid = EntityService.get().nidForPublicId(publicId);

        // this is the Other Name
        Latest<SemanticEntityVersion> latestEntityVersion = viewCalculator.latest(nid);
        latestEntityVersion.ifPresent(semanticEntityVersion -> {
            StampEntity stampEntity = latestEntityVersion.get().stamp();
            String otherName = viewCalculator.getDescriptionText(nid).get();
            this.nameTextField.setText(otherName);

            // get all descendant modules
            IntIdSet moduleDescendents = viewProperties.parentView().calculator().descendentsOf(TinkarTerm.MODULE.nid());
            Set<ConceptEntity> allModules =
                    moduleDescendents.intStream()
                            .mapToObj(moduleNid -> (ConceptEntity) Entity.getFast(moduleNid))
                            .collect(Collectors.toSet());
            setupComboBoxEdit(moduleComboBox, allModules);

            // populate the current module and select it (e.g. 'SNOMED CT core module')
            findByNid(moduleComboBox.getItems(), stampEntity.moduleNid())
                    .ifPresent(concept -> fqnViewModel.setPropertyValue(MODULE, concept));

            // get all statuses
            IntIdSet statusDescendents = viewProperties.parentView().calculator().descendentsOf(TinkarTerm.STATUS_VALUE.nid());
            Set<ConceptEntity> allStatuses = statusDescendents.intStream()
                    .mapToObj(statusNid -> (ConceptEntity) Entity.getFast(statusNid))
                    .collect(Collectors.toSet());
            setupComboBoxEdit(statusComboBox, allStatuses);

            // populate the current status (ACTIVE | INACTIVE) and select it
            findByNid(statusComboBox.getItems(), stampEntity.stateNid())
                    .ifPresent(concept -> fqnViewModel.setPropertyValue(STATUS, concept));


            // populate all case significance choices
            IntIdSet caseSenseDescendents = viewProperties.parentView().calculator().descendentsOf(DESCRIPTION_CASE_SIGNIFICANCE.nid());
            Set<ConceptEntity> allCaseDescendents = caseSenseDescendents.intStream()
                    .mapToObj(caseNid -> (ConceptEntity) Entity.getFast(caseNid))
                    .collect(Collectors.toSet());
            setupComboBoxEdit(caseSignificanceComboBox, allCaseDescendents);

            // get case concept's case sensitivity (e.g. 'Case insensitive')
            PatternEntity<PatternEntityVersion> patternEntity = latestEntityVersion.get().pattern();
            PatternEntityVersion patternEntityVersion = viewCalculator.latest(patternEntity).get();
            int indexCaseSig = patternEntityVersion.indexForMeaning(DESCRIPTION_CASE_SIGNIFICANCE);
            ConceptFacade caseSigConceptFacade = (ConceptFacade) latestEntityVersion.get().fieldValues().get(indexCaseSig);
            findByNid(caseSignificanceComboBox.getItems(), caseSigConceptFacade.nid())
                    .ifPresent(concept -> fqnViewModel.setPropertyValue(CASE_SIGNIFICANCE, concept));


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
                    .ifPresent(concept -> fqnViewModel.setPropertyValue(LANGUAGE, concept));

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

    public void updateModel(final ViewProperties viewProperties, EntityFacade entityFacade) {
        this.viewProperties = viewProperties;
        this.entityFacade = entityFacade;
    }



}
