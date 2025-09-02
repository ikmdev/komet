package dev.ikm.komet.kview.mvvm.view.properties;

import dev.ikm.komet.kview.events.ClosePropertiesPanelEvent;
import dev.ikm.komet.kview.events.CreateConceptEvent;
import dev.ikm.komet.kview.mvvm.model.DescrName;
import dev.ikm.komet.kview.mvvm.model.DescriptionFormType;
import dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.beans.InvalidationListener;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.CASE_SIGNIFICANCE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.LANGUAGE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.MODULE;

public class DescriptionAddFqnController extends DescriptionBaseController<EntityFacade>{
    private static final Logger LOG = LoggerFactory.getLogger(DescriptionAddFqnController.class);
    @InjectViewModel
    private DescrNameViewModel fqnViewModel;

    public DescriptionAddFqnController(UUID conceptTopic) {
        super(DescriptionFormType.ADD_FQN, conceptTopic);
    }

    @Override
    protected void initializeData() {
        configureDialectVisibility(false);

        submitButton.setDisable(true);

        // Initialize the fqnViewModel
        fqnViewModel
                .setPropertyValue(NAME_TYPE, TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE)
                .setPropertyValue(STATUS, TinkarTerm.ACTIVE_STATE);

        // register listeners
        InvalidationListener formValid = (obs) -> {
            boolean isFormValid = isFormPopulated();
            if (isFormValid) {
                copyUIToViewModelProperties();
            }
            submitButton.setDisable(!isFormValid);
        };

        // Initialize combo boxes with appropriate data for FQN add mode
        nameTextField.textProperty().addListener(formValid);
        setupComboBoxAdd(moduleComboBox, formValid);
        setupComboBoxAdd(statusComboBox, formValid);
        setupComboBoxAdd(caseSignificanceComboBox, formValid);
        setupComboBoxAdd(languageComboBox, formValid);


    }

    @Override
    protected void onCancel() {
        close(cancelButton);
    }

    @Override
    protected void onSubmit() {
        // TODO assuming it's valid save() check for errors and publish event

        fqnViewModel.save();
        if (fqnViewModel.getValidationMessages().isEmpty()) {
            // publish event with the fqnViewModel.
            // ... This property may not be needed.
            fqnViewModel.setPropertyValue(IS_SUBMITTED, true);
        }

        LOG.info("Ready to add to the concept view model: " + fqnViewModel);

        //////////////////////////////////////////////////////////////////////////////////////////
        // event received in Details Controller that will call conceptViewModel.createConcept()
        //////////////////////////////////////////////////////////////////////////////////////////
        DescrName fqnDescrName = fqnViewModel.create();
        eventBus.publish(conceptTopic, new CreateConceptEvent(this, CreateConceptEvent.ADD_FQN, fqnDescrName));

        // clear the form after saving.  otherwise when you navigate back to Add Other Name
        // you would have the previous form values still there
        clearView();
        close(submitButton);
    }


    private void populate(ComboBox comboBox, Collection<ConceptEntity> entities) {
        comboBox.getItems().setAll(entities);
    }

    @Override
    public void updateView() {

        // populate form combo fields module, status, case significance, lang.
        populate(moduleComboBox, fqnViewModel.findAllModules(getViewProperties()));
        populate(statusComboBox, fqnViewModel.findAllStatuses(getViewProperties()));
        populate(caseSignificanceComboBox, fqnViewModel.findAllCaseSignificants(getViewProperties()));
        populate(languageComboBox, fqnViewModel.findAllLanguages(getViewProperties()));

        // Set UI to default values
        caseSignificanceComboBox.setValue(TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE);
        statusComboBox.setValue(Entity.getFast(State.ACTIVE.nid()));
        moduleComboBox.setValue(TinkarTerm.DEVELOPMENT_MODULE);
        languageComboBox.setValue(TinkarTerm.ENGLISH_LANGUAGE);
    }

    @Override
    public void clearView() {
        nameTextField.setText("");
        moduleComboBox.setValue(null);
        statusComboBox.setValue(null);
        caseSignificanceComboBox.setValue(null);
        languageComboBox.setValue(null);
//        if (fqnViewModel != null) {
//            copyUIToViewModelProperties();
//            fqnViewModel.save(true); // make UI properties as model values
//        }
        moduleComboBox.getItems().clear();
        statusComboBox.getItems().clear();
        caseSignificanceComboBox.getItems().clear();
        languageComboBox.getItems().clear();
    }

    /**
     * TODO: This is appropriate. A better solution is binding properties on the view model. If so, we'd need to unbind.
     * This copies form values into the ViewModel's property values. It does not save or validate.
     */
    private void copyUIToViewModelProperties() {
        if (fqnViewModel != null) {
            fqnViewModel.setPropertyValue(NAME_TEXT, nameTextField.getText())
                    .setPropertyValue(NAME_TYPE, TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE)
                    .setPropertyValue(CASE_SIGNIFICANCE, caseSignificanceComboBox.getSelectionModel().getSelectedItem())
                    .setPropertyValue(STATUS, statusComboBox.getSelectionModel().getSelectedItem())
                    .setPropertyValue(MODULE, moduleComboBox.getSelectionModel().getSelectedItem())
                    .setPropertyValue(LANGUAGE, languageComboBox.getSelectionModel().getSelectedItem());
        }
    }


    @Override
    public DescrNameViewModel getViewModel() {
        return fqnViewModel;
    }


}
