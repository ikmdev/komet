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

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Control;
import javafx.scene.control.TextArea;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.AbstractPropertyEditor;
import org.controlsfx.property.editor.PropertyEditor;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import dev.ikm.komet.framework.controls.EntityLabelWithDragAndDrop;
import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.panel.axiom.AxiomView;
import dev.ikm.komet.framework.propsheet.editor.IntIdListEditor;
import dev.ikm.komet.framework.propsheet.editor.IntIdSetEditor;
import dev.ikm.komet.framework.propsheet.editor.PasswordEditor;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.component.FieldDefinition;
import dev.ikm.tinkar.component.graph.DiTree;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.TinkarTerm;

import java.util.Optional;

public class SheetItem<T> implements PropertySheet.Item {

    private final Class<?> classType;
    private final String category;
    private final String name;
    private final String description;
    private final Property<T> property;
    private final Class propertyEditorClass;
    private final ValidationSupport validationSupport;
    private final Validator<T> validator;
    ObservableField<?> observableField;


    private SheetItem(Class<?> classType, String category, String name,
                      String description, Property<T> property, Class propertyEditorClass,
                      ValidationSupport validationSupport, Validator<T> validator, ObservableField<?> observableField) {
        this.classType = classType;
        this.category = category;
        this.name = name;
        this.description = description;
        this.property = property;
        this.propertyEditorClass = propertyEditorClass;
        this.validationSupport = validationSupport;
        this.validator = validator;
        this.observableField = observableField;
    }

    public ObservableField<?> getObservableField() {
        return observableField;
    }

    public static <T> SheetItem<T> make(StringProperty property, ValidationSupport validationSupport, Validator<T> validator, ObservableField<?> observableField) {
        if (validationSupport == null || validator == null) {
            throw new IllegalStateException("Validation and validation support cannot be null");
        }
        return new SheetItem(String.class, null, property.getName(),
                null, property, KometPropertyEditorFactory.TextFieldEditor.class, validationSupport, validator, observableField);
    }

    public static <T> SheetItem<T> make(ObservableField field, SemanticEntityVersion version, ViewProperties viewProperties) {
        return make(field, null, version, viewProperties);
    }

    public static <T> SheetItem<T> make(ObservableField field, String category, SemanticEntityVersion version, ViewProperties viewProperties) {
        Class<?> classType;
        // meaning
        String name = viewProperties.calculator().getDescriptionTextOrNid(field.fieldDefinition(viewProperties.calculator()).meaningNid());
        // Purpose
        String description = viewProperties.calculator().getDescriptionTextOrNid(field.fieldDefinition(viewProperties.calculator()).purposeNid());
        ObjectProperty property = field.editableValueProperty();

        Class propertyEditorClass = null;
        switch (field.fieldDefinition(viewProperties.calculator()).fieldDataType()) {
            case STRING:
                classType = String.class;
                propertyEditorClass = null;
                break;
            case CONCEPT:
            case CONCEPT_CHRONOLOGY:
            case CONCEPT_VERSION:
            case SEMANTIC:
            case SEMANTIC_CHRONOLOGY:
            case SEMANTIC_VERSION:
            case PATTERN:
            case PATTERN_CHRONOLOGY:
            case PATTERN_VERSION:
            case IDENTIFIED_THING:
                classType = EntityFacade.class;
                propertyEditorClass = EntityLabelWithDragAndDrop.class;
                break;
            case COMPONENT_ID_LIST:
                // leave list in same order...
                classType = IntIdList.class;
                propertyEditorClass = IntIdListEditor.class;
                break;
            case COMPONENT_ID_SET:
                // sort set for presentation, order does not matter in set.
                classType = IntIdSet.class;
                propertyEditorClass = IntIdSetEditor.class;
                break;
            case DITREE: {
                classType = DiTree.class;
                propertyEditorClass = AxiomView.class;
            }
            break;
            default:
                classType = Object.class;
                propertyEditorClass = null;
        }
        ValidationSupport validationSupport = null;
        Validator<T> validator = null;

        return new SheetItem<>(classType, category, name,
                description, property, propertyEditorClass,
                validationSupport, validator, field);
    }

    public static <T> SheetItem<T> make(ObservableField field, ViewProperties viewProperties) {
        return make(field, null, null, viewProperties);
    }

    public static <T> SheetItem<T> make(ObservableField field, String category, ViewProperties viewProperties) {
        return make(field, category, null, viewProperties);
    }

