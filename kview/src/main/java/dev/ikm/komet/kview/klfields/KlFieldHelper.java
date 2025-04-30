package dev.ikm.komet.kview.klfields;

import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.obtainObservableField;
import static dev.ikm.tinkar.terms.TinkarTerm.INTEGER_FIELD;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableSemanticSnapshot;
import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.kview.controls.KLReadOnlyBaseControl;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentControl;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentListControl;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentSetControl;
import dev.ikm.komet.kview.controls.KLReadOnlyDataTypeControl;
import dev.ikm.komet.kview.klfields.booleanfield.KlBooleanFieldFactory;
import dev.ikm.komet.kview.klfields.componentfield.KlComponentFieldFactory;
import dev.ikm.komet.kview.klfields.componentlistfield.KlComponentListFieldFactory;
import dev.ikm.komet.kview.klfields.componentsetfield.KlComponentSetFieldFactory;
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
import dev.ikm.tinkar.entity.PatternVersionRecord;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class KlFieldHelper {


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
     * function to return the correct node given the semantic entity and field information
     * @param fieldRecord
     * @param observableField
     * @param viewProperties
     * @return
     */
    public static Node generateNode(FieldRecord fieldRecord, ObservableField observableField, ViewProperties viewProperties, boolean editable) {

        Node node = null;
        ReadOnlyKLFieldFactory rowf = ReadOnlyKLFieldFactory.getInstance();
        int dataTypeNid = fieldRecord.dataType().nid();

        //TODO use service loader instead of factories

        if (dataTypeNid == TinkarTerm.COMPONENT_FIELD.nid()) {
            // load a read-only component
            KlComponentFieldFactory componentFieldFactory = new KlComponentFieldFactory();
            node = componentFieldFactory.create(observableField, viewProperties.nodeView(), editable).klWidget();
        } else if (dataTypeNid == TinkarTerm.STRING_FIELD.nid() || fieldRecord.dataType().nid() == TinkarTerm.STRING.nid()) {
            KlStringFieldFactory stringFieldTextFactory = new KlStringFieldFactory();
            node = stringFieldTextFactory.create(observableField, viewProperties.nodeView(), editable).klWidget();
        } else if (dataTypeNid == TinkarTerm.COMPONENT_ID_SET_FIELD.nid()) {
            KlComponentSetFieldFactory klComponentSetFieldFactory = new KlComponentSetFieldFactory();
            node = klComponentSetFieldFactory.create(observableField, viewProperties.nodeView(), editable).klWidget();
        } else if (dataTypeNid == TinkarTerm.COMPONENT_ID_LIST_FIELD.nid()) {
            KlComponentListFieldFactory klComponentListFieldFactory = new KlComponentListFieldFactory();
            node = klComponentListFieldFactory.create(observableField, viewProperties.nodeView(), editable).klWidget();
        } else if (dataTypeNid == TinkarTerm.DITREE_FIELD.nid()) {
            node = rowf.createReadOnlyDiTree(viewProperties, fieldRecord);
        } else if (dataTypeNid == TinkarTerm.FLOAT_FIELD.nid() || fieldRecord.dataType().nid() == TinkarTerm.FLOAT.nid()) {
            KlFloatFieldFactory klFloatFieldFactory = new KlFloatFieldFactory();
            node = klFloatFieldFactory.create(observableField, viewProperties.nodeView(), editable).klWidget();
        } else if (dataTypeNid == INTEGER_FIELD.nid()) {
            KlIntegerFieldFactory klIntegerFieldFactory = new KlIntegerFieldFactory();
            node = klIntegerFieldFactory.create(observableField, viewProperties.nodeView(), editable).klWidget();
        } else if (dataTypeNid == TinkarTerm.BOOLEAN_FIELD.nid()) {
            KlBooleanFieldFactory klBooleanFieldFactory = new KlBooleanFieldFactory();
            node = klBooleanFieldFactory.create(observableField, viewProperties.nodeView(), editable).klWidget();
        } else if (dataTypeNid == TinkarTerm.BYTE_ARRAY_FIELD.nid()) {
            //TODO: We're using BYTE_ARRAY for the moment for Image data type
            //TODO: using IMAGE_FIELD would require more comprehensive changes to our schema (back end)
            //TODO: We can come back later to this when for instance we need BYTE_ARRAY for something else other than Image
            KlImageFieldFactory imageFieldFactory = new KlImageFieldFactory();
            node = imageFieldFactory.create(observableField, viewProperties.nodeView(), editable).klWidget();
        }

        return node;
    }

    /**
     * Returns a list of observable fields and displays editable controls on a Pane using the latest semantic entity version.
     * @param viewProperties View Properties
     * @param items list of JavaFX Nodes; each node is a custom UI control that is either read only or editable
     * @param semanticEntityVersionLatest Semantic Entity Version object containing all field records and their field definitions & value
     * @param editable flag for editable vs readonly
     * @return A list of observable fields
     */
    public static List<ObservableField<?>> generateObservableFieldsAndNodes(ViewProperties viewProperties, List<Node> items,
                                                                            Latest<SemanticEntityVersion> semanticEntityVersionLatest, boolean editable) {

        List<ObservableField<?>> observableFields = new ArrayList<>();
        Consumer<FieldRecord<Object>> generateConsumer = (fieldRecord) -> {
            ObservableField<?> writeObservableField = obtainObservableField(viewProperties, semanticEntityVersionLatest, fieldRecord, editable);
            ObservableField<?> observableField = new ObservableField<>(writeObservableField.field(), editable);
            observableFields.add(observableField);

            Node node = generateNode(fieldRecord, observableField, viewProperties, editable);
            items.add(node);
        };
        generateSemanticUIFields(viewProperties, semanticEntityVersionLatest, generateConsumer);

        return observableFields;
    }

    /**
     * This method calculates the hashValue for the passed semantic version.
     * It implements its own logic for fieldRecordConsumer and reuses the  generateSemanticUIFields(...)
     * method to get the field records.
     * @param semanticEntityVersionLatest
     * @param viewProperties
     * @return integer hashCode for field values.
     */
    public static int generateHashValue(Latest<SemanticEntityVersion> semanticEntityVersionLatest, ViewProperties viewProperties ) {
        List<ObservableField<?>> observableFieldsList = new ArrayList<>();
        Consumer<FieldRecord<Object>> fieldRecordConsumer = (fieldRecord) -> {
            ObservableField<?> writeObservableField = obtainObservableField(viewProperties, semanticEntityVersionLatest, fieldRecord, false);
            ObservableField<?> observableField = new ObservableField<>(writeObservableField.field(), false);
            observableFieldsList.add(observableField);
        };
        generateSemanticUIFields(viewProperties, semanticEntityVersionLatest, fieldRecordConsumer);
        return calculteHashValue(observableFieldsList);
    }

    /**
     * This method will return the latest commited version.
     * //TODO this method can be generalized to return latest<EntityVersion> As of now it is just returning SemanticEntityVersion.
     * // TODO need to implement logic for create Semantic.
     *
     * @return entityVersionLatest
     * */
    public static Latest<SemanticEntityVersion> retrieveCommittedLatestVersion(ObservableSemanticSnapshot observableSemanticSnapshot) {
        if(observableSemanticSnapshot == null){
            return new Latest<>();
        }
        AtomicReference<Latest<SemanticEntityVersion>> entityVersionLatest = new AtomicReference<>();
        SemanticEntityVersion semanticEntityVersion = (SemanticEntityVersion) observableSemanticSnapshot.getLatestVersion().get().getEntityVersion();
        if(semanticEntityVersion.committed()){
            return new Latest<>(semanticEntityVersion);
        }
        //Get list of previously committed data sorted in latest at the top.
        ImmutableList<ObservableSemanticVersion> observableSemanticVersionImmutableList = observableSemanticSnapshot.getHistoricVersions();
        // Filter out Uncommitted data. Data whose time stamp parameter is Long.MAX_VALUE. and get the 1st available.
        Optional<ObservableSemanticVersion> observableSemanticVersionOptional = observableSemanticVersionImmutableList.stream().filter(p -> p.stamp().time() != Long.MAX_VALUE).findFirst();
        observableSemanticVersionOptional.ifPresentOrElse((p) -> {
            entityVersionLatest.set(new Latest<>(p));
        }, () -> {entityVersionLatest.set(new Latest<>());});
        return entityVersionLatest.get();
    }

    /**
     * This method just concatenates all observableFiled values and generates a hashCode to return.
     * @param observableFieldsList
     * @return hashCode for all the field values.
     *
     * TODO: This method can be moved to DataModelHelper class.
     *  During create (new Semantic) the user can change the reference component.
     *  the hash is stating any change. By default a reference component during created would be TinkarTerms.ANONOUMOUS_CONCEPT (I can't remember).
     */
    public static int calculteHashValue(List<ObservableField<?>> observableFieldsList ) {
        StringBuilder stringBuilder = new StringBuilder();
        observableFieldsList.forEach(observableField -> {
            // TODO re-evaluate if toString is the right approach for complex datatypes.
            var observableFieldValue = observableField.valueProperty().get();
            if (observableFieldValue == null || (observableFieldValue instanceof EntityProxy entityProxy && entityProxy.nid() == KLComponentControl.EMPTY_NID)) {
                stringBuilder.append("|");
            } else {
                stringBuilder.append(observableField.valueProperty().get().toString()).append("|");
            }
        });
        return stringBuilder.toString().hashCode();
    }

    /**
     * generate the UI controls in create mode
     *
     * @param patternVersionRecord pattern to inspect fields

     * @param viewProperties       view properties
     * @return
     */
    public static List<KLReadOnlyBaseControl> addReadOnlyBlankControlsToContainer(PatternVersionRecord patternVersionRecord, ViewProperties viewProperties) {
        List<KLReadOnlyBaseControl> defaultNodes = new ArrayList<>();
        patternVersionRecord.fieldDefinitions().forEach(fieldDefinitionRecord -> {
            Tooltip tooltip = new Tooltip(viewProperties.calculator().getDescriptionTextOrNid(fieldDefinitionRecord.purposeNid()));

            KLReadOnlyBaseControl control = null;
            if (fieldDefinitionRecord.dataTypeNid() == TinkarTerm.COMPONENT_FIELD.nid()) {
                control = new KLReadOnlyComponentControl();
            } else if (fieldDefinitionRecord.dataTypeNid() == TinkarTerm.STRING_FIELD.nid()
                    || fieldDefinitionRecord.dataTypeNid() == TinkarTerm.STRING.nid()) {
                control = new KLReadOnlyDataTypeControl<>(String.class);
            } else if (fieldDefinitionRecord.dataTypeNid() == INTEGER_FIELD.nid()) {
                control = new KLReadOnlyDataTypeControl<>(Integer.class);
            } else if (fieldDefinitionRecord.dataTypeNid() == TinkarTerm.FLOAT_FIELD.nid()) {
                control = new KLReadOnlyDataTypeControl<>(Float.class);
            } else if (fieldDefinitionRecord.dataTypeNid() == TinkarTerm.BOOLEAN_FIELD.nid()) {
                control = new KLReadOnlyDataTypeControl<>(Boolean.class);
            } else if (fieldDefinitionRecord.dataTypeNid() == TinkarTerm.COMPONENT_ID_LIST_FIELD.nid()) {
                control = new KLReadOnlyComponentListControl();
            } else if (fieldDefinitionRecord.dataTypeNid() == TinkarTerm.COMPONENT_ID_SET_FIELD.nid()) {
                control = new KLReadOnlyComponentSetControl();
            }

            control.setTitle(fieldDefinitionRecord.meaning().description());
            control.setTooltip(tooltip);
            defaultNodes.add(control);
        });
        return defaultNodes;
    }
}
