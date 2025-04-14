package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.IconRegion;
import dev.ikm.komet.kview.controls.SearchControl;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Subscription;

import java.util.Locale;

public class SearchControlSkin extends SkinBase<SearchControl> {

    private static final PseudoClass SEARCH_HAS_RESULTS = PseudoClass.getPseudoClass("has-results");

    private final TextField textField;
    private final StackPane searchPane;
    private final StackPane closePane;
    private final StackPane filterPane;
    private Subscription subscription;

    private final ListView<SearchControl.SearchResult> resultsPane;

    public SearchControlSkin(SearchControl control) {
        super(control);

        resultsPane = new ListView<>();
        resultsPane.getStyleClass().add("search-results-list-view");
        resultsPane.setCellFactory(_ -> new ListCell<>() {
            private final SearchResultBox searchResult;
            {
                searchResult = new SearchResultBox(control);
                setText(null);
            }

            @Override
            public void updateIndex(int newIndex) {
                super.updateIndex(newIndex);
                if (newIndex == -1) {
                    searchResult.cleanup();
                }
            }

            @Override
            protected void updateItem(SearchControl.SearchResult item, boolean empty) {
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

        textField = new TextField();
        searchPane = new StackPane(new IconRegion("icon", "search"));
        searchPane.getStyleClass().add("region");

        closePane = new StackPane(new IconRegion("icon", "close"));
        closePane.getStyleClass().add("region");
        closePane.setOnMouseClicked(_ -> {
            textField.clear();
            resultsPane.getItems().clear();
            resultsPane.setVisible(false);
        });

        filterPane = new StackPane(new IconRegion("icon", "filter"));
        filterPane.getStyleClass().add("filter-region");
        filterPane.setOnMouseClicked(_ -> {
            if (control.getOnFilterAction() != null) {
                control.getOnFilterAction().handle(new ActionEvent());
            }
        });
        subscription = control.filterSetProperty().subscribe(isSet -> {
            if (isSet) {
                filterPane.getChildren().setAll(new IconRegion("icon", "filter-dot"), new IconRegion("icon", "dot"));
            } else {
                filterPane.getChildren().setAll(new IconRegion("icon", "filter"));
            }
        });
        subscription = subscription.and(resultsPane.visibleProperty().
                subscribe(b -> control.pseudoClassStateChanged(SEARCH_HAS_RESULTS, b)));

        getChildren().addAll(textField, searchPane, closePane, filterPane, resultsPane);

        textField.textProperty().bindBidirectional(control.textProperty());
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
    }

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
        super.dispose();
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        double prefHeight = textField.prefHeight(width) + snappedTopInset() + snappedBottomInset();
        if (resultsPane.isVisible()) {
            prefHeight += 12 + resultsPane.prefHeight(width);
        }
        return prefHeight;
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        double x = snapPositionX(contentX);
        double y = snapPositionY(contentY);
        double searchPaneWidth = snapSizeX(searchPane.prefWidth(contentHeight));
        double closePaneWidth = snapSizeX(closePane.prefWidth(contentHeight));
        double filterPaneWidth = snapSizeX(filterPane.prefWidth(contentHeight));
        double textFieldWidth = contentWidth - filterPaneWidth - 8;
        double textFieldHeight = snapSizeY(textField.prefHeight(textFieldWidth));
        textField.resizeRelocate(x, y, textFieldWidth, textFieldHeight);

        double searchHeight = snapSizeY(searchPane.prefHeight(searchPaneWidth));
        searchPane.resizeRelocate(x + 8, y + (textFieldHeight - searchHeight) / 2,
                searchPaneWidth, searchHeight);

        double closeHeight = snapSizeY(closePane.prefHeight(searchPaneWidth));
        closePane.resizeRelocate(x + textFieldWidth - 4 - closePaneWidth, y + (textFieldHeight - closeHeight) / 2,
                closePaneWidth, closeHeight);

        double filterHeight = snapSizeY(filterPane.prefHeight(filterPaneWidth));
        filterPane.resizeRelocate(contentWidth - filterPaneWidth + snappedLeftInset(), y + (textFieldHeight - filterHeight) / 2,
                filterPaneWidth, filterHeight);

        if (resultsPane.isVisible()) {
            double resultsHeight = snapSizeY(resultsPane.prefHeight(contentWidth));
            resultsPane.resizeRelocate(x,y + textFieldHeight + 12, contentWidth, resultsHeight);
        }
    }

    static class SearchResultBox extends Pane {

        private static final PseudoClass LONG_HOVER_PSEUDO_CLASS = PseudoClass.getPseudoClass("long-hover");

        private final Label parentDescriptionLabel;
        private final Label descriptionLabel;
        private final StackPane selectStackPane;
        private Text actualDescriptionText;
        private String highlight;
        private PauseTransition hoverTransition;
        private Subscription subscription;
        private SearchControl.SearchResult searchResult;

        public SearchResultBox(SearchControl searchControl) {
            subscription = Subscription.EMPTY;

            parentDescriptionLabel = new Label();
            parentDescriptionLabel.getStyleClass().add("parent-description-label");

            IconRegion iconRegion = new IconRegion("icon", "curve");
            StackPane stackPane = new StackPane(iconRegion);
            stackPane.getStyleClass().add("region");
            descriptionLabel = new Label(null, stackPane) {
                @Override
                protected Skin<?> createDefaultSkin() {
                    Skin<?> defaultSkin = super.createDefaultSkin();
                    actualDescriptionText = getChildren().stream()
                            .filter(Text.class::isInstance)
                            .map(Text.class::cast)
                            .findFirst()
                            .orElseThrow();
                    subscription = subscription.and(actualDescriptionText.textProperty().subscribe(text -> {
                        Platform.runLater(() -> addHighlightPaths(highlight));
                        if (text != null && !text.isEmpty() && !text.equals(getText())) {
                            setTooltip(new Tooltip(getText()));
                        }
                    }));
                    return defaultSkin;
                }
            };
            descriptionLabel.contentDisplayProperty().bind(Bindings.when(parentDescriptionLabel.textProperty().isNotEmpty())
                    .then(ContentDisplay.LEFT).otherwise(ContentDisplay.TEXT_ONLY));
            descriptionLabel.getStyleClass().add("description-label");

            IconRegion selectIcon = new IconRegion("icon", "select");
            selectStackPane = new StackPane(selectIcon);
            selectStackPane.getStyleClass().add("select-region");

            getChildren().addAll(parentDescriptionLabel, descriptionLabel, selectStackPane);

            subscription = subscription.and(hoverProperty().subscribe(h -> {
                if (h) {
                    hoverTransition = new PauseTransition(new Duration(searchControl.getActivation()));
                    hoverTransition.setOnFinished(_ -> pseudoClassStateChanged(LONG_HOVER_PSEUDO_CLASS, true));
                    hoverTransition.playFromStart();
                } else if (hoverTransition != null) {
                    pseudoClassStateChanged(LONG_HOVER_PSEUDO_CLASS, false);
                    hoverTransition.stop();
                }
            }));
            getStyleClass().add("search-result");
            getStylesheets().add(SearchControl.class.getResource("search-control.css").toExternalForm());
        }

        void setSearchResult(SearchControl.SearchResult result) {
            this.searchResult = result;
            highlight = result.highlight().toLowerCase(Locale.ROOT);
            parentDescriptionLabel.setText(result.parentConcept() != null ? result.parentConcept().description() : null);
            descriptionLabel.setText(result.concept().description());
        }

        void cleanup() {
            pseudoClassStateChanged(LONG_HOVER_PSEUDO_CLASS, false);
            if (subscription != null) {
                subscription.unsubscribe();
            }
            if (hoverTransition != null) {
                hoverTransition.stop();
                hoverTransition = null;
            }
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            double selectHeight = snapSizeX(selectStackPane.prefHeight(getWidth()));
            double selectWidth = snapSizeY(selectStackPane.prefWidth(getHeight()));
            selectStackPane.resizeRelocate(getWidth() - snappedRightInset() - selectWidth,
                    (getHeight() - snappedTopInset() - snappedBottomInset() - selectHeight) / 2 + snappedTopInset(),
                    selectWidth, selectHeight);
            double maxWidth = getWidth() - snappedLeftInset() - selectWidth - snappedRightInset();
            double parentWidth = snapSizeX(Math.min(parentDescriptionLabel.prefWidth(getHeight()), maxWidth));
            parentDescriptionLabel.resizeRelocate(snappedLeftInset(), snappedTopInset(), parentWidth, parentDescriptionLabel.prefHeight(parentWidth));
            double conceptWidth = snapSizeX(Math.min(descriptionLabel.prefWidth(getHeight()), maxWidth));
            descriptionLabel.resizeRelocate(snappedLeftInset(), snappedTopInset() + parentDescriptionLabel.getHeight() + 1, conceptWidth, descriptionLabel.prefHeight(conceptWidth));
        }

        private void addHighlightPaths(String highlight) {
            getChildren().removeIf(Path.class::isInstance);
            String text = actualDescriptionText.getText();
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