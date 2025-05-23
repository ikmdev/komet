package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.FilterOptions;
import dev.ikm.komet.kview.controls.FilterOptionsPopup;
import dev.ikm.komet.kview.controls.IconRegion;
import dev.ikm.komet.kview.controls.InvertedTree;
import dev.ikm.komet.kview.controls.KLSearchControl;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Subscription;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * <p>Custom skin implementation for the {@link KLSearchControl}.
 * </p>
 * <p>This implementation takes care of adding a {@link TextField} as a search field for the user,
 * with a clear button in it, a filter button and results area to display a
 * {@link ListView<dev.ikm.komet.kview.controls.KLSearchControl.SearchResult>} with the
 * search results, with a limited height. This area is only shown when the search field contains some text,
 * the user has pressed ENTER and a search engine has performed a certain search, setting the results in
 * the {@link KLSearchControl#resultsProperty()}.
 * </p>
 */
public class KLSearchControlSkin extends SkinBase<KLSearchControl> {

    private static final double SEARCH_RESULT_HEIGHT = 43; // 38 + 5

    private final TextField textField;
    private final StackPane searchPane;
    private final StackPane closePane;
    private final StackPane filterPane;
    private Subscription subscription;

    private final ListView<KLSearchControl.SearchResult> resultsPane;
    private final FilterOptionsPopup filterOptionsPopup;

    /**
     * <p>Creates a {@link KLSearchControlSkin} instance.
     * </p>
     * <p>Create the {@link ListView<dev.ikm.komet.kview.controls.KLSearchControl.SearchResult>} instance, and
     * provide a custom cell factory that uses a {@link SearchResultBox} instance for the graphic node.
     * </p>
     * <p>The list height is limited, and up until 4 search results will be visible.
     * </p>
     * <p>Adds bindings and listeners to perform the necessary actions based on the {@link KLSearchControl}
     * properties.
     * </p>
     * @param control The control that this skin should be installed onto
     */
    public KLSearchControlSkin(KLSearchControl control) {
        super(control);
        ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.search-control");

        resultsPane = new ListView<>();
        resultsPane.getStyleClass().add("search-results-list-view");

        Label placeholder = new Label();
        placeholder.getStyleClass().add("placeholder-label");
        placeholder.textProperty().bind(Bindings.createStringBinding(() -> {
            if (control.getResultsPlaceholder() != null) {
                return control.getResultsPlaceholder();
            }
            return resources.getString("no.results.found.text");
        },  control.resultsPlaceholderProperty()));
        resultsPane.setPlaceholder(placeholder);

        resultsPane.setCellFactory(_ -> new ListCell<>() {
            private final SearchResultBox searchResult;
            {
                searchResult = new SearchResultBox(control);
                setText(null);
                getStyleClass().add("search-result-cell");
            }

            @Override
            public void updateIndex(int newIndex) {
                super.updateIndex(newIndex);
                if (newIndex == -1) {
                    searchResult.cleanup();
                }
            }

            @Override
            protected void updateItem(KLSearchControl.SearchResult item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    searchResult.setSearchResult(item);
                    setGraphic(searchResult);
                } else {
                    setGraphic(null);
                }
            }
        });
        resultsPane.managedProperty().bind(resultsPane.visibleProperty());
        resultsPane.setVisible(false);
        resultsPane.setItems(control.resultsProperty());
        control.resultsProperty().addListener((ListChangeListener<KLSearchControl.SearchResult>) _ -> {
            int itemCount = Math.max(1, Math.min(resultsPane.getItems().size(), 4));
            resultsPane.setPrefHeight(itemCount * SEARCH_RESULT_HEIGHT);
        });
        resultsPane.setPrefHeight(SEARCH_RESULT_HEIGHT);

        textField = new TextField();
        searchPane = new StackPane(new IconRegion("icon", "search"));
        searchPane.getStyleClass().add("region");

        closePane = new StackPane(new IconRegion("icon", "close"));
        closePane.getStyleClass().add("region");
        closePane.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (control.getOnClearSearch() != null) {
                    control.getOnClearSearch().handle(new ActionEvent());
                }
                textField.clear();
                resultsPane.getItems().clear();
                resultsPane.setVisible(false);
            }
        });

        filterOptionsPopup = new FilterOptionsPopup();
        filterOptionsPopup.navigatorProperty().bind(control.navigatorProperty());
        getSkinnable().parentProperty().subscribe(parent -> {
            if (parent instanceof Region region) {
                region.heightProperty().subscribe(h -> filterOptionsPopup.setStyle("-popup-pref-height: " + h));
            }
        });
        filterPane = new StackPane(new IconRegion("icon", "filter"));
        filterPane.getStyleClass().add("filter-region");
        filterPane.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && control.getOnFilterAction() != null) {
                if (filterOptionsPopup.isShowing()) {
                    filterOptionsPopup.hide();
                } else {
                    Bounds bounds = control.localToScreen(control.getLayoutBounds());
                    filterOptionsPopup.show(control.getScene().getWindow(), bounds.getMaxX(), bounds.getMinY());
                }
                control.getOnFilterAction().handle(new ActionEvent());
            }
        });
        filterOptionsPopup.setFilterOptions(new FilterOptions());
        subscription = control.filterSetProperty().subscribe(isSet -> {
            if (isSet) {
                filterPane.getChildren().setAll(new IconRegion("icon", "filter-dot"), new IconRegion("icon", "dot"));
            } else {
                filterPane.getChildren().setAll(new IconRegion("icon", "filter"));
            }
        });

        getChildren().addAll(textField, searchPane, closePane, filterPane, resultsPane);

        textField.textProperty().bindBidirectional(control.textProperty());
        if (control.getPromptText() == null) {
            control.setPromptText(resources.getString("prompt.text"));
        }
        textField.promptTextProperty().bind(control.promptTextProperty());
        closePane.visibleProperty().bind(textField.textProperty().isNotEmpty());
        closePane.managedProperty().bind(closePane.visibleProperty());

        textField.setOnAction(_ -> {
            control.resultsProperty().clear();
            if (textField.getText() == null || textField.getText().isEmpty()) {
                return;
            }
            if (control.getOnAction() != null) {
                control.getOnAction().handle(new ActionEvent());
            }
            resultsPane.setVisible(true);
            control.requestLayout();
        });

        // Clicking anywhere outside the control, hides the resultsPane
        EventHandler<MouseEvent> eventFilter = e -> {
            Point2D point2D = new Point2D(e.getSceneX(), e.getSceneY());
            if (resultsPane.isVisible() && !control.contains(control.sceneToLocal(point2D))) {
                resultsPane.setVisible(false);
            }
        };
        // install/uninstall event filter to the scene that holds the control
        subscription = resultsPane.visibleProperty().subscribe((_, b) -> {
            Scene scene = control.getScene();
            if (scene != null) {
                if (b) {
                    scene.addEventFilter(MouseEvent.MOUSE_CLICKED, eventFilter);
                } else {
                    scene.removeEventFilter(MouseEvent.MOUSE_CLICKED, eventFilter);
                }
            }
        });
        // when clicking inside the textField, show resultsPane
        textField.addEventHandler(MouseEvent.MOUSE_CLICKED, _ -> {
            if (!resultsPane.getItems().isEmpty() && !resultsPane.isVisible()) {
                resultsPane.setVisible(true);
            }
        });
    }

    /** {@inheritDoc} **/
    @Override
    public void dispose() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
        textField.textProperty().unbind();
        textField.promptTextProperty().unbind();
        textField.onActionProperty().unbind();
        closePane.visibleProperty().unbind();
        closePane.managedProperty().unbind();
        filterOptionsPopup.navigatorProperty().unbind();
        super.dispose();
    }

    /** {@inheritDoc} **/
    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    /** {@inheritDoc} **/
    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        double prefHeight = textField.prefHeight(width) + snappedTopInset() + snappedBottomInset();
        if (resultsPane.isVisible()) {
            prefHeight += 12 + resultsPane.prefHeight(width);
        }
        return prefHeight;
    }

    /** {@inheritDoc} **/
    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    /** {@inheritDoc} **/
    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        double x = snapPositionX(contentX);
        double y = snapPositionY(contentY);
        double searchPaneWidth = snapSizeX(searchPane.prefWidth(contentHeight));
        double closePaneWidth = snapSizeX(closePane.prefWidth(contentHeight));
        double filterPaneWidth = snapSizeX(filterPane.prefWidth(contentHeight));
        double textFieldWidth = contentWidth - filterPaneWidth - 12;
        double textFieldHeight = snapSizeY(textField.prefHeight(textFieldWidth));
        textField.resizeRelocate(x, y, textFieldWidth, textFieldHeight);

        double searchHeight = snapSizeY(searchPane.prefHeight(searchPaneWidth));
        searchPane.resizeRelocate(x + 8, y + (textFieldHeight - searchHeight) / 2,
                searchPaneWidth, searchHeight);

        double closeHeight = snapSizeY(closePane.prefHeight(searchPaneWidth));
        closePane.resizeRelocate(x + textFieldWidth - 4 - closePaneWidth, y + (textFieldHeight - closeHeight) / 2,
                closePaneWidth, closeHeight);

        double filterHeight = snapSizeY(filterPane.prefHeight(filterPaneWidth));
        filterPane.resizeRelocate(contentWidth - filterPaneWidth + snappedLeftInset() - 4, y + (textFieldHeight - filterHeight) / 2,
                filterPaneWidth, filterHeight);

        if (resultsPane.isVisible()) {
            double resultsHeight = snapSizeY(resultsPane.prefHeight(contentWidth));
            resultsPane.resizeRelocate(x,y + textFieldHeight + 12, contentWidth, resultsHeight);
        }
    }

    /**
     * <p>Custom implementation of a {@link Pane} layout, to render the content of a
     * {@link dev.ikm.komet.kview.controls.KLSearchControl.SearchResult}.
     * </p>
     * <p>This can be used to render search results in a {@link ListView} control.
     * </p>
     */
    static class SearchResultBox extends Pane {

        private static final PseudoClass LONG_HOVER_PSEUDO_CLASS = PseudoClass.getPseudoClass("long-hover");

        private final Label parentDescriptionLabel;
        private final Label descriptionLabel;
        private final KLSearchControl searchControl;
        private Text actualDescriptionText;
        private String highlight;
        private PauseTransition hoverTransition;
        private Subscription subscription;
        private KLSearchControl.SearchResult searchResult;
        private final StringProperty actualTextProperty;

        /**
         * <p>Creates an instance of a {@link SearchResultBox}.
         * </p>
         * <p>A {@link Label} node is used on top with the {@link ConceptFacade#description()} of the parent concept.
         * </p>
         * <p>A {@link Label} node is used below with the {@link ConceptFacade#description()} of the concept found in
         * the search, highlighting the occurrences of the {@link KLSearchControl#textProperty()}.
         * </p>
         * @param searchControl the {@link KLSearchControl} with a {@link ListView} control, that
         *                      will use this box as graphic node for its cells.
         */
        public SearchResultBox(KLSearchControl searchControl) {
            this.searchControl = searchControl;
            subscription = Subscription.EMPTY;

            parentDescriptionLabel = new Label();
            parentDescriptionLabel.getStyleClass().add("parent-description-label");

            IconRegion iconRegion = new IconRegion("icon", "curve");
            StackPane stackPane = new StackPane(iconRegion);
            stackPane.getStyleClass().add("region");
            actualTextProperty = new SimpleStringProperty();
            descriptionLabel = new Label(null, stackPane) {
                @Override
                protected Skin<?> createDefaultSkin() {
                    Skin<?> defaultSkin = super.createDefaultSkin();
                    actualDescriptionText = getChildren().stream()
                            .filter(Text.class::isInstance)
                            .map(Text.class::cast)
                            .findFirst()
                            .orElseThrow();
                    actualTextProperty.bind(actualDescriptionText.textProperty());
                    return defaultSkin;
                }
            };
            descriptionLabel.contentDisplayProperty().bind(Bindings.when(parentDescriptionLabel.textProperty().isNotEmpty())
                    .then(ContentDisplay.LEFT).otherwise(ContentDisplay.TEXT_ONLY));
            descriptionLabel.getStyleClass().add("description-label");

            getChildren().addAll(parentDescriptionLabel, descriptionLabel);

            getStyleClass().add("search-result");
            getStylesheets().add(KLSearchControl.class.getResource("search-control.css").toExternalForm());
        }

        /**
         * <p>Update the content of this {@link SearchResultBox} with a new
         * {@link dev.ikm.komet.kview.controls.KLSearchControl.SearchResult}
         * </p>
         * @param result a {@link dev.ikm.komet.kview.controls.KLSearchControl.SearchResult}
         */
        void setSearchResult(KLSearchControl.SearchResult result) {
            cleanup();
            this.searchResult = result;
            highlight = result.highlight().toLowerCase(Locale.ROOT);
            parentDescriptionLabel.setText(result.parentConcept() != null ? result.parentConcept().description() : null);
            descriptionLabel.setText(result.concept().description());
            subscription = subscription.and(actualTextProperty.subscribe(text -> {
                Platform.runLater(this::addHighlightPaths);
                if (text != null && !text.isEmpty() && descriptionLabel.getText() != null && !text.equals(descriptionLabel.getText())) {
                    descriptionLabel.setTooltip(new Tooltip(descriptionLabel.getText()));
                }
            }));
            subscription = subscription.and(hoverProperty().subscribe(h -> {
                if (h) {
                    hoverTransition = new PauseTransition(new Duration(searchControl.getActivation()));
                    hoverTransition.setOnFinished(_ -> {
                        pseudoClassStateChanged(LONG_HOVER_PSEUDO_CLASS, true);
                        if (searchControl.getOnLongHover() != null) {
                            searchControl.getOnLongHover().accept(new InvertedTree.ConceptItem(
                                    searchResult.parentConcept().nid(),
                                    searchResult.concept().nid(),
                                    searchResult.parentConcept().description()));
                        }
                    });
                    hoverTransition.playFromStart();
                } else if (hoverTransition != null) {
                    pseudoClassStateChanged(LONG_HOVER_PSEUDO_CLASS, false);
                    hoverTransition.stop();
                }
            }));
            setOnMouseClicked(_ -> {
                if (searchControl.getOnSearchResultClick() != null) {
                    searchControl.getOnSearchResultClick().accept(new InvertedTree.ConceptItem(
                            searchResult.parentConcept().nid(),
                            searchResult.concept().nid(),
                            searchResult.parentConcept().description()));
                }
                searchControl.getChildrenUnmodifiable().stream()
                        .filter(ListView.class::isInstance)
                        .findFirst()
                        .ifPresent(n -> n.setVisible(false));
            });
        }

        /**
         * Since this {@link SearchResultBox} is reused in the cells of a listView, before a new
         * {@link dev.ikm.komet.kview.controls.KLSearchControl.SearchResult} is set, this method performs
         * necessary clean up operations.
         */
        void cleanup() {
            pseudoClassStateChanged(LONG_HOVER_PSEUDO_CLASS, false);
            if (subscription != null) {
                subscription.unsubscribe();
            }
            if (hoverTransition != null) {
                hoverTransition.stop();
                hoverTransition = null;
            }
            descriptionLabel.setTooltip(null);
        }

        /** {@inheritDoc} **/
        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            double maxWidth = getWidth() - snappedLeftInset() - snappedRightInset();
            double parentWidth = snapSizeX(Math.min(parentDescriptionLabel.prefWidth(getHeight()), maxWidth));
            parentDescriptionLabel.resizeRelocate(snappedLeftInset(), snappedTopInset(), parentWidth, parentDescriptionLabel.prefHeight(parentWidth));
            double conceptWidth = snapSizeX(Math.min(descriptionLabel.prefWidth(getHeight()), maxWidth));
            descriptionLabel.resizeRelocate(snappedLeftInset(), snappedTopInset() + parentDescriptionLabel.getHeight() + 1, conceptWidth, descriptionLabel.prefHeight(conceptWidth));
        }

        /**
         * Find and add the background {@link Path} nodes to this {@link SearchResultBox}, highlighting
         * the occurrences of the {@link KLSearchControl#textProperty()} in the
         * {@link ConceptFacade#description()}.
         */
        private void addHighlightPaths() {
            getChildren().removeIf(Path.class::isInstance);
            String text = actualTextProperty.get();
            if (text == null || text.isEmpty()) {
                return;
            }
            String label = descriptionLabel.getText().toLowerCase(Locale.ROOT);
            int lastIndex = 0;
            while (lastIndex != -1) {
                lastIndex = label.indexOf(highlight, lastIndex);
                if (lastIndex != -1) {
                    addBackgroundPath(lastIndex, Math.min(lastIndex + highlight.length(), text.length()));
                    lastIndex += highlight.length();
                    if (lastIndex > text.length()) {
                        break;
                    }
                }
            }
        }

        /**
         * Uses the {@link Text#rangeShape(int, int)} API to get a {@link Path} that
         * can be filled to define the background of a given segment of a {@link Label},
         * simulating a highlighting effect.
         */
        private void addBackgroundPath(int start, int end) {
            final Path path = new Path(actualDescriptionText.rangeShape(start, end));
            path.getStyleClass().add("highlight-path");
            Bounds bounds = actualDescriptionText.getLayoutBounds();
            double graphicWidth = descriptionLabel.getContentDisplay() == ContentDisplay.TEXT_ONLY ? 0 :
                    descriptionLabel.getGraphic().getLayoutBounds().getWidth();
            path.setLayoutX(descriptionLabel.getLayoutX() + graphicWidth - bounds.getMinX());
            path.setLayoutY(descriptionLabel.getLayoutY() - bounds.getMinY());
            path.toBack();
            getChildren().addFirst(path);
        }
    }
}