/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.kview.mvvm.view.genediting;


import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.klfields.booleanfield.KlBooleanFieldFactory;
import dev.ikm.komet.kview.events.genediting.GenEditingEvent;
import dev.ikm.komet.kview.klfields.editable.EditableKLFieldFactory;
import dev.ikm.komet.kview.klfields.floatfield.KlFloatFieldFactory;
import dev.ikm.komet.kview.klfields.integerfield.KlIntegerFieldFactory;
import dev.ikm.komet.kview.klfields.readonly.ReadOnlyKLFieldFactory;
import dev.ikm.komet.kview.klfields.stringfield.KlStringFieldFactory;
import dev.ikm.komet.layout.component.version.field.KlField;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.FieldRecord;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.viewmodel.ValidationViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static dev.ikm.komet.kview.events.genediting.GenEditingEvent.PUBLISH;
import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.obtainObservableField;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.SEMANTIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.WINDOW_TOPIC;

public class SemanticFieldsController {

    private static final Logger LOG = LoggerFactory.getLogger(SemanticFieldsController.class);

    @FXML
    private VBox editFieldsVBox;
    @FXML
    private Button cancelButton;

    @FXML
    private Button clearFormButton;

    @FXML
    private Button submitButton;

    @InjectViewModel
    private ValidationViewModel semanticFieldsViewModel;

    private List<ObservableField<?>> observableFields = new ArrayList<>();

    @FXML
    private void initialize() {
        // clear all semantic details.
        editFieldsVBox.setSpacing(8.0);
        editFieldsVBox.getChildren().clear();

        EntityFacade semantic = semanticFieldsViewModel.getPropertyValue(SEMANTIC);
        if (semantic != null) {
            StampCalculator stampCalculator = getViewProperties().calculator().stampCalculator();
            Latest<SemanticEntityVersion> semanticEntityVersionLatest = stampCalculator.latest(semantic.nid());
            if (semanticEntityVersionLatest.isPresent()) {
                // Populate the Semantic Details
                setupSemanticDetailsUI(semanticEntityVersionLatest);
            } else {
                // TODO Add a new semantic based on a pattern (blank fields).
                System.out.println("Display all fields for adding a semantic entity");
            }

        }
    }

    public ViewProperties getViewProperties() {
        return semanticFieldsViewModel.getPropertyValue(VIEW_PROPERTIES);
    }

    private Separator createSeparator() {
        Separator separator = new Separator();
        separator.getStyleClass().add("field-separator");
        return separator;
    }

    private void setupSemanticDetailsUI(Latest<SemanticEntityVersion> semanticEntityVersionLatest) {
        ReadOnlyKLFieldFactory rowf = ReadOnlyKLFieldFactory.getInstance();
        EditableKLFieldFactory editFieldFactory = EditableKLFieldFactory.getInstance();

        Consumer<FieldRecord<Object>> updateUIConsumer = (fieldRecord) -> {

            Node node = null;
            int dataTypeNid = fieldRecord.dataType().nid();
            if (dataTypeNid == TinkarTerm.COMPONENT_FIELD.nid()) {
                // load a read-only component
                KlField klField = editFieldFactory.createComponent(fieldRecord);
                node = klField.klWidget();
                observableFields.add(klField.field());
            } else if (dataTypeNid == TinkarTerm.STRING_FIELD.nid() || fieldRecord.dataType().nid() == TinkarTerm.STRING.nid()) {
                KlStringFieldFactory stringFieldTextFactory = new KlStringFieldFactory();
                ObservableField<String> stringObservableField = obtainObservableField(getViewProperties(), semanticEntityVersionLatest, fieldRecord);
                node = stringFieldTextFactory.create(stringObservableField, getViewProperties().nodeView(), true).klWidget();
                observableFields.add(stringObservableField);
            } else if (dataTypeNid == TinkarTerm.COMPONENT_ID_SET_FIELD.nid()) {
                node = rowf.createReadOnlyComponentSet(getViewProperties(), fieldRecord);
                observableFields.add(null);
            } else if (dataTypeNid == TinkarTerm.DITREE_FIELD.nid()) {
                node = rowf.createReadOnlyDiTree(getViewProperties(), fieldRecord);
                observableFields.add(null);
            } else if (dataTypeNid == TinkarTerm.FLOAT_FIELD.nid() || fieldRecord.dataType().nid() == TinkarTerm.FLOAT.nid()) {
                ObservableField<Float> floatObservableField = obtainObservableField(getViewProperties(), semanticEntityVersionLatest, fieldRecord);
                KlFloatFieldFactory klFloatFieldFactory = new KlFloatFieldFactory();
                node = klFloatFieldFactory.create(floatObservableField, getViewProperties().nodeView(), true).klWidget();
                observableFields.add(floatObservableField);
            } else if (dataTypeNid == TinkarTerm.INTEGER_FIELD.nid() || fieldRecord.dataType().nid() == TinkarTerm.INTEGER_FIELD.nid()) {
                ObservableField<Integer> integerObservableField = obtainObservableField(getViewProperties(), semanticEntityVersionLatest, fieldRecord);
                KlIntegerFieldFactory klIntegerFieldFactory = new KlIntegerFieldFactory();
                node = klIntegerFieldFactory.create(integerObservableField, getViewProperties().nodeView(), true).klWidget();
                observableFields.add(integerObservableField);
            } else if (dataTypeNid == TinkarTerm.BOOLEAN_FIELD.nid()) {
                ObservableField<Boolean> booleanObservableField = obtainObservableField(getViewProperties(), semanticEntityVersionLatest, fieldRecord);
                KlBooleanFieldFactory klBooleanFieldFactory = new KlBooleanFieldFactory();
                node = klBooleanFieldFactory.create(booleanObservableField, getViewProperties().nodeView(), true).klWidget();
                observableFields.add(booleanObservableField);
            }
            // Add to VBox
            if (node != null) {
                editFieldsVBox.getChildren().add(node);
                // Add separator
                editFieldsVBox.getChildren().add(createSeparator());
            }
        };
        rowf.setupSemanticDetailsUI(getViewProperties(), semanticEntityVersionLatest, updateUIConsumer);
    }

    @FXML
    private void cancel(ActionEvent actionEvent) {
        actionEvent.consume();
        // if previous state was closed cancel will close properties bump out.
        // else show
        System.out.println(actionEvent);
    }

    @FXML
    private void clearForm(ActionEvent actionEvent) {
        actionEvent.consume();
        System.out.println(actionEvent);
    }

    @FXML
    public void submit(ActionEvent actionEvent) {
        actionEvent.consume();
        List<ObservableField> list = new ArrayList<>();
        observableFields.forEach(observableField -> {
            if (observableField != null) {
                observableField.writeToDataBase();
            }
            list.add(observableField);
        });

        //EventBus implementation changes to refresh the details area
        EvtBusFactory.getDefaultEvtBus().publish(semanticFieldsViewModel.getPropertyValue(WINDOW_TOPIC), new GenEditingEvent(actionEvent.getSource(), PUBLISH, list));

//        clearView(actionEvent);
    }
}
