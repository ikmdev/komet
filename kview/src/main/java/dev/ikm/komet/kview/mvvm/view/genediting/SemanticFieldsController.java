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


import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.SEMANTIC;
import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableSemantic;
import dev.ikm.komet.framework.observable.ObservableSemanticSnapshot;
import dev.ikm.komet.framework.view.ObservableViewBase;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.klfields.StringFieldTextFactory;
import dev.ikm.komet.kview.klfields.editable.EditableKLFieldFactory;
import dev.ikm.komet.kview.klfields.readonly.ReadOnlyKLFieldFactory;
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
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

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
    private Separator createSeparator(){
        Separator separator = new Separator();
        separator.getStyleClass().add("field-separator");
        return separator;
    }
    private void setupSemanticDetailsUI(Latest<SemanticEntityVersion> semanticEntityVersionLatest) {
        ReadOnlyKLFieldFactory rowf = ReadOnlyKLFieldFactory.getInstance();
        EditableKLFieldFactory editFieldFactory = EditableKLFieldFactory.getInstance();

        Consumer<FieldRecord<Object>> updateUIConsumer = (fieldRecord) -> {

            Node node = null;
            System.out.println("---> dataType() " + fieldRecord.dataType().description());
            int dataTypeNid = fieldRecord.dataType().nid();
            if (dataTypeNid == TinkarTerm.COMPONENT_FIELD.nid()) {
                // load a read-only component
                KlField klField = editFieldFactory.createComponent(fieldRecord);
                node = klField.klWidget();
                node.setUserData(klField.field());
            } else if (dataTypeNid == TinkarTerm.STRING_FIELD.nid() || fieldRecord.dataType().nid() == TinkarTerm.STRING.nid()) {

                StringFieldTextFactory stringFieldTextFactory = new StringFieldTextFactory();
                ObservableSemantic observableSemantic = ObservableEntity.get(semanticEntityVersionLatest.get().nid());
                ObservableSemanticSnapshot observableSemanticSnapshot = observableSemantic.getSnapshot(getViewProperties().calculator());
                ImmutableList<ObservableField> observableFields = observableSemanticSnapshot.getLatestFields().get();
                ObservableViewBase observableViewBase = getViewProperties().nodeView();

                ObservableField<String> stringObservableField =observableFields.get(fieldRecord.fieldIndex());

                //StringField klWidget returns the widget container which is an HBox with a hard coded label
                node = stringFieldTextFactory.create(stringObservableField, observableViewBase, true).klWidget();

                node.setUserData(stringObservableField);
            } else if (dataTypeNid == TinkarTerm.COMPONENT_ID_SET_FIELD.nid()) {
                node = rowf.createReadOnlyComponentSet(getViewProperties(), fieldRecord);
            } else if (dataTypeNid == TinkarTerm.DITREE_FIELD.nid()) {
                node = rowf.createReadOnlyDiTree(getViewProperties(), fieldRecord);
            }

            // Add to VBox
            if (node != null) {
                editFieldsVBox.getChildren().add(node);
                // Add separator
                editFieldsVBox.getChildren().add(createSeparator());
            }
            System.out.println("field record: " + fieldRecord);
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
        System.out.println(actionEvent);
        editFieldsVBox.getChildren().forEach(node -> {
            ObservableField observableField = (ObservableField) node.getUserData();
            if (observableField != null) {
                observableField.writeToDataBase();
            }
        });
//        clearView(actionEvent);
    }

}
