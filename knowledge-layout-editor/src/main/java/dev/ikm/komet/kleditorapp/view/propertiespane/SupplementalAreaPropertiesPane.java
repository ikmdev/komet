package dev.ikm.komet.kleditorapp.view.propertiespane;

import dev.ikm.komet.kleditorapp.view.ControlBasePropertiesPane;
import dev.ikm.komet.layout.editor.model.EditorSupplementalAreaModel;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Properties pane for a placed supplemental area: shows the area type (title) and the backing
 * factory class name, and provides the shared DELETE action. Editing of area-specific settings
 * (for example a Claude check's criterion) is a planned follow-up.
 */
public class SupplementalAreaPropertiesPane extends ControlBasePropertiesPane<EditorSupplementalAreaModel> {
    public static final String DEFAULT_STYLE_CLASS = "supplemental-area-properties";

    private final VBox mainVBox = new VBox();
    private final Label typeValueLabel = new Label();
    private final Label factoryValueLabel = new Label();

    public SupplementalAreaPropertiesPane() {
        super(true);

        Label typeLabel = new Label("Area Type:");
        Label factoryLabel = new Label("Factory:");
        factoryValueLabel.setWrapText(true);

        mainVBox.setSpacing(4);
        mainVBox.getChildren().addAll(typeLabel, typeValueLabel, factoryLabel, factoryValueLabel);

        mainContainer.setCenter(mainVBox);

        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    @Override
    protected void doInit() {
        if (previouslyShownModel != null) {
            typeValueLabel.textProperty().unbind();
        }
        typeValueLabel.textProperty().bind(currentlyShownModel.titleProperty());
        factoryValueLabel.setText(currentlyShownModel.getAreaFactoryClassName());
    }
}
