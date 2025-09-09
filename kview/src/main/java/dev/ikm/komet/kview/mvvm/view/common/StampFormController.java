package dev.ikm.komet.kview.mvvm.view.common;

import static dev.ikm.komet.kview.mvvm.viewmodel.StampFormViewModelBase.StampProperties.*;
import dev.ikm.komet.framework.view.*;
import dev.ikm.komet.kview.common.ViewCalculatorUtils;
import dev.ikm.komet.kview.mvvm.viewmodel.*;
import dev.ikm.tinkar.terms.*;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.control.*;


public class StampFormController {

    public static final String STAMP_FORM_FXML_FILE = "stamp-form.fxml";

    @FXML
    private Label formTitle;

    @FXML
    private TextField authorTextField;

    @FXML
    private Label lastUpdatedLabel;

    @FXML
    private TextField lastUpdatedTextField;

    @FXML
    private Button submitButton;

    @FXML
    private Button resetOrClearButton;

    @FXML
    private ComboBox<ComponentWithNid> statusComboBox;

    @FXML
    private ComboBox<ComponentWithNid> moduleComboBox;

    @FXML
    private ComboBox<ComponentWithNid> pathComboBox;

    private StampFormViewModelBase stampFormViewModel;

    @FXML
    public void initialize() {
    }

    public void init(StampFormViewModelBase stampFormViewModel) {
        this.stampFormViewModel = stampFormViewModel;

        initFormTitle();
        initBottomButtons();

        initLastUpdatedField();
        initAuthorField();
        initModuleComboBox();
        initPathComboBox();
        initStatusComboBox();

        BooleanProperty isStampValuesTheSameOrEmpty = this.stampFormViewModel.getProperty(IS_STAMP_VALUES_THE_SAME_OR_EMPTY);
        submitButton.disableProperty().bind(isStampValuesTheSameOrEmpty);
        resetOrClearButton.disableProperty().bind(isStampValuesTheSameOrEmpty);
    }

    private void initFormTitle() {
        formTitle.textProperty().bind(stampFormViewModel.getProperty(FORM_TITLE));
    }

    private void initBottomButtons() {
        resetOrClearButton.textProperty().bind(stampFormViewModel.getProperty(CLEAR_RESET_BUTTON_TEXT));
        submitButton.textProperty().bind(stampFormViewModel.getProperty(SUBMIT_BUTTON_TEXT));
    }

    private ViewProperties getViewProperties() {
        return stampFormViewModel.getViewProperties();
    }

    private void initAuthorField() {
        StringBinding authorTextBinding = new StringBinding() {
            {
                super.bind(stampFormViewModel.getProperty(AUTHOR));
            }

            @Override
            protected String computeValue() {
                if (getViewProperties() != null) {
                    ConceptFacade authorConcept = stampFormViewModel.getPropertyValue(AUTHOR);
                    return getViewProperties().calculator().getPreferredDescriptionTextWithFallbackOrNid(authorConcept.nid());
                } else {
                    return "Author Not selected";
                }
            }
        };
        authorTextField.textProperty().bind(authorTextBinding);
    }

    private void initLastUpdatedField() {
        // Label
        lastUpdatedLabel.setText("Last\nUpdated");

        // TextField
        lastUpdatedTextField.textProperty().bind(stampFormViewModel.getProperty(FORM_TIME_TEXT));
    }

    private void initStatusComboBox() {
        ViewCalculatorUtils.initComboBox(statusComboBox, stampFormViewModel.getObservableList(STATUSES), this::getViewProperties);

        statusComboBox.valueProperty().bindBidirectional(stampFormViewModel.getProperty(STATUS));
    }

    private void initPathComboBox() {
        ViewCalculatorUtils.initComboBox(pathComboBox, stampFormViewModel.getObservableList(PATHS), this::getViewProperties);

        pathComboBox.valueProperty().bindBidirectional(stampFormViewModel.getProperty(PATH));
    }

    private void initModuleComboBox() {
        ViewCalculatorUtils.initComboBox(moduleComboBox, stampFormViewModel.getObservableList(MODULES), this::getViewProperties);

        moduleComboBox.valueProperty().bindBidirectional(stampFormViewModel.getProperty(MODULE));
    }

    public StampFormViewModelBase getStampFormViewModel() { return stampFormViewModel; }

    @FXML
    public void cancelForm(ActionEvent actionEvent) {
        stampFormViewModel.cancel();
    }

    @FXML
    public void resetOrClear(ActionEvent actionEvent) { stampFormViewModel.resetOrClearForm(actionEvent); }

    public void submit(ActionEvent actionEvent) { stampFormViewModel.submitOrConfirm(); }
}
