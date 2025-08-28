package dev.ikm.komet.kview.mvvm.view.properties;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.ClosePropertiesPanelEvent;
import dev.ikm.komet.kview.mvvm.model.DescriptionFormType;
import dev.ikm.komet.kview.mvvm.view.AbstractBasicController;
import dev.ikm.komet.kview.mvvm.view.BasicController;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.events.EvtBus;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.beans.InvalidationListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public abstract class DescriptionBaseController<T> extends AbstractBasicController {

    private static final Logger LOG = LoggerFactory.getLogger(DescriptionBaseController.class);

    protected UUID conceptTopic;

    protected ViewProperties viewProperties;

    protected EntityFacade entityFacade;

    protected PublicId publicId;

    @FXML
    protected Label titleLabel;

    @FXML
    protected TextField nameTextField; // TODO more descriptive name

    @FXML
    protected ComboBox<T> typeComboBox;

    @FXML
    protected ComboBox<T> moduleComboBox;

    @FXML
    protected ComboBox<T> statusComboBox;

    @FXML
    protected ComboBox<T> caseSignificanceComboBox;

    @FXML
    protected ComboBox<T> languageComboBox;


    @FXML
    protected RowConstraints dialectCommentsRowConstraints; // shared constrain in edit/add mode

    @FXML
    protected Label commentsLabel; // label shown in add mode only


    // Dialects (only visible in edit mode)
    @FXML
    protected VBox dialectsContainer;

    @FXML
    protected Label dialect1;

    @FXML
    protected Label dialect2;

    @FXML
    protected Label dialect3;

    @FXML
    protected ComboBox dialectComboBox1;

    @FXML
    protected ComboBox dialectComboBox2;

    @FXML
    protected ComboBox dialectComboBox3;


    //
    @FXML
    protected Button submitButton;

    @FXML
    protected Button cancelButton;

    protected EvtBus eventBus;

    protected final DescriptionFormType formType;

    public  DescriptionBaseController(DescriptionFormType formType, UUID conceptTopic) {
        this.formType = formType;
        this.conceptTopic = conceptTopic;
    }

    @FXML
    public final void initialize() {
        // Common initialization that ALWAYS runs
        eventBus = EvtBusFactory.getDefaultEvtBus();
        clearView();

        // Let subclasses do their specific initialization
        initializeData();

    }

    protected abstract void initializeData();

    @Override
    public void clearView() {
        // Common view clearing logic
        if (nameTextField != null) {
            nameTextField.clear();
        }
        //if (commentsTextArea != null) {
        //    commentsTextArea.clear();
        //}
        // Clear combo boxes
        if (caseSignificanceComboBox != null) {
            caseSignificanceComboBox.getSelectionModel().clearSelection();
        }
        if (statusComboBox != null) {
            statusComboBox.getSelectionModel().clearSelection();
        }
        if (moduleComboBox != null) {
            moduleComboBox.getSelectionModel().clearSelection();
        }
        if (languageComboBox != null) {
            languageComboBox.getSelectionModel().clearSelection();
        }
    }

    @Override
    public void cleanup() {

    }

    @FXML
    protected void handleCancel() {
        onCancel();
    }

    @FXML
    protected void handleSubmit() {
        onSubmit();
    }

    protected abstract void onCancel();
    protected abstract void onSubmit();

    private String getDisplayText(ConceptEntity conceptEntity) {
        Optional<String> stringOptional = getViewProperties().calculator().getRegularDescriptionText(conceptEntity.nid());
        return stringOptional.orElse("");
    }

    protected void setupComboBoxAdd(ComboBox comboBox, InvalidationListener listener) {
        comboBox.setConverter(new StringConverter<ConceptEntity>() {

            @Override
            public String toString(ConceptEntity conceptEntity) {
                return getDisplayText(conceptEntity);
            }

            @Override
            public ConceptEntity fromString(String string) {
                return null;
            }
        });

        comboBox.setCellFactory(new Callback<>() {

            /**
             * @param param The single argument upon which the returned value should be
             *              determined.
             * @return
             */
            @Override
            public ListCell<ConceptEntity> call(Object param) {
                return new ListCell<>(){
                    @Override
                    protected void updateItem(ConceptEntity conceptEntity, boolean b) {
                        super.updateItem(conceptEntity, b);
                        if (conceptEntity != null) {
                            setText(getDisplayText(conceptEntity));
                        } else {
                            setText(null);
                        }

                    }
                };
            }
        });
        // register invalidation listener
        comboBox.getSelectionModel().selectedItemProperty().addListener(listener);

    }

    protected void setupComboBoxEdit(ComboBox comboBox, Collection<ConceptEntity> conceptEntities) {

        comboBox.getItems().clear();
        comboBox.setConverter(new StringConverter<ConceptEntity>() {

            @Override
            public String toString(ConceptEntity conceptEntity) {
                return getDisplayText(conceptEntity);
            }

            @Override
            public ConceptEntity fromString(String string) {
                return null;
            }
        });

        comboBox.setCellFactory(new Callback<>() {

            /**
             * @param param The single argument upon which the returned value should be
             *              determined.
             * @return
             */
            @Override
            public ListCell<ConceptEntity> call(Object param) {
                return new ListCell<>(){
                    @Override
                    protected void updateItem(ConceptEntity conceptEntity, boolean b) {
                        super.updateItem(conceptEntity, b);
                        if (conceptEntity != null) {
                            setText(getDisplayText(conceptEntity));
                        } else {
                            setText(null);
                        }

                    }
                };
            }
        });
        comboBox.getItems().addAll(conceptEntities);
    }

    boolean isFormPopulated() { // 1 AddOther used
        return (nameTextField.getText() != null && !nameTextField.getText().isEmpty())
                && (moduleComboBox.getSelectionModel().getSelectedItem() != null)
                && (statusComboBox.getSelectionModel().getSelectedItem() != null)
                && (caseSignificanceComboBox.getSelectionModel().getSelectedItem() != null)
                && (languageComboBox.getSelectionModel().getSelectedItem() != null);
    }

    protected void populateDialectComboBoxes() {
        // currently no UNACCEPTABLE in TinkarTerm
        Entity<? extends EntityVersion> acceptable = EntityService.get().getEntityFast(TinkarTerm.ACCEPTABLE);
        Entity<? extends EntityVersion> preferred = EntityService.get().getEntityFast(TinkarTerm.PREFERRED);

        // each combo box has a separate list instance
        setupComboBoxEdit(dialectComboBox1, Arrays.asList(Entity.getFast(acceptable.nid()), Entity.getFast(preferred.nid())));
        dialectComboBox1.getSelectionModel().select(Entity.getFast(acceptable.nid()));
        setupComboBoxEdit(dialectComboBox2, Arrays.asList(Entity.getFast(acceptable.nid()), Entity.getFast(preferred.nid())));
        dialectComboBox2.getSelectionModel().select(Entity.getFast(preferred.nid()));
        setupComboBoxEdit(dialectComboBox3, Arrays.asList(Entity.getFast(acceptable.nid()), Entity.getFast(preferred.nid())));
        dialectComboBox3.getSelectionModel().select(Entity.getFast(preferred.nid()));
    }

    protected void validateForm() {
        boolean isFqnTextEmpty = nameTextField.getText().trim().isEmpty();
        boolean isModuleComboBoxSelected = moduleComboBox.getValue() != null;
        boolean isCaseSignificanceComboBoxSelected = caseSignificanceComboBox.getValue() != null;
        boolean isStatusComboBoxComboBoxSelected = statusComboBox.getValue() != null;
        boolean isLanguageComboBoxComboBoxSelected = languageComboBox.getValue() != null;

        submitButton.setDisable(
                isFqnTextEmpty || !isModuleComboBoxSelected
                        || !isCaseSignificanceComboBoxSelected || !isLanguageComboBoxComboBoxSelected
                        || !isStatusComboBoxComboBoxSelected);
    }


    protected void close(Button button) {
        // close the properties bump out
        eventBus.publish(conceptTopic, new ClosePropertiesPanelEvent(button,
                ClosePropertiesPanelEvent.CLOSE_PROPERTIES));
    }

    protected void configureDialectVisibility(boolean showDialects) {


        if (commentsLabel != null) {
            commentsLabel.setVisible(!showDialects);
            commentsLabel.setManaged(!showDialects);
        }

        if (dialectsContainer != null) {
            dialectsContainer.setVisible(showDialects);
            dialectsContainer.setManaged(showDialects);

        }

        // Sets the correct minHeight constraint depending on add/edit mode
        if (dialectCommentsRowConstraints != null) {
            // a alternative solution would be to introduce a duplicated description-form.fxml for add / edit mode
            if (showDialects) {
                dialectCommentsRowConstraints.setMaxHeight(200.0);
                dialectCommentsRowConstraints.setMinHeight(200.0);
                dialectCommentsRowConstraints.setPrefHeight(200.0);
            } else {
                dialectCommentsRowConstraints.setMaxHeight(40.0);
                dialectCommentsRowConstraints.setMinHeight(40.0);
                dialectCommentsRowConstraints.setPrefHeight(40.0);
            }
        }

    }



}
