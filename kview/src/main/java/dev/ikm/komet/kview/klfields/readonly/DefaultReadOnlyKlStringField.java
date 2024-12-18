package dev.ikm.komet.kview.klfields.readonly;

import dev.ikm.komet.framework.observable.ObservableField;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;

public class DefaultReadOnlyKlStringField implements ReadOnlyKlStringField {
    private ObservableField<String> field = new ObservableField<>(null);
    private Node node;

    @Override
    public ObservableField<String> field() {
        return field;
    }

    @Override
    public Node sceneGraphNode() {
        if (node == null) {
            JFXNode<Pane, Void> jfxNode = FXMLMvvmLoader.make(this.getClass().getResource("/dev/ikm/komet/kview/controls/read-only-value-field.fxml"));
            Pane componentRow = jfxNode.node();
            // update field's meaning title label
            Label fieldMeaning = (Label) componentRow.lookup(".semantic-field-type-label");
            Label fieldValue = (Label) componentRow.lookup(".semantic-field-value");

            field().fieldProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    // when text changes update UI
                    fieldMeaning.setTooltip(new Tooltip(newValue.purpose().description()));
                    fieldMeaning.setText(newValue.meaning().description());
                    fieldValue.setText(newValue.value());
                }
            });

            fieldMeaning.setTooltip(new Tooltip(field().fieldRecord().purpose().description()));
            fieldMeaning.setText(field().fieldRecord().meaning().description());
            fieldValue.setText(field().fieldRecord().value());

            node = componentRow;
        }
        return node;
    }
}
