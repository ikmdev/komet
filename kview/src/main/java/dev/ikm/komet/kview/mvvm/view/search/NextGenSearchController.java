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
package dev.ikm.komet.kview.mvvm.view.search;

import static dev.ikm.komet.kview.events.SearchSortOptionEvent.SORT_BY_COMPONENT;
import static dev.ikm.komet.kview.events.SearchSortOptionEvent.SORT_BY_COMPONENT_ALPHA;
import static dev.ikm.komet.kview.events.SearchSortOptionEvent.SORT_BY_SEMANTIC;
import static dev.ikm.komet.kview.events.SearchSortOptionEvent.SORT_BY_SEMANTIC_ALPHA;
import static dev.ikm.komet.kview.mvvm.model.DragAndDropType.CONCEPT;
import static dev.ikm.komet.kview.mvvm.model.DragAndDropType.PATTERN;
import static dev.ikm.komet.kview.mvvm.model.DragAndDropType.SEMANTIC;
import static dev.ikm.komet.kview.mvvm.model.DragAndDropType.STAMP;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.tinkar.events.FrameworkTopics.SEARCH_SORT_TOPIC;
import dev.ikm.komet.framework.concurrent.TaskWrapper;
import dev.ikm.komet.framework.dnd.DragImageMaker;
import dev.ikm.komet.framework.dnd.KometClipboard;
import dev.ikm.komet.framework.search.SearchPanelController;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.AutoCompleteTextField;
import dev.ikm.komet.kview.controls.FilterOptions;
import dev.ikm.komet.kview.controls.FilterOptionsPopup;
import dev.ikm.komet.kview.events.SearchSortOptionEvent;
import dev.ikm.komet.kview.fxutils.FXUtils;
import dev.ikm.komet.kview.mvvm.model.DragAndDropInfo;
import dev.ikm.komet.kview.mvvm.model.DragAndDropType;
import dev.ikm.komet.kview.mvvm.viewmodel.NextGenSearchViewModel;
import dev.ikm.komet.kview.tasks.FilterMenuTask;
import dev.ikm.komet.navigator.graph.Navigator;
import dev.ikm.komet.navigator.graph.ViewNavigator;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.util.text.NaturalOrder;
import dev.ikm.tinkar.common.util.uuid.UuidUtil;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.LatestVersionSearchResult;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.events.EvtBus;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.Subscriber;
import dev.ikm.tinkar.provider.search.TypeAheadSearch;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.loader.JFXNode;
import org.controlsfx.control.PopOver;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.TreeMap;
import java.util.UUID;



public class NextGenSearchController {

    private static final Logger LOG = LoggerFactory.getLogger(NextGenSearchController.class);

    public static final String SORT_OPTIONS_POPUP_FXML = "sort-options.fxml";

    public static final String BUTTON_TEXT_TOP_COMPONENT = "SORT BY: TOP COMPONENT";

    public static final String BUTTON_TEXT_TOP_COMPONENT_ALPHA = "SORT BY: TOP COMPONENT (ALPHABETICAL)";

    public static final String BUTTON_TEXT_DESCRIPTION_SEMANTIC = "SORT BY: MATCHED DESCRIPTION SEMANTIC";

    public static final String BUTTON_TEXT_DESCRIPTION_SEMANTIC_ALPHA = "SORT BY: MATCHED DESCRIPTION SEMANTIC (ALPHABETICAL)";

    public static final int MAX_RESULT_SIZE = 1000;

    private static final PseudoClass FILTER_SHOWING = PseudoClass.getPseudoClass("filter-showing");

    @FXML
    private Pane root;

    @FXML
    private ListView searchResultsListView;

    @FXML
    private Button sortByButton;

    @FXML
    private Button filterPane;

    @FXML
    private AutoCompleteTextField<EntityFacade> searchField;

    private PopOver sortOptions;

    private EvtBus eventBus;

    private FilterOptionsPopup filterOptionsPopup;

    private SearchResultType currentSearchResultType;

