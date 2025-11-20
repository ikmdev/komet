package dev.ikm.komet.kview.mvvm.view.landingpage;

import dev.ikm.komet.kview.controls.KometIcon;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import static dev.ikm.komet.kview.controls.KometIcon.IconValue.TRASH;

/**
 * This control represents a Card in the KL Landing Page.
 */
public class KLLandingPageCardControl extends Region {
    private final StackPane cardMainContainer = new StackPane();
    private final VBox cardContainer = new VBox();
    private final StackPane iconContainer = new StackPane();
    private final Region icon = new Region();

    private final HBox bottomContainer = new HBox();
    private final Label titleLabel = new Label();
    private final Region plusIcon = new Region();

    private final Button menuOptionsButton = new Button();

    private final ContextMenu contextMenu = new ContextMenu();

    public KLLandingPageCardControl() {
        getStyleClass().add("card");

        cardMainContainer.getStyleClass().add("main-container");

        cardContainer.getStyleClass().add("card-container");

        iconContainer.getStyleClass().add("icon-container");
        iconContainer.getChildren().add(icon);

        plusIcon.getStyleClass().addAll("icon", "plus-without-circle");
        plusIcon.visibleProperty().bind(createDecorationsProperty());
        plusIcon.managedProperty().bind(createDecorationsProperty());

        bottomContainer.getStyleClass().add("bottom-container");
        bottomContainer.getChildren().addAll(plusIcon, titleLabel);

        titleLabel.textProperty().bind(new StringBinding() {
            {
                super.bind(title);
            }

            @Override
            protected String computeValue() {
                String titleText = title.get();
                if (titleText == null) {
                    return "";
                } else {
                    return titleText.substring(0, 1).toUpperCase() + titleText.substring(1);
                }
            }
        });
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        cardContainer.getChildren().addAll(iconContainer, bottomContainer);

        cardMainContainer.getChildren().addAll(cardContainer, menuOptionsButton);

        initContextMenu();
        initMenuOptionsButton();

        getChildren().add(cardMainContainer);

        // Standard style class
        setIconStyleClass("kl-editable-layout");
    }

    private void initContextMenu() {
        contextMenu.getStyleClass().add("kview-context-menu");

        MenuItem deleteMenuItem = new MenuItem("Delete", KometIcon.create(TRASH));
        deleteMenuItem.setOnAction(event -> {
            if (getOnDeleteAction() != null) {
                getOnDeleteAction().run();
            }
        });
        contextMenu.getItems().add(deleteMenuItem);
    }

    private void initMenuOptionsButton() {
        menuOptionsButton.getStyleClass().add("card-menu-options");

        Region menuOptionsIcon = new Region();
        menuOptionsIcon.getStyleClass().add("card-menu-options-icon");

        menuOptionsButton.setGraphic(menuOptionsIcon);

        StackPane.setAlignment(menuOptionsButton, Pos.TOP_RIGHT);

        menuOptionsButton.setOnAction(event -> {
            contextMenu.setHideOnEscape(true);

            Bounds currentBounds = menuOptionsButton.getLayoutBounds();
            Bounds boundsInScene = menuOptionsButton.localToScreen(currentBounds);

            // Show context menu under button (based on Scene)
            double x = boundsInScene.getMinX();
            double y = boundsInScene.getMaxY() + menuOptionsButton.getInsets().getTop() + menuOptionsButton.getInsets().getBottom();

            contextMenu.show(menuOptionsButton.getScene().getWindow(), x, y);
        });

        menuOptionsButton.setVisible(false);

        addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, mouseEvent -> {
            if (!isDeletable()) {
                return;
            }

            menuOptionsButton.setVisible(true);
        });

        addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, mouseEvent -> {
            if (!isDeletable()) {
                return;
            }

            if (!this.isHover() && !contextMenu.isShowing()) {
                menuOptionsButton.setVisible(false);
            }
        });

        contextMenu.setOnHidden(windowEvent -> {
            if (!this.isHover()) {
                menuOptionsButton.setVisible(false);
            }
        });
    }

    @Override
    protected void layoutChildren() {
        double leftInsets = snappedLeftInset();
        double rightInsets = snappedRightInset();
        double topInsets = snappedTopInset();
        double bottomInsets = snappedBottomInset();
        double width = getWidth();
        double height = getHeight();

        cardMainContainer.resizeRelocate(leftInsets, topInsets,
                width - leftInsets - rightInsets, height - topInsets - bottomInsets);
    }

    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/

    // -- title
    private StringProperty title = new SimpleStringProperty();
    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    public void setTitle(String title) { this.title.set(title); }

    // -- icon style class
    private StringProperty iconStyleClass = new SimpleStringProperty() {
        @Override
        protected void invalidated() {
            icon.getStyleClass().setAll("icon", get());
        }
    };
    public String getIconStyleClass() { return iconStyleClass.get(); }
    public StringProperty iconStyleClassProperty() { return iconStyleClass; }
    public void setIconStyleClass(String iconStyleClass) { this.iconStyleClass.set(iconStyleClass); }

    // -- create decorations
    BooleanProperty createDecorations = new SimpleBooleanProperty(false);
    public boolean isCreateDecorations() { return createDecorations.get(); }
    public BooleanProperty createDecorationsProperty() { return createDecorations; }
    public void setCreateDecorations(boolean createDecorations) { this.createDecorations.set(createDecorations); }

    // -- deletable
    private BooleanProperty deletable = new SimpleBooleanProperty(false);
    public boolean isDeletable() { return deletable.get(); }
    public BooleanProperty deletableProperty() { return deletable; }
    public void setDeletable(boolean deletable) { this.deletable.set(deletable); }

    // on delete action
    private ObjectProperty<Runnable> onDeleteAction = new SimpleObjectProperty<>();
    public Runnable getOnDeleteAction() { return onDeleteAction.get(); }
    public ObjectProperty<Runnable> onDeleteActionProperty() { return onDeleteAction; }
    public void setOnDeleteAction(Runnable onDeleteAction) { this.onDeleteAction.set(onDeleteAction); }
}
