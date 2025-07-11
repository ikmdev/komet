package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.DateFilterTitledPane;
import dev.ikm.komet.kview.controls.FilterOptions;
import dev.ikm.komet.kview.controls.FilterOptionsPopup;
import dev.ikm.komet.kview.controls.FilterTitledPane;
import dev.ikm.komet.kview.controls.IconRegion;
import dev.ikm.komet.kview.controls.SavedFiltersPopup;
import dev.ikm.komet.navigator.graph.Navigator;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.Preferences;
import dev.ikm.tinkar.coordinate.navigation.calculator.Edge;
import dev.ikm.tinkar.entity.Entity;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Skin;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Subscription;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class FilterOptionsPopupSkin implements Skin<FilterOptionsPopup> {

    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.filter-options");
    private static final String FILTER_OPTIONS_KEY = "filter-options";
    private static final String SAVED_FILTERS_KEY = "saved-filters";
    private static final String DEFAULT_OPTIONS_KEY = "default-options";

    private final FilterOptionsPopup control;
    private final VBox root;

    private final Accordion accordion;
    private final Button revertButton;
    private final Button applyButton;
    private final SavedFiltersPopup savedFiltersPopup;
    private final KometPreferences kometPreferences;

    private Subscription subscription;
    private Subscription filterSubscription;

    private final FilterOptions defaultFilterOptions = new FilterOptions();
    private final ObjectProperty<FilterOptions> currentFilterOptionsProperty = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            FilterOptions filterOptions = get();
            if (filterOptions != null) {
                applyButton.setDisable(control.getFilterOptions().equals(filterOptions));
                revertButton.setDisable(defaultFilterOptions.equals(filterOptions));
            }
        }
    };

    public FilterOptionsPopupSkin(FilterOptionsPopup control) {
        this.control = control;
        savedFiltersPopup = new SavedFiltersPopup(this::applyFilter, this::removeFilter);
        kometPreferences = Preferences.get().getConfigurationPreferences()
                .node(FILTER_OPTIONS_KEY)
                .node(control.getFilterType().name());

        StackPane closePane = new StackPane(new IconRegion("icon", "close"));
        closePane.getStyleClass().add("region");
        closePane.setOnMouseClicked(_ -> getSkinnable().hide());

        Label title = new Label(resources.getString("control.title"));
        title.getStyleClass().add("title");

        ToggleButton filterPane = new ToggleButton(null, new IconRegion("icon", "filter"));
        filterPane.getStyleClass().add("filter-button");

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
            // copy options from titledPanes into control
            control.setFilterOptions(currentFilterOptionsProperty.get());
        });
        StackPane region = new StackPane(new IconRegion("icon", "filter"));
        region.getStyleClass().add("region");

        Button saveButton = new Button(resources.getString("button.save"), region);
        saveButton.setOnAction(_ -> {
            // close all titled panes to update the current filter options
            accordion.setExpandedPane(null);
            List<String> savedFilters = kometPreferences.getList(SAVED_FILTERS_KEY, new ArrayList<>());
            try {
                String key = "" + (savedFilters.isEmpty() ? 1 : Integer.parseInt(savedFilters.getLast()) + 1);
                byte[] data = serialize(currentFilterOptionsProperty.get());
                kometPreferences.putByteArray(key, data);
                savedFilters.add(key);
                kometPreferences.putList(SAVED_FILTERS_KEY, savedFilters);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        revertButton = new Button(resources.getString("button.revert"));
        revertButton.setOnAction(_ -> revertFilterOptions());

        VBox bottomBox = new VBox(applyButton, saveButton, revertButton);
        bottomBox.getStyleClass().add("bottom-box");

        root = new VBox(headerBox, scrollPane, spacer, bottomBox);
        root.getStyleClass().add("filter-options-popup");
        root.getStylesheets().add(FilterOptionsPopup.class.getResource("filter-options-popup.css").toExternalForm());

        subscription = control.filterOptionsProperty().subscribe(this::setupFilter);
        subscription = subscription.and(control.navigatorProperty().subscribe(this::setOptionsFromNavigator));

        subscription = subscription.and(savedFiltersPopup.showingProperty().subscribe((_, showing) -> {
            if (!showing) {
                filterPane.setSelected(false);
            }
        }));
        subscription = subscription.and(filterPane.selectedProperty().subscribe((_, selected) -> {
            if (selected) {
                updateSavedFilterList();
                Bounds bounds = control.getStyleableNode().localToScreen(control.getStyleableNode().getLayoutBounds());
                savedFiltersPopup.show(control.getScene().getWindow(), bounds.getMaxX(), bounds.getMinY());
            } else if (savedFiltersPopup.isShowing()) {
                savedFiltersPopup.hide();
            }
        }));
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
                    filterSubscription = filterSubscription.and(pane.optionProperty().subscribe((_, _) ->
                            updateCurrentFilterOptions()));
                });

        // pass filter options to titledPane controls
        accordion.getPanes().stream()
                .filter(FilterTitledPane.class::isInstance)
                .map(FilterTitledPane.class::cast)
                .forEach(pane -> {
                    FilterOptions.Option optionForItem = filterOptions.getOptionForItem(pane.getOption().item());
                    if (optionForItem.availableOptions().isEmpty()) {
                        optionForItem.availableOptions().addAll(pane.getOption().availableOptions());
                    }
                    pane.setOption(optionForItem);
                });
        updateCurrentFilterOptions();
        control.getProperties().put(DEFAULT_OPTIONS_KEY, defaultFilterOptions.equals(control.getFilterOptions()));
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

    private void revertFilterOptions() {
        accordion.setExpandedPane(null);
        control.setFilterOptions(null);
        setOptionsFromNavigator(control.getNavigator());
        updateCurrentFilterOptions();
    }

    private void updateCurrentFilterOptions() {
        FilterOptions currentFilterOptions = new FilterOptions();
        accordion.getPanes().stream()
                .filter(FilterTitledPane.class::isInstance)
                .map(FilterTitledPane.class::cast)
                .forEach(pane -> {
                    FilterOptions.Option optionForItem = pane.getOption();
                    currentFilterOptions.setOptionForItem(pane.getOption().item(), optionForItem);
                });
        currentFilterOptionsProperty.set(currentFilterOptions);
    }

    private void setOptionsFromNavigator(Navigator navigator) {
        FilterOptions filterOptions = new FilterOptions();
        if (navigator == null || navigator.getRootNids() == null || navigator.getRootNids().length == 0) {
            return;
        }
        int rootNid = navigator.getRootNids()[0];

        FilterOptions.Option option = filterOptions.getType();
        FilterTitledPane typeFilterTitledPane = setupTitledPane(option);
        if (control.getFilterType() == FilterOptionsPopup.FILTER_TYPE.SEARCH) {
            setDefaultOptions(option);
        }

        // header:  All first children of root
        List<String> headerList = navigator.getChildEdges(rootNid).stream()
                .map(edge -> Entity.getFast(edge.destinationNid()).description())
                .toList();
        option = filterOptions.getHeader();
        setAvailableOptions(option, headerList);
        FilterTitledPane headerFilterTitledPane = setupTitledPane(option);
        if (control.getFilterType() == FilterOptionsPopup.FILTER_TYPE.NAVIGATOR) {
            setDefaultOptions(option);
        }

        // status: all descendents of Status
        option = filterOptions.getStatus();
        setAvailableOptions(option, getDescendentsList(navigator, rootNid, FilterOptions.OPTION_ITEM.STATUS.getPath()));
        FilterTitledPane statusFilterTitledPane = setupTitledPane(option);
        setDefaultOptions(option);

        // module: all descendents of Module
        option = filterOptions.getModule();
        setAvailableOptions(option, getDescendentsList(navigator, rootNid, FilterOptions.OPTION_ITEM.MODULE.getPath()));
        FilterTitledPane moduleFilterTitledPane = setupTitledPane(option);
        setDefaultOptions(option);

        // path: all descendents of Path
        option = filterOptions.getPath();
        setAvailableOptions(option, getDescendentsList(navigator, rootNid, FilterOptions.OPTION_ITEM.PATH.getPath()));
        FilterTitledPane pathFilterTitledPane = setupTitledPane(option);
        setDefaultOptions(option);

        // language: all descendents of Model concept->Tinkar Model concept->Language
        option = filterOptions.getLanguage();
        setAvailableOptions(option, getDescendentsList(navigator, rootNid, FilterOptions.OPTION_ITEM.LANGUAGE.getPath()));
        FilterTitledPane languageFilterTitledPane = setupTitledPane(option);
        setDefaultOptions(option);

        option = filterOptions.getDescription();
        FilterTitledPane descriptionFilterTitledPane = setupTitledPane(option);
        setDefaultOptions(option);

        option = filterOptions.getKindOf();
        FilterTitledPane kindOfFilterTitledPane = setupTitledPane(option);
        setDefaultOptions(option);

        option = filterOptions.getMembership();
        FilterTitledPane membershipFilterTitledPane = setupTitledPane(option);
        setDefaultOptions(option);

        option = filterOptions.getSortBy();
        FilterTitledPane sortByFilterTitledPane = setupTitledPane(option);
        if (control.getFilterType() == FilterOptionsPopup.FILTER_TYPE.SEARCH) {
            setDefaultOptions(option);
        }

        option = filterOptions.getDate();
        FilterTitledPane dateFilterTitledPane = setupTitledPane(option);
        defaultFilterOptions.getOptionForItem(option.item()).selectedOptions().clear(); // latest has no selected options

        if (control.getFilterType() == FilterOptionsPopup.FILTER_TYPE.NAVIGATOR) {
            accordion.getPanes().setAll(
                    headerFilterTitledPane,
                    statusFilterTitledPane,
                    moduleFilterTitledPane,
                    pathFilterTitledPane,
                    languageFilterTitledPane,
                    descriptionFilterTitledPane,
                    kindOfFilterTitledPane,
                    membershipFilterTitledPane,
                    dateFilterTitledPane);
        } else {
            accordion.getPanes().setAll(
                    typeFilterTitledPane,
                    statusFilterTitledPane,
                    moduleFilterTitledPane,
                    pathFilterTitledPane,
                    languageFilterTitledPane,
                    descriptionFilterTitledPane,
                    kindOfFilterTitledPane,
                    membershipFilterTitledPane,
                    sortByFilterTitledPane,
                    dateFilterTitledPane);
        }

        // initially, set default options
        control.setFilterOptions(defaultFilterOptions);
        updateCurrentFilterOptions();
    }

    private FilterTitledPane setupTitledPane(FilterOptions.Option option) {
        FilterTitledPane titledPane = option.item() == FilterOptions.OPTION_ITEM.DATE ?
                new DateFilterTitledPane() : new FilterTitledPane();
        titledPane.setTitle(option.title());
        titledPane.setOption(option);
        titledPane.setExpanded(false);
        return titledPane;
    }

    private void setAvailableOptions(FilterOptions.Option option, List<String> options) {
        option.availableOptions().clear();
        option.availableOptions().addAll(options);
    }

    private void setDefaultOptions(FilterOptions.Option option) {
        defaultFilterOptions.getOptionForItem(option.item()).selectedOptions().clear();
        if (option.isMultiSelectionAllowed()) {
            defaultFilterOptions.getOptionForItem(option.item()).selectedOptions().addAll(option.availableOptions());
        } else {
            defaultFilterOptions.getOptionForItem(option.item()).selectedOptions().add(option.availableOptions().getFirst());
        }
    }

    private void applyFilter(String i) {
        kometPreferences.getByteArray(i)
                .ifPresent(data -> {
                    try {
                        revertFilterOptions();
                        FilterOptions newFilterOptions = deserialize(data);
                        control.setFilterOptions(newFilterOptions);
                    } catch (IOException | ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private void removeFilter(String i) {
        kometPreferences.remove(i);
        List<String> list = kometPreferences.getList(SAVED_FILTERS_KEY);
        list.remove(i);
        kometPreferences.putList(SAVED_FILTERS_KEY, list);
        updateCurrentFilterOptions();
    }

    private void updateSavedFilterList() {
        List<String> savedFilters = kometPreferences.getList(SAVED_FILTERS_KEY, new ArrayList<>());
        savedFiltersPopup.getSavedFiltersList().setAll(savedFilters);
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

    private static byte[] serialize(FilterOptions filterOptions) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (ObjectOutputStream outputStream = new ObjectOutputStream(out)) {
            outputStream.writeObject(filterOptions);
        }

        return out.toByteArray();
    }

    private FilterOptions deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data)) {
            return (FilterOptions) new ObjectInputStream(bis).readObject();
        }
    }
}
