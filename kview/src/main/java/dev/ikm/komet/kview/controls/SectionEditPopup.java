package dev.ikm.komet.kview.controls;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Separator;
import javafx.scene.control.Skin;
import javafx.scene.layout.VBox;

/**
 * A Menu Popup currently used in Knowledge Layout windows that pops up with options
 * to edit a Section.
 */
public class SectionEditPopup extends PopupControl {
    private static final String POPUP_ENTRY_STYLE_CLASS = "popup-entry";

    /** Active on the owner node while this popup is showing, so it can style its edit affordance as pressed. */
    private static final PseudoClass POPUP_SHOWING = PseudoClass.getPseudoClass("popup-showing");

    public SectionEditPopup() {
        setAutoHide(true);

        showingProperty().subscribe(showing -> {
            Node ownerNode = getOwnerNode();
            if (ownerNode != null) {
                ownerNode.pseudoClassStateChanged(POPUP_SHOWING, showing);
            }
        });
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TitledMenuPopupSkin(this);
    }

    // -- items
    private final ObservableList<Node> items = FXCollections.observableArrayList();
    public ObservableList<Node> getItems() { return items; }

    // -- create actions
    /**
     * One entry of the popup's create area: its label text and the action it runs. The popup
     * hides itself after running the action.
     */
    public record CreateAction(String text, Runnable action) {
    }

    /**
     * The entries of the popup's create area, shown above the EDIT SEMANTIC list — typically a
     * single "Create new Semantic" entry, or the set-creation entries of the axiom section.
     */
    private final ObservableList<CreateAction> createActions = FXCollections.observableArrayList();
    public ObservableList<CreateAction> getCreateActions() { return createActions; }


    /***************************************************************************
     *                                                                         *
     * Supporting Classes                                                      *
     *                                                                         *
     **************************************************************************/

    private static class TitledMenuPopupSkin implements Skin<SectionEditPopup> {

        private final VBox mainContainer = new VBox();
        private SectionEditPopup skinnable;
        private final VBox createContent = new VBox();
        private final VBox popupContent = new VBox();

        /**
         * Constructor for all SkinBase instances.
         *
         * @param control The control for which this Skin should attach to.
         */
        public TitledMenuPopupSkin(SectionEditPopup control) {
            this.skinnable = control;

            Separator separator = new Separator();

            Label editSemanticTitleLabel = new Label("EDIT SEMANTIC");

            mainContainer.getChildren().addAll(
                    createContent,
                    separator,
                    editSemanticTitleLabel,
                    popupContent
            );

            control.createActions.addListener((ListChangeListener<? super CreateAction>) change ->
                    rebuildCreateContent(control));
            rebuildCreateContent(control);

            Bindings.bindContent(popupContent.getChildren(), control.getItems());
            control.items.addListener(this::onItemsChanged);
            control.items.forEach(this::addPopupEntryStyleClass);

            // The separator only earns its place between two non-empty areas.
            BooleanBinding hasCreateActions = Bindings.isNotEmpty(control.getCreateActions());
            BooleanBinding hasItems = Bindings.isNotEmpty(control.getItems());
            bindVisibility(separator, hasCreateActions.and(hasItems));
            bindVisibility(createContent, hasCreateActions);
            bindVisibility(editSemanticTitleLabel, hasItems);
            bindVisibility(popupContent, hasItems);

            // CSS
            editSemanticTitleLabel.getStyleClass().add("title-label");
            mainContainer.getStyleClass().add("edit-semantic-popup");
        }

        private void rebuildCreateContent(SectionEditPopup control) {
            createContent.getChildren().clear();
            for (CreateAction createAction : control.getCreateActions()) {
                Label createLabel = new Label(createAction.text(), KometIcon.create(KometIcon.IconValue.PLUS));
                createLabel.getStyleClass().add(POPUP_ENTRY_STYLE_CLASS);
                createLabel.setOnMousePressed(mouseEvent -> {
                    createAction.action().run();
                    control.hide(); // hide popup after the create action has been executed
                });
                createContent.getChildren().add(createLabel);
            }
        }

        private void bindVisibility(Node node, BooleanBinding condition) {
            node.visibleProperty().bind(condition);
            node.managedProperty().bind(condition);
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
