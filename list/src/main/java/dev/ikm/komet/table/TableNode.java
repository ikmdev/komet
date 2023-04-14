package dev.ikm.komet.table;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import org.eclipse.collections.api.list.ImmutableList;
import dev.ikm.komet.framework.ExplorationNodeAbstract;
import dev.ikm.komet.framework.TopPanelFactory;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.component.Component;
import dev.ikm.tinkar.coordinate.stamp.StampFields;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.EntityFacade;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class TableNode extends ExplorationNodeAbstract {
    protected static final String STYLE_ID = "table-node";
    protected static final String TITLE = "Semantic table";
    final SimpleObjectProperty<EntityFacade> entityFocusProperty = new SimpleObjectProperty<>();
    private final TreeItem<Component> root;
    private final BorderPane contentPane = new BorderPane();
    private final TreeTableView<Component> treeTableView;

    public TableNode(ViewProperties viewProperties, KometPreferences nodePreferences) {
        super(viewProperties, nodePreferences);

        this.root = new TreeItem<>();
        root.setExpanded(true);
        this.treeTableView = new TreeTableView<>(root);
        this.treeTableView.setTableMenuButtonVisible(true);
        this.treeTableView.setShowRoot(false);

        entityFocusProperty.addListener(this::focusChanged);
        Platform.runLater(() -> {
            setupTopPanel(viewProperties);
            // TODO: temp line for development simplicity. Use preferences in the future.
            // Platform.runLater(() -> entityFocusProperty.set(TinkarTerm.PATH_ORIGINS_PATTERN));
        });
    }

    private void focusChanged(ObservableValue<? extends EntityFacade> observable, EntityFacade oldValue, EntityFacade newValue) {
        this.root.getChildren().clear();
        Optional<? extends Entity<? extends EntityVersion>> optionalNewEntity = Entity.get(newValue);
        optionalNewEntity.ifPresent(newEntity -> {
            if (newEntity instanceof ConceptEntity conceptEntity) {
                // Don't know what to do...
            } else if (newEntity instanceof PatternEntity patternEntity) {
                setupPattern(patternEntity, true);
            } else if (newEntity instanceof SemanticEntity semanticEntity) {
                setupSemantic(semanticEntity);
            }
        });
    }

    private void setupTopPanel(ViewProperties viewProperties) {
        this.contentPane.setCenter(this.treeTableView);
        Node topPanel = TopPanelFactory.make(viewProperties, entityFocusProperty,
                activityStreamKeyProperty, optionForActivityStreamKeyProperty, true);
        this.contentPane.setTop(topPanel);
    }

    private void setupPattern(PatternEntity patternEntity, boolean populate) {
        this.treeTableView.getColumns().clear();
        this.root.getChildren().clear();
        this.viewProperties.calculator().latestPatternEntityVersion(patternEntity).ifPresent(patternEntityVersion -> {
            {
                String meaningText = viewCalculator().getPreferredDescriptionTextWithFallbackOrNid(patternEntityVersion.semanticMeaningNid());
                String purposeText = viewCalculator().getPreferredDescriptionTextWithFallbackOrNid(patternEntityVersion.semanticPurposeNid());

                TreeTableColumn<Component, Object> column = makeColumn(meaningText + " of ", purposeText);
                column.setCellValueFactory(param -> {
                    Component component = param.getValue().getValue();
                    if (component instanceof Entity entity) {
                        return new SimpleObjectProperty(languageCalculator().getPreferredDescriptionTextWithFallbackOrNid(entity));
                    } else if (component instanceof SemanticEntityVersion entityVersion) {
                        // Suppress display for the semantic version...
                        return new SimpleObjectProperty();
                    }
                    if (component != null) {
                        new SimpleObjectProperty<>("Can't handle: " + component);
                    }
                    return new SimpleObjectProperty<>();
                });
                column.setCellFactory(new Callback<TreeTableColumn<Component, Object>, TreeTableCell<Component, Object>>() {
                    @Override
                    public TreeTableCell<Component, Object> call(TreeTableColumn<Component, Object> param) {
                        return new SemanticTreeTableCell();
                    }
                });

                this.treeTableView.getColumns().add(column);
            }
            ImmutableList<? extends FieldDefinitionForEntity> fieldDefinitions = patternEntityVersion.fieldDefinitions();
            for (int i = 0; i < fieldDefinitions.size(); i++) {
                FieldDefinitionForEntity fieldDef = fieldDefinitions.get(i);
                String meaningText = viewCalculator().getPreferredDescriptionTextWithFallbackOrNid(fieldDef.meaningNid());
                String purposeText = viewCalculator().getPreferredDescriptionTextWithFallbackOrNid(fieldDef.purposeNid());
                TreeTableColumn<Component, Object> fieldColumn = makeColumn(meaningText, purposeText, fieldDef, i);
                this.treeTableView.getColumns().add(fieldColumn);
            }
            this.treeTableView.getColumns().add(makeColumn("Status", "Define the status of this version", StampFields.STATUS));
            this.treeTableView.getColumns().add(makeColumn("Time", "Represents the time this version was committed", StampFields.TIME));
            this.treeTableView.getColumns().add(makeColumn("Author", "Represents the author of this version", StampFields.AUTHOR));
            this.treeTableView.getColumns().add(makeColumn("Module", "Represents the module this version is part of", StampFields.MODULE));
            this.treeTableView.getColumns().add(makeColumn("Path", "Define path that this version is created on", StampFields.PATH));
            if (populate) {
                TinkExecutor.threadPool().execute(() -> {
                    AtomicInteger count = new AtomicInteger();
                    PrimitiveData.get().forEachSemanticNidOfPattern(patternEntity.nid(), semanticNid -> {
                        if (count.getAndIncrement() < 5000) {
                            SemanticEntity semanticEntity = Entity.getFast(semanticNid);
                            TreeItem semanticParent = new TreeItem(Entity.getFast(semanticEntity.referencedComponentNid()));
                            semanticParent.setExpanded(true);
                            Platform.runLater(() -> this.root.getChildren().add(semanticParent));
                            semanticEntity.versions().forEach(semanticEntityVersion -> {
                                TreeItem semanticVersionItem = new TreeItem(semanticEntityVersion);
                                Platform.runLater(() -> semanticParent.getChildren().add(semanticVersionItem));
                            });
                        }
                    });
                });
            }
        });
    }

    private void setupSemantic(SemanticEntity semanticEntity) {
        setupPattern(Entity.getFast(semanticEntity.patternNid()), false);
        TreeItem semanticParent = new TreeItem(Entity.getFast(semanticEntity.referencedComponentNid()));
        semanticParent.setExpanded(true);
        Platform.runLater(() -> this.root.getChildren().add(semanticParent));
        semanticEntity.versions().forEach(semanticEntityVersion -> {
            TreeItem semanticVersionItem = new TreeItem(semanticEntityVersion);
            Platform.runLater(() -> semanticParent.getChildren().add(semanticVersionItem));
        });
    }

    private TreeTableColumn<Component, Object> makeColumn(String meaningText, String purposeText) {
        TreeTableColumn<Component, Object> fieldColumn = new TreeTableColumn<>(meaningText);
        fieldColumn.setMinWidth(50);
        fieldColumn.setPrefWidth(150);
        fieldColumn.setMaxWidth(1000);
        Label fieldLabelWithTooltip = new Label();
        fieldLabelWithTooltip.setTooltip(new Tooltip(purposeText));
        fieldColumn.setGraphic(fieldLabelWithTooltip);
        return fieldColumn;
    }

    private TreeTableColumn<Component, Object> makeColumn(String meaningText, String purposeText, FieldDefinitionForEntity fieldDef, int fieldIndex) {
        TreeTableColumn<Component, Object> fieldColumn = makeColumn(meaningText, purposeText);
        fieldColumn.setCellValueFactory(param -> {
            Component component = param.getValue().getValue();
            if (component instanceof SemanticEntityVersion entityVersion) {
                Object obj = entityVersion.fieldValues().get(fieldIndex);
                if (obj instanceof EntityFacade entityFacade) {
                    return new SimpleObjectProperty(languageCalculator().getPreferredDescriptionTextWithFallbackOrNid(entityFacade));
                } else if (obj instanceof Instant instant) {
                    return new SimpleObjectProperty(DateTimeUtil.format(instant));
                }
                return new SimpleObjectProperty(obj.toString());
            } else if (component instanceof Entity entity) {
                // Suppress the parent for all the other columns...
                return new SimpleObjectProperty();
            }
            if (component != null) {
                return new SimpleObjectProperty<>("Can't handle: " + component);
            }
            return new SimpleObjectProperty<>();
        });
        return fieldColumn;
    }

    private TreeTableColumn<Component, Object> makeColumn(String meaningText, String purposeText, StampFields stampField) {
        TreeTableColumn<Component, Object> fieldColumn = makeColumn(meaningText, purposeText);
        fieldColumn.setCellValueFactory(param -> {
            Component component = param.getValue().getValue();
            if (component instanceof EntityVersion entityVersion) {
                switch (stampField) {
                    case STATUS:
                        return new SimpleObjectProperty(languageCalculator().getPreferredDescriptionTextWithFallbackOrNid(entityVersion.stamp().stateNid()));
                    case TIME:
                        return new SimpleObjectProperty<>(DateTimeUtil.format(entityVersion.time()));
                    case AUTHOR:
                        return new SimpleObjectProperty(languageCalculator().getPreferredDescriptionTextWithFallbackOrNid(entityVersion.stamp().authorNid()));
                    case MODULE:
                        return new SimpleObjectProperty(languageCalculator().getPreferredDescriptionTextWithFallbackOrNid(entityVersion.stamp().moduleNid()));
                    case PATH:
                        return new SimpleObjectProperty(languageCalculator().getPreferredDescriptionTextWithFallbackOrNid(entityVersion.stamp().pathNid()));
                }
            }
            // Suppress the parent for all the other columns...
            return new SimpleObjectProperty();
        });
        return fieldColumn;
    }

    @Override
    public String getDefaultTitle() {
        return TITLE;
    }

    @Override
    public void handleActivity(ImmutableList<EntityFacade> entities) {
        // Nothing to do...
    }

    @Override
    public void revertAdditionalPreferences() {

    }

    @Override
    public String getStyleId() {
        return STYLE_ID;
    }

    @Override
    protected void saveAdditionalPreferences() {

    }

    @Override
    public Node getNode() {
        return contentPane;
    }

    @Override
    public void close() {

    }

    @Override
    public boolean canClose() {
        return false;
    }

    @Override
    public Class factoryClass() {
        return TableNodeFactory.class;
    }
}