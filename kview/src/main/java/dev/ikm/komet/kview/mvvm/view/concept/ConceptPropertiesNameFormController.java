package dev.ikm.komet.kview.mvvm.view.concept;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.common.ViewCalculatorUtils;
import dev.ikm.komet.kview.mvvm.viewmodel.ConceptViewModelNext;
import dev.ikm.komet.kview.mvvm.viewmodel.ConceptViewModelNext.*;
import dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModelNext;
import dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModelNext.*;
import dev.ikm.tinkar.terms.ComponentWithNid;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConceptPropertiesNameFormController {
    private static final Logger LOG = LoggerFactory.getLogger(ConceptPropertiesNameFormController.class);

    public static final String CONCEPT_PROP_NAMES_FXML_FILE = "concept-prop-name-form.fxml";

    // <top>
    @FXML private Label titleLabel;

    // <center>
    @FXML private TextField nameTextField;  // holds either FQN or otherName
    @FXML private ComboBox<ComponentWithNid> typeDisplayComboBox;  // TODO: check recent commits if still disabled in FXML
    @FXML private ComboBox<ComponentWithNid> caseSignificanceComboBox;
    @FXML private ComboBox<ComponentWithNid> statusComboBox;
    @FXML private ComboBox<ComponentWithNid> moduleComboBox;
    @FXML private ComboBox<ComponentWithNid> languageComboBox;

    // Dialects (visible once a language is selected) //TODO: make that happen
    @FXML private VBox dialectsContainer;
    @FXML private Label dialect1Label;
    @FXML private Label dialect2Label;
    @FXML private Label dialect3Label;
    @FXML private ComboBox dialectComboBox1;
    @FXML private ComboBox dialectComboBox2;
    @FXML private ComboBox dialectComboBox3;

    @FXML private Label commentsLabel; // // TODO remove as unused

    @FXML private TextArea commentsTextArea;


    // <bottom>
    @FXML private Button submitButton;
    @FXML private Button cancelButton;


    @InjectViewModel
    private ConceptViewModelNext conceptViewModelNext;

    @InjectViewModel
    private DescrNameViewModelNext descrNameViewModelNext;

    public ConceptPropertiesNameFormController() {
        // conceptTopic is available through ConceptViewModel
    }

    @FXML
    public void initialize() {


        initFormTitle();

        initNameText();

        initTypeComboBox();
        initCaseSignificanceComboBox();
        initStatusComboBox();
        initModuleComboBox();
        initLanguageComboBox();

        bindLanguageSelectionToDialects();

        // -- bottom buttons --
        submitButton.disableProperty().bind(descrNameViewModelNext.invalidProperty()); // TODO, use validator that already present
        cancelButton.setOnMouseClicked(mouseEvent -> { // TODO: conceptViewModel update
            closeView();
        });
    }

    private ViewProperties getViewProperties() {
        return conceptViewModelNext.getViewProperties();
    }

    private void initFormTitle() {
        titleLabel.textProperty().bindBidirectional(descrNameViewModelNext.getProperty(DescrPropKeys.TITLE_TEXT));
    }

    private void initNameText() {
        titleLabel.textProperty().bindBidirectional(descrNameViewModelNext.getProperty(DescrPropKeys.NAME));
    }

    private void initTypeComboBox() {
        ViewCalculatorUtils.initComboBox(
                typeDisplayComboBox,
                descrNameViewModelNext.getObservableList(DescrPropKeys.NAME_TYPE_VARIANTS),
                this::getViewProperties);


        typeDisplayComboBox.valueProperty().bindBidirectional(descrNameViewModelNext.getProperty(DescrPropKeys.SELECTED_NAME_TYPE));
    }

    private void initCaseSignificanceComboBox() {
        ViewCalculatorUtils.initComboBox(
                caseSignificanceComboBox,
                descrNameViewModelNext.getObservableList(DescrPropKeys.CASE_SIGNIFICANCE_VARIANTS),
                this::getViewProperties);

        caseSignificanceComboBox.valueProperty().bindBidirectional(descrNameViewModelNext.getProperty(DescrPropKeys.SELECTED_CASE_SIGNIFICANCE));
    }

    private void initStatusComboBox() {
        ViewCalculatorUtils.initComboBox(
                statusComboBox,
                descrNameViewModelNext.getObservableList(DescrPropKeys.STATUS_VARIANTS),
                this::getViewProperties);

        statusComboBox.valueProperty().bindBidirectional(descrNameViewModelNext.getProperty(DescrPropKeys.SELECTED_STATUS));
    }

    private void initModuleComboBox() {
        ViewCalculatorUtils.initComboBox(
                moduleComboBox,
                descrNameViewModelNext.getObservableList(DescrPropKeys.MODULE_VARIANTS),
                this::getViewProperties);

        moduleComboBox.valueProperty().bindBidirectional(descrNameViewModelNext.getProperty(DescrPropKeys.SELECTED_MODULE));
    }

    private void initLanguageComboBox() {
        ViewCalculatorUtils.initComboBox(
                languageComboBox,
                descrNameViewModelNext.getObservableList(DescrPropKeys.LANGUAGE_VARIANTS),
                this::getViewProperties);

        languageComboBox.valueProperty().bindBidirectional(descrNameViewModelNext.getProperty(DescrPropKeys.SELECTED_LANGUAGE));
    }

    private void bindLanguageSelectionToDialects() {
        // TODO: enable it after cleaning up the layout
//        BooleanBinding isLanguageSelected = languageComboBox.valueProperty().isNotNull();
//        dialectsContainer.visibleProperty().bind(isLanguageSelected);
    }


    @FXML
    private void onCancel(ActionEvent actionEvent) {
        actionEvent.consume();

        closeView();
    }

    @FXML
    private void onSubmit(ActionEvent actionEvent) {
        actionEvent.consume();

        // implicitly calls validate
//        descrNameViewModelNext.save();
//
//        // check the validation result
//        if (descrNameViewModelNext.hasErrorMsgs()) {
//            descrNameViewModelNext.getValidationMessages().forEach(msg -> LOG.error("Validation error {}", msg));
//            return;
//        }
//
//        // validate check ok -> save copied the ViewProperties into ModelProperties
//        // we build a record out of the updated ModelProerties
//        DescrName record = descrNameViewModelNext.create(); // TODO: check why SMEANTIC_PUB_ID and PARENT_PUB_ID are needed | do they need exist befor this?
//
//        // check if we are in create or view/edit mode
//        String mode = conceptViewModel.getValue(MODE);
//
//
//        PublicId thisNamePublicId = conceptViewModel.getValue(SELECTED_NAME_DESCRIPTION_PUBLIC_ID);
//        ConceptViewModel.SelectedNameDescriptionKind thisNameKind = conceptViewModel.getValue(SELECTED_NAME_DESCRIPTION_KIND);
//
//
//
//        if (CREATE.equals(mode)) { // than we have no publicID for this name -> but the record itself should track that info
//            switch (thisNameKind) {
//                case FQN -> {
//                    ObservableList<DescrName> fqnNames =  conceptViewModel.getObservableList(ADDED_BUT_NOT_COMMITED_FQN_NAME_RECORDS);
//                    fqnNames.add(record);
//                }
//                case otherName -> {
//                    ObservableList<DescrName> otherNames = conceptViewModel.getObservableList(ADDED_BUT_NOT_COMMITED_OTHER_NAME_RECORDS);
//                    otherNames.add(record); // TODO does this trigger an update of the model or do we need to reinsert?
//                }
//            }
//
//        } else { // edit/view mode TODO: is it allowed to add name / kind in edit view mode?
//
//            ViewProperties viewProperties = conceptViewModel.getViewProperties();
//
//            if (thisNamePublicId != null) { // this is a Name we Edit
//                switch (thisNameKind) {
//                    case FQN -> { // TODO: investigate if this calls should be decoupled like in Pattern ?
//                        // THIS CALL DOES NOT UPDATE THE VIEWMODEL BUT THE DB (cache?)
//                        descrNameViewModelNext.updateFullyQualifiedName(thisNamePublicId,viewProperties);
//                    }
//                    case otherName -> {
//                        // THIS CALL DOES NOT UPDATE THE VIEWMODEL BUT THE DB (cache?)
//                        descrNameViewModelNext.updateOtherName(thisNamePublicId,viewProperties);
//                    }
//                }
//            } else { // this is a Name we Add to existing ones
//
//                switch (thisNameKind) {
//                    case FQN -> {
//                        ObservableList<DescrName> fqnNames =  conceptViewModel.getObservableList(ADDED_BUT_NOT_COMMITED_FQN_NAME_RECORDS);
//                        fqnNames.add(record);
//                    }
//                    case otherName -> {
//                        ObservableList<DescrName> otherNames = conceptViewModel.getObservableList(ADDED_BUT_NOT_COMMITED_OTHER_NAME_RECORDS);
//                        otherNames.add(record); // TODO does this trigger an update of the model or do we need to reinsert?
//                    }
//                }
//            }
//
//        }

        // TODO: should we clear the associate descrViewModel? i think its better to always start with a fresh one for a window.

        closeView();


    }

    private void closeView() {
        conceptViewModelNext.setPropertyValue(ConceptPropertyKeys.SELECTED_PROPERTY_WINDOW_KIND, SelectedPropertyWindowKind.NONE);
    }

}

