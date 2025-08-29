package dev.ikm.komet.kview.mvvm.view.properties;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.CreateConceptEvent;
import dev.ikm.komet.kview.mvvm.model.DescriptionFormType;
import dev.ikm.komet.kview.mvvm.viewmodel.OtherNameViewModel;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.beans.InvalidationListener;
import javafx.scene.control.ComboBox;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.viewmodel.ViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.UUID;

import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.CASE_SIGNIFICANCE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.LANGUAGE;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.MODULE;
import static dev.ikm.komet.kview.mvvm.viewmodel.OtherNameViewModel.OtherNameProperties.*;

public class DescriptionAddOtherController  extends DescriptionBaseController<EntityFacade>{
    private static final Logger LOG = LoggerFactory.getLogger(DescriptionAddOtherController.class);
    @InjectViewModel
    private OtherNameViewModel otherNameViewModel; // TODO why we need that wrapper only for this class but can get away in all 3 others


    // TODO: how does it work with no UUID
    //public DescriptionAddOtherController() {
    //    super(DescriptionFormType.ADD_OTHER_NAME, conceptTopic);
    //}

    public DescriptionAddOtherController(UUID conceptTopic) {
        super(DescriptionFormType.ADD_OTHER_NAME, conceptTopic);
    }


    @Override
    protected void initializeData() {
        configureDialectVisibility(false);

        submitButton.setDisable(true);

        titleLabel.setText("Add New Description: Other Name"); // TODO: check if needed ?
        otherNameViewModel
                .setPropertyValue(NAME_TYPE, TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE)
                .setPropertyValue(STATUS, TinkarTerm.ACTIVE_STATE);
        // register listeners
        InvalidationListener formValid = (obs) -> {
            boolean isFormValid = isFormPopulated();
            if (isFormValid) {
                copyUIToViewModelProperties();
            }
            submitButton.setDisable(!isFormValid);
        };

        nameTextField.textProperty().addListener(formValid);
        setupComboBoxAdd(moduleComboBox, formValid);
        setupComboBoxAdd(statusComboBox, formValid);
        setupComboBoxAdd(caseSignificanceComboBox, formValid);
        setupComboBoxAdd(languageComboBox, formValid);
    }

    @Override
    public void updateView() {
        // populate form combo fields module, status, case significance, lang.
        populate(moduleComboBox, otherNameViewModel.findAllModules(getViewProperties()));
        populate(statusComboBox, otherNameViewModel.findAllStatuses(getViewProperties()));
        populate(caseSignificanceComboBox, otherNameViewModel.findAllCaseSignificants(getViewProperties()));
        populate(languageComboBox, otherNameViewModel.findAllLanguages(getViewProperties()));

        boolean hasOtherName = getViewModel().getValue(HAS_OTHER_NAME);

        if (hasOtherName) {
            caseSignificanceComboBox.setValue(getViewModel().getValue(FQN_CASE_SIGNIFICANCE));
        } else {
            caseSignificanceComboBox.setValue(TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE);
        }
        statusComboBox.setValue(Entity.getFast(State.ACTIVE.nid()));
        moduleComboBox.setValue(TinkarTerm.DEVELOPMENT_MODULE);
        if (hasOtherName) {
            languageComboBox.setValue(getViewModel().getValue(FQN_LANGUAGE));
        } else {
            languageComboBox.setValue(TinkarTerm.ENGLISH_LANGUAGE);
        }
    }

    public void updateModel(final ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
    }

    @Override
    public void clearView() {
        nameTextField.setText("");
        moduleComboBox.setValue(null);
        statusComboBox.setValue(null);
        caseSignificanceComboBox.setValue(null);
        languageComboBox.setValue(null);
        moduleComboBox.getItems().clear();
        statusComboBox.getItems().clear();
        caseSignificanceComboBox.getItems().clear();
        languageComboBox.getItems().clear();
    }

    @Override
    protected void onCancel() {
        close(cancelButton);
    }

    @Override
    protected void onSubmit() {
        // TODO assuming it's valid save() check for errors and publish event
        otherNameViewModel.setPropertyValue(IS_SUBMITTED, true);
        otherNameViewModel.save();
        if (otherNameViewModel.hasNoErrorMsgs()) {
            // publish event with the otherNameViewModel.
            // ...
            LOG.info("Ready to add to the concept view model: " + otherNameViewModel);
            eventBus.publish(conceptTopic, new CreateConceptEvent(this, CreateConceptEvent.ADD_OTHER_NAME,
                    otherNameViewModel.create()));
            clearView();
            close(submitButton);
        }
    }

    @Override
    public OtherNameViewModel getViewModel() {
        return otherNameViewModel;
    }

      /**
     * TODO: This is appropriate. A better solution is binding properties on the view model. If so, we'd need to unbind.
     * This copies form values into the ViewModel's property values. It does not save or validate.
     */
    private void copyUIToViewModelProperties() {
        if (otherNameViewModel != null) {
            otherNameViewModel.setPropertyValue(NAME_TEXT, nameTextField.getText())
                    .setPropertyValue(NAME_TYPE, TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE)
                    .setPropertyValue(CASE_SIGNIFICANCE, caseSignificanceComboBox.getSelectionModel().getSelectedItem())
                    .setPropertyValue(STATUS, statusComboBox.getSelectionModel().getSelectedItem())
                    .setPropertyValue(MODULE, moduleComboBox.getSelectionModel().getSelectedItem())
                    .setPropertyValue(LANGUAGE, languageComboBox.getSelectionModel().getSelectedItem());
        }
    }

    public void setAddOtherNameTitleLabel(String addDescriptionTitleLabelText) {
        this.titleLabel.setText(addDescriptionTitleLabelText);
    }

    public void populate(ComboBox comboBox, Collection<ConceptEntity> entities) {
        comboBox.getItems().addAll(entities);
    }

    public void hideNonUsed() {
        dialect1.setVisible(false);
        dialectComboBox1.setVisible(false);

    }
}
