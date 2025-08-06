package dev.ikm.komet.kview.mvvm.view.properties;

import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.*;
import dev.ikm.komet.framework.view.*;
import dev.ikm.komet.kview.mvvm.view.genediting.*;
import dev.ikm.komet.kview.mvvm.viewmodel.*;
import dev.ikm.tinkar.terms.*;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.*;
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
        statusComboBox.setItems(stampViewModel.getObservableList(STATUSES));

        statusComboBox.setCellFactory(stateListView -> createConceptListCell());
        statusComboBox.setButtonCell(createConceptListCell());

        statusComboBox.valueProperty().bindBidirectional(stampViewModel.getProperty(STATUS));
    }

    private void initPathComboBox() {
        pathComboBox.setItems(stampViewModel.getObservableList(PATHS));

        pathComboBox.setCellFactory(_ -> createConceptListCell());
        pathComboBox.setButtonCell(createConceptListCell());

        pathComboBox.valueProperty().bindBidirectional(stampViewModel.getProperty(PATH));
    }

    private void initModuleComboBox() {
        moduleComboBox.setItems(stampViewModel.getObservableList(MODULES));

        moduleComboBox.setCellFactory(_ -> createConceptListCell());
        moduleComboBox.setButtonCell(createConceptListCell());

        moduleComboBox.valueProperty().bindBidirectional(stampViewModel.getProperty(MODULE));
    }

    private String getDescriptionTextWithFallbackOrNid(ComponentWithNid conceptEntity) {
        String descr = "" + conceptEntity.nid();
        if (getViewProperties() != null) {
            descr = getViewProperties().calculator().getPreferredDescriptionTextWithFallbackOrNid(conceptEntity.nid());
        }
        return descr;
    }

    private ListCell<ComponentWithNid> createConceptListCell() {
        return new ListCell<>(){
            @Override
            protected void updateItem(ComponentWithNid item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(getDescriptionTextWithFallbackOrNid(item));
                }
            }
        };
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
