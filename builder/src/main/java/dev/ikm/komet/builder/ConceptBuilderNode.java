package dev.ikm.komet.builder;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.eclipse.collections.api.list.ImmutableList;
import dev.ikm.komet.framework.ExplorationNodeAbstract;
import dev.ikm.komet.framework.performance.StatementStore;
import dev.ikm.komet.framework.performance.Topic;
import dev.ikm.komet.framework.performance.impl.RequestRecord;
import dev.ikm.komet.framework.rulebase.Consequence;
import dev.ikm.komet.framework.rulebase.GeneratedActionImmediate;
import dev.ikm.komet.framework.rulebase.GeneratedActionSuggested;
import dev.ikm.komet.framework.rulebase.RuleBase;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.terms.EntityFacade;

// TODO change to entity builder node...
public class ConceptBuilderNode extends ExplorationNodeAbstract {
    protected static final String STYLE_ID = "concept-builder-node";
    protected static final String TITLE = "Concept Builder";
    protected final BorderPane builderPane = new BorderPane();
    protected TextField conceptText = new TextField();
    protected Button requestNewConcept = new Button("request proposal");
    protected final ToolBar toolBar = new ToolBar(conceptText, requestNewConcept);

    public ConceptBuilderNode(ViewProperties viewProperties, KometPreferences nodePreferences) {
        super(viewProperties, nodePreferences);
        builderPane.setTop(toolBar);
        requestNewConcept.setOnAction(this::requestConceptProposal);
    }

    private void requestConceptProposal(ActionEvent actionEvent) {
        toolBar.getItems().clear();
        toolBar.getItems().addAll(conceptText, requestNewConcept);
        RequestRecord request = RequestRecord.make(Topic.NEW_CONCEPT_REQUEST, conceptText.getText());
        StatementStore statementStore = StatementStore.make(request);
        ImmutableList<Consequence<?>> consequences = RuleBase.execute(statementStore, viewProperties.calculator(), Coordinates.Edit.Default());
        for (Consequence consequence : consequences) {
            switch (consequence.get()) {
                case Action action
                    when    action instanceof GeneratedActionImmediate  -> {
                    Button actionButton = ActionUtils.createButton(action);
                    toolBar.getItems().add(actionButton);
                }
                case Action action
                    when    action instanceof GeneratedActionSuggested  -> {
                    Button actionButton = ActionUtils.createButton(action);
                    toolBar.getItems().add(actionButton);
                }
                default -> throw new IllegalStateException("Unexpected value: " + consequence.get());
            }
        }
    }

    @Override
    protected boolean showActivityStreamIcon() {
        return false;
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
        return builderPane;
    }

    @Override
    public void close() {

    }

    @Override
    public Class factoryClass() {
        return ConceptBuilderNodeFactory.class;
    }
}