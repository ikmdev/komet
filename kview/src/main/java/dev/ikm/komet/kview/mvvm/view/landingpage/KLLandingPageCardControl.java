package dev.ikm.komet.kview.mvvm.view.landingpage;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * This control represents a Card in the KL Landing Page.
 */
public class KLLandingPageCardControl extends Region {
    private final VBox cardMainContainer = new VBox();
    private final StackPane iconContainer = new StackPane();
    private final Region icon = new Region();

    private final HBox bottomContainer = new HBox();
    private final Label titleLabel = new Label();
    private final Region plusIcon = new Region();

    public KLLandingPageCardControl() {
        cardMainContainer.getStyleClass().add("card");

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

        cardMainContainer.getChildren().addAll(iconContainer, bottomContainer);

        getChildren().add(cardMainContainer);

        // Standard style class
        setIconStyleClass("kl-editable-layout");
    }

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
}
