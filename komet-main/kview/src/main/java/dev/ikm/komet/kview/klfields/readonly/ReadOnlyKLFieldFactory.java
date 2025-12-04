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

    public static String text(ViewProperties viewProperties, int nid) {
        return viewProperties.calculator().languageCalculator().getDescriptionText(nid).orElse("No Description found");
    }
}
