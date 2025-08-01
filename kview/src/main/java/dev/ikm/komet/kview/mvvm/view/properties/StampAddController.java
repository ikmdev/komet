package dev.ikm.komet.kview.mvvm.view.properties;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import javafx.beans.property.BooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.carlfx.cognitive.loader.InjectViewModel;

import java.util.UUID;

import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.IS_STAMP_VALUES_THE_SAME;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.MODULE;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.MODULES;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.PATH;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.PATHS;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.STATUS;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.STATUSES;


public class StampAddController {

    @FXML
    private Button submitButton;

    @FXML
    private Button resetButton;

    @FXML
    private Button cancelButton;

    @FXML
    private ComboBox<State> statusComboBox;

    @FXML
    private ComboBox<ConceptEntity> moduleComboBox;

    @FXML
    private ComboBox<ConceptEntity> pathComboBox;

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

    public void updateModel(ViewProperties viewProperties, EntityFacade entity, UUID topic) {
        if (entity != null) {
            stampViewModel.init(entity, topic, viewProperties);
        }
    }

    private void initStatusComboBox() {
        statusComboBox.setItems(stampViewModel.getObservableList(STATUSES));

        statusComboBox.setCellFactory(new Callback<>() {
            @Override
            public ListCell<State> call(ListView<State> stateListView) {
                return new ListCell<>() {

                    @Override
                    protected void updateItem(State state, boolean empty) {
                        super.updateItem(state, empty);

                        if (state == null || empty) {
                            setText(null);
                        } else {
                            setText(state.name());
                        }
                    }
                };
            }
        });

        statusComboBox.valueProperty().bindBidirectional(stampViewModel.getProperty(STATUS));
    }

    private void initPathComboBox() {
        pathComboBox.setItems(stampViewModel.getObservableList(PATHS));

        pathComboBox.setCellFactory(_ -> createConceptListCell());
        pathComboBox.setButtonCell(createConceptListCell());

        pathComboBox.valueProperty().bindBidirectional(stampViewModel.getProperty(PATH));
    }

    private void initModuleComboBox() {
        // populate modules
        moduleComboBox.setItems(stampViewModel.getObservableList(MODULES));

        moduleComboBox.setCellFactory(_ -> createConceptListCell());
        moduleComboBox.setButtonCell(createConceptListCell());

        moduleComboBox.valueProperty().bindBidirectional(stampViewModel.getProperty(MODULE));
    }

    private ListCell<ConceptEntity> createConceptListCell() {
        return new ListCell<>(){
            @Override
            protected void updateItem(ConceptEntity item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item.description());
                }
            }
        };
    }

    public StampViewModel2 getStampViewModel() { return stampViewModel; }

    public void cancelForm(ActionEvent actionEvent) {
        stampViewModel.cancel(cancelButton);
    }

    public void resetForm(ActionEvent actionEvent) {
        stampViewModel.reset(resetButton);
    }

    public void confirm(ActionEvent actionEvent) {

    }
}
