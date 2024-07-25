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
package dev.ikm.komet.kview.mvvm.view.properties;

import dev.ikm.komet.framework.ExplorationNodeAbstract;
import dev.ikm.komet.framework.activity.ActivityStreamOption;
import dev.ikm.komet.framework.controls.EntityLabelWithDragAndDrop;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.flow.FlowSubscriber;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static dev.ikm.komet.kview.fxutils.CssHelper.defaultStyleSheet;

public class PropertiesNode extends ExplorationNodeAbstract {
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesNode.class);

    protected final SimpleObjectProperty<EntityFacade> entityFocusProperty = new SimpleObjectProperty<>();
    protected FlowSubscriber<Integer> invalidationSubscriber;
    protected static final String CONCEPT_PROPERTIES_VIEW_FXML_FILE = "pattern-properties.fxml";
    protected static final String STYLE_ID = "kview-properties-node";
    protected static final String TITLE = "Knowledge View Properties";
    private BorderPane propertiesViewBorderPane;
    private PropertiesController propertiesViewController;

    public PropertiesNode(ViewProperties viewProperties, KometPreferences nodePreferences) {
        super(viewProperties, nodePreferences);
        init();
        registerListeners(viewProperties);
        revertPreferences();
    }

    /**
     * Initialization view panel(fxml) and it's view.
     */
    private void init() {
        try {
            // Load Concept Properties View Panel (FXML & Controller)
            FXMLLoader loader = new FXMLLoader(getClass().getResource(CONCEPT_PROPERTIES_VIEW_FXML_FILE));
            this.propertiesViewBorderPane = loader.load();
            this.propertiesViewController = loader.getController();

            // Programmatically change CSS Theme
            String styleSheet = defaultStyleSheet();
            this.propertiesViewBorderPane.getStylesheets().add(styleSheet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Wireup listeners(handler code) that will respond on change. E.g. The entityFocusProperty changes when a user selects a concept (in a Navigator tree view).
     * @param viewProperties
     */
    private void registerListeners(ViewProperties viewProperties) {

        // When a new entity is selected populate the view. An entity has been selected upstream (activity stream)
        this.entityFocusProperty.addListener((observable, oldEntityFacade, newEntityFacade) -> {
            if (newEntityFacade != null) {
                titleProperty.set(viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(newEntityFacade));
                toolTipTextProperty.set(viewProperties.calculator().getFullyQualifiedDescriptionTextWithFallbackOrNid(newEntityFacade));

                // Also publish(dispatch) to any subscribers of this view.
                if (ActivityStreamOption.PUBLISH.keyForOption().equals(super.optionForActivityStreamKeyProperty.get()) ||
                        ActivityStreamOption.SYNCHRONIZE.keyForOption().equals(super.optionForActivityStreamKeyProperty.get())) {
                    getActivityStream().dispatch(newEntityFacade);
                }

                // Populate Detail View
                if (getPropertiesViewController() != null) {
                    getPropertiesViewController().updateModel(viewProperties, newEntityFacade);
                    getPropertiesViewController().updateView();
                }

            } else {
                // Show a blank view (nothing selected)
                titleProperty.set(EntityLabelWithDragAndDrop.EMPTY_TEXT);
                toolTipTextProperty.set(EntityLabelWithDragAndDrop.EMPTY_TEXT);
                getPropertiesViewController().clearView();
            }
        });

        // If database updates the underlying entity, this will do a force update of the UI.
        this.invalidationSubscriber = new FlowSubscriber<>(nid -> {
            if (entityFocusProperty.get() != null && entityFocusProperty.get().nid() == nid) {
                // component has changed, need to update.
                Platform.runLater(() -> entityFocusProperty.set(null));
                Platform.runLater(() -> entityFocusProperty.set(Entity.provider().getEntityFast(nid)));
            }
        });

        // Register to the Entity Service
        Entity.provider().addSubscriberWithWeakReference(this.invalidationSubscriber);

    }

    protected void revertDetailsPreferences() {

    }

    /**
     * Returns the associated view to update the UI.
     * @return PropertiesController The attached view to the Details view (fxml)
     */
    private PropertiesController getPropertiesViewController() {
        return propertiesViewController;
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
            EntityFacade entityFacade = entities.get(0);
            // Only display Concept Details.
            if (entityFacade instanceof ConceptFacade) {
                entityFocusProperty.set(entityFacade);
            } else {
                entityFocusProperty.set(null);
            }
        }
    }

    @Override
    public final void revertAdditionalPreferences() {
        if (nodePreferences.hasKey(DetailNodeKey.ENTITY_FOCUS)) {
            nodePreferences.getEntity(DetailNodeKey.ENTITY_FOCUS).ifPresentOrElse(entityFacade -> entityFocusProperty.set(entityFacade),
                    () -> entityFocusProperty.set(null));
        }
        revertDetailsPreferences();
    }

    @Override
    public String getStyleId() {
        return STYLE_ID;
    }

    @Override
    protected void saveAdditionalPreferences() {
        if (entityFocusProperty.get() != null) {
            nodePreferences.putEntity(DetailNodeKey.ENTITY_FOCUS, entityFocusProperty.get());
        } else {
            nodePreferences.remove(DetailNodeKey.ENTITY_FOCUS);
        }
        nodePreferences.putBoolean(DetailNodeKey.REQUEST_FOCUS_ON_ACTIVITY, false);
        saveDetailsPreferences();
    }

    protected void saveDetailsPreferences() {

    }

    @Override
    public Node getNode() {
        return this.propertiesViewBorderPane;
    }

    @Override
    public void close() {

    }

    @Override
    public boolean canClose() {
        return false;
    }

    @Override
    public Class<PropertiesNodeFactory> factoryClass() {
        return PropertiesNodeFactory.class;
    }
    public enum DetailNodeKey {
        ENTITY_FOCUS,
        REQUEST_FOCUS_ON_ACTIVITY
    }

}
