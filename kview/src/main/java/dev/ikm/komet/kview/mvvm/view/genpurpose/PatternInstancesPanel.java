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

import dev.ikm.komet.kview.controls.AutoCompleteTextField;
import dev.ikm.komet.kview.controls.KLComponentControlFactory;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.PatternInstanceListCell;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * The INSTANCES tab of the standard Pattern window's properties panel: the semantics of the
 * window's pattern, each a card with its identicon, title, last-updated date and status, under a
 * title bar with the pattern's full instance count and a search field.
 * <p>
 * The list holds the first {@value #MAX_INSTANCES} instances, read when the tab is shown (ported
 * from the classic Pattern window's Instances bumpout, IKE-Network/ike-issues#1249); the header
 * count is the full total. The search field is the inline (type-ahead) search of the KL component
 * controls: typing suggests components by name, and choosing one narrows the list to the
 * instances that reference it — again the first {@value #MAX_INSTANCES}. Clearing the field
 * lists all instances again.
 */
public class PatternInstancesPanel extends BorderPane {

    /** How many instances the list reads; the rest are only counted. */
    private static final int MAX_INSTANCES = 150;

    private final Label countLabel = new Label();
    private final AutoCompleteTextField<EntityProxy> searchField;
    private final ListView<Long> instancesListView = new ListView<>();
    private final Label placeholder = new Label();

    /** The window's pattern; set on the FX thread, read by the search field's completer off it. */
    private volatile EntityFacade pattern;

    private Consumer<EntityFacade> onInstanceOpened = _ -> { };

    /**
     * @param viewCalculator the view the instances are described and stamped through, and the
     *                       search runs through
     */
    public PatternInstancesPanel(ViewCalculator viewCalculator) {
        searchField = KLComponentControlFactory.createInlineSearchField(viewCalculator);
        // The component controls' completer suggests any component whose name matches; here a
        // suggestion is only worth picking if the window's pattern has an instance of it, so the
        // others are dropped. Runs on the field's own search thread, like the completer it wraps.
        Function<String, List<EntityProxy>> componentCompleter = searchField.getCompleter();
        searchField.setCompleter(text -> componentCompleter.apply(text).stream()
                .filter(this::hasInstanceReferencing)
                .toList());
        instancesListView.setCellFactory(_ -> new PatternInstanceListCell(viewCalculator, this::openInstance));
        buildView();

        // A chosen component narrows the list; clearing the text widens it again (the field keeps
        // its last value, so the text decides whether the value still applies).
        searchField.valueProperty().subscribe(() -> loadInstances());
        searchField.textProperty().subscribe(text -> {
            if (text.isEmpty()) {
                loadInstances();
            }
        });
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

        placeholder.getStyleClass().add("instances-placeholder");
        instancesListView.setPlaceholder(placeholder);
        instancesListView.getStyleClass().add("instances-list");
        instancesListView.setPrefHeight(380);

        getStyleClass().add("pattern-instances");
        setPrefWidth(500);
        setTop(header);
        setCenter(instancesListView);
    }

    /**
     * Sets what opens an instance the user double-clicks: the window puts it in a semantic window.
     */
    public void setOnInstanceOpened(Consumer<EntityFacade> onInstanceOpened) {
        this.onInstanceOpened = onInstanceOpened;
    }

    /**
     * Lists the pattern's instances: the header shows how many there are in all, the list the
     * first {@value #MAX_INSTANCES} of them that reference the component chosen in the search field.
     */
    public void showInstances(EntityFacade pattern) {
        this.pattern = pattern;
        int count = EntityService.get().countSemanticsOfPattern(pattern.nid());
        countLabel.setText(NumberFormat.getIntegerInstance().format(count));
        loadInstances();
    }

    /**
     * The component the list is narrowed to: the one chosen in the search field, as long as its
     * name is still what the field shows; null lists every instance.
     */
    private EntityProxy getSearchedComponent() {
        return searchField.getText().isEmpty() ? null : searchField.getValue();
    }

    private void loadInstances() {
        if (pattern == null) {
            return;
        }
        EntityProxy component = getSearchedComponent();
        placeholder.setText(component == null ? "No instances" : "No instances match");
        Stream<SemanticEntity<SemanticEntityVersion>> instances = component == null
                ? EntityService.get().semanticsOfPattern(pattern.nid())
                : EntityService.get().semanticsForComponentOfPattern(component.nid(), pattern.nid());
        List<Long> instanceNids = instances
                .limit(MAX_INSTANCES)
                .map(semantic -> semantic.nid())
                .toList();
        instancesListView.getItems().setAll(instanceNids);
        instancesListView.scrollTo(0);
    }

    private void openInstance(EntityFacade instance) {
        onInstanceOpened.accept(instance);
    }

    /** Whether at least one instance of the window's pattern references the component. */
    private boolean hasInstanceReferencing(EntityProxy component) {
        return EntityService.get().semanticsForComponentOfPattern(component.nid(), pattern.nid())
                .findAny()
                .isPresent();
    }
}
