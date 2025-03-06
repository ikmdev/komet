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
package dev.ikm.komet.framework.propsheet;

import dev.ikm.komet.framework.controls.EntityLabelWithDragAndDrop;
import dev.ikm.komet.framework.observable.ObservableSemantic;
import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import dev.ikm.komet.framework.panel.axiom.AxiomView;
import dev.ikm.komet.framework.propsheet.editor.IntIdListEditor;
import dev.ikm.komet.framework.propsheet.editor.IntIdSetEditor;
import dev.ikm.komet.framework.propsheet.editor.ListEditor;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.component.graph.DiTree;
import dev.ikm.tinkar.coordinate.logic.PremiseType;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.AbstractPropertyEditor;
import org.controlsfx.property.editor.DefaultPropertyEditorFactory;
import org.controlsfx.property.editor.PropertyEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class KometPropertyEditorFactory implements Callback<PropertySheet.Item, PropertyEditor<?>> {
    private static final Logger LOG = LoggerFactory.getLogger(KometPropertyEditorFactory.class);

    private static Timer timer;
    private final ViewProperties viewProperties;
    DefaultPropertyEditorFactory defaultFactory = new DefaultPropertyEditorFactory();

    public KometPropertyEditorFactory(ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
    }

    @Override
    public PropertyEditor<?> call(PropertySheet.Item item) {
        PropertyEditor<?> propertyEditor;
        Optional<Class<? extends PropertyEditor<?>>> optionalPropertyEditorClass = item.getPropertyEditorClass();
        if (optionalPropertyEditorClass.isPresent() && optionalPropertyEditorClass.get() == KometPropertyEditorFactory.TextFieldEditor.class) {
            propertyEditor = new TextFieldEditor(item);
        } else if (item.getType() == String.class) {
            propertyEditor = createTextAreaEditor(item);
        } else if (item.getPropertyEditorClass().isPresent()) {
            Optional<PropertyEditor<?>> ed = createCustomEditor((SheetItem<?>) item, viewProperties);
            if (ed.isPresent()) {
                propertyEditor = ed.get();
            } else {
                propertyEditor = null;
                AlertStreams.getRoot().dispatch(AlertObject.makeWarning("No editor for item " + item.getName(), item.toString()));
            }
        } else {
            return null;
        }

        if (item instanceof SheetItem sheetItem) {
            if (propertyEditor != null && propertyEditor.getEditor() instanceof Control control) {
                sheetItem.addValidation(control);
            }
        }
        return propertyEditor;
    }

    public static final PropertyEditor<?> createTextAreaEditor(PropertySheet.Item property) {
        TextArea textArea = initializeTextAreaEditor(property);
        return new AbstractPropertyEditor<String, TextArea>(property, textArea) {

            {
                getEditor().setWrapText(true);
                getEditor().setPrefRowCount(2);
                enableAutoSelectAll(getEditor());
            }

            @Override
            protected StringProperty getObservableValue() {
                return getEditor().textProperty();
            }

            @Override
            public void setValue(String value) {
                if (value.length() < 60) {
                    getEditor().setPrefRowCount(1);
                } else {
                    getEditor().setPrefRowCount(2 + value.length() / 80);
                }
                getEditor().setText(value);
            }
        };
    }

    private static TextArea initializeTextAreaEditor(PropertySheet.Item property) {
        SheetItem sheetItem = property instanceof SheetItem<?> ? (SheetItem) property: null;
        BooleanProperty refreshProp = new SimpleBooleanProperty(true);
        if (sheetItem != null) {
            sheetItem.observableField.refreshProperties.bind(refreshProp);
        }

        TextArea textArea= new TextArea();
        textArea.addEventFilter(KeyEvent.ANY, event -> {
            if(event.getCode() == KeyCode.TAB){
                textArea.getParent().requestFocus();
                event.consume();
            }
        });

        List<TimerTask> timerTasks = Collections.synchronizedList(new ArrayList<>()); // create a list to maintain list of timer tasks.
         //Initialize timer for text Area.
        //On Every key press:
        textArea.addEventFilter(KeyEvent.ANY, event -> {
            if(timer !=null){
                timer.cancel();
            }
            timer = new Timer();
            timerTasks.forEach(timerTask -> timerTask.cancel()); // on every key press cancel all the timerTasks
            timerTasks.clear(); // Clear all the tasks from list.
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {   // Create a new timer task.
                    timer.cancel();
                    refreshProp.set(false);
                }
            };
            timerTasks.add(timerTask); // Add the task to the tasks list.
            timer.schedule(timerTask, 1000);  // Schedule the task to be run after every 1 second.
        });

        textArea.focusedProperty().addListener((ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue)  -> {
          timerTasks.forEach(timerTask -> timerTask.cancel());
          timerTasks.clear();
          if(!newValue && timer!=null){
             timer.cancel();
          }
        });
        return textArea;
    }

    public static final Optional<PropertyEditor<?>> createCustomEditor(final SheetItem<?> property, final ViewProperties viewProperties) {
        try {
            if (property.getPropertyEditorClass().isPresent()) {
                Class editorClass = property.getPropertyEditorClass().get();
                if (editorClass == ListEditor.class) {
                    return Optional.of(new ListEditor(viewProperties, (SimpleObjectProperty<ObservableList<EntityFacade>>) property.getObservableValue().get()));
                }
                if (editorClass == IntIdSetEditor.class) {
                    return Optional.of(new IntIdSetEditor(viewProperties, (SimpleObjectProperty<IntIdSet>) property.getObservableValue().get()));
                }
                if (editorClass == IntIdListEditor.class) {
                    return Optional.of(new IntIdListEditor(viewProperties, (SimpleObjectProperty<IntIdList>) property.getObservableValue().get()));
                }
                if (editorClass == EntityLabelWithDragAndDrop.class) {
                    return Optional.of(EntityLabelWithDragAndDrop.make(viewProperties, (ObjectProperty<EntityFacade>) property.getObservableValue().get()));
                }
                if (editorClass == AxiomView.class) {
                    //TODO add stated/inferred to root property?
                    DiTree<EntityVertex> axiomTree = (DiTree<EntityVertex>) property.getValue();
                    PremiseType premiseType = PremiseType.STATED;
                    if (property.getObservableField().meaningNid() == TinkarTerm.EL_PLUS_PLUS_INFERRED_TERMINOLOGICAL_AXIOMS.nid()) {
                        premiseType = PremiseType.INFERRED;
                    }
                    int semanticNid = property.observableField.field().nid();
                    ObservableSemantic axiomSemantic = ObservableSemantic.get(semanticNid);
                    ObservableSemanticVersion axiomSemanticVersion = axiomSemantic.getVersionFast(property.observableField.field().versionStampNid());

                    AxiomView axiomView = AxiomView.create(axiomSemanticVersion, premiseType, viewProperties);
                    return Optional.of(axiomView);
                }
            }
            return property.getPropertyEditorClass().map(cls -> {
                try {
                    Constructor<?> cn = cls.getConstructor(PropertySheet.Item.class, ViewProperties.class);
                    return (PropertyEditor<?>) cn.newInstance(property, viewProperties);
                } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    LOG.debug("No constructor(PropertySheet.Item.class, ViewProperties.class). Will try next pattern.");
                }
                try {
                    Constructor<?> cn = cls.getConstructor(PropertySheet.Item.class);
                    return (PropertyEditor<?>) cn.newInstance(property);
                } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    LOG.debug("No (PropertySheet.Item.class) constructor. Will return null.");
                }
                return null;
            });
        } catch (Exception e) {
            AlertStreams.getRoot().dispatch(AlertObject.makeError(e));
            return Optional.empty();
        }
    }

    private static void enableAutoSelectAll(final TextInputControl control) {
        control.focusedProperty().addListener((ObservableValue<? extends Boolean> o, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                Platform.runLater(() -> {
                    control.selectAll();
                });
            }
        });
    }

    public static class TextFieldEditor extends AbstractPropertyEditor<String, TextField> {
        {
            enableAutoSelectAll(getEditor());
        }

        public TextFieldEditor(PropertySheet.Item property) {
            super(property, new TextField());
        }

        public TextFieldEditor(PropertySheet.Item property, boolean readonly) {
            super(property, new TextField(), readonly);
        }

        @Override
        protected StringProperty getObservableValue() {
            return getEditor().textProperty();
        }

        @Override
        public void setValue(String value) {
            getEditor().setText(value);
        }
    }
}
