package dev.ikm.komet.kview.klfields;

import static dev.ikm.komet.terms.KometTerm.BLANK_CONCEPT;
import static dev.ikm.tinkar.terms.TinkarTerm.ARRAY_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.BOOLEAN_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.BYTE_ARRAY_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.COMPONENT_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.COMPONENT_ID_LIST_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.COMPONENT_ID_SET_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.CONCEPT_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.DECIMAL_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.DIGRAPH_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.DITREE_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.DOUBLE_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.FLOAT;
import static dev.ikm.tinkar.terms.TinkarTerm.FLOAT_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.IMAGE_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.INSTANT_LITERAL;
import static dev.ikm.tinkar.terms.TinkarTerm.INTEGER_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.LOGICAL_EXPRESSION_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.LONG_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.POLYMORPHIC_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.STRING;
import static dev.ikm.tinkar.terms.TinkarTerm.STRING_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.UUID_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.VERTEX_FIELD;
import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.framework.observable.ObservableEntitySnapshot;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservablePatternSnapshot;
import dev.ikm.komet.framework.observable.ObservablePatternVersion;
import dev.ikm.komet.framework.observable.ObservableVersion;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.KLReadOnlyBaseControl;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentControl;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentListControl;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentSetControl;
import dev.ikm.komet.kview.controls.KLReadOnlyDataTypeControl;
import dev.ikm.komet.kview.controls.KLReadOnlyImageControl;
import dev.ikm.komet.kview.klfields.booleanfield.KlBooleanFieldFactory;
import dev.ikm.komet.kview.klfields.componentfield.KlComponentFieldFactory;
import dev.ikm.komet.kview.klfields.componentlistfield.KlComponentListFieldFactory;
import dev.ikm.komet.kview.klfields.componentsetfield.KlComponentSetFieldFactory;
import dev.ikm.komet.kview.klfields.floatfield.KlFloatFieldFactory;
import dev.ikm.komet.kview.klfields.imagefield.KlImageFieldFactory;
import dev.ikm.komet.kview.klfields.integerfield.KlIntegerFieldFactory;
import dev.ikm.komet.kview.klfields.readonly.ReadOnlyKLFieldFactory;
import dev.ikm.komet.kview.klfields.stringfield.KlStringFieldFactory;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.FieldRecord;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.PatternVersionRecord;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class KlFieldHelper {

    /**
     * The currently supported field data types when editing a semantic.
     */
    public final static int[] SUPPORTED_FIELD_TYPE_NIDS = new int[]{
            COMPONENT_FIELD.nid(),
            STRING_FIELD.nid(),
            COMPONENT_ID_SET_FIELD.nid(),
            COMPONENT_ID_LIST_FIELD.nid(),
            FLOAT_FIELD.nid(),
            INTEGER_FIELD.nid(),
            BOOLEAN_FIELD.nid(),
            IMAGE_FIELD.nid(),
    };

    /**
     * The unsupported field data types that defaults to read-only components.
     */
    public final static int[] UNSUPPORTED_FIELD_TYPE_NIDS = new int[]{
            CONCEPT_FIELD.nid(), /* for edit mode you can see the field as a readonly component field. */
            INSTANT_LITERAL.nid(),
            DIGRAPH_FIELD.nid(),
            DITREE_FIELD.nid(),
            DECIMAL_FIELD.nid(),
            BYTE_ARRAY_FIELD.nid(), /* for edit mode you can see the field as an image field. */
            DECIMAL_FIELD.nid(),
            ARRAY_FIELD.nid(),
            UUID_FIELD.nid(),
            LONG_FIELD.nid(),
            DOUBLE_FIELD.nid(),
            LOGICAL_EXPRESSION_FIELD.nid(),
            POLYMORPHIC_FIELD.nid(),
            VERTEX_FIELD.nid()
    };

    /**
     * A useful utility based on a pattern facade to determine if any field datatypes are not supported.
     * @param pattern a Pattern facade to detect field definitions
     * @return boolean true if any field has an unsupported field.
     */
    public static boolean hasAnyUnsupportedFieldType(PatternFacade pattern) {
        Optional<Entity<EntityVersion>> patternEntityOpt =  Entity.get(pattern);
        if (patternEntityOpt.isPresent()) {
            PatternEntityVersion patternEntityVersion = (PatternEntityVersion) patternEntityOpt.get().versions().get(0);
            List<Integer> nids = patternEntityVersion
                    .fieldDefinitions()
                    .stream()
                    .map(fieldDefinitionForEntity ->
                            fieldDefinitionForEntity.dataTypeNid()).toList();
            for (int value : UNSUPPORTED_FIELD_TYPE_NIDS) { // Iterate through each element in the int array
                if (nids.contains(value)) { // Check if the current int is present in the List
                    return true; // If found, return true immediately
                }
            }
        }
        return false;
    }

    /**
     * A useful utility based on a pattern facade to determine if all field datatypes are supported.
     * @param pattern
     * @return boolean true if all field are supported. otherwise false.
     */
    public static boolean hasAllFieldTypesSupported(PatternFacade pattern) {
        if (pattern == null) {
            return false;
        }
        Optional<Entity<EntityVersion>> patternEntityOpt =  Entity.get(pattern);
        if (patternEntityOpt.isPresent()) {
            PatternEntityVersion patternEntityVersion = (PatternEntityVersion) patternEntityOpt.get().versions().get(0);
            List<Integer> nids = patternEntityVersion
                    .fieldDefinitions()
                    .stream()
                    .map(fieldDefinitionForEntity ->
                            fieldDefinitionForEntity.dataTypeNid()).toList();
            // if any are not in the supported list than return false
            List<Integer> supportedFieldTypeNids = Arrays.stream(SUPPORTED_FIELD_TYPE_NIDS).boxed().toList();
            for (int value : nids) { // Iterate through each element in the int array
                if (!supportedFieldTypeNids.contains(value)) { // Check if the current int is present in the List
                    return false;
                }
            }
            return  true; // all fields are supported. Some patterns (membership patterns) don't have fields.
        }
        return false; // no pattern
    }
    /**
     * function to return the correct node given the semantic entity and field information
     * @param fieldRecord
     * @param observableField
     * @param viewProperties
     * @return
     */
    public static Node generateNode(FieldRecord fieldRecord, ObservableField observableField, ViewProperties viewProperties, boolean editable, UUID journalTopic) {

        Node node = null;
        ReadOnlyKLFieldFactory rowf = ReadOnlyKLFieldFactory.getInstance();
        int dataTypeNid = fieldRecord.dataType().nid();

        //TODO use service loader instead of factories

        if (dataTypeNid == COMPONENT_FIELD.nid()) {
            // load a read-only component
            KlComponentFieldFactory componentFieldFactory = new KlComponentFieldFactory();
            node = componentFieldFactory.create(observableField, viewProperties.nodeView(), editable).klWidget();
        } else if (dataTypeNid == CONCEPT_FIELD.nid()) {
            // TODO: Create validation error message to the user to only allow concepts into this field.
            //       This will be a read-only component field for now (editable = false).
            KlComponentFieldFactory componentFieldFactory = new KlComponentFieldFactory();
            node = componentFieldFactory.create(observableField, viewProperties.nodeView(), false).klWidget();
        } else if (dataTypeNid == STRING_FIELD.nid() || fieldRecord.dataType().nid() == STRING.nid()) {
            KlStringFieldFactory stringFieldTextFactory = new KlStringFieldFactory();
            node = stringFieldTextFactory.create(observableField, viewProperties.nodeView(), editable).klWidget();
        } else if (dataTypeNid == COMPONENT_ID_SET_FIELD.nid()) {
            KlComponentSetFieldFactory klComponentSetFieldFactory = new KlComponentSetFieldFactory();
            node = klComponentSetFieldFactory.create(observableField, viewProperties.nodeView(), editable, journalTopic).klWidget();
        } else if (dataTypeNid == COMPONENT_ID_LIST_FIELD.nid()) {
            KlComponentListFieldFactory klComponentListFieldFactory = new KlComponentListFieldFactory();
            node = klComponentListFieldFactory.create(observableField, viewProperties.nodeView(), editable, journalTopic).klWidget();
        } else if (dataTypeNid == FLOAT_FIELD.nid() || fieldRecord.dataType().nid() == FLOAT.nid()) {
            KlFloatFieldFactory klFloatFieldFactory = new KlFloatFieldFactory();
            node = klFloatFieldFactory.create(observableField, viewProperties.nodeView(), editable).klWidget();
        } else if (dataTypeNid == INTEGER_FIELD.nid()) {
            KlIntegerFieldFactory klIntegerFieldFactory = new KlIntegerFieldFactory();
            node = klIntegerFieldFactory.create(observableField, viewProperties.nodeView(), editable).klWidget();
        } else if (dataTypeNid == BOOLEAN_FIELD.nid()) {
            KlBooleanFieldFactory klBooleanFieldFactory = new KlBooleanFieldFactory();
            node = klBooleanFieldFactory.create(observableField, viewProperties.nodeView(), editable).klWidget();
        } else if (dataTypeNid == IMAGE_FIELD.nid()) {
            KlImageFieldFactory imageFieldFactory = new KlImageFieldFactory();
            node = imageFieldFactory.create(observableField, viewProperties.nodeView(), editable).klWidget();
        } else if (dataTypeNid == BYTE_ARRAY_FIELD.nid()) {
            //TODO: We're using BYTE_ARRAY for the moment for Image data type
            //TODO: using IMAGE_FIELD would require more comprehensive changes to our schema (back end)
            //TODO: We can come back later to this when for instance we need BYTE_ARRAY for something else other than Image
            KlImageFieldFactory imageFieldFactory = new KlImageFieldFactory();
            node = imageFieldFactory.create(observableField, viewProperties.nodeView(), editable).klWidget();
        } else {
            // This fixes the exceptions the user experiences when a semantic (GenEditWindow) is summoned. The exception
            // happens when a datatype field that doesn't have a JavaFX custom control created yet.
            // The else is a catchall for any datatypes we do not support to be shown as read-only.
            // For example if a digraph or ditree the toString() would show text represented as OWL notation.
            KlStringFieldFactory stringFieldTextFactory = new KlStringFieldFactory();
            node = stringFieldTextFactory.create(observableField, viewProperties.nodeView(), false).klWidget();
        }
        return node;
    }

    /**
     * Returns a list of field values.
     * @param patternVersion Pattern Version containing the field definitions
     * @return Returns a list of default values.
     * @param <T> T is of type List
     */
    public static <T extends List<Object>> T generateDefaultFieldValues(PatternEntityVersion patternVersion) {
        MutableList<Object> fieldsValues = Lists.mutable.ofInitialCapacity(patternVersion.fieldDefinitions().size());
        patternVersion.fieldDefinitions().forEach(f -> {
            if (f.dataTypeNid() == COMPONENT_FIELD.nid()) {
                fieldsValues.add(BLANK_CONCEPT);
            } else if (f.dataTypeNid() == STRING_FIELD.nid()
                    || f.dataTypeNid() == STRING.nid()) {
                fieldsValues.add("");
            } else if (f.dataTypeNid() == INTEGER_FIELD.nid()) {
                fieldsValues.add(0);
            } else if (f.dataTypeNid() == FLOAT_FIELD.nid()) {
                fieldsValues.add(0.0F);
            } else if (f.dataTypeNid() == BOOLEAN_FIELD.nid()) {
                fieldsValues.add(false);
            } else if (f.dataTypeNid() == COMPONENT_ID_LIST_FIELD.nid()) {
                fieldsValues.add(IntIds.list.empty());
            } else if (f.dataTypeNid() == COMPONENT_ID_SET_FIELD.nid()) {
                fieldsValues.add(IntIds.set.empty());
            } else if (f.dataTypeNid() == IMAGE_FIELD.nid()) {
                // create empty byte array to save in DB implies blank image
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte [] ba = bos.toByteArray();
                fieldsValues.add(ba);
            } else if (f.dataTypeNid() == BYTE_ARRAY_FIELD.nid()) {
                //TODO: We're using BYTE_ARRAY for the moment for Image data type
                //TODO: using IMAGE_FIELD would require more comprehensive changes to our schema (back end)
                //TODO: We can come back later to this when for instance we need BYTE_ARRAY for something else other than Image
                // The NULL value will not work since the object requires to be NON-NULL
                fieldsValues.add(null);
            } else {
                // Any unsupported fields will be null value in the list. This will keep things aligned where the ui
                // controls and values would be the same number.
                fieldsValues.add(null);
            }
        });
        return (T) fieldsValues;
    }

    /**
     * Create default semantic fields value based on the provided pattern
     * @param pattern
     * @param viewProperties
     * @return fieldValues.toImmutable() - returns list of immutable field values.
     */
    public static ImmutableList<Object> createDefaultFieldValues(EntityFacade pattern, ViewProperties viewProperties) {
        ObservableEntity observableEntity = ObservableEntity.get(pattern.nid());
        ObservablePatternSnapshot observablePatternSnapshot = (ObservablePatternSnapshot) observableEntity.getSnapshot(viewProperties.calculator());
        ObservablePatternVersion observablePatternVersion = observablePatternSnapshot.getLatestVersion().get();
        MutableList<Object> fieldsValues = generateDefaultFieldValues(observablePatternVersion);
        return fieldsValues.toImmutable();
    }

    /**
     * This method will return the latest commited version.
     * //TODO this method can be generalized to return latest<EntityVersion> As of now it is just returning SemanticEntityVersion.
     * // TODO need to implement logic for create Semantic.
     *
     * @return entityVersionLatest
     * */
//    public static Latest<SemanticEntityVersion> retrieveCommittedLatestVersion(ObservableSemanticSnapshot observableSemanticSnapshot) {
//        if(observableSemanticSnapshot == null){
//            return new Latest<>();
//        }
//        AtomicReference<Latest<SemanticEntityVersion>> entityVersionLatest = new AtomicReference<>();
//        SemanticEntityVersion semanticEntityVersion = (SemanticEntityVersion) observableSemanticSnapshot.getLatestVersion().get().getEntityVersion();
//        if(semanticEntityVersion.committed()){
//            return new Latest<>(semanticEntityVersion);
//        }
//        //Get list of previously committed data sorted in latest at the top.
//        ImmutableList<ObservableSemanticVersion> observableSemanticVersionImmutableList = observableSemanticSnapshot.getHistoricVersions();
//        // Filter out Uncommitted data. Data whose time stamp parameter is Long.MAX_VALUE. and get the 1st available.
//        Optional<ObservableSemanticVersion> observableSemanticVersionOptional = observableSemanticVersionImmutableList.stream().filter(p -> p.stamp().time() != Long.MAX_VALUE).findFirst();
//        observableSemanticVersionOptional.ifPresentOrElse((p) -> {
//            entityVersionLatest.set(new Latest<>(p));
//        }, () -> {entityVersionLatest.set(new Latest<>());});
//        return entityVersionLatest.get();
//    }

    /**
     * This method will return the latest commited version.
     *
     * @return entityVersionLatest
     * */
    public static Latest<EntityVersion> retrieveCommittedLatestVersion(ObservableEntitySnapshot observableEntitySnapshot) {
        if(observableEntitySnapshot == null){
            return new Latest<>();
        }
        AtomicReference<Latest<EntityVersion>> entityVersionLatest = new AtomicReference<>();
        ObservableVersion observableVersion = (ObservableVersion) observableEntitySnapshot.getLatestVersion().get();
        EntityVersion entityVersion = observableVersion.getEntityVersion();
        if(entityVersion.committed()){
            return new Latest<>(entityVersion);
        }
        //Get list of previously committed data sorted in latest at the top.
        ImmutableList<ObservableVersion> historicVersions = observableEntitySnapshot.getHistoricVersions();
        // Filter out Uncommitted data. Data whose time stamp parameter is Long.MAX_VALUE. and get the 1st available.
        Optional<ObservableVersion> optionalObservableVersion = historicVersions.stream().filter(p -> p.stamp().time() != Long.MAX_VALUE).findFirst();
        optionalObservableVersion.ifPresentOrElse((p) -> {
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
    public static int calculateHashValue(List<ObservableField<?>> observableFieldsList ) {
        StringBuilder stringBuilder = new StringBuilder();
        observableFieldsList.forEach(observableField -> {
            if (observableField.dataTypeNid() == IMAGE_FIELD.nid() || observableField.dataTypeNid() == BYTE_ARRAY_FIELD.nid()) {
                // need to handle byte array to ensure that the same image is not getting uploaded and resaved. This is to enable/disable submit button.
                byte [] byteArray = (byte[]) observableField.valueProperty().get();
                String str = new String(byteArray, java.nio.charset.StandardCharsets.UTF_8);
                stringBuilder.append(str);
            } else if (observableField.valueProperty().get() != null) {
                // TODO re-evaluate if toString is the right approach for complex datatypes.
                stringBuilder.append(observableField.valueProperty().get().toString());
            }

            stringBuilder.append("|");
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
            if (fieldDefinitionRecord.dataTypeNid() == COMPONENT_FIELD.nid()) {
                control = new KLReadOnlyComponentControl();
            } else if (fieldDefinitionRecord.dataTypeNid() == CONCEPT_FIELD.nid()) {
                control = new KLReadOnlyComponentControl();
            } else if (fieldDefinitionRecord.dataTypeNid() == STRING_FIELD.nid()
                    || fieldDefinitionRecord.dataTypeNid() == STRING.nid()) {
                control = new KLReadOnlyDataTypeControl<>(String.class);
            } else if (fieldDefinitionRecord.dataTypeNid() == INTEGER_FIELD.nid()) {
                control = new KLReadOnlyDataTypeControl<>(Integer.class);
            } else if (fieldDefinitionRecord.dataTypeNid() == FLOAT_FIELD.nid()) {
                control = new KLReadOnlyDataTypeControl<>(Float.class);
            } else if (fieldDefinitionRecord.dataTypeNid() == BOOLEAN_FIELD.nid()) {
                control = new KLReadOnlyDataTypeControl<>(Boolean.class);
            } else if (fieldDefinitionRecord.dataTypeNid() == COMPONENT_ID_LIST_FIELD.nid()) {
                control = new KLReadOnlyComponentListControl();
            } else if (fieldDefinitionRecord.dataTypeNid() == COMPONENT_ID_SET_FIELD.nid()) {
                control = new KLReadOnlyComponentSetControl();
            } else if (fieldDefinitionRecord.dataTypeNid() == IMAGE_FIELD.nid()) {
                control = new KLReadOnlyImageControl();
            } else if (fieldDefinitionRecord.dataTypeNid() == BYTE_ARRAY_FIELD.nid()) {
                //TODO: We're using BYTE_ARRAY for the moment for Image data type
                //TODO: using IMAGE_FIELD would require more comprehensive changes to our schema (back end)
                //TODO: We can come back later to this when for instance we need BYTE_ARRAY for something else other than Image
                control = new KLReadOnlyImageControl();
            } else {
                // This is temporary to avoid an exception for datatypes we don't support.
                control = new KLReadOnlyDataTypeControl<>(String.class);
            }

            control.setTitle(fieldDefinitionRecord.meaning().description());
            control.setTooltip(tooltip);
            defaultNodes.add(control);
        });
        return defaultNodes;
    }

    /**
     * Creates and returns a new JavaFX Image from a byte[]
     *
     * @param imageByteArray the byte[] containing the Image.
     * @returna new JavaFX Image.
     */
    public static Image newImageFromByteArray(byte[] imageByteArray) {
        // If the image is blank or empty then return null
        if(imageByteArray.length == 0){
            return null;
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(imageByteArray);
        Image image = new Image(bis);
        return image;
    }

    /**
     * Creates and returns a new byte[] from a JavaFX Image
     *
     * @param image the JavaFX Image.
     * @return new byte[].
     */
    public static byte[] newByteArrayFromImage(Image image) {
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "png", bos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bos.toByteArray();
    }
}