    public static <T> SheetItem<T> make(FieldDefinitionRecord fieldDefinition, String category, ViewProperties viewProperties) {
        Class<?> classType;
        String name = fieldDefinition.propertyName();
        String description = fieldDefinition.propertyDescription();
        classType = EntityFacade.class;
        Class propertyEditorClass = EntityLabelWithDragAndDrop.class;
        ValidationSupport validationSupport = null;
        Validator<T> validator = null;

        return new SheetItem<>(classType, category, name,
                description, fieldDefinition.valueProperty(), propertyEditorClass,
                validationSupport, validator,
                null);
    }

    public static <T> SheetItem<T> make(StringProperty property) {

        return new SheetItem(String.class, null, property.getName(),
                null, property, null, null, null, null);
    }

    public static <T> SheetItem<T> makeForPassword(StringProperty property) {

        return new SheetItem(String.class, null, property.getName(),
                null, property, PasswordEditor.class, null, null, null);
    }

    public static <T> SheetItem<T> makeForPassword(StringProperty property, ValidationSupport validationSupport, Validator<T> validator) {

        return new SheetItem(String.class, null, property.getName(),
                null, property, PasswordEditor.class, validationSupport, validator, null);
    }

    public static PropertySheet.Item make(BooleanProperty booleanProperty) {

        return new SheetItem(Boolean.class, null, booleanProperty.getName(),
                null, booleanProperty, null, null, null, null);
    }

    @Override
    public Class<?> getType() {
        return classType;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public T getValue() {
        return property.getValue();
    }

    @Override
    public void setValue(Object o) {
        property.setValue((T) o);
    }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.ofNullable(property);
    }

    public Optional<Class<? extends PropertyEditor<?>>> getPropertyEditorClass() {
        return Optional.ofNullable(this.propertyEditorClass);
    }

    public void set(T o) {
        property.setValue(o);
    }

    public void addValidation(Control control) {
        if (this.validationSupport != null) {
            this.validationSupport.registerValidator(control, validator);
        }
    }

    // ========== EDITABLE PROPERTY FACTORY METHODS ==========
    // These methods create SheetItems that bind to ObservableField.Editable from ObservableSemanticVersion.Editable
    // rather than read-only ObservableField.

    /**
     * Creates a SheetItem for an editable field from ObservableSemanticVersion.Editable.
     * <p>
     * This is used when you want to edit a semantic field with changes cached in the
     * ObservableSemanticVersion.Editable until save() or commit() is called.
     * <p>
     * <b>API Symmetry:</b>
     * <ul>
     *   <li>{@link #make(ObservableField, ViewProperties)} - read-only field</li>
     *   <li>{@link #makeEditable(ObservableSemanticVersion.Editable, int, ViewProperties)} - editable field</li>
     * </ul>
     *
     * @param editableVersion the editable version containing editable fields
     * @param fieldIndex the index of the field in the semantic version
     * @param viewProperties view properties for field definition lookup
     * @return a SheetItem bound to the editable field
     */
    public static <T> SheetItem<T> makeEditable(
            ObservableSemanticVersion.Editable editableVersion,
            int fieldIndex,
            ViewProperties viewProperties) {
        return makeEditable(editableVersion, fieldIndex, null, viewProperties);
    }

    /**
     * Creates a SheetItem directly from an ObservableField.Editable.
     * <p>
     * Symmetric to {@link #make(ObservableField, ViewProperties)}.
     *
     * @param editableField the editable field
     * @param viewProperties view properties for field definition lookup
     * @return a SheetItem bound to the editable field
     */
    public static <T> SheetItem<T> makeEditable(
            ObservableField.Editable<?> editableField,
            ViewProperties viewProperties) {
        return makeEditable(editableField, null, viewProperties);
    }

    /**
     * Creates a SheetItem for an editable field from ObservableSemanticVersion.Editable with a category.
     * <p>
     * This is used when you want to edit a semantic field with changes cached in the
     * ObservableSemanticVersion.Editable until save() or commit() is called.
     *
     * @param editableVersion the editable version containing editable fields
     * @param fieldIndex the index of the field in the semantic version
     * @param category optional category for grouping in property sheet
     * @param viewProperties view properties for field definition lookup
     * @return a SheetItem bound to the editable field
     */
    public static <T> SheetItem<T> makeEditable(
            ObservableSemanticVersion.Editable editableVersion,
            int fieldIndex,
            String category,
            ViewProperties viewProperties) {

        // Get the editable field from the editable version
        ObservableField.Editable<?> editableField = editableVersion.getEditableField(fieldIndex);
        return makeEditable(editableField, category, viewProperties);
    }

