package dev.ikm.komet.kview.mvvm.view.navigation;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.PatternVersionRecord;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.eclipse.collections.api.list.ImmutableList;

public class SemanticTooltip extends Tooltip {

    private final BorderPane mainContainer;
    private final Label title;
    private final GridPane grid;
    private final VBox valuesContainer;

    private final ViewProperties viewProperties;

    public SemanticTooltip(ViewProperties viewProperties) {
        this.viewProperties = viewProperties;

        mainContainer = new BorderPane();
        title = new Label();
        grid = new GridPane();
        valuesContainer = new VBox(new Separator(), grid);

        mainContainer.setTop(title);
        mainContainer.setCenter(valuesContainer);

        title.setWrapText(true);

        setupGridPane();

        setGraphic(mainContainer);

        setShowDuration(Duration.seconds(10));

        // CSS
        getStyleClass().add("semantic-tooltip");
        mainContainer.getStyleClass().add("main-container");
        title.getStyleClass().add("title");
        valuesContainer.getStyleClass().add("values-container");
        grid.getStyleClass().add("grid");
    }

    private void setupGridPane() {
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(50);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(50);
        column2.setHalignment(HPos.RIGHT);
        grid.getColumnConstraints().addAll(column1, column2);
    }

    public void update(SemanticEntity<?> semanticEntity, String titleText) {
        Latest<? extends SemanticEntityVersion> latestId = viewProperties.calculator().latest(semanticEntity);
        ImmutableList fields = latestId.get().fieldValues();
        PatternVersionRecord patternVersionRecord = (PatternVersionRecord) viewProperties.calculator().latest(latestId.get().pattern()).get();

        this.title.setText(titleText);

        grid.getChildren().clear();
        int rowIndex = 0;
        for (int i = 0; i < patternVersionRecord.fieldDefinitions().size(); ++i) {
            String fieldTitle = patternVersionRecord.fieldDefinitions().get(i).meaning().description();
            String fieldValue = fields.get(i).toString();

            Label titleLabel = new Label(fieldTitle + ":");
            Label valueLabel = new Label(fieldValue);
            titleLabel.getStyleClass().add("field-title");
            valueLabel.getStyleClass().add("field-value");

            grid.add(titleLabel, 0, rowIndex);
            grid.add(valueLabel, 1, rowIndex);

            ++rowIndex;

            if (i < patternVersionRecord.fieldDefinitions().size() - 1) {
                Separator line = new Separator();
                GridPane.setColumnSpan(line, 2);
                grid.add(line, 0, rowIndex);

                ++rowIndex;
            }
        }
    }
}