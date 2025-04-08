package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.IconRegion;
import dev.ikm.komet.kview.controls.SearchControl;
import javafx.event.ActionEvent;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.util.Subscription;

public class SearchControlSkin extends SkinBase<SearchControl> {

    private final TextField textField;
    private final StackPane searchPane;
    private final StackPane closePane;
    private final StackPane filterPane;
    private final Subscription subscription;

    public SearchControlSkin(SearchControl control) {
        super(control);

        textField = new TextField();
        searchPane = new StackPane(new IconRegion("icon", "search"));
        searchPane.getStyleClass().add("region");

        closePane = new StackPane(new IconRegion("icon", "close"));
        closePane.getStyleClass().add("region");
        closePane.setOnMouseClicked(_ -> textField.clear());

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

        getChildren().addAll(textField, searchPane, closePane, filterPane);

        textField.textProperty().bindBidirectional(control.textProperty());
        textField.promptTextProperty().bind(control.promptTextProperty());
        textField.onActionProperty().bind(control.onActionProperty());
        closePane.visibleProperty().bind(textField.textProperty().isNotEmpty());
        closePane.managedProperty().bind(closePane.visibleProperty());
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
        filterPane.resizeRelocate(contentWidth - filterPaneWidth + snappedLeftInset(), y + (contentHeight - filterHeight) / 2,
                filterPaneWidth, filterHeight);
    }
}