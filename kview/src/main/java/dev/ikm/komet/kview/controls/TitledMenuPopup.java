package dev.ikm.komet.kview.controls;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Separator;
import javafx.scene.control.Skin;
import javafx.scene.layout.VBox;

/**
 * A Menu Popup that shows a title and below the title a list of Nodes that come from the "items" ObservableList.
 */
public class TitledMenuPopup extends PopupControl {
    public TitledMenuPopup() {
        setAutoHide(true);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TitledMenuPopupSkin(this);
    }

    // -- title
    private StringProperty title = new SimpleStringProperty();
    public String getTitle() { return title.get(); }
    public void setTitle(String title) { this.title.set(title); }
    public StringProperty titleProperty() { return title; }

    // -- items
    ObservableList<Node> items = FXCollections.observableArrayList();
    public ObservableList<Node> getItems() { return items; }

    private static class TitledMenuPopupSkin implements Skin<TitledMenuPopup> {

        private final VBox mainContainer = new VBox();
        private TitledMenuPopup skinnable;
        private final VBox popupContent = new VBox();

        /**
         * Constructor for all SkinBase instances.
         *
         * @param control The control for which this Skin should attach to.
         */
        public TitledMenuPopupSkin(TitledMenuPopup control) {
            this.skinnable = control;

            Label titleLabel = new Label();
            titleLabel.textProperty().bind(control.title);

            Separator separator = new Separator();

            mainContainer.getChildren().addAll(titleLabel, separator, popupContent);

            Bindings.bindContent(popupContent.getChildren(), control.getItems());

            // CSS
            titleLabel.getStyleClass().add("title-label");
            mainContainer.getStyleClass().add("edit-semantic-popup");
        }


        @Override
        public TitledMenuPopup getSkinnable() {
            return skinnable;
        }

        @Override
        public Node getNode() {
            return mainContainer;
        }

        @Override
        public void dispose() {

        }
    }
}
