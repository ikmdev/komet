package dev.ikm.komet.kview.mvvm.view.properties;

import dev.ikm.komet.kview.mvvm.view.AbstractBasicController;
import dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2;
import dev.ikm.tinkar.common.util.text.NaturalOrder;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import org.carlfx.cognitive.loader.InjectViewModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.MODULE;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.MODULES;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.PATH;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.PATHS;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.STATUS;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.STATUSES;


public class StampAddController {

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private ComboBox<String> moduleComboBox;

    @FXML
    private ComboBox<String> pathComboBox;

    @InjectViewModel
    private StampViewModel2 stampViewModel;

    @FXML
    public void initialize() {
        initModuleComboBox();
        initPathComboBox();
        initStatusComboBox();
    }

    private void initStatusComboBox() {
        ObservableList<State> statuses = stampViewModel.getObservableList(STATUSES);
        statuses.addListener((ListChangeListener<State>) c -> {
            List<String> statusesStrings = statuses.stream()
                                                   .map(this::toFirstLetterCapitalized)
                                                   .collect(Collectors.toList());
            Collections.sort(statusesStrings, NaturalOrder.getObjectComparator());
            statusComboBox.getItems().setAll(statusesStrings);
        });

        ObjectProperty<State> statusProperty = stampViewModel.getProperty(STATUS);
        statusProperty.subscribe(state -> {
            if (state != null) {
                statusComboBox.setValue(toFirstLetterCapitalized(state));
            }
        });
    }

    private void initPathComboBox() {
        ObservableList<ConceptEntity> paths = stampViewModel.getObservableList(PATHS);
        paths.addListener((ListChangeListener<ConceptEntity>) c -> {
            List<String> pathStrings = paths.stream()
                    .map(EntityFacade::description)
                    .collect(Collectors.toList());
            Collections.sort(pathStrings, NaturalOrder.getObjectComparator());
            pathComboBox.getItems().setAll(pathStrings);
        });

        ObjectProperty<ConceptEntity> pathProperty = stampViewModel.getProperty(PATH);
        pathProperty.subscribe(conceptEntity -> {
            if (conceptEntity != null) {
                pathComboBox.setValue(conceptEntity.description());
            }
        });
    }

    private void initModuleComboBox() {
        // populate modules
        ObservableList<ConceptEntity> modules = stampViewModel.getObservableList(MODULES);

        modules.addListener((ListChangeListener<ConceptEntity>) c -> {
            List<String> moduleStrings = modules.stream()
                    .map(EntityFacade::description)
                    .collect(Collectors.toList());
            Collections.sort(moduleStrings, NaturalOrder.getObjectComparator());
            moduleComboBox.getItems().setAll(moduleStrings);
        });

        ObjectProperty<ConceptEntity> moduleProperty = stampViewModel.getProperty(MODULE);
        moduleProperty.subscribe(conceptEntity -> {
            if (conceptEntity != null) {
                moduleComboBox.setValue(conceptEntity.description());
            }
        });
    }

    private String toFirstLetterCapitalized(State status) {
        String statusString = status.toString();
        return statusString.substring(0, 1).toUpperCase() + statusString.substring(1).toLowerCase();
    }

    public void cancel(ActionEvent actionEvent) {

    }

    public void clearForm(ActionEvent actionEvent) {

    }

    public void confirm(ActionEvent actionEvent) {

    }
}
