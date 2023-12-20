/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.builder;

import dev.ikm.komet.framework.ExplorationNodeAbstract;
import dev.ikm.komet.framework.performance.Topic;
import dev.ikm.komet.framework.performance.impl.RequestRecord;
import dev.ikm.komet.framework.rulebase.Consequence;
import dev.ikm.komet.framework.rulebase.GeneratedActionImmediate;
import dev.ikm.komet.framework.rulebase.GeneratedActionSuggested;
import dev.ikm.komet.framework.rulebase.RuleService;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

/**
 * an implementation of a KometNode for creating patterns
 * this integrates into the UI
 */
public class PatternBuilderNode extends ExplorationNodeAbstract {

    protected static final String STYLE_ID = "pattern-builder-node";
    protected static final String TITLE = "Pattern Builder";

    protected final BorderPane builderPane = new BorderPane();
    protected TextField patternText = new TextField();
    protected Button requestNewPattern = new Button("request proposal");

    protected final ToolBar toolBar = new ToolBar(patternText, requestNewPattern);

    /**
     * construct the PatternBuilderNode
     * @param viewProperties view
     * @param nodePreferences
     */
    public PatternBuilderNode(ViewProperties viewProperties, KometPreferences nodePreferences) {
        super(viewProperties, nodePreferences);
        builderPane.setTop(toolBar);
        requestNewPattern.setOnAction(this::requestPatternProposal);
    }

    private void requestPatternProposal(ActionEvent actionEvent) {
        // integrate into the UI
        toolBar.getItems().clear();
        toolBar.getItems().addAll(patternText, requestNewPattern);
        // create the request to pass to the rules engine
        RequestRecord request = RequestRecord.make(Topic.NEW_PATTERN_REQUEST, patternText.getText());
        ImmutableList<Consequence<?>> consequences =
                RuleService.get().execute("Knowledge Base Name",
                        Lists.immutable.of(request),
                        viewProperties,
                        Coordinates.Edit.Default());
        // display buttons in the UI to add the pattern request
        for (Consequence consequence : consequences) {
            switch (consequence.get()) {
                case Action action
                        when    action instanceof GeneratedActionImmediate -> {
                    Button actionButton = ActionUtils.createButton(action);
                    toolBar.getItems().add(actionButton);
                }
                case Action action
                        when    action instanceof GeneratedActionSuggested -> {
                    Button actionButton = ActionUtils.createButton(action);
                    toolBar.getItems().add(actionButton);
                }
                default -> throw new IllegalStateException("Unexpected value: " + consequence.get());
            }
        }
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
    public void revertAdditionalPreferences() { }

    @Override
    public String getStyleId() {
        return STYLE_ID;
    }


    @Override
    protected void saveAdditionalPreferences() { }

    @Override
    public Node getNode() {
        return builderPane;
    }

    @Override
    public Class factoryClass() {
        return PatternBuilderNodeFactory.class;
    }

    @Override
    public void close() {  }
}
