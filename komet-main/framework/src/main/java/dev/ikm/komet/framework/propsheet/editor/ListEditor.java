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
package dev.ikm.komet.framework.propsheet.editor;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.controlsfx.property.editor.PropertyEditor;
import dev.ikm.komet.framework.controls.EntityLabelWithDragAndDrop;
import dev.ikm.komet.framework.graphics.Icon;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.terms.EntityFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class ListEditor<L extends ObservableList<EntityFacade>>
        implements PropertyEditor<L>, ListChangeListener<EntityFacade> {
    private static final Logger LOG = LoggerFactory.getLogger(ListEditor.class);
    private final BorderPane editorPane = new BorderPane();
    private final Button newItem = new Button("", Icon.PLUS.makeIcon());
    private final ToolBar editorToolbar = new ToolBar(newItem);
    private final VBox listView = new VBox();
    private final ViewProperties viewProperties;
    SimpleObjectProperty<ObservableList<EntityFacade>> entitiesListProperty;

    public ListEditor(ViewProperties viewProperties, SimpleObjectProperty<ObservableList<EntityFacade>> entitiesListProperty) {
        this.editorPane.setCenter(listView);
        this.entitiesListProperty = entitiesListProperty;
        //this.editorPane.setTop(editorToolbar);
        this.newItem.setOnAction(this::newItem);
        this.viewProperties = viewProperties;
        updateList();
        this.entitiesListProperty.addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.removeListener(this);
            }
            updateList();
        });
        this.entitiesListProperty.getValue().addListener(this);
    }

    private void newItem(Event event) {
        throw new UnsupportedOperationException();
//        listView.getItems().add(newObjectSupplier.get());
//        listView.requestLayout();
    }

    private void updateList() {
        listView.getChildren().clear();
        for (EntityFacade entityFacade : entitiesListProperty.getValue()) {
            EntityLabelWithDragAndDrop entityLabelWithDragAndDrop =
                    EntityLabelWithDragAndDrop.make(this.viewProperties, new SimpleObjectProperty<>(entityFacade));
            listView.getChildren().add(entityLabelWithDragAndDrop);
        }
    }

    public void onChanged(ListChangeListener.Change<? extends EntityFacade> c) {
        updateList();
    }

    @Override
    public Node getEditor() {
        return editorPane;
    }

    @Override
    public L getValue() {
        return (L) entitiesListProperty.getValue();
    }

    @Override
    public void setValue(L value) {
        entitiesListProperty.setValue(value);
    }

}
