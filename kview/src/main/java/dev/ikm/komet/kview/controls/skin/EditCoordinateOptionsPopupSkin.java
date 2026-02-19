package dev.ikm.komet.kview.controls.skin;

import static dev.ikm.komet.kview.controls.EditCoordinateOptions.OPTION_ITEM.AUTHOR_FOR_CHANGE;
import static dev.ikm.komet.kview.controls.EditCoordinateOptions.OPTION_ITEM.DEFAULT_MODULE;
import static dev.ikm.komet.kview.controls.EditCoordinateOptions.OPTION_ITEM.DEFAULT_PATH;
import static dev.ikm.komet.kview.controls.EditCoordinateOptions.OPTION_ITEM.DESTINATION_MODULE;
import static dev.ikm.komet.kview.controls.EditCoordinateOptions.OPTION_ITEM.PROMOTION_PATH;
import dev.ikm.komet.framework.observable.ObservableEntityHandle;
import dev.ikm.komet.framework.search.SearchPanelController;
import dev.ikm.komet.framework.view.ObservableEditCoordinate;
import dev.ikm.komet.kview.controls.EditCoordinateOptions;
import dev.ikm.komet.kview.controls.EditCoordinateOptionsPopup;
import dev.ikm.komet.kview.controls.EditCoordinateTitledPane;
import dev.ikm.komet.kview.controls.IconRegion;
import dev.ikm.komet.kview.controls.SavedFiltersPopup;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.Preferences;
import dev.ikm.komet.rules.actions.axiom.ChooseConceptMenu;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Skin;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.function.*;

public class EditCoordinateOptionsPopupSkin implements Skin<EditCoordinateOptionsPopup> {
    private static final Logger LOG = LoggerFactory.getLogger(EditCoordinateOptionsPopupSkin.class);

    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.edit-coordinate-options");
    private static final String EDIT_COORDINATE_OPTIONS_KEY = "edit-coordinate-options";
    private static final String SAVED_FILTERS_KEY = "saved-filters";
    private static final String DEFAULT_OPTIONS_KEY = "default-options";

    private final EditCoordinateOptionsPopup control;
    private final VBox root;

    private final AccordionBox accordionBox;
    private final ScrollPane scrollPane;
    private final Button revertButton;
    private final SavedFiltersPopup savedFiltersPopup;
    private final KometPreferences kometPreferences;

    private Subscription subscription;
    private Subscription filterSubscription;
    private boolean updating, skipUpdateFilterOptions;

