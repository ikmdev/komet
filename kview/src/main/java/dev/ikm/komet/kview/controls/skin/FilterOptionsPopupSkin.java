package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.framework.temp.FxGet;
import dev.ikm.komet.kview.controls.DateFilterTitledPane;
import dev.ikm.komet.kview.controls.FilterOptions;
import dev.ikm.komet.kview.controls.FilterOptionsPopup;
import dev.ikm.komet.kview.controls.FilterOptionsUtils;
import dev.ikm.komet.kview.controls.FilterTitledPane;
import dev.ikm.komet.kview.controls.IconRegion;
import dev.ikm.komet.kview.controls.LangFilterTitledPane;
import dev.ikm.komet.kview.controls.SavedFiltersPopup;
import dev.ikm.komet.navigator.graph.Navigator;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.Preferences;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.State;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Skin;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class FilterOptionsPopupSkin implements Skin<FilterOptionsPopup> {

    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.filter-options");
    private static final String FILTER_OPTIONS_KEY = "filter-options";
    private static final String SAVED_FILTERS_KEY = "saved-filters";
    private static final String DEFAULT_OPTIONS_KEY = "default-options";

    private final FilterOptionsPopup control;
    private final VBox root;

    private final AccordionBox accordionBox;
    private final ScrollPane scrollPane;
    private final Button revertButton;
    private final SavedFiltersPopup savedFiltersPopup;
    private final KometPreferences kometPreferences;

    private Subscription subscription;
    private Subscription filterSubscription;
    private boolean updating, skipUpdateFilterOptions;

    private FilterOptions defaultFilterOptions;
    private final ObjectProperty<FilterOptions> currentFilterOptionsProperty = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            FilterOptions filterOptions = get();
            if (filterOptions != null) {
                boolean isDefault = defaultFilterOptions.equals(filterOptions);
                if (!updating) {
                    control.setFilterOptions(filterOptions);
                    control.getProperties().put(DEFAULT_OPTIONS_KEY, isDefault);
                }
                revertButton.setDisable(isDefault);

                List<FilterOptions.LanguageFilterCoordinates> languageCoordinatesList = filterOptions.getLanguageCoordinatesList();
                boolean disable = languageCoordinatesList.stream()
                        .anyMatch(l -> l.getLanguage().selectedOptions().isEmpty()) ||
                        languageCoordinatesList.size() == FxGet.allowedLanguages().size();
                accordionBox.disableAddButton(disable);

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

        accordionBox = new AccordionBox();
        scrollPane = new ScrollPane(accordionBox);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        StackPane region = new StackPane(new IconRegion("icon", "filter"));
        region.getStyleClass().add("region");

        Button saveButton = new Button(resources.getString("button.save"), region);
        //TODO: Disabling “Save to List” button in NextGen Search Filter Menu for now
        // since this functionality has not been designed yet.
        saveButton.setDisable(true);
        saveButton.setOnAction(_ -> {
            // close all titled panes to update the current filter options
            accordionBox.setExpandedPane(null);
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
        revertButton.getStyleClass().add("revert-button");

        VBox bottomBox = new VBox(saveButton, revertButton);
        bottomBox.getStyleClass().add("bottom-box");

        root = new VBox(headerBox, scrollPane, spacer, bottomBox);
        root.getStyleClass().add("filter-options-popup");
        root.getStylesheets().add(FilterOptionsPopup.class.getResource("filter-options-popup.css").toExternalForm());

        // create panes, without any filter option yet
        createAccordionBoxPanes();

        subscription = control.filterOptionsProperty().subscribe(this::setupFilter);
        subscription = subscription.and(control.navigatorProperty().subscribe(this::setupDefaultFilterOptions));

        subscription = subscription.and(savedFiltersPopup.showingProperty().subscribe((_, showing) -> {
            if (!showing) {
                filterPane.setSelected(false);
            }
        }));
        subscription = subscription.and(control.getInheritedFilterOptions().observableViewForFilterProperty().subscribe((_, _) -> {
            if (control.getNavigator() != null) {
                // parentView -> inheritedF.O. -> refresh default
                setupDefaultFilterOptions(control.getNavigator());
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

        control.setOnShown(_ -> scrollPane.setVvalue(scrollPane.getVmin()));
    }

    private void setupFilter(FilterOptions filterOptions) {
        if (filterSubscription != null) {
            filterSubscription.unsubscribe();
        }
        if (filterOptions == null) {
            return;
        }
        boolean isDefault = control.getFilterOptions() != null && filterOptions.equals(defaultFilterOptions);

        updating = true;
        filterSubscription = Subscription.EMPTY;

        // changes from titledPane controls:
        accordionBox.updateMainPanes(pane ->
            filterSubscription = filterSubscription.and(pane.optionProperty().subscribe((_, _) ->
                    updateCurrentFilterOptions())));
        accordionBox.updateLangPanes(pane ->
            filterSubscription = filterSubscription.and(pane.langCoordinatesProperty().subscribe((_, _) ->
                    updateCurrentFilterOptions())));

        // pass filter options to titledPane controls
        skipUpdateFilterOptions = true;
        accordionBox.updateMainPanes(pane -> {
                FilterOptions.Option optionForItem = filterOptions.getOptionForItem(pane.getOption().item());
                if (optionForItem.availableOptions().isEmpty()) {
                    optionForItem.availableOptions().addAll(pane.getOption().availableOptions());
                }
                if (isDefault && control.getFilterOptions().getOptionForItem(pane.getOption().item()).isInOverride()) {
                    // when passing default filter options, if the option is inOverride mode,
                    // keep the one in the control, don't set the default one
                    pane.setOption(control.getFilterOptions().getOptionForItem(pane.getOption().item()).copy());
                } else {
                    pane.setOption(optionForItem.copy());
                }
            });
        accordionBox.updateLangPanes(pane -> {
                int ordinal = pane.getOrdinal();
                if (ordinal < filterOptions.getLanguageCoordinatesList().size()) {
                    FilterOptions.LanguageFilterCoordinates languageCoordinates = filterOptions.getLanguageCoordinates(ordinal);
                    for (int i = 0; i < languageCoordinates.getOptions().size(); i++) {
                        FilterOptions.Option<EntityFacade> option = languageCoordinates.getOptions().get(i);
                        if (option.availableOptions().isEmpty()) {
                            option.availableOptions().addAll(pane.getLangCoordinates().getOptions().get(i).availableOptions());
                        }
                    }
                    // update excluded languages
                    List<EntityFacade> list = filterOptions.getLanguageCoordinatesList().stream()
                            .filter(l -> l.getOrdinal() != ordinal && !l.getLanguage().selectedOptions().isEmpty())
                            .map(l -> l.getLanguage().selectedOptions().getFirst())
                            .filter(Objects::nonNull)
                            .toList();
                    languageCoordinates.getLanguage().excludedOptions().clear();
                    languageCoordinates.getLanguage().excludedOptions().addAll(list);
                    pane.setLangCoordinates(languageCoordinates.copy());
                }
            });
        skipUpdateFilterOptions = false;
        updateCurrentFilterOptions();
        updating = false;
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
        accordionBox.setExpandedPane(null);
        accordionBox.getLangAccordion().getPanes().removeIf(t -> t instanceof LangFilterTitledPane langFilterTitledPane
                && langFilterTitledPane.getOrdinal() > 0);
        accordionBox.disableAddButton(FxGet.allowedLanguages().size() < 2);
        updating = true;
        currentFilterOptionsProperty.set(null);
        setupFilter(null);
        control.setFilterOptions(null);
        setupFilter(defaultFilterOptions);
        updating = false;
        updateCurrentFilterOptions();
        currentFilterOptionsProperty.set(defaultFilterOptions.copy());
    }

    private <T> void updateCurrentFilterOptions() {
        if (skipUpdateFilterOptions) {
            return;
        }
        FilterOptions currentFilterOptions = new FilterOptions(getSkinnable().getParentViewCoordinate());
        accordionBox.updateMainPanes(pane -> {
            FilterOptions.Option<T> optionForItem = pane.getOption();
            currentFilterOptions.setOptionForItem(pane.getOption().item(), optionForItem);
        });
        accordionBox.updateLangPanes(pane -> {
            FilterOptions.LanguageFilterCoordinates languageCoordinates = pane.getLangCoordinates();
            if (pane.getOrdinal() > 0) {
                currentFilterOptions.addLanguageCoordinates();
            }
            currentFilterOptions.setLangCoordinates(pane.getOrdinal(), languageCoordinates.copy());
        });
        currentFilterOptionsProperty.set(currentFilterOptions);
    }

    private void createAccordionBoxPanes() {
        FilterOptions filterOptions = new FilterOptions(getSkinnable().getParentViewCoordinate());

        // Main Coordinates

        FilterOptions.Option<String> typeOption = filterOptions.getMainCoordinates().getType();
        FilterTitledPane typeFilterTitledPane = setupTitledPane(typeOption);

        FilterOptions.Option<PatternFacade> navOption = filterOptions.getMainCoordinates().getNavigator();
        FilterTitledPane navigatorFilterTitledPane = setupTitledPane(navOption);

        // status: all descendants of Status
        FilterOptions.Option<State> stateOption = filterOptions.getMainCoordinates().getStatus();
        FilterTitledPane statusFilterTitledPane = setupTitledPane(stateOption);

        FilterOptions.Option<String> timeOption = filterOptions.getMainCoordinates().getTime();
        FilterTitledPane timeFilterTitledPane = setupTitledPane(timeOption);

        // module: all descendants of Module
        FilterOptions.Option<ConceptFacade> moduleOption = filterOptions.getMainCoordinates().getModule();
        FilterTitledPane moduleFilterTitledPane = setupTitledPane(moduleOption);

        // path: all descendants of Path
        FilterOptions.Option<ConceptFacade> pathOption = filterOptions.getMainCoordinates().getPath();
        FilterTitledPane pathFilterTitledPane = setupTitledPane(pathOption);

        FilterOptions.Option<String> kindOption = filterOptions.getMainCoordinates().getKindOf();
        FilterTitledPane kindOfFilterTitledPane = setupTitledPane(kindOption);

        FilterOptions.Option<String> memberOption = filterOptions.getMainCoordinates().getMembership();
        FilterTitledPane membershipFilterTitledPane = setupTitledPane(memberOption);

        if (control.getFilterType() == FilterOptionsPopup.FILTER_TYPE.NAVIGATOR) {
            // header: All first children of root
            FilterOptions.Option<String> headerOption = filterOptions.getMainCoordinates().getHeader();
            FilterTitledPane headerFilterTitledPane = setupTitledPane(headerOption);

            accordionBox.getPanes().setAll(
                    navigatorFilterTitledPane,
                    headerFilterTitledPane,
                    statusFilterTitledPane,
                    timeFilterTitledPane,
                    moduleFilterTitledPane,
                    pathFilterTitledPane,
                    kindOfFilterTitledPane,
                    membershipFilterTitledPane);
        } else if (control.getFilterType() == FilterOptionsPopup.FILTER_TYPE.SEARCH) {
            FilterOptions.Option<String> sortOption = filterOptions.getMainCoordinates().getSortBy();
            FilterTitledPane sortByFilterTitledPane = setupTitledPane(sortOption);

            accordionBox.getPanes().setAll(
                    typeFilterTitledPane,
                    statusFilterTitledPane,
                    timeFilterTitledPane,
                    moduleFilterTitledPane,
                    pathFilterTitledPane,
                    kindOfFilterTitledPane,
                    membershipFilterTitledPane,
                    sortByFilterTitledPane);
        } else {
            accordionBox.getPanes().setAll(
                    statusFilterTitledPane,
                    timeFilterTitledPane,
                    moduleFilterTitledPane,
                    pathFilterTitledPane,
                    navigatorFilterTitledPane);
        }

        // Language Coordinates

        FilterOptions.LanguageFilterCoordinates languageCoordinates = filterOptions.getLanguageCoordinates(0);
        LangFilterTitledPane langFilterTitledPane = setupLangTitledPane(languageCoordinates);
        accordionBox.getLangAccordion().getPanes().add(langFilterTitledPane);
    }

    private void setupDefaultFilterOptions(Navigator navigator) {
        if (defaultFilterOptions == null) {
            // create default filter options
            defaultFilterOptions = new FilterOptions(getSkinnable().getParentViewCoordinate());
            // once we have navigator, update pending options with av/sel default options
            setAvailableOptionsFromNavigator(defaultFilterOptions, navigator);
        }
        // then pass the inherited options, to override av/sel default options where set
        setDefaultOptions(control.getInheritedFilterOptions());
        // pass default options to panes
        accordionBox.updateMainPanes(pane ->
                pane.setDefaultOption(defaultFilterOptions.getOptionForItem(pane.getOption().item())));
        accordionBox.updateLangPanes(pane -> {
            if (pane.getOrdinal() == 0) {
                // only primary language gets synced
                pane.setDefaultLangCoordinates(defaultFilterOptions.getLanguageCoordinates(0).copy());
            }
        });
        // finally, setup filter with default options
        setupFilter(defaultFilterOptions);
    }

    private void setDefaultOptions(FilterOptions filterOptions) {
        filterOptions.getMainCoordinates().getOptions().forEach(sourceOption ->
                setInheritedOptions(sourceOption, defaultFilterOptions.getOptionForItem(sourceOption.item())));
        filterOptions.getLanguageCoordinates(0).getOptions().forEach(sourceOption ->
                setInheritedOptions(sourceOption, defaultFilterOptions.getLangOptionForItem(0, sourceOption.item())));
    }

    private void setAvailableOptionsFromNavigator(FilterOptions options, Navigator navigator) {
        if (navigator == null || navigator.getRootNids() == null || navigator.getRootNids().length == 0) {
            return;
        }
        int rootNid = navigator.getRootNids()[0];

        if (control.getFilterType() == FilterOptionsPopup.FILTER_TYPE.NAVIGATOR) {
            // header: All first children of root
            List<String> headerList = navigator.getChildEdges(rootNid).stream()
                    .map(edge -> FilterOptionsUtils.getDescriptionTextOrNid(navigator.getViewCalculator(), edge.destinationNid()))
                    .toList();
            setAvailableOptions(options.getMainCoordinates().getHeader(), headerList);
        }

        // module: all descendants of Module
        // TODO: modules: all descendants of Module
//        List<EntityFacade> descendentsList = FilterOptionsUtils.getDescendentsList(navigator, rootNid, MODULE.getPath());
//        setAvailableOptions(options.getMainCoordinates().getModule(), descendentsList.stream().map(ConceptFacade.class::cast).toList());

        // path: all descendants of Path
//        List<EntityFacade> descendentsList = FilterOptionsUtils.getDescendentsList(navigator, rootNid, FilterOptions.OPTION_ITEM.PATH.getPath());
        List<EntityFacade> descendentsList = FxGet.pathCoordinates(navigator.getViewCalculator()).values()
                .stream().map(v -> (EntityFacade) v.pathConcept()).toList();
        setAvailableOptions(options.getMainCoordinates().getPath(), descendentsList.stream().map(ConceptFacade.class::cast).toList());

        // TODO: language: all descendants of Model concept->Tinkar Model concept->Language
//        List<EntityFacade> descendentsList = FilterOptionsUtils.getDescendentsList(navigator, rootNid, FilterOptions.OPTION_ITEM.LANGUAGE.getPath());
//        for (int i = 0; i < options.getLanguageCoordinatesList().size(); i++) {
//            setAvailableOptions(options.getLanguageCoordinates(i).getLanguage(), descendentsList.stream().map(ConceptFacade.class::cast).toList()));
//        }
    }

    private FilterTitledPane setupTitledPane(FilterOptions.Option option) {
        FilterTitledPane titledPane = option.item() == FilterOptions.OPTION_ITEM.TIME ?
                new DateFilterTitledPane() : new FilterTitledPane();
        titledPane.navigatorProperty().bind(control.navigatorProperty());
        titledPane.setTitle(option.title());
        titledPane.setOption(option);
        titledPane.setExpanded(false);
        return titledPane;
    }

    private LangFilterTitledPane setupLangTitledPane(FilterOptions.LanguageFilterCoordinates languageCoordinates) {
        LangFilterTitledPane titledPane = new LangFilterTitledPane();
        titledPane.navigatorProperty().bind(control.navigatorProperty());
        titledPane.setOrdinal(languageCoordinates.getOrdinal());
        titledPane.setTitle(resources.getString("language.coordinates.ordinal" + (languageCoordinates.getOrdinal() + 1)));
        titledPane.setLangCoordinates(languageCoordinates);
        titledPane.setExpanded(false);
        return titledPane;
    }

    private <T> void setAvailableOptions(FilterOptions.Option<T> option, List<? extends T> options) {
        option.availableOptions().clear();
        option.availableOptions().addAll(options);
        option.selectedOptions().clear();
        option.selectedOptions().addAll(option.isMultiSelectionAllowed() || options.isEmpty() ? options : List.of(options.getFirst()));
    }

    private <T> void setInheritedOptions(FilterOptions.Option<T> sourceOption, FilterOptions.Option<T> targetOption) {
        targetOption.selectedOptions().clear();
        if (targetOption.hasAny()) {
            targetOption.setAny(sourceOption.any());
        }
        if (sourceOption.isMultiSelectionAllowed()) {
            if (!sourceOption.selectedOptions().isEmpty()) {
                targetOption.selectedOptions().addAll(sourceOption.selectedOptions().stream().toList());
            } else {
                targetOption.selectedOptions().addAll(targetOption.availableOptions().stream().toList());
            }
        } else if (!(sourceOption.selectedOptions().isEmpty() || sourceOption.selectedOptions().getFirst() == null)) {
            targetOption.selectedOptions().add(sourceOption.selectedOptions().getFirst());
        }
        if (targetOption.hasExcluding()) {
            targetOption.excludedOptions().clear();
            targetOption.excludedOptions().addAll(sourceOption.excludedOptions().stream().toList());
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

    class AccordionBox extends VBox {

        private final ObservableList<FilterTitledPane> panes = FXCollections.observableArrayList();
        private final Accordion langAccordion;
        private TitledPane expandedPane = null;
        private final Map<TitledPane, ChangeListener<Boolean>> listeners = new HashMap<>();
        private final StackPane addButton;

        public AccordionBox() {
            getStyleClass().add("accordion");

            langAccordion = new Accordion();
            langAccordion.getPanes().addListener((ListChangeListener<TitledPane>) c -> {
                while (c.next()) {
                    if (c.wasRemoved()) {
                        c.getRemoved().stream()
                                .filter(LangFilterTitledPane.class::isInstance)
                                .map(LangFilterTitledPane.class::cast)
                                .map(LangFilterTitledPane::getOrdinal)
                                .findFirst()
                                .ifPresent(i -> {
                                    updateLangPanes(pane -> {
                                        if (pane.getOrdinal() > i) {
                                            pane.setOrdinal(pane.getOrdinal() - 1);
                                            pane.setTitle(resources.getString("language.coordinates.ordinal" + (pane.getOrdinal() + 1)));
                                        }
                                    });
                                });
                        updateCurrentFilterOptions();
                        Platform.runLater(() -> {
                            langAccordion.requestLayout();
                            scrollPane.requestLayout();
                            scrollPane.setVvalue(scrollPane.getVmax());
                        });
                    }
                }
            });

            Label titleLabel = new Label(resources.getString("language.title"));
            titleLabel.getStyleClass().add("title-label");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            addButton = new StackPane(new IconRegion("icon", "add"));
            addButton.getStyleClass().add("add-pane");
            addButton.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
                e.consume();
                langAccordion.setExpandedPane(null);
                addButton.setDisable(true);
                List<EntityFacade> list = currentFilterOptionsProperty.get().getLanguageCoordinatesList().stream()
                        .map(l -> l.getLanguage().selectedOptions().getFirst())
                        .filter(Objects::nonNull)
                        .toList();
                FilterOptions.LanguageFilterCoordinates languageCoordinates = currentFilterOptionsProperty.get().addLanguageCoordinates();
                languageCoordinates.getLanguage().excludedOptions().clear();
                languageCoordinates.getLanguage().excludedOptions().addAll(list);
                languageCoordinates.getLanguage().selectedOptions().clear();
                languageCoordinates.getOptions().forEach(sourceOption ->
                        setInheritedOptions(sourceOption, defaultFilterOptions.getLangOptionForItem(0, sourceOption.item())));
                LangFilterTitledPane langFilterTitledPane = setupLangTitledPane(languageCoordinates);
                langAccordion.getPanes().add(langFilterTitledPane);
                // add new pane to subscription
                filterSubscription = filterSubscription.and(langFilterTitledPane.langCoordinatesProperty().subscribe(_ ->
                        updateCurrentFilterOptions()));
            });

            HBox titleBox = new HBox(titleLabel, spacer, addButton);
            titleBox.getStyleClass().add("title-box");

            TitledPane languageTitledPane = new TitledPane();
            languageTitledPane.getStyleClass().add("lang-titled-pane");
            languageTitledPane.setGraphic(titleBox);
            languageTitledPane.setContent(langAccordion);
            languageTitledPane.heightProperty().subscribe(_ -> {
                if (languageTitledPane.isExpanded() && scrollPane != null) {
                    scrollPane.setVvalue(scrollPane.getVmax());
                }
            });

            getChildren().add(languageTitledPane);
            panes.addListener((ListChangeListener<FilterTitledPane>) c -> {
                getChildren().setAll(panes);
                getChildren().add(languageTitledPane);
                while (c.next()) {
                    if (c.wasRemoved()) {
                        removeTitledPaneListeners(c.getRemoved());
                    }
                    if (c.wasAdded()) {
                        initTitledPaneListeners(c.getAddedSubList());
                    }
                }
            });
        }

        // expandedPane
        private final ObjectProperty<FilterTitledPane> expandedPaneProperty = new SimpleObjectProperty<>(this, "expandedPane");
        public final ObjectProperty<FilterTitledPane> expandedPaneProperty() {
           return expandedPaneProperty;
        }
        public final FilterTitledPane getExpandedPane() {
           return expandedPaneProperty.get();
        }
        public final void setExpandedPane(FilterTitledPane value) {
            expandedPaneProperty.set(value);
        }

        public ObservableList<FilterTitledPane> getPanes() {
            return panes;
        }

        public Accordion getLangAccordion() {
            return langAccordion;
        }

        public void disableAddButton(boolean disable) {
            addButton.setDisable(disable);
        }

        public void updateMainPanes(Consumer<FilterTitledPane> onAccept) {
            getPanes().forEach(onAccept);
        }

        public void updateLangPanes(Consumer<LangFilterTitledPane> onAccept) {
            getLangAccordion().getPanes().stream()
                    .filter(LangFilterTitledPane.class::isInstance)
                    .map(LangFilterTitledPane.class::cast)
                    .forEach(onAccept);
        }

        private void initTitledPaneListeners(List<? extends FilterTitledPane> list) {
            for (final FilterTitledPane tp: list) {
                tp.setExpanded(tp == getExpandedPane());
                if (tp.isExpanded()) {
                    expandedPane = tp;
                }
                ChangeListener<Boolean> changeListener = expandedPropertyListener(tp);
                tp.expandedProperty().addListener(changeListener);
                listeners.put(tp, changeListener);
            }
        }

        private void removeTitledPaneListeners(List<? extends FilterTitledPane> list) {
            for (final FilterTitledPane tp: list) {
                if (listeners.containsKey(tp)) {
                    tp.expandedProperty().removeListener(listeners.get(tp));
                    listeners.remove(tp);
                }
            }
        }

        private ChangeListener<Boolean> expandedPropertyListener(final FilterTitledPane tp) {
            return (_, _, expanded) -> {
                if (expanded) {
                    if (expandedPane != null) {
                        expandedPane.setExpanded(false);
                    }
                    if (tp != null) {
                        setExpandedPane(tp);
                    }
                    expandedPane = getExpandedPane();
                } else {
                    expandedPane = null;
                    setExpandedPane(null);
                }
            };
        }
    }
}
