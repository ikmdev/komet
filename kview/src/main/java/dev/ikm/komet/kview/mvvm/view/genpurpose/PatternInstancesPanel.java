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
package dev.ikm.komet.kview.mvvm.view.genpurpose;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.PatternInstanceListCell;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.util.List;

/**
 * The INSTANCES tab of the standard Pattern window's properties panel: the semantics of the
 * window's pattern, each a card with its identicon, title, last-updated date and status, under a
 * title bar with the pattern's full instance count and a search field.
 * <p>
 * The list holds the first {@value #MAX_INSTANCES} instances, read when the tab is shown (ported
 * from the classic Pattern window's Instances bumpout, IKE-Network/ike-issues#1249); the header
 * count is the full total. The search field is laid out but not yet wired to filter the list.
 */
public class PatternInstancesPanel extends BorderPane {

    /** How many instances the list reads; the rest are only counted. */
    private static final int MAX_INSTANCES = 150;

    private final Label countLabel = new Label();
    private final TextField searchField = new TextField();
    private final ListView<Long> instancesListView = new ListView<>();

    /**
     * @param viewProperties the view the instances are described and stamped through
     */
    public PatternInstancesPanel(ViewProperties viewProperties) {
        instancesListView.setCellFactory(_ -> new PatternInstanceListCell(viewProperties));
        buildView();
    }

    private void buildView() {
        Label titleLabel = new Label("Instances");
        titleLabel.getStyleClass().add("lidr-device-title-label");
        countLabel.getStyleClass().add("instances-count");
        HBox titleBar = new HBox(titleLabel, countLabel);
        titleBar.getStyleClass().add("instances-title-bar");

        searchField.setPromptText("Search instances");
        Region searchIcon = new Region();
        searchIcon.getStyleClass().add("search-icon");
        StackPane.setAlignment(searchIcon, Pos.CENTER_LEFT);
        Region filterIcon = new Region();
        filterIcon.getStyleClass().add("filter-icon");
        StackPane.setAlignment(filterIcon, Pos.CENTER_RIGHT);
        StackPane search = new StackPane(searchField, searchIcon, filterIcon);
        search.getStyleClass().add("instances-search");

        VBox header = new VBox(titleBar, search);
        header.getStyleClass().add("instances-header");

        instancesListView.getStyleClass().add("instances-list");
        instancesListView.setPrefHeight(380);

        getStyleClass().add("pattern-instances");
        setPrefWidth(500);
        setTop(header);
        setCenter(instancesListView);
    }

    /**
     * Lists the pattern's instances: the header shows how many there are in all, the list the
     * first {@value #MAX_INSTANCES} of them.
     */
    public void showInstances(EntityFacade pattern) {
        long patternNid = pattern.nid();
        int count = EntityService.get().countSemanticsOfPattern(patternNid);
        countLabel.setText(NumberFormat.getIntegerInstance().format(count));

        List<Long> instanceNids = EntityService.get().semanticsOfPattern(patternNid)
                .limit(MAX_INSTANCES)
                .map(semantic -> semantic.nid())
                .toList();
        instancesListView.getItems().setAll(instanceNids);
        instancesListView.scrollTo(0);
    }
}
