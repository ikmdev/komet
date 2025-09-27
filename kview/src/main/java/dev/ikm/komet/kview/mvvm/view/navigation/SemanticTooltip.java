package dev.ikm.komet.kview.mvvm.view.navigation;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.skin.ComponentItemNode;
import dev.ikm.komet.kview.klfields.KlFieldHelper;
import dev.ikm.komet.kview.klfields.KlFieldType;
import dev.ikm.tinkar.common.id.IntIdCollection;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.EntityBinding;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.Optional;

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

        setShowDuration(Duration.seconds(15));

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
        column2.setFillWidth(false);

        grid.getColumnConstraints().addAll(column1, column2);
    }
    public void update(PatternEntity patternEntity, String titleText) {
        this.title.setText(titleText);
        grid.getChildren().clear();

        Latest<? extends PatternEntityVersion> latestId = viewProperties.calculator().latest(patternEntity);

        latestId.ifPresent(pv -> {
            ImmutableList fields = pv.fieldDefinitions();
            PatternVersionRecord patternVersionRecord = (PatternVersionRecord) viewProperties.calculator().latest(EntityBinding.Pattern.pattern()).get();
            int rowIndex = 0;
            for (int i = 0; i < patternVersionRecord.fieldDefinitions().size(); ++i) {
                addFieldToGridPane(patternVersionRecord, i, fields, rowIndex);
                ++rowIndex;
                if (i < patternVersionRecord.fieldDefinitions().size() - 1) {
                    addSeparatorToGridPane(rowIndex);
                    ++rowIndex;
                }
            }
        });
    }
    public void update(ConceptEntity conceptEntity, String titleText) {
        this.title.setText(titleText);
        grid.getChildren().clear();
        viewProperties.calculator().latest(conceptEntity);
        Label  label = new Label(viewProperties.calculator().getDescriptionTextOrNid(conceptEntity));
        grid.add(label, 0, 0);
    }

    public void update(StampEntity stampEntity, String titleText) {
        this.title.setText(titleText);
        grid.getChildren().clear();

        Latest<? extends StampEntityVersion> latestId = viewProperties.calculator().latest(stampEntity);
        latestId.ifPresent(sv -> {
            ImmutableList fields = Lists.immutable.of(sv.state(), sv.instant(), sv.author(), sv.module(), sv.path());
            viewProperties.calculator().latestPatternEntityVersion(EntityBinding.Stamp.pattern()).ifPresent(patternEntityVersion -> {
                        int rowIndex = 0;
                        for (int i = 0; i < patternEntityVersion.fieldDefinitions().size(); ++i) {
                            addFieldToGridPane(patternEntityVersion, i, fields, rowIndex);
                            ++rowIndex;

                            if (i < patternEntityVersion.fieldDefinitions().size() - 1) {
                                addSeparatorToGridPane(rowIndex);
                                ++rowIndex;
                            }
                        }
            });
        });
    }

    public void update(SemanticEntity<?> semanticEntity, String titleText) {
        this.title.setText(titleText);
        grid.getChildren().clear();

        Latest<? extends SemanticEntityVersion> latestId = viewProperties.calculator().latest(semanticEntity);
        latestId.ifPresent(sv -> {
            ImmutableList fields = latestId.get().fieldValues();
            viewProperties.calculator().latestPatternEntityVersion(EntityBinding.Stamp.pattern()).ifPresent(patternEntityVersion -> {
                int rowIndex = 0;
                for (int i = 0; i < patternEntityVersion.fieldDefinitions().size(); ++i) {
                    addFieldToGridPane(patternEntityVersion, i, fields, rowIndex);
                    ++rowIndex;
                    if (i < patternEntityVersion.fieldDefinitions().size() - 1) {
                        addSeparatorToGridPane(rowIndex);
                        ++rowIndex;
                    }
                }
            });
        });
    }

    private void addSeparatorToGridPane(int rowIndex) {
        Separator line = new Separator();
        GridPane.setColumnSpan(line, 2);
        grid.add(line, 0, rowIndex);

        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setValignment(VPos.TOP);
        grid.getRowConstraints().add(rowConstraints);
    }

    private void addFieldToGridPane(PatternEntityVersion patternEntityVersion, int fieldIndex, ImmutableList fields, int rowIndex) {
        // Field Title
        String fieldTitle = patternEntityVersion.fieldDefinitions().get(fieldIndex).meaning().description();
        Label titleLabel = new Label(fieldTitle + ":");
        grid.add(titleLabel, 0, rowIndex);
        titleLabel.getStyleClass().add("field-title");

        // Field Value
        int dataTypeNid = patternEntityVersion.fieldDefinitions().get(fieldIndex).dataTypeNid();
        Optional<KlFieldType> optionalFieldType = KlFieldType.of(dataTypeNid);

        optionalFieldType.ifPresent(fieldType -> {
            Object field = fields.get(fieldIndex);
            Node nodeToAdd = null;

            switch(fieldType) {
                case STRING, INTEGER, FLOAT, BOOLEAN -> {
                    nodeToAdd = createSimpleFieldValue(field);
                }
                case IMAGE -> {
                    nodeToAdd = createImageFieldValue((byte[]) field);
                }

                case COMPONENT -> {
                    nodeToAdd = createComponentFieldValue((EntityProxy) field);
                }
                case C_LIST, C_SET -> {
                    nodeToAdd = createCListOrCSetFieldValue((IntIdCollection) field);
                }
            }

            if (nodeToAdd != null) {
                grid.add(nodeToAdd, 1, rowIndex);
            }
        });

        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setValignment(VPos.TOP);
        grid.getRowConstraints().add(rowConstraints);
    }

    private FlowPane createCListOrCSetFieldValue(IntIdCollection field) {
        FlowPane flowPane = new FlowPane();

        IntIdCollection intIdCollection = field;
        intIdCollection.forEach(nid -> {
            EntityProxy entityProxy = EntityProxy.make(nid);
            ComponentItemNode componentItemNode = createComponentItemNode(entityProxy);
            flowPane.getChildren().add(componentItemNode);
        });

        flowPane.getStyleClass().add("components-container");
        return flowPane;
    }

    private ComponentItemNode createComponentFieldValue(EntityProxy field) {
        ComponentItemNode componentItemNode = createComponentItemNode(field);

        componentItemNode.getStyleClass().add("single-component");
        return componentItemNode;
    }

    private BorderPane createImageFieldValue(byte[] field) {
        Image image = KlFieldHelper.newImageFromByteArray(field);

        ImageView imagePreview = new ImageView(image);
        imagePreview.setPreserveRatio(true);
        imagePreview.getStyleClass().add("field-value");

        if (image.getWidth() > 100) {
            imagePreview.setFitWidth(100);
        }
        if (image.getHeight() > 100) {
            imagePreview.setFitHeight(100);
        }

        Rectangle rectangle = new Rectangle(imagePreview.getBoundsInLocal().getWidth(), imagePreview.getBoundsInLocal().getHeight());
        rectangle.setArcHeight(15);
        rectangle.setArcWidth(15);
        imagePreview.setClip(rectangle);

        BorderPane imageContainer = new BorderPane();
        imageContainer.setRight(imagePreview);

        imageContainer.getStyleClass().add("image-container");
        return imageContainer;
    }

    private Label createSimpleFieldValue(Object field) {
        String fieldValue = field.toString();

        Label valueLabel = new Label(fieldValue);
        valueLabel.getStyleClass().add("field-value");
        return valueLabel;
    }

    private ComponentItemNode createComponentItemNode(EntityProxy entityProxy) {
        String description = viewProperties.calculator().languageCalculator()
                .getFullyQualifiedDescriptionTextWithFallbackOrNid(entityProxy.nid());
        Image identicon = Identicon.generateIdenticonImage(entityProxy.publicId());

        ComponentItemNode componentItemNode = new ComponentItemNode(description, identicon);
        componentItemNode.setCircular(true);

        return componentItemNode;
    }
}