    @InjectViewModel
    private NextGenSearchViewModel nextGenSearchViewModel;

    public void initialize() {
        eventBus = EvtBusFactory.getDefaultEvtBus();

        clearView();
        setUpTypeAhead();
        setUpSearchOptionsPopOver();

        Subscriber<SearchSortOptionEvent> searchSortOptionListener = (evt -> {
            if (evt.getEventType() == SORT_BY_COMPONENT) {
                sortByButton.setText(BUTTON_TEXT_TOP_COMPONENT);
            } else if (evt.getEventType() == SORT_BY_COMPONENT_ALPHA) {
                sortByButton.setText(BUTTON_TEXT_TOP_COMPONENT_ALPHA);
            } else if (evt.getEventType() == SORT_BY_SEMANTIC) {
                sortByButton.setText(BUTTON_TEXT_DESCRIPTION_SEMANTIC);
            } else if (evt.getEventType() == SORT_BY_SEMANTIC_ALPHA) {
                sortByButton.setText(BUTTON_TEXT_DESCRIPTION_SEMANTIC_ALPHA);
            }

            if (evt.getEventType() == SORT_BY_COMPONENT || evt.getEventType() == SORT_BY_COMPONENT_ALPHA) {
                setCurrentSearchResultType(SearchResultType.TOP_COMPONENT);
            } else if (evt.getEventType() == SORT_BY_SEMANTIC || evt.getEventType() == SORT_BY_SEMANTIC_ALPHA) {
                setCurrentSearchResultType(SearchResultType.DESCRIPTION_SEMANTICS);
            }

            sortOptions.hide();
        });
        eventBus.subscribe(SEARCH_SORT_TOPIC, SearchSortOptionEvent.class, searchSortOptionListener);

        initSearchResultType();

        filterOptionsPopup = new FilterOptionsPopup(FilterOptionsPopup.FILTER_TYPE.SEARCH);

        TinkExecutor.threadPool().execute(TaskWrapper.make(new FilterMenuTask(getViewProperties()),
                (FilterOptions filterOptions) ->
                        FXUtils.runOnFxThread(() ->
                            filterOptionsPopup.initialFilterOptionsProperty().setValue(filterOptions))
        ));

        root.heightProperty().subscribe(h -> filterOptionsPopup.setStyle("-popup-pref-height: " + h));
        filterPane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (filterOptionsPopup.getNavigator() == null) {
                Navigator navigator = new ViewNavigator(getViewProperties().nodeView());
                filterOptionsPopup.setNavigator(navigator);
            }
            if (e.getButton() == MouseButton.PRIMARY) {
                if (filterOptionsPopup.isShowing()) {
                    e.consume();
                    filterOptionsPopup.hide();
                } else {
                    Bounds bounds = root.localToScreen(root.getLayoutBounds());
                    filterOptionsPopup.show(root.getScene().getWindow(), bounds.getMaxX(), bounds.getMinY());
                }
            }
        });
        filterOptionsPopup.showingProperty().subscribe(showing ->
                filterPane.pseudoClassStateChanged(FILTER_SHOWING, showing));

        // listen for changes to the filter options
        filterOptionsPopup.filterOptionsProperty().subscribe((oldFilterOptions, newFilterOptions) -> {
            if (newFilterOptions != null) {
                // state
                if (!newFilterOptions.getStatus().selectedOptions().isEmpty()) {
                    StateSet stateSet = StateSet.make(
                            newFilterOptions.getStatus().selectedOptions().stream().map(
                                    s -> State.valueOf(s.toUpperCase())).toList());
                    // update the STATUS
                    getViewProperties().nodeView().stampCoordinate().allowedStatesProperty().setValue(stateSet);
                }
                //TODO Type, Module, Path, Language, Description Type, Kind of, Membership, Sort By, Date
            }
        });

        getViewProperties().nodeView().addListener((observableValue, oldViewRecord, newViewRecord) -> {
            doSearch(new ActionEvent(null, null));
        });
    }

    private void initSearchResultType() {
        sortByButton.setText(BUTTON_TEXT_TOP_COMPONENT);
        setCurrentSearchResultType(SearchResultType.TOP_COMPONENT);
    }

    private void setCurrentSearchResultType(SearchResultType newSearchResultType) {
        if (newSearchResultType == currentSearchResultType) {
            return;
        }

        searchResultsListView.getItems().clear();

        switch (newSearchResultType) {
            case TOP_COMPONENT ->
                searchResultsListView.setCellFactory((Callback<ListView<Map.Entry<SearchPanelController.NidTextRecord, List<LatestVersionSearchResult>>>, ListCell<Map.Entry<SearchPanelController.NidTextRecord, List<LatestVersionSearchResult>>>>) param ->
                        new SearchCellTopComponent(getViewProperties(), getJournalTopic(), getViewProperties().parentView())
                );
            case DESCRIPTION_SEMANTICS ->
                searchResultsListView.setCellFactory((Callback<ListView<LatestVersionSearchResult>, ListCell<LatestVersionSearchResult>>) param ->
                        new SearchCellDescriptionSemantic(getViewProperties(), getJournalTopic(), getViewProperties().parentView()));
            case NID ->
                searchResultsListView.setCellFactory((Callback<ListView<Integer>, ListCell<Integer>>) param ->
                        new SearchCellNid(getViewProperties(), getViewProperties().parentView(), getJournalTopic()));
        }

        currentSearchResultType = newSearchResultType;
    }

    private void setUpTypeAhead() {
        searchField.setCompleter(newSearchText -> {
            TypeAheadSearch typeAheadSearch = TypeAheadSearch.get();
            List<EntityFacade> entityFacadeResults = typeAheadSearch.typeAheadSuggestions(
                getViewProperties().nodeView().calculator().navigationCalculator(), /* nav calculator */
                searchField.getText(), /* text */
                10  /* max results returned */
            );
            return entityFacadeResults;
        });

        searchField.setConverter(new StringConverter<>() {
            @Override
            public String toString(EntityFacade entityFacade) {
                return getViewProperties().nodeView().calculator().getFullyQualifiedDescriptionTextWithFallbackOrNid(entityFacade.nid());
            }

            @Override
            public EntityFacade fromString(String string) {
                return null;
            }
        });
    }

    private void setUpSearchOptionsPopOver() {
        JFXNode<Pane, SortOptionsController> sortJFXNode = FXMLMvvmLoader
                .make(SortOptionsController.class.getResource(SORT_OPTIONS_POPUP_FXML));

        Pane sortOptionsPane = sortJFXNode.node();
        PopOver popOver = new PopOver(sortOptionsPane);
        SortOptionsController sortOptionsController = sortJFXNode.controller();
        popOver.setArrowSize(0);
        popOver.setDetachable(false);
        popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
        popOver.setOnHidden(windowEvent -> {
            //TODO save vm?, get values and publish to the eventbus?
        });

        sortOptions = popOver;
    }


    @FXML
    private void showSearchOptions(ActionEvent event) {
        Button button = (Button) event.getSource();
        sortOptions.show(button, -12);
    }

    @FXML
    private void doSearch(ActionEvent actionEvent) {
        clearView();
        String queryText = searchField.getText().strip();
        try {
            if (queryText.startsWith("-") && parseInt(queryText).isPresent()) {
                addComponentFromNid(queryText);
            } else if (queryText.startsWith("[") && queryText.endsWith("]")) {
                queryText = queryText.replace("[", "").replace("]", "");
                String[] nidStrings = queryText.split(",");
                for (String nidString : nidStrings) {
                    addComponentFromNid(nidString.strip());
                }
            } else if (queryText.length() == 36 && UuidUtil.isUUID(queryText)) {
                UuidUtil.getUUID(queryText).ifPresent(uuid -> {
                    addComponentFromNid(PrimitiveData.nid(PublicIds.of(uuid)));
                });
            } else {
                List<LatestVersionSearchResult> results = getViewProperties().calculator().search(queryText, MAX_RESULT_SIZE).toList();
                LOG.info(String.valueOf(results.size()));

                List processedResults;
                switch (sortByButton.getText()) {
                    case BUTTON_TEXT_TOP_COMPONENT -> {
                        setCurrentSearchResultType(SearchResultType.TOP_COMPONENT);

                        // used linked hash map to maintain insertion order
                        LinkedHashMap<SearchPanelController.NidTextRecord, List<LatestVersionSearchResult>> topItems = new LinkedHashMap<>();

                        // sort by top component score order
                        results.sort((o1, o2) -> Float.compare(o2.score(), o1.score()));

                        createMapOfEntries(topItems, results);

                        // sort children inside each by score
                        topItems.forEach((k, v) -> Collections.sort(v, (o1, o2) ->
                                Float.compare(o1.score(), o2.score())));

                        List<Map.Entry<SearchPanelController.NidTextRecord, List<LatestVersionSearchResult>>> myList = new ArrayList<>(topItems.entrySet());

                        Collections.sort(myList, (m1, m2) ->
                                Float.compare(m2.getValue().get(0).score(), m1.getValue().get(0).score()));

                        processedResults = myList;
                    }
                    case BUTTON_TEXT_TOP_COMPONENT_ALPHA -> {
                        setCurrentSearchResultType(SearchResultType.TOP_COMPONENT);

                        // sort by natural order
                        results.sort((o1, o2) -> NaturalOrder.compareStrings(o1.latestVersion().get().fieldValues().get(o1.fieldIndex()).toString(),
                                o2.latestVersion().get().fieldValues().get(o2.fieldIndex()).toString()));

                        Map<SearchPanelController.NidTextRecord, List<LatestVersionSearchResult>> topItems = new HashMap<>();
                        // create the sort order for the topItems map collection
                        topItems = new TreeMap<>((o1, o2) -> NaturalOrder.compareStrings(o1.text(), o2.text()));

                        createMapOfEntries(topItems, results);

                        List<Map.Entry<SearchPanelController.NidTextRecord, List<LatestVersionSearchResult>>> myList = new ArrayList<>(topItems.entrySet());

                        // sort the children
                        myList.forEach(m -> Collections.sort(m.getValue(), (e1, e2) ->
                                NaturalOrder.compareStrings(formatHighlightedString(e1.highlightedString()), formatHighlightedString(e2.highlightedString()))
                        ));

                        processedResults = myList;
                    }
                    case BUTTON_TEXT_DESCRIPTION_SEMANTIC -> {
                        setCurrentSearchResultType(SearchResultType.DESCRIPTION_SEMANTICS);

                        results.sort((o1, o2) -> Float.compare(o2.score(), o1.score()));

                        processedResults = results;
                    }
                    case BUTTON_TEXT_DESCRIPTION_SEMANTIC_ALPHA -> {
                        setCurrentSearchResultType(SearchResultType.DESCRIPTION_SEMANTICS);

                        results.sort((o1, o2) -> NaturalOrder.compareStrings(formatHighlightedString(o1.highlightedString()),
                                formatHighlightedString(o2.highlightedString())));

                        processedResults = results;
                    }
                    default -> throw new RuntimeException("Sort by button text is Invalid and doesn't correspond to any supported search type");

                }

                searchResultsListView.getItems().setAll(processedResults);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addComponentFromNid(String queryText) {
        int nid = parseInt(queryText).getAsInt();
        addComponentFromNid(nid);
    }

    private void addComponentFromNid(int nid) {
        setCurrentSearchResultType(SearchResultType.NID);

        searchResultsListView.getItems().add(nid);
    }

    static DragAndDropType getDragAndDropType(Entity entity) {
        return switch (entity){
            case ConceptEntity conceptEntity -> CONCEPT;
            case SemanticEntity semanticEntity -> SEMANTIC;
            case PatternEntity patternEntity -> PATTERN;
            case StampEntity stampEntity -> STAMP;
            default -> throw new IllegalStateException("Unexpected value: " + entity);
        };
    }

    /**
     * Configures the specified {@link Node} to support drag-and-drop operations associated with the given {@link Entity}.
     * <p>
     * When a drag is detected on the node, this method initializes a dragboard with the entity's identifier and
     * sets a custom drag image for visual feedback.
     * </p>
     *
     * @param node   the JavaFX {@link Node} to be made draggable
     * @param entity the {@link Entity} associated with the node, providing data for the drag operation
     * @throws NullPointerException if either {@code node} or {@code entity} is {@code null}
     */
    static void setUpDraggable(Node node, Entity<?> entity, DragAndDropType dropType) {
        Objects.requireNonNull(node, "The node must not be null.");
        Objects.requireNonNull(entity, "The entity must not be null.");

        // Associate the node with the entity's public ID and type for later retrieval or identification
        node.setUserData(new DragAndDropInfo(dropType, entity.publicId()));

        // Set up the drag detection event handler
        node.setOnDragDetected(mouseEvent -> {
            // Initiate a drag-and-drop gesture with copy or move transfer mode
            Dragboard dragboard = node.startDragAndDrop(TransferMode.COPY_OR_MOVE);

            // Create the content to be placed on the dragboard
            // Here, KometClipboard is used to encapsulate the entity's unique identifier (nid)
            KometClipboard content = new KometClipboard(EntityFacade.make(entity.nid()));

            // Generate the drag image using DragImageMaker
            DragImageMaker dragImageMaker = new DragImageMaker(node);
            Image dragImage = dragImageMaker.getDragImage();
            // Set the drag image on the dragboard
            if (dragImage != null) {
                dragboard.setDragView(dragImage);
            }

            // Place the content on the dragboard
            dragboard.setContent(content);

            // Log the drag event details for debugging or auditing
            LOG.info("Drag detected on node: " + mouseEvent.toString());

            // Consume the mouse event to prevent further processing
            mouseEvent.consume();
        });
    }

    private OptionalInt parseInt(String possibleInt) {
        try {
            return OptionalInt.of(Integer.parseInt(possibleInt));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    private void createMapOfEntries(Map<SearchPanelController.NidTextRecord, List<LatestVersionSearchResult>> topItems,
                                    List<LatestVersionSearchResult> results) {
        MutableIntObjectMap<MutableList<LatestVersionSearchResult>> topNidMatchMap = IntObjectMaps.mutable.empty();
        for (LatestVersionSearchResult result : results) {
            topNidMatchMap.getIfAbsentPut(result.latestVersion().get().chronology().topEnclosingComponentNid(),
                    () -> Lists.mutable.empty()).add(result);
        }
        // topItems is similar to tempRoot
        for (int topNid : topNidMatchMap.keySet().toArray()) {
            String topText = getViewProperties().nodeView().calculator().getFullyQualifiedDescriptionTextWithFallbackOrNid(topNid);
            Latest<EntityVersion> latestTopVersion = getViewProperties().nodeView().calculator().latest(topNid);
            latestTopVersion.ifPresent(entityVersion -> {
                topItems.put(new SearchPanelController.NidTextRecord(topNid, topText, entityVersion.active()),
                        topNidMatchMap.get(topNid));
            });
        }
    }

    public ViewProperties getViewProperties() {
        return nextGenSearchViewModel.getPropertyValue(VIEW_PROPERTIES);
    }

    private UUID getJournalTopic() {
        return nextGenSearchViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC);
    }

    private String formatHighlightedString(String highlightedString) {
        String string = (highlightedString == null) ? "" : highlightedString;
        return string.replaceAll("<B>", "")
                .replaceAll("</B>", "")
                .replaceAll("\\s+", " ");
    }



    public void clearView() {
        searchResultsListView.getItems().clear();
    }

    public void cleanup() {

    }


    /***************************************************************************
     *                                                                         *
     * Support Classes                                                         *
     *                                                                         *
     **************************************************************************/

    private enum SearchResultType {
        TOP_COMPONENT,
        DESCRIPTION_SEMANTICS,
        NID
    }
}

