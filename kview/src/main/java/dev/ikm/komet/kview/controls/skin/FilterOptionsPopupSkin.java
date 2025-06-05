package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.DateFilterTitledPane;
import dev.ikm.komet.kview.controls.FilterOptions;
import dev.ikm.komet.kview.controls.FilterOptionsPopup;
import dev.ikm.komet.kview.controls.FilterTitledPane;
import dev.ikm.komet.kview.controls.IconRegion;
import dev.ikm.komet.navigator.graph.Navigator;
import dev.ikm.tinkar.coordinate.navigation.calculator.Edge;
import dev.ikm.tinkar.entity.Entity;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Skin;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Subscription;

import java.util.List;
import java.util.ResourceBundle;

public class FilterOptionsPopupSkin implements Skin<FilterOptionsPopup> {

    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.filter-options");

    private final FilterOptionsPopup control;
    private final VBox root;

    private final Accordion accordion;
    private final Button revertButton;
    private final Button applyButton;

    private Subscription subscription;
    private Subscription filterSubscription;

    private final FilterOptions defaultFilterOptions = new FilterOptions();
    private final ObjectProperty<FilterOptions> controlFilterOptionsProperty = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            if (get() != null) {
                applyButton.setDisable(control.getFilterOptions().equals(get()));
            }
        }
    };

    private final ObjectProperty<FilterOptions> currentFilterOptionsProperty = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            if (get() != null) {
                revertButton.setDisable(defaultFilterOptions.equals(get()));
            }
        }
    };

    public FilterOptionsPopupSkin(FilterOptionsPopup control) {
        this.control = control;

        StackPane closePane = new StackPane(new IconRegion("icon", "close"));
        closePane.getStyleClass().add("region");
        closePane.setOnMouseClicked(_ -> getSkinnable().hide());

        Label title = new Label(resources.getString("header.title"));
        title.getStyleClass().add("title");

        StackPane filterPane = new StackPane(new IconRegion("icon", "filter"));
        filterPane.getStyleClass().add("region");
        filterPane.setOnMouseClicked(_ -> {
        });

        HBox headerBox = new HBox(closePane, title, filterPane);
        headerBox.getStyleClass().add("header-box");

        accordion = new Accordion();
        ScrollPane scrollPane = new ScrollPane(accordion);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        applyButton = new Button(resources.getString("button.apply"));
        applyButton.getStyleClass().add("apply");
        applyButton.setOnAction(_ -> {
            accordion.setExpandedPane(null);
            FilterOptions currentFilterOptions = new FilterOptions();
            accordion.getPanes().stream()
                    .map(FilterTitledPane.class::cast)
                    .forEach(pane -> {
                        if (pane.getUserData() instanceof FilterOptions.Option option) {
                            FilterOptions.Option optionForItem = currentFilterOptions.getOptionForItem(option.item());
                            optionForItem.selectedOptions().setAll(pane.getSelectedOptions());
                            if (pane.isExcluding()) {
                                optionForItem.excludedOptions().setAll(pane.getExcludedOptions());
                            }
                        }
                    });
            control.setFilterOptions(currentFilterOptions);
            updateInstantFilterOptions();
        });
        StackPane region = new StackPane(new IconRegion("icon", "filter"));
        region.getStyleClass().add("region");

        Button saveButton = new Button(resources.getString("button.save"), region);
        saveButton.setOnAction(_ -> System.out.println("Save"));

        revertButton = new Button(resources.getString("button.revert"));
        revertButton.setOnAction(_ -> {
            accordion.setExpandedPane(null);
            control.setFilterOptions(null);
            setOptionsFromNavigator(control.getNavigator());
            updateInstantFilterOptions();
        });

        VBox bottomBox = new VBox(applyButton, saveButton, revertButton);
        bottomBox.getStyleClass().add("bottom-box");

        root = new VBox(headerBox, scrollPane, spacer, bottomBox);
        root.getStyleClass().add("filter-options-popup");
        root.getStylesheets().add(FilterOptionsPopup.class.getResource("filter-options-popup.css").toExternalForm());

        subscription = control.filterOptionsProperty().subscribe(this::setupFilter);
        subscription = subscription.and(control.navigatorProperty().subscribe(this::setOptionsFromNavigator));
    }

    private void setupFilter(FilterOptions filterOptions) {
        if (filterSubscription != null) {
            filterSubscription.unsubscribe();
        }
        if (filterOptions == null) {
            return;
        }
        // changes from titledPane control:
        filterSubscription = Subscription.EMPTY;
        accordion.getPanes().stream()
                .filter(FilterTitledPane.class::isInstance)
                .map(FilterTitledPane.class::cast)
                .forEach(pane -> {
                    filterSubscription = filterSubscription.and(pane.optionProperty().subscribe((o, s) -> {
                        if (s != null && pane.getUserData() instanceof FilterOptions.Option option) {
                            option.selectedOptions().setAll(pane.getSelectedOptions());
                            if (pane.isExcluding()) {
                                option.excludedOptions().setAll(pane.getExcludedOptions());
                            }
                            updateInstantFilterOptions();
                        }
                    }));
                });

        // pass popup control options to titledPane controls
        accordion.getPanes().stream()
                .filter(FilterTitledPane.class::isInstance)
                .map(FilterTitledPane.class::cast)
                .forEach(pane -> {
                    if (pane.getUserData() instanceof FilterOptions.Option option) {
                        FilterOptions.Option optionForItem = filterOptions.getOptionForItem(option.item());
                        pane.getSelectedOptions().setAll(optionForItem.selectedOptions());
                        if (optionForItem.excludedOptions() != null) {
                            pane.getExcludedOptions().setAll(optionForItem.excludedOptions());
                        }
                    }
                });
    }

    @Override
    public FilterOptionsPopup getSkinnable() {
        return control;
    }

    @Override
    public Node getNode() {
        return root;
    }

    @Override
    public void dispose() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
        if (filterSubscription != null) {
            filterSubscription.unsubscribe();
        }
    }

    private void updateInstantFilterOptions() {
        FilterOptions currentFilterOptions = new FilterOptions();
        accordion.getPanes().stream()
                .filter(FilterTitledPane.class::isInstance)
                .map(FilterTitledPane.class::cast)
                .forEach(pane -> {
                    if (pane.getUserData() instanceof FilterOptions.Option option) {
                        FilterOptions.Option optionForItem = currentFilterOptions.getOptionForItem(option.item());
                        optionForItem.selectedOptions().setAll(pane.getSelectedOptions());
                        if (pane.isExcluding()) {
                            optionForItem.excludedOptions().setAll(pane.getExcludedOptions());
                        }
                    }
                });
        currentFilterOptionsProperty.set(currentFilterOptions);
        controlFilterOptionsProperty.set(currentFilterOptions);
    }

    private void setOptionsFromNavigator(Navigator navigator) {
        FilterOptions filterOptions = new FilterOptions();
        if (navigator == null || navigator.getRootNids() == null || navigator.getRootNids().length == 0) {
            return;
        }
        int rootNid = navigator.getRootNids()[0];

        // sort by:  All first children of root
        List<String> sortByList = navigator.getChildEdges(rootNid).stream()
                .map(edge -> Entity.getFast(edge.destinationNid()).description())
                .toList();
        FilterOptions.Option option = filterOptions.getSortBy();
        option.availableOptions().setAll(sortByList);
        FilterTitledPane sortByFilterTitledPane = setupTitledPane(option);
        defaultFilterOptions.getOptionForItem(option.item()).selectedOptions().setAll(option.availableOptions());

        // status: all descendents of Status
        option = filterOptions.getStatus();
        option.availableOptions().setAll(getDescendentsList(navigator, rootNid, FilterOptions.OPTION_ITEM.STATUS.getPath()));
        FilterTitledPane statusFilterTitledPane = setupTitledPane(option);
        defaultFilterOptions.getOptionForItem(option.item()).selectedOptions().setAll(option.availableOptions());

        // module: all descendents of Module
        option = filterOptions.getModule();
        option.availableOptions().setAll(getDescendentsList(navigator, rootNid, FilterOptions.OPTION_ITEM.MODULE.getPath()));
        FilterTitledPane moduleFilterTitledPane = setupTitledPane(option);
        defaultFilterOptions.getOptionForItem(option.item()).selectedOptions().setAll(option.availableOptions());

        // path: all descendents of Path
        option = filterOptions.getPath();
        option.availableOptions().setAll(getDescendentsList(navigator, rootNid, FilterOptions.OPTION_ITEM.PATH.getPath()));
        FilterTitledPane pathFilterTitledPane = setupTitledPane(option);
        defaultFilterOptions.getOptionForItem(option.item()).selectedOptions().setAll(option.availableOptions());

        // language: all descendents of Model concept->Tinkar Model concept->Language
        option = filterOptions.getLanguage();
        option.availableOptions().setAll(getDescendentsList(navigator, rootNid, FilterOptions.OPTION_ITEM.LANGUAGE.getPath()));
        FilterTitledPane languageFilterTitledPane = setupTitledPane(option);
        defaultFilterOptions.getOptionForItem(option.item()).selectedOptions().setAll(option.availableOptions());

        option = filterOptions.getDescription();
        FilterTitledPane descriptionFilterTitledPane = setupTitledPane(option);
        defaultFilterOptions.getOptionForItem(option.item()).selectedOptions().setAll(option.availableOptions());

        option = filterOptions.getKindOf();
        FilterTitledPane kindOfFilterTitledPane = setupTitledPane(option);
        defaultFilterOptions.getOptionForItem(option.item()).selectedOptions().setAll(option.availableOptions());

        option = filterOptions.getMembership();
        FilterTitledPane membershipFilterTitledPane = setupTitledPane(option);
        defaultFilterOptions.getOptionForItem(option.item()).selectedOptions().setAll(option.availableOptions());

        DateFilterTitledPane dateFilterTitledPane = new DateFilterTitledPane();
        dateFilterTitledPane.setTitle(resources.getString("date.title"));

        accordion.getPanes().setAll(
                sortByFilterTitledPane,
                statusFilterTitledPane,
                moduleFilterTitledPane,
                pathFilterTitledPane,
                languageFilterTitledPane,
                descriptionFilterTitledPane,
                kindOfFilterTitledPane,
                membershipFilterTitledPane,
                dateFilterTitledPane);

        // set default options
        control.setFilterOptions(defaultFilterOptions);
    }

    private FilterTitledPane setupTitledPane(FilterOptions.Option option) {
        FilterTitledPane titledPane = new FilterTitledPane();
        titledPane.setUserData(option);
        titledPane.setTitle(option.title());
        titledPane.setDefaultOption(option.defaultOption());
        titledPane.setMultiSelect(option.isMultiSelectionAllowed());
        titledPane.getAvailableOptions().setAll(option.availableOptions());
        titledPane.getSelectedOptions().setAll(option.selectedOptions());
        if (option.excludedOptions() != null) {
            titledPane.getExcludedOptions().setAll(option.excludedOptions());
            titledPane.setExcluding(true);
        } else {
            titledPane.getExcludedOptions().clear();
            titledPane.setExcluding(false);
        }
        titledPane.setExpanded(false);
        return titledPane;
    }

    private static int findNidForDescription(Navigator navigator, int nid, String description) {
        return navigator.getChildEdges(nid).stream()
                .filter(edge -> Entity.getFast(edge.destinationNid()).description().equals(description))
                .findFirst()
                .map(Edge::destinationNid)
                .orElseThrow();
    }

    private static List<String> getDescendentsList(Navigator navigator, int parentNid, String description) {
        int nid = parentNid;
        for (String s : description.split(", ")) {
            nid = findNidForDescription(navigator, nid, s);
        }
        return navigator.getViewCalculator().descendentsOf(nid).intStream().boxed()
                .map(i -> Entity.getFast(i).description())
                .sorted()
                .toList();
    }
}