    /**
     * Creates a SheetItem for an editable field with a category.
     * <p>
     * Symmetric to {@link #make(ObservableField, String, ViewProperties)}.
     *
     * @param editableField the editable field
     * @param category optional category for grouping in property sheet
     * @param viewProperties view properties for field definition lookup
     * @return a SheetItem bound to the editable field
     */
    public static <T> SheetItem<T> makeEditable(
            ObservableField.Editable<?> editableField,
            String category,
            ViewProperties viewProperties) {

        // Get field definition from the underlying observable field
        ObservableField<?> observableField = editableField.getObservableFeature();
        FieldDefinition fieldDef = observableField.fieldDefinition(viewProperties.calculator());

        // Get field metadata
        String name = viewProperties.calculator().getDescriptionTextOrNid(fieldDef.meaningNid());
        String description = viewProperties.calculator().getDescriptionTextOrNid(fieldDef.purposeNid());

        // Determine class type and editor based on field data type
        Class<?> classType;
        Class propertyEditorClass = null;

        switch (fieldDef.fieldDataType()) {
            case STRING:
                classType = String.class;
                propertyEditorClass = null;
                break;
            case CONCEPT:
            case CONCEPT_CHRONOLOGY:
            case CONCEPT_VERSION:
            case SEMANTIC:
            case SEMANTIC_CHRONOLOGY:
            case SEMANTIC_VERSION:
            case PATTERN:
            case PATTERN_CHRONOLOGY:
            case PATTERN_VERSION:
            case IDENTIFIED_THING:
                classType = EntityFacade.class;
                propertyEditorClass = EntityLabelWithDragAndDrop.class;
                break;
            case COMPONENT_ID_LIST:
                classType = IntIdList.class;
                propertyEditorClass = IntIdListEditor.class;
                break;
            case COMPONENT_ID_SET:
                classType = IntIdSet.class;
                propertyEditorClass = IntIdSetEditor.class;
                break;
            case DITREE:
                classType = DiTree.class;
                propertyEditorClass = AxiomView.class;
                break;
            default:
                classType = Object.class;
                propertyEditorClass = null;
        }

        ValidationSupport validationSupport = null;
        Validator validator = null;

        @SuppressWarnings("unchecked")
        SheetItem<T> item = new SheetItem(classType, category, name, description,
                editableField.editableValueProperty(), propertyEditorClass,
                validationSupport, validator, observableField);
        return item;
    }

    /**
     * Creates a SheetItem for an editable field with custom property and field definition.
     * <p>
     * This is a lower-level method that allows binding to any SimpleObjectProperty
     * with metadata from a FieldDefinition.
     *
     * @param editableProperty the editable property to bind to
     * @param fieldDefinition the field definition for metadata (meaning, purpose, data type)
     * @param category optional category for grouping in property sheet
     * @param viewProperties view properties for description lookup
     * @return a SheetItem bound to the editable property
     */
    public static <T> SheetItem<T> makeEditableWithDefinition(
            SimpleObjectProperty<Object> editableProperty,
            FieldDefinition fieldDefinition,
            String category,
            ViewProperties viewProperties) {

        // Get field metadata
        String name = viewProperties.calculator().getDescriptionTextOrNid(fieldDefinition.meaningNid());
        String description = viewProperties.calculator().getDescriptionTextOrNid(fieldDefinition.purposeNid());

        // Determine class type and editor based on field data type
        Class<?> classType;
        Class propertyEditorClass = null;

        switch (fieldDefinition.fieldDataType()) {
            case STRING:
                classType = String.class;
                break;
            case CONCEPT:
            case CONCEPT_CHRONOLOGY:
            case CONCEPT_VERSION:
            case SEMANTIC:
            case SEMANTIC_CHRONOLOGY:
            case SEMANTIC_VERSION:
            case PATTERN:
            case PATTERN_CHRONOLOGY:
            case PATTERN_VERSION:
            case IDENTIFIED_THING:
                classType = EntityFacade.class;
                propertyEditorClass = EntityLabelWithDragAndDrop.class;
                break;
            case COMPONENT_ID_LIST:
                classType = IntIdList.class;
                propertyEditorClass = IntIdListEditor.class;
                break;
            case COMPONENT_ID_SET:
                classType = IntIdSet.class;
                propertyEditorClass = IntIdSetEditor.class;
                break;
            case DITREE:
                classType = DiTree.class;
                propertyEditorClass = AxiomView.class;
                break;
            default:
                classType = Object.class;
        }

        @SuppressWarnings("unchecked")
        SheetItem<T> item = new SheetItem(classType, category, name, description,
                editableProperty, propertyEditorClass,
                null, null, null);
        return item;
    }

}
