package dev.ikm.komet.kview.klfields;

import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.obtainObservableField;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.klfields.booleanfield.KlBooleanFieldFactory;
import dev.ikm.komet.kview.klfields.componentfield.KlComponentFieldFactory;
import dev.ikm.komet.kview.klfields.componentsetfield.KlComponentSetFieldFactory;
import dev.ikm.komet.kview.klfields.componentlistfield.KlComponentListFieldFactory;
import dev.ikm.komet.kview.klfields.floatfield.KlFloatFieldFactory;
import dev.ikm.komet.kview.klfields.imagefield.KlImageFieldFactory;
import dev.ikm.komet.kview.klfields.integerfield.KlIntegerFieldFactory;
import dev.ikm.komet.kview.klfields.readonly.ReadOnlyKLFieldFactory;
import dev.ikm.komet.kview.klfields.stringfield.KlStringFieldFactory;
import dev.ikm.komet.kview.mvvm.model.DataModelHelper;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.FieldRecord;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class KlFieldHelper {

    private static Separator createSeparator() {
        Separator separator = new Separator();
        separator.getStyleClass().add("field-separator");
        return separator;
    }
    public static void generateSemanticUIFields(ViewProperties viewProperties,
                                       Latest<SemanticEntityVersion> semanticEntityVersionLatest,
                                       Consumer<FieldRecord<Object>> updateUIConsumer) {
        semanticEntityVersionLatest.ifPresent(semanticEntityVersion -> {
            StampCalculator stampCalculator = viewProperties.calculator().stampCalculator();
            Latest<PatternEntityVersion> patternEntityVersionLatest = stampCalculator.latest(semanticEntityVersion.pattern());
            patternEntityVersionLatest.ifPresent(patternEntityVersion -> {
                List<FieldRecord<Object>> fieldRecords = DataModelHelper.fieldRecords(semanticEntityVersion, patternEntityVersion);
                fieldRecords.forEach(fieldRecord -> updateUIConsumer.accept(fieldRecord));
            });
        });
    }

    /**
     * Returns a list of observable fields and displays editable controls on a Pane using the latest semantic entity version.
     * @param viewProperties View Properties
     * @param container A JavaFX Pane. e.g. VBox
     * @param semanticEntityVersionLatest Semantic Entity Version object containing all field records and their field definitions & value
     * @return A list of observable fields
     */
    public static List<ObservableField<?>> displayEditableSemanticFields(ViewProperties viewProperties, Pane container, Latest<SemanticEntityVersion> semanticEntityVersionLatest) {
        ReadOnlyKLFieldFactory rowf = ReadOnlyKLFieldFactory.getInstance();
        List<ObservableField<?>> observableFields = new ArrayList<>();
        Consumer<FieldRecord<Object>> updateUIConsumer = (fieldRecord) -> {

            Node node = null;
            int dataTypeNid = fieldRecord.dataType().nid();
            ObservableField writeObservableField = obtainObservableField(viewProperties, semanticEntityVersionLatest, fieldRecord);
            ObservableField observableField = new ObservableField(writeObservableField.field(), false);
            observableFields.add(observableField);

            if (dataTypeNid == TinkarTerm.COMPONENT_FIELD.nid()) {
                // load a read-only component
                KlComponentFieldFactory componentFieldFactory = new KlComponentFieldFactory();
                node = componentFieldFactory.create(observableField, viewProperties.nodeView(), true).klWidget();
            } else if (dataTypeNid == TinkarTerm.STRING_FIELD.nid() || fieldRecord.dataType().nid() == TinkarTerm.STRING.nid()) {
                KlStringFieldFactory stringFieldTextFactory = new KlStringFieldFactory();
                node = stringFieldTextFactory.create(observableField, viewProperties.nodeView(), true).klWidget();
            } else if (dataTypeNid == TinkarTerm.COMPONENT_ID_SET_FIELD.nid()) {
                KlComponentSetFieldFactory klComponentSetFieldFactory = new KlComponentSetFieldFactory();
                node = klComponentSetFieldFactory.create(observableField, viewProperties.nodeView(), true).klWidget();
            } else if (dataTypeNid == TinkarTerm.COMPONENT_ID_LIST_FIELD.nid()) {
                KlComponentListFieldFactory klComponentListFieldFactory = new KlComponentListFieldFactory();
                node = klComponentListFieldFactory.create(observableField, viewProperties.nodeView(), true).klWidget();
            } else if (dataTypeNid == TinkarTerm.DITREE_FIELD.nid()) {
                node = rowf.createReadOnlyDiTree(viewProperties, fieldRecord);
            } else if (dataTypeNid == TinkarTerm.FLOAT_FIELD.nid() || fieldRecord.dataType().nid() == TinkarTerm.FLOAT.nid()) {
                KlFloatFieldFactory klFloatFieldFactory = new KlFloatFieldFactory();
                node = klFloatFieldFactory.create(observableField, viewProperties.nodeView(), true).klWidget();
            } else if (dataTypeNid == TinkarTerm.INTEGER_FIELD.nid() || fieldRecord.dataType().nid() == TinkarTerm.INTEGER_FIELD.nid()) {
                KlIntegerFieldFactory klIntegerFieldFactory = new KlIntegerFieldFactory();
                node = klIntegerFieldFactory.create(observableField, viewProperties.nodeView(), true).klWidget();
            } else if (dataTypeNid == TinkarTerm.BOOLEAN_FIELD.nid()) {
                KlBooleanFieldFactory klBooleanFieldFactory = new KlBooleanFieldFactory();
                node = klBooleanFieldFactory.create(observableField, viewProperties.nodeView(), true).klWidget();
//            } else if (dataTypeNid == TinkarTerm.IMAGE_FIELD.nid()) {
//                KlImageFieldFactory imageFieldFactory = new KlImageFieldFactory();
//                node = imageFieldFactory.create(observableField, viewProperties.nodeView(), true).klWidget();
            } else if (dataTypeNid == TinkarTerm.BYTE_ARRAY_FIELD.nid()) {
                KlImageFieldFactory imageFieldFactory = new KlImageFieldFactory();
                node = imageFieldFactory.create(observableField, viewProperties.nodeView(), true).klWidget();
            }

            // Add to VBox
            if (node != null) {
                container.getChildren().add(node);
                // Add separator
                container.getChildren().add(createSeparator());
            }
        };
        generateSemanticUIFields(viewProperties, semanticEntityVersionLatest, updateUIConsumer);
        return observableFields;
    }

    public static List<ObservableField<?>> displayReadOnlySemanticFields(ViewProperties viewProperties, Pane container, Latest<SemanticEntityVersion> semanticEntityVersionLatest) {

            //FIXME use a different factory
            ReadOnlyKLFieldFactory rowf = ReadOnlyKLFieldFactory.getInstance();
            List<ObservableField<?>> observableFields = new ArrayList<>();
            Consumer<FieldRecord<Object>> updateUIConsumer = (fieldRecord) -> {

                Node readOnlyNode = null;
                int dataTypeNid = fieldRecord.dataType().nid();
                ObservableField<?> writeObservableField = obtainObservableField(viewProperties, semanticEntityVersionLatest, fieldRecord);
                ObservableField observableField = new ObservableField(writeObservableField.field(), false);
                observableFields.add(observableField);

                // substitute each data type.
                if (dataTypeNid == TinkarTerm.COMPONENT_FIELD.nid()) {
                    // load a read-only component
                    KlComponentFieldFactory klComponentFieldFactory = new KlComponentFieldFactory();
                    readOnlyNode = klComponentFieldFactory.create(observableField, viewProperties.nodeView(), false).klWidget();
                } else if (dataTypeNid == TinkarTerm.STRING_FIELD.nid() || fieldRecord.dataType().nid() == TinkarTerm.STRING.nid()) {
                    KlStringFieldFactory klStringFieldFactory = new KlStringFieldFactory();
                    readOnlyNode = klStringFieldFactory.create(observableField, viewProperties.nodeView(), false).klWidget();
                } else if (dataTypeNid == TinkarTerm.COMPONENT_ID_SET_FIELD.nid()) {
                    KlComponentSetFieldFactory klComponentSetFieldFactory = new KlComponentSetFieldFactory();
                    readOnlyNode = klComponentSetFieldFactory.create(observableField, viewProperties.nodeView(), false).klWidget();
                } else if (dataTypeNid == TinkarTerm.COMPONENT_ID_LIST_FIELD.nid()) {
                    KlComponentListFieldFactory klComponentListFieldFactory = new KlComponentListFieldFactory();
                    readOnlyNode = klComponentListFieldFactory.create(observableField, viewProperties.nodeView(), false).klWidget();
                } else if (dataTypeNid == TinkarTerm.DITREE_FIELD.nid()) {
                    readOnlyNode = rowf.createReadOnlyDiTree(viewProperties, fieldRecord);
                } else if (dataTypeNid == TinkarTerm.FLOAT_FIELD.nid()) {
                    KlFloatFieldFactory klFloatFieldFactory = new KlFloatFieldFactory();
                    readOnlyNode = klFloatFieldFactory.create(observableField, viewProperties.nodeView(), false).klWidget();
                } else if (dataTypeNid == TinkarTerm.INTEGER_FIELD.nid()) {
                    KlIntegerFieldFactory klIntegerFieldFactory = new KlIntegerFieldFactory();
                    readOnlyNode = klIntegerFieldFactory.create(observableField, viewProperties.nodeView(), false).klWidget();
                } else if (dataTypeNid == TinkarTerm.BOOLEAN_FIELD.nid()) {
                    KlBooleanFieldFactory klBooleanFieldFactory = new KlBooleanFieldFactory();
                    readOnlyNode = klBooleanFieldFactory.create(observableField, viewProperties.nodeView(), false).klWidget();
//                } else if (dataTypeNid == TinkarTerm.IMAGE_FIELD.nid()) {
//                    KlImageFieldFactory imageFieldFactory = new KlImageFieldFactory();
//                    readOnlyNode = imageFieldFactory.create(observableField, viewProperties.nodeView(), false).klWidget();
                } else if (dataTypeNid == TinkarTerm.BYTE_ARRAY_FIELD.nid()) {
                    KlImageFieldFactory imageFieldFactory = new KlImageFieldFactory();
                    readOnlyNode = imageFieldFactory.create(observableField, viewProperties.nodeView(), false).klWidget();
                }
                // Add to VBox
                if (readOnlyNode != null) {
                    container.getChildren().add(readOnlyNode);
                }
            };
            generateSemanticUIFields(viewProperties, semanticEntityVersionLatest, updateUIConsumer);
            return observableFields;
        }
}
