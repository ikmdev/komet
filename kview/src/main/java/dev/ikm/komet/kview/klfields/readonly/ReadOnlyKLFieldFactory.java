package dev.ikm.komet.kview.klfields.readonly;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.entity.FieldRecord;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;

import java.net.URL;

/**
 * @deprecated This class is going to be removed once we have a "proper" implementation of DiTree
 */
@Deprecated
public class ReadOnlyKLFieldFactory {

    private ReadOnlyKLFieldFactory() {}
    // Inner class to provide instance of class
    private static class FieldTypeSingleton {
        private static final ReadOnlyKLFieldFactory INSTANCE = new ReadOnlyKLFieldFactory();
    }

    public static ReadOnlyKLFieldFactory getInstance() {
        return FieldTypeSingleton.INSTANCE;
    }
    private URL getResource(String name) {
        return getClass().getResource(name);
    }

    public Node createReadOnlyDiTree(ViewProperties viewProperties, FieldRecord<?> fieldRecord) {
        JFXNode<Node, Void> jfxNode = FXMLMvvmLoader.make(getResource("/dev/ikm/komet/kview/controls/read-only-ditree-field.fxml"));
        Node componentRow = jfxNode.node();
        // update field's meaning title label
        Label fieldMeaning = (Label) componentRow.lookup(".semantic-field-type-label");
        fieldMeaning.setTooltip(new Tooltip(text(viewProperties, fieldRecord.purposeNid())));
        fieldMeaning.setText(text(viewProperties, fieldRecord.meaningNid()));

        // update field's purpose
        TextFlow fieldValue = (TextFlow) componentRow.lookup(".semantic-field-ditree-value");
        DiTreeEntity value = (DiTreeEntity) fieldRecord.value();
        fieldValue.getChildren().add(new Text(value.toString()));
        return componentRow;
    }


//    /**
//     * Creates a read-only component. Such as language value is English Language.
//     * @param fieldRecord
//     * @return JavaFX Node representing a component for semantic details.
//     */
//    public Node createReadOnlyComponent(ViewProperties viewProperties, FieldRecord<?> fieldRecord) {
//        JFXNode<Node, Void> jfxNode = FXMLMvvmLoader.make(getResource("/dev/ikm/komet/kview/controls/read-only-component-field.fxml"));
//        Node componentRow = jfxNode.node();
//        // update field's meaning title label
//        Label fieldMeaning = (Label) componentRow.lookup(".semantic-field-type-label");
//        fieldMeaning.setTooltip(new Tooltip(text(viewProperties, fieldRecord.purposeNid())));
//        fieldMeaning.setText(text(viewProperties, fieldRecord.meaningNid()));
//
//        // update identicon
//        ImageView identicon = (ImageView) componentRow.lookup(".identicon-image-view");
//        identicon.setImage(Identicon.generateIdenticonImage(fieldRecord.purpose().publicId()));
//
//        // update field's purpose
//        Label fieldValue = (Label) componentRow.lookup(".semantic-field-value");
//        EntityFacade value = (EntityFacade) fieldRecord.value();
//        fieldValue.setText(value.description());
//        return componentRow;
//    }
//    public Node createReadOnlyComponentSet(ViewProperties viewProperties, FieldRecord<?> fieldRecord) {
//        JFXNode<Node, Void> jfxNode = FXMLMvvmLoader.make(getResource("/dev/ikm/komet/kview/controls/read-only-component-set-field.fxml"));
//        Node componentRow = jfxNode.node();
//        // obtain vbox to add items from set
//        VBox container = (VBox) componentRow.lookup(".semantic-field-collection-container");
//
//        // update field's meaning title label
//        Label fieldMeaning = (Label) componentRow.lookup(".semantic-field-type-label");
//        fieldMeaning.setTooltip(new Tooltip(text(viewProperties, fieldRecord.purposeNid())));
//        fieldMeaning.setText(text(viewProperties, fieldRecord.meaningNid()));
//
//        // loop through all components
//        IntIdSet componentSet = (IntIdSet) fieldRecord.value();
//        componentSet.forEach(componentId -> {
//            Latest<EntityVersion> component = viewProperties.calculator().stampCalculator().latest(componentId);
//            component.ifPresent(entityVersion -> {
//                Node componentRow2 = createReadOnlyComponentListItem(viewProperties, component);
//                container.getChildren().add(componentRow2);
//            });
//        });
//        return componentRow;
//    }

//    public Node createReadOnlyComponentList(ViewProperties viewProperties, FieldRecord<?> fieldRecord) {
//        JFXNode<Node, Void> jfxNode = FXMLMvvmLoader.make(getResource("/dev/ikm/komet/kview/controls/read-only-component-list-field.fxml"));
//        Node componentRow = jfxNode.node();
//        // obtain vbox to add items from set
//        VBox container = (VBox) componentRow.lookup(".semantic-field-collection-container");
//
//        // update field's meaning title label
//        Label fieldMeaning = (Label) componentRow.lookup(".semantic-field-type-label");
//        fieldMeaning.setTooltip(new Tooltip(text(viewProperties, fieldRecord.purposeNid())));
//        fieldMeaning.setText(text(viewProperties, fieldRecord.meaningNid()));
//
//        // loop through all components
//        IntIdList componentList = (IntIdList) fieldRecord.value();
//        componentList.forEach(componentId -> {
//            Latest<EntityVersion> component = viewProperties.calculator().stampCalculator().latest(componentId);
//            component.ifPresent(entityVersion -> {
//                Node componentRow2 = createReadOnlyComponentListItem(viewProperties, component);
//                container.getChildren().add(componentRow2);
//            });
//        });
//        return componentRow;
//    }

//    public Node createReadOnlyComponentListItem(ViewProperties viewProperties, Latest<EntityVersion> component){
//        if (component.isPresent()) {
//            JFXNode<Node, Void> jfxNodeItem = FXMLMvvmLoader.make(getResource("/dev/ikm/komet/kview/controls/read-only-component-list-item.fxml"));
//            Node componentRowItem = jfxNodeItem.node();
//            // update identicon
//            ImageView identicon = (ImageView) componentRowItem.lookup(".identicon-image-view");
//            identicon.setImage(Identicon.generateIdenticonImage(component.get().publicId()));
//
//            // update field's purpose
//            Label fieldValue = (Label) componentRowItem.lookup(".semantic-field-value");
//            EntityFacade value = component.get().entity();
//            fieldValue.setText(text(viewProperties, value.nid()));
//            return componentRowItem;
//        }
//        return null;
//    }

//    public ReadOnlyKlStringField createStringField(FieldRecord fieldRecord) {
//        JFXNode<Node, Void> jfxNode = FXMLMvvmLoader.make(getResource("/dev/ikm/komet/kview/controls/read-only-value-field.fxml"));
//        Node componentRow = jfxNode.node();
//        // update field's meaning title label
//        Label fieldMeaning = (Label) componentRow.lookup(".semantic-field-type-label");
//        fieldMeaning.setTooltip(new Tooltip(text(viewProperties, fieldRecord.purposeNid())));
//        fieldMeaning.setText(text(viewProperties, fieldRecord.meaningNid()));
//
//        // update field's purpose
//        Label fieldValue = (Label) componentRow.lookup(".semantic-field-value");
//        Object value = fieldRecord.value();
//        fieldValue.setText(String.valueOf(value));
//        return componentRow;

//        Optional<ReadOnlyKlStringField> stringField = PluggableService.load(ReadOnlyKlStringField.class).findFirst();
//        ServiceLoader<ReadOnlyKlStringField> stringField = ServiceLoader.load(ReadOnlyKlStringField.class).findFirst();
//        if (stringField.isPresent()) {
//            ReadOnlyKlStringField readOnlyKlStringField = stringField.get();
//            readOnlyKlStringField.field().fieldProperty().set(fieldRecord);
////            readOnlyKlStringField.setFieldRecord(fieldRecord);
////            readOnlyKlStringField.
//            return readOnlyKlStringField;
//        }
//        throw new RuntimeException("Unable to load ReadOnlyKlStringField");
//    }

    public static String text(ViewProperties viewProperties, int nid) {
        return viewProperties.calculator().languageCalculator().getDescriptionText(nid).orElse("No Description found");
    }

//
//    public void setupSemanticDetailsUI(ViewProperties viewProperties,
//                                        Latest<SemanticEntityVersion> semanticEntityVersionLatest,
//                                        Consumer<FieldRecord<Object>> updateUIConsumer) {
//        semanticEntityVersionLatest.ifPresent(semanticEntityVersion -> {
//            StampCalculator stampCalculator = viewProperties.calculator().stampCalculator();
//            Latest<PatternEntityVersion> patternEntityVersionLatest = stampCalculator.latest(semanticEntityVersion.pattern());
//            patternEntityVersionLatest.ifPresent(patternEntityVersion -> {
//                List<FieldRecord<Object>> fieldRecords = DataModelHelper.fieldRecords(semanticEntityVersion, patternEntityVersion);
//                fieldRecords.forEach(fieldRecord -> Platform.runLater(() -> updateUIConsumer.accept(fieldRecord)));
//            });
//        });
//    }
}
