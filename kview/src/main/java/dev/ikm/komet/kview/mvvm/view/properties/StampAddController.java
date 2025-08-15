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

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


public class StampAddController {

    @FXML
    private Label lastUpdatedLabel;

    @FXML
    private TextField lastUpdatedTextField;

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
        controllerUtils = new ControllerUtils<>(this::getViewProperties);

        initLastUpdatedField();

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

    private void initLastUpdatedField() {
        // Label
        lastUpdatedLabel.setText("Last\nUpdated");

        // TextField
        StringBinding timeTextProperty = new StringBinding() {
            DateTimeFormatter dateTimeFormatter;
            {
                dateTimeFormatter = DateTimeFormatter
                        .ofPattern("yyyy-MMM-dd HH:mm:ss")
                        .withLocale(Locale.getDefault())
                        .withZone(ZoneId.systemDefault());

                super.bind(stampViewModel.getProperty(TIME));
            }

            @Override
            protected String computeValue() {
                Instant stampInstance = Instant.ofEpochSecond((Long)stampViewModel.getPropertyValue(TIME) / 1000);
                return dateTimeFormatter.format(stampInstance);
            }
        };
        lastUpdatedTextField.textProperty().bind(timeTextProperty);
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
