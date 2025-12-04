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
package dev.ikm.komet.details;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import org.eclipse.collections.api.list.ImmutableList;
import dev.ikm.komet.framework.DetailNodeAbstract;
import dev.ikm.komet.framework.KometNode;
import dev.ikm.komet.framework.TopPanelFactory;
import dev.ikm.komet.framework.panel.ComponentPanel;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.EntityFacade;

public class DetailsNode extends DetailNodeAbstract {
    protected static final String STYLE_ID = "concept-details-node";
    protected static final String TITLE = "Details Viewer";
    private final BorderPane detailsPane = new BorderPane();
    private final ComponentPanel componentPanel;

    public DetailsNode(ViewProperties viewProperties, KometPreferences nodePreferences) {
        super(viewProperties, nodePreferences);
        this.componentPanel = new ComponentPanel(entityFocusProperty, viewProperties);
        Entity.provider().addSubscriberWithWeakReference(this.invalidationSubscriber);

        this.viewProperties.nodeView().addListener((observable, oldValue, newValue) -> {
            setupTopPanel(viewProperties);
            this.componentPanel.changed(entityFocusProperty, null, entityFocusProperty.getValue());
        });

        Platform.runLater(() -> {
            setupTopPanel(viewProperties);
        });
    }

    private void setupTopPanel(ViewProperties viewProperties) {
        this.detailsPane.setCenter(this.componentPanel.getComponentDetailPane());
        Node topPanel = TopPanelFactory.make(viewProperties, entityFocusProperty,
                activityStreamKeyProperty, optionForActivityStreamKeyProperty, true);
        this.detailsPane.setTop(topPanel);
    }

    protected static void addDefaultNodePreferences(KometPreferences nodePreferences) {
        nodePreferences.put(PreferenceKey.TEST, "test");
        nodePreferences.get(KometNode.PreferenceKey.PARENT_ALERT_STREAM_KEY);
    }

    @Override
    public String getDefaultTitle() {
        return TITLE;
    }

    @Override
    public void handleActivity(ImmutableList<EntityFacade> entities) {
        if (entities.isEmpty()) {
            entityFocusProperty.set(null);
        } else {
            entityFocusProperty.set(entities.get(0));
        }
    }

    @Override
    public String getStyleId() {
        return STYLE_ID;
    }


    @Override
    public Node getNode() {
        return this.detailsPane;
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
        return DetailsNodeFactory.class;
    }

    @Override
    protected void saveDetailsPreferences() {

    }

    @Override
    protected void revertDetailsPreferences() {

    }

    enum PreferenceKey {
        TEST
    }
}