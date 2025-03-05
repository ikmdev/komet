package dev.ikm.komet.kview.klfields;

import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.obtainObservableField;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ViewProperties;
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
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.FieldRecord;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampRecord;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.scene.Node;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

    // TODO: These methods below are in temporarily so we can add a Image data type that doesn't fetch anything from the database.
    // TODO: once the database has the capability for Image Data types we can remove these methods
    private static boolean hasAddedReadOnlyImage = false;
    private static boolean hasAddedEditableImage = false;


    /**
     * function to return the correct node given the semantic entity and field information
     * @param fieldRecord
     * @param observableField
     * @param viewProperties
     * @param semanticEntityVersionLatest
     * @return
     */
    public static Node generateNode(FieldRecord fieldRecord, ObservableField observableField, ViewProperties viewProperties,
                                    Latest<SemanticEntityVersion> semanticEntityVersionLatest, boolean editable) {

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
        } else if (dataTypeNid == TinkarTerm.INTEGER_FIELD.nid()) {
            KlIntegerFieldFactory klIntegerFieldFactory = new KlIntegerFieldFactory();
            node = klIntegerFieldFactory.create(observableField, viewProperties.nodeView(), editable).klWidget();
        } else if (dataTypeNid == TinkarTerm.BOOLEAN_FIELD.nid()) {
            KlBooleanFieldFactory klBooleanFieldFactory = new KlBooleanFieldFactory();
            node = klBooleanFieldFactory.create(observableField, viewProperties.nodeView(), editable).klWidget();
        } else if (dataTypeNid == TinkarTerm.IMAGE_FIELD.nid() || (editable &&
                (PublicId.equals(semanticEntityVersionLatest.get().entity().publicId(),
                        PublicIds.of(UUID.fromString("48633874-f3d2-434a-9f11-2a07e4c4311b")))
                        && !hasAddedEditableImage))) {
            KlImageFieldFactory imageFieldFactory = new KlImageFieldFactory();
            node = imageFieldFactory.create(observableField, viewProperties.nodeView(), editable).klWidget();
            hasAddedEditableImage = true;
        } else if (dataTypeNid == TinkarTerm.IMAGE_FIELD.nid() || (!editable &&
                (PublicId.equals(semanticEntityVersionLatest.get().entity().publicId(),
                        PublicIds.of(UUID.fromString("48633874-f3d2-434a-9f11-2a07e4c4311b")))
                        && !hasAddedReadOnlyImage))) {
            KlImageFieldFactory imageFieldFactory = new KlImageFieldFactory();
            node = imageFieldFactory.create(observableField, viewProperties.nodeView(), editable).klWidget();
            hasAddedReadOnlyImage = true;
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
            ObservableField writeObservableField = obtainObservableField(viewProperties, semanticEntityVersionLatest, fieldRecord, editable);
            ObservableField observableField = new ObservableField(writeObservableField.field(), editable);
            observableFields.add(observableField);

            // In edit view when we load uncommited data, we need to check if the transactions exists.
            if(editable){
                checkUncommitedTransactions(observableField.field());
            }

            // TODO: this method below will be removed once the database has the capability to add and edit Image data types
            // TODO: then all the code will be inside an if clause just like for the other data types.
            //maybeAddEditableImageControl(viewProperties, container, semanticEntityVersionLatest, observableField);
            Node node = generateNode(fieldRecord, observableField, viewProperties, semanticEntityVersionLatest, editable);
            items.add(node);
        };
        generateSemanticUIFields(viewProperties, semanticEntityVersionLatest, generateConsumer);

        return observableFields;
    }

    /**
     * This method check for any versions that are uncommited but have missing transactions.
     *      *
     * @param field
     */
    private static void checkUncommitedTransactions(FieldRecord field) {
        // Get stamp record for field
        StampRecord stampRecord = Entity.getStamp(field.semanticVersionStampNid());
        // Get current version
        SemanticVersionRecord semanticVersionRecord = Entity.getVersionFast(field.semanticNid(),field.semanticVersionStampNid());
        //check last version uncommited and transaction not created then create missing transaction.
        if(stampRecord.lastVersion().uncommitted() &&  Transaction.forVersion(semanticVersionRecord).isEmpty()){
            SemanticRecord semanticRecord = Entity.getFast(field.semanticNid());
            createMissingFieldTransaction(semanticRecord, stampRecord, semanticVersionRecord);
        }
    }

    public static void createMissingFieldTransaction(SemanticRecord semanticRecord, StampRecord stamp, SemanticVersionRecord version){
        MutableList fieldsForNewVersion = Lists.mutable.of(version.fieldValues().toArray());
        // Create transaction
        Transaction t = Transaction.make();
        // newStamp already written to the entity store.
        StampEntity newStamp = t.getStampForEntities(stamp.state(), stamp.authorNid(), stamp.moduleNid(), stamp.pathNid(), version.entity());

        // Create new version...
        SemanticVersionRecord newVersion = version.with().fieldValues(fieldsForNewVersion.toImmutable()).stampNid(newStamp.nid()).build();

        SemanticRecord analogue = semanticRecord.with(newVersion).build();

        // Entity provider will broadcast the nid of the changed entity.
        Entity.provider().putEntity(analogue);
    }
}
