package dev.ikm.komet.kview.mvvm.view.properties;

import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.*;
import dev.ikm.komet.framework.view.*;
import dev.ikm.komet.kview.common.ViewCalculatorUtils;
import dev.ikm.komet.kview.mvvm.viewmodel.*;
import dev.ikm.tinkar.terms.*;
import javafx.beans.property.*;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import org.carlfx.cognitive.loader.*;


public class StampAddController {

    @FXML
    private Button submitButton;

    @FXML
    private Button resetButton;

    @FXML
    private ComboBox<ComponentWithNid> statusComboBox;

    @FXML
    private ComboBox<ComponentWithNid> moduleComboBox;

    @FXML
    private ComboBox<ComponentWithNid> pathComboBox;

    @InjectViewModel
    private StampViewModel2 stampViewModel;

    @FXML
    public void initialize() {
        initModuleComboBox();
        initPathComboBox();
        initStatusComboBox();

        BooleanProperty isStampValuesTheSame = stampViewModel.getProperty(IS_STAMP_VALUES_THE_SAME);
        submitButton.disableProperty().bind(isStampValuesTheSame);
        resetButton.disableProperty().bind(isStampValuesTheSame);
    }

    private ViewProperties getViewProperties() {
        return stampViewModel.getViewProperties();
    }

    private void initStatusComboBox() {
        ViewCalculatorUtils.initComboBox(statusComboBox, stampViewModel.getObservableList(STATUSES), this::getViewProperties);

        statusComboBox.valueProperty().bindBidirectional(stampViewModel.getProperty(STATUS));
    }

    private void initPathComboBox() {
        ViewCalculatorUtils.initComboBox(pathComboBox, stampViewModel.getObservableList(PATHS), this::getViewProperties);

        pathComboBox.valueProperty().bindBidirectional(stampViewModel.getProperty(PATH));
    }

    private void initModuleComboBox() {
        ViewCalculatorUtils.initComboBox(moduleComboBox, stampViewModel.getObservableList(MODULES), this::getViewProperties);

        moduleComboBox.valueProperty().bindBidirectional(stampViewModel.getProperty(MODULE));
    }


    public StampViewModel2 getStampViewModel() { return stampViewModel; }

    @FXML
    public void cancelForm(ActionEvent actionEvent) {
        stampViewModel.cancel();
    }

    @FXML
    public void resetForm(ActionEvent actionEvent) { stampViewModel.resetForm(actionEvent); }

    public void submit(ActionEvent actionEvent) { stampViewModel.save(); }
}
