package dev.ikm.komet.kview.controls;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Separator;
import javafx.scene.control.Skin;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

/**
 * A Menu Popup currently used in Knowledge Layout windows that pops up with options
 * to edit a Section.
 */
public class SectionEditPopup extends PopupControl {
    private static final String POPUP_ENTRY_STYLE_CLASS = "popup-entry";

    public SectionEditPopup() {
        setAutoHide(true);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TitledMenuPopupSkin(this);
    }

    // -- items
    private final ObservableList<Node> items = FXCollections.observableArrayList();
    public ObservableList<Node> getItems() { return items; }

    // -- on create semantic action
    private final ObjectProperty<Runnable> onCreateSemanticAction = new SimpleObjectProperty<>();
    public Runnable getOnCreateSemanticAction() { return onCreateSemanticAction.get(); }
    public ObjectProperty<Runnable> onCreateSemanticActionProperty() { return onCreateSemanticAction; }
    public void setOnCreateSemanticAction(Runnable runnable) { onCreateSemanticAction.set(runnable); }


    /***************************************************************************
     *                                                                         *
     * Supporting Classes                                                      *
     *                                                                         *
     **************************************************************************/

    private static class TitledMenuPopupSkin implements Skin<SectionEditPopup> {

        private final VBox mainContainer = new VBox();
        private SectionEditPopup skinnable;
        private final VBox popupContent = new VBox();

        /**
         * Constructor for all SkinBase instances.
         *
         * @param control The control for which this Skin should attach to.
         */
        public TitledMenuPopupSkin(SectionEditPopup control) {
            this.skinnable = control;

            Label createSemanticLabel = new Label("Create new Semantic", KometIcon.create(KometIcon.IconValue.PLUS));
            createSemanticLabel.getStyleClass().add(POPUP_ENTRY_STYLE_CLASS);
            createSemanticLabel.setOnMousePressed(onCreateSemanticAction(control));

            Separator separator = new Separator();

            Label editSemanticTitleLabel = new Label("EDIT SEMANTIC");

            mainContainer.getChildren().addAll(
                    createSemanticLabel,
                    separator,
                    editSemanticTitleLabel,
                    popupContent
            );

            Bindings.bindContent(popupContent.getChildren(), control.getItems());
            control.items.addListener(this::onItemsChanged);
            control.items.forEach(this::addPopupEntryStyleClass);

            bindVisibilityToList(separator, control.getItems());
            bindVisibilityToList(editSemanticTitleLabel, control.getItems());
            bindVisibilityToList(popupContent, control.getItems());

            // CSS
            editSemanticTitleLabel.getStyleClass().add("title-label");
            mainContainer.getStyleClass().add("edit-semantic-popup");
        }

        private void bindVisibilityToList(Node node, ObservableList<?> list) {
            BooleanBinding hasItems = Bindings.isNotEmpty(list);
            node.visibleProperty().bind(hasItems);
            node.managedProperty().bind(hasItems);
        }

        private EventHandler<MouseEvent> onCreateSemanticAction(SectionEditPopup control) {
            return mouseEvent -> {
                if (control.getOnCreateSemanticAction() != null) {
                    control.getOnCreateSemanticAction().run();
                    control.hide(); // hide popup after the create Semantic action has been executed
                }
            };
        }

        private void onItemsChanged(ListChangeListener.Change<? extends Node> change) {
            while(change.next()) {
                if (change.wasAdded()) {
                    for (Node item : change.getAddedSubList()) {
                        addPopupEntryStyleClass(item);
                    }
                }
            }
        }

        private void addPopupEntryStyleClass(Node item) {
            if (!item.getStyleClass().contains(POPUP_ENTRY_STYLE_CLASS)) {
                item.getStyleClass().add(POPUP_ENTRY_STYLE_CLASS);
            }
        }


        @Override
        public SectionEditPopup getSkinnable() {
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