    private EditCoordinateOptions defaultFilterOptions;
    private final ObjectProperty<EditCoordinateOptions> currentFilterOptionsProperty = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            EditCoordinateOptions filterOptions = get();
            if (filterOptions != null) {
                boolean isDefault = defaultFilterOptions.equals(filterOptions);
                if (!updating) {
                    control.setFilterOptions(filterOptions);
                    control.getProperties().put(DEFAULT_OPTIONS_KEY, isDefault);
                }
                revertButton.setDisable(isDefault);

            }
        }
    };

    public EditCoordinateOptionsPopupSkin(EditCoordinateOptionsPopup control) {
        this.control = control;
        this.defaultFilterOptions = control.getFilterOptions();

        savedFiltersPopup = new SavedFiltersPopup(this::applyFilter, this::removeFilter);
        kometPreferences = Preferences.get().getConfigurationPreferences()
                .node(EDIT_COORDINATE_OPTIONS_KEY)
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

        VBox bottomBox = new VBox(saveButton, revertButton);
        bottomBox.getStyleClass().add("bottom-box");

        root = new VBox(headerBox, scrollPane, spacer, bottomBox);
        root.getStyleClass().add("filter-options-popup");
        root.getStylesheets().add(EditCoordinateOptionsPopup.class.getResource("filter-options-popup.css").toExternalForm());

        // create panes, without any filter option yet
        createAccordionBoxPanes();

        subscription = control.filterOptionsProperty().subscribe(this::setupFilter);
        subscription = subscription.and(control.filterOptionsProperty().subscribe(this::setupDefaultFilterOptions));

        subscription = subscription.and(savedFiltersPopup.showingProperty().subscribe((_, showing) -> {
            if (!showing) {
                filterPane.setSelected(false);
            }
        }));
        subscription = subscription.and(control.getInheritedFilterOptions().observableEditCoordinateForOptionsProperty().subscribe((_, _) -> {
//            if (control.getNavigator() != null) {
//                // parentView -> inheritedF.O. -> refresh default
                setupDefaultFilterOptions();
//            }
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

//        subscription = subscription.and()
        control.setOnShown(_ -> scrollPane.setVvalue(scrollPane.getVmin()));
    }

    private void setupFilter(EditCoordinateOptions filterOptions) {
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

        // pass filter options to titledPane controls
        skipUpdateFilterOptions = true;
        accordionBox.updateMainPanes(pane -> {
                EditCoordinateOptions.Option optionForItem = filterOptions.getOptionForItem(pane.getOption().item());
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
        skipUpdateFilterOptions = false;
        updateCurrentFilterOptions();
        updating = false;
    }

    @Override
    public EditCoordinateOptionsPopup getSkinnable() {
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
//        EditCoordinateOptions currentFilterOptions = new EditCoordinateOptions(getSkinnable().getFilterOptions().observableEditCoordinateForOptionsProperty());
        EditCoordinateOptions currentFilterOptions = control.getFilterOptions();
        accordionBox.updateMainPanes(pane -> {
            EditCoordinateOptions.Option<T> optionForItem = pane.getOption();
            currentFilterOptions.setOptionForItem(pane.getOption().item(), optionForItem);
        });

        currentFilterOptionsProperty.set(currentFilterOptions);
    }

    private void createAccordionBoxPanes() {
//        EditCoordinateOptions filterOptions = new EditCoordinateOptions(getSkinnable().getFilterOptions().observableEditCoordinateForOptionsProperty());
        EditCoordinateOptions filterOptions = control.getFilterOptions();

        // Main Coordinates
        // authors: all descendants of Author concept
        EditCoordinateOptions.Option<ConceptFacade> authorForChangeOption = filterOptions.getMainCoordinates().getAuthorForChange();
        EditCoordinateTitledPane authorForChangeTitledPane = setupTitledPane(authorForChangeOption);

        // default module
        EditCoordinateOptions.Option<ConceptFacade> defaultModuleOption = filterOptions.getMainCoordinates().getDefaultModule();
        EditCoordinateTitledPane defaultModuleTitledPane = setupTitledPane(defaultModuleOption);

        // default module
        EditCoordinateOptions.Option<ConceptFacade> destinationModuleOption = filterOptions.getMainCoordinates().getDestinationModule();
        EditCoordinateTitledPane destinationModuleTitledPane = setupTitledPane(destinationModuleOption);

        // Default Path
        EditCoordinateOptions.Option<ConceptFacade> defaultPathOption = filterOptions.getMainCoordinates().getDefaultPath();
        EditCoordinateTitledPane defaultPathTitledPane = setupTitledPane(defaultPathOption);

        // Promotion Path
        EditCoordinateOptions.Option<ConceptFacade> promotionPathOption = filterOptions.getMainCoordinates().getPromotionPath();
        EditCoordinateTitledPane promotionPathTitledPane = setupTitledPane(promotionPathOption);

        accordionBox.getPanes().setAll(
                authorForChangeTitledPane,
                defaultModuleTitledPane,
                destinationModuleTitledPane,
                defaultPathTitledPane,
                promotionPathTitledPane
        );

    }

    private void setupDefaultFilterOptions() {
        if (defaultFilterOptions == null) {
            // create default filter options
            defaultFilterOptions = control.getFilterOptions();

            // once we have navigator, update pending options with av/sel default options
//            setAvailableOptionsFromNavigator(defaultFilterOptions, navigator);
            // path: all descendants of Path
//            ViewCalculator viewCalculator = ViewCoordinateHelper.createViewCalculatorLatestByTime(getSkinnable().getViewProperties());
//            Set<ConceptEntity> defaultPaths = DataModelHelper.fetchDescendentsOfConcept(viewCalculator, TinkarTerm.PATH);
//          List<EntityFacade> descendentsList = ViewCoordinateHelper.createNavigationCalculatorWithPatternNidsLatest(viewCalculator)FilterOptionsUtils.getDescendentsList(navigator, rootNid, FilterOptions.OPTION_ITEM.PATH.getPath());
//        List<EntityFacade> descendentsList = FxGet.pathCoordinates(navigator.getViewCalculator()).values()
//                .stream().map(v -> (EntityFacade) v.pathConcept()).toList();
//            setAvailableOptions(defaultFilterOptions.getMainCoordinates().getDefaultPath(), defaultPaths.stream().map(ConceptFacade.class::cast).toList());

        }
        // then pass the inherited options, to override av/sel default options where set
        setDefaultOptions(control.getInheritedFilterOptions());
        // pass default options to panes
        accordionBox.updateMainPanes(pane ->
                pane.setDefaultOption(defaultFilterOptions.getOptionForItem(pane.getOption().item())));
//        accordionBox.updateLangPanes(pane -> {
//            if (pane.getOrdinal() == 0) {
//                // only primary language gets synced
//                pane.setDefaultLangCoordinates(defaultFilterOptions.getLanguageCoordinates(0).copy());
//            }
//        });
        // finally, setup filter with default options
        setupFilter(defaultFilterOptions);
    }

    private void setDefaultOptions(EditCoordinateOptions filterOptions) {
        filterOptions.getMainCoordinates().getOptions().forEach(sourceOption ->
                setInheritedOptions(sourceOption, defaultFilterOptions.getOptionForItem(sourceOption.item())));
    }

    /**
     * Creates a titled pane for the given option. A context menu allows user to search for a concept to add as available options.
     * @param option The option to create a titled pane for.
     * @return
     */
    private EditCoordinateTitledPane setupTitledPane(EditCoordinateOptions.Option option) {
        EditCoordinateTitledPane titledPane = new EditCoordinateTitledPane();
        MenuItem chooseConceptMenuItem = new ChooseConceptMenu(
                "Find " + option.title(),
                control.getViewProperties().calculator(),
                titledPane, control.getViewProperties(), nidTextRecord -> {
                SearchPanelController.NidTextRecord nidTextRecord1 = (SearchPanelController.NidTextRecord) nidTextRecord;
                LOG.info("══════════════════════════════════════════════════════════════════════════════════════════════════");
                LOG.info("EditCoordinates - {} : Adding {} to available options. nid: {}", option.title(), nidTextRecord1.text(), nidTextRecord1.nid());
                LOG.info("══════════════════════════════════════════════════════════════════════════════════════════════════");
                try {
                    ObservableEntityHandle
                            .get(nidTextRecord1.nid())
                            .asConcept()
                            .ifPresent( observableConcept -> {
                                EditCoordinateOptions.Option optionForItem = control.getFilterOptions().getOptionForItem(option.item());
                                optionForItem.availableOptions().add(observableConcept.entity());
                                optionForItem.selectedOptions().clear();
                                optionForItem.selectedOptions().add(observableConcept.entity());

                                option.availableOptions().add(observableConcept.entity());
//                                option.selectedOptions().clear();
//                                option.selectedOptions().add(observableConcept.entity());

                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
        });
        ContextMenu contextMenu = new ContextMenu(chooseConceptMenuItem);
        titledPane.setContextMenu(contextMenu);

        titledPane.setTitle(option.title());
        titledPane.setOption(option);
        titledPane.setExpanded(false);
        titledPane.expandedProperty().addListener((observable, _, expanded) -> {
            LOG.info("{} expanded: {}", option.title(), expanded);
            if (!expanded) {
                // not expanded is accepting the selected option.
                if (titledPane.getOption().selectedOptions().size() > 0) {
                    LOG.info("Selected option: {}", titledPane.getOption().selectedOptions().get(0));
                    updateMainEditCoordinateRecord();
                }
            }
        });
        return titledPane;
    }

    private void updateMainEditCoordinateRecord() {
        List<EditCoordinateOptions.Option> options = new ArrayList<>(control.getFilterOptions().getMainCoordinates().getOptions());
        options.forEach(option -> {
            boolean selected = !option.selectedOptions().isEmpty();
            Object item = selected ? option.selectedOptions().getFirst() : null;
            LOG.info("Updating main edit coordinate record for option: {} selected: {} ", option.title(), item);
            ObservableEditCoordinate editCoordinate = control.getViewProperties().parentView().editCoordinate();
            EditCoordinateOptions.OPTION_ITEM itemType = option.item();
            if (selected) {
                if (itemType == AUTHOR_FOR_CHANGE) {
                    editCoordinate.authorForChangesProperty().setValue((ConceptFacade) item);
                } else if (itemType == DEFAULT_MODULE) {
                    editCoordinate.defaultModuleProperty().setValue((ConceptFacade) item);
                } else if (itemType == DESTINATION_MODULE) {
                    editCoordinate.destinationModuleProperty().setValue((ConceptFacade) item);
                } else if (itemType == DEFAULT_PATH) {
                    editCoordinate.defaultPathProperty().setValue((ConceptFacade) item);
                } else if (itemType == PROMOTION_PATH) {
                    editCoordinate.promotionPathProperty().setValue((ConceptFacade) item);
                }
            }
        });
        // TODO output all selected options to console
    }

    private <T> void setAvailableOptions(EditCoordinateOptions.Option<T> option, List<? extends T> options) {
        option.availableOptions().clear();
        option.availableOptions().addAll(options);
        option.selectedOptions().clear();
        option.selectedOptions().addAll(option.isMultiSelectionAllowed() || options.isEmpty() ? options : List.of(options.getFirst()));
    }

    private <T> void setInheritedOptions(EditCoordinateOptions.Option<T> sourceOption, EditCoordinateOptions.Option<T> targetOption) {
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
                        EditCoordinateOptions newFilterOptions = deserialize(data);
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

    private static byte[] serialize(EditCoordinateOptions filterOptions) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (ObjectOutputStream outputStream = new ObjectOutputStream(out)) {
            outputStream.writeObject(filterOptions);
        }

        return out.toByteArray();
    }

    private EditCoordinateOptions deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data)) {
            return (EditCoordinateOptions) new ObjectInputStream(bis).readObject();
        }
    }

    class AccordionBox extends VBox {

        private final ObservableList<EditCoordinateTitledPane> panes = FXCollections.observableArrayList();
        private TitledPane expandedPane = null;
        private final Map<TitledPane, ChangeListener<Boolean>> listeners = new HashMap<>();

        public AccordionBox() {
            getStyleClass().add("accordion");

            panes.addListener((ListChangeListener<EditCoordinateTitledPane>) c -> {
                getChildren().setAll(panes);
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
        private final ObjectProperty<EditCoordinateTitledPane> expandedPaneProperty = new SimpleObjectProperty<>(this, "expandedPane");
        public final ObjectProperty<EditCoordinateTitledPane> expandedPaneProperty() {
           return expandedPaneProperty;
        }
        public final EditCoordinateTitledPane getExpandedPane() {
           return expandedPaneProperty.get();
        }
        public final void setExpandedPane(EditCoordinateTitledPane value) {
            expandedPaneProperty.set(value);
        }

        public ObservableList<EditCoordinateTitledPane> getPanes() {
            return panes;
        }



        public void updateMainPanes(Consumer<EditCoordinateTitledPane> onAccept) {
            getPanes().forEach(onAccept);
        }

        private void initTitledPaneListeners(List<? extends EditCoordinateTitledPane> list) {
            for (final EditCoordinateTitledPane tp: list) {
                tp.setExpanded(tp == getExpandedPane());
                if (tp.isExpanded()) {
                    expandedPane = tp;
                }
                ChangeListener<Boolean> changeListener = expandedPropertyListener(tp);
                tp.expandedProperty().addListener(changeListener);
                listeners.put(tp, changeListener);
            }
        }

        private void removeTitledPaneListeners(List<? extends EditCoordinateTitledPane> list) {
            for (final EditCoordinateTitledPane tp: list) {
                if (listeners.containsKey(tp)) {
                    tp.expandedProperty().removeListener(listeners.get(tp));
                    listeners.remove(tp);
                }
            }
        }

        private ChangeListener<Boolean> expandedPropertyListener(final EditCoordinateTitledPane tp) {
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
