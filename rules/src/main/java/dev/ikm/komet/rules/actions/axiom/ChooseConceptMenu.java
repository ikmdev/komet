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
package dev.ikm.komet.rules.actions.axiom;

import dev.ikm.komet.framework.activity.ActivityStream;
import dev.ikm.komet.framework.activity.ActivityStreams;
import dev.ikm.komet.framework.context.SearchForConceptActionEventHandler;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class ChooseConceptMenu extends Menu {
    private static final Logger LOG = LoggerFactory.getLogger(ChooseConceptMenu.class);

    ViewCalculator viewCalculator;

    public ChooseConceptMenu(String s, ViewCalculator viewCalculator, Node nodeForPopover, ViewProperties viewProperties,
                             Consumer<Object> chosenConceptConsumer) {
        this(s, viewCalculator, nodeForPopover, viewProperties, chosenConceptConsumer, null);
    }

    /**
     * TODO: Not sure of value of initialEntityFocus, may consider removal.
     * @param s
     * @param viewCalculator
     * @param nodeForPopover
     * @param viewProperties
     * @param chosenConceptConsumer
     * @param initialEntityFocus
     */
    public ChooseConceptMenu(final String s, final ViewCalculator viewCalculator, final Node nodeForPopover, final ViewProperties viewProperties,
                             final Consumer<Object> chosenConceptConsumer,
                             final EntityFacade initialEntityFocus) {
        super(s);
        this.viewCalculator = viewCalculator;
        Platform.runLater(() -> {
            MenuItem searchForConceptMenuItem = new MenuItem("Search for concept");
            searchForConceptMenuItem.setOnAction(new SearchForConceptActionEventHandler(nodeForPopover, viewProperties,
                    chosenConceptConsumer, initialEntityFocus));
            this.getItems().add(searchForConceptMenuItem);

            for (ActivityStream activityStream: ActivityStreams.ACTIVITY_STREAMS()) {
                if (activityStream.getHistory().size() > 0) {
                    Menu activityStreamMenu = new Menu(activityStream.getStreamName() + " history", activityStream.getStreamIcon());
                    getItems().add(activityStreamMenu);
                    for (EntityFacade historyItem: activityStream.getHistory()) {
                        MenuItem historyMenuItem = new MenuItem(viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(historyItem));
                        historyMenuItem.setOnAction(event -> {
                            chosenConceptConsumer.accept(historyItem);
                        });
                        activityStreamMenu.getItems().add(historyMenuItem);
                    }
                }
            }
        });
    }
}
