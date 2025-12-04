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
package dev.ikm.komet.framework.context;

import dev.ikm.komet.framework.activity.ActivityStream;
import dev.ikm.komet.framework.activity.ActivityStreams;
import dev.ikm.komet.framework.search.SearchControllerAndNode;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import org.controlsfx.control.PopOver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Consumer;

public class SearchForConceptActionEventHandler implements EventHandler<ActionEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(SearchForConceptActionEventHandler.class);

    final Node nodeToPopover;
    final ViewProperties viewProperties;
    final EntityFacade initialEntityFocus;
    final Consumer<Object> doubleClickConsumer;

    public SearchForConceptActionEventHandler(Node nodeToPopover, ViewProperties viewProperties,
                                              Consumer<Object> doubleClickConsumer,
                                              EntityFacade initialEntityFocus) {
        this.nodeToPopover = nodeToPopover;
        this.viewProperties = viewProperties;
        this.doubleClickConsumer = doubleClickConsumer;
        this.initialEntityFocus = initialEntityFocus;
    }

    @Override
    public void handle(ActionEvent actionEvent) {
        String seedText = "";
        if (initialEntityFocus != null) {
            seedText = viewProperties.calculator().getFullyQualifiedDescriptionTextWithFallbackOrNid(initialEntityFocus.nid());
            seedText = "+" + seedText.replace(" ", " +");
            seedText = seedText.replace("(", "");
            seedText = seedText.replace(")", "");
            seedText.strip();
        }
        PopOver popover = new PopOver(nodeToPopover);
        try {
            SearchControllerAndNode searchControllerAndNode = new SearchControllerAndNode();
            ReadOnlyObjectProperty<PublicIdStringKey<ActivityStream>> activityStreamKeyProperty = new SimpleObjectProperty<>(ActivityStreams.UNLINKED);
            searchControllerAndNode.controller().setProperties(popover.getRoot(), activityStreamKeyProperty, viewProperties, null);
            searchControllerAndNode.controller().setQueryString(seedText);
            searchControllerAndNode.controller().getDoubleCLickConsumers().add(o -> {
                doubleClickConsumer.accept(o);
                popover.hide();
            });
            popover.setContentNode(searchControllerAndNode.searchPanelPane());
            searchControllerAndNode.searchPanelPane().setMinSize(450, 400);
            searchControllerAndNode.searchPanelPane().setPrefSize(450, 400);
            searchControllerAndNode.searchPanelPane().setMaxSize(700, 1024);
            //popover.setCloseButtonEnabled(true);
            //popover.setHeaderAlwaysVisible(true);

            Object source = actionEvent.getSource();
            popover.setArrowLocation(PopOver.ArrowLocation.TOP_LEFT);
            popover.setDetachable(false);
            if (source instanceof MenuItem menuItem) {
                //popover.show(control, 10);
                //TODO relocate top or bottom based on location in window...
                Bounds controlBounds = nodeToPopover.localToScreen(nodeToPopover.getBoundsInLocal());
                popover.show(nodeToPopover, controlBounds.getMinX() + 25, controlBounds.getMaxY() - 5);
            } else {
                AlertStreams.getRoot().dispatch(AlertObject.makeError(
                        new IllegalStateException("Event is not associated with a window: " + actionEvent)));
            }
        } catch (IOException e) {
            AlertStreams.getRoot().dispatch(AlertObject.makeError(e));
        }
        actionEvent.consume();

    }
}
