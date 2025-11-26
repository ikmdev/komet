package dev.ikm.komet.kleditorapp.view;

import dev.ikm.komet.kleditorapp.view.control.EditorWindowBaseControl;
import dev.ikm.komet.kleditorapp.view.control.PatternViewControl;
import dev.ikm.komet.kleditorapp.view.control.SectionViewControl;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class PropertiesPane extends Region {
    public static String DEFAULT_STYLE_CLASS = "right-properties-pane";

    private final BorderPane mainContainer = new BorderPane();
    private final StackPane controlPropertiesContainer = new StackPane();

    private final SectionPropertiesPane sectionPropertiesPane = new SectionPropertiesPane();
    private final PatternPropertiesPane patternPropertiesPane = new PatternPropertiesPane();

    private ControlBasePropertiesPane currentPropertiesPane;

    public PropertiesPane() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);

        // Title
        Label titleLabel = new Label();
        titleLabel.getStyleClass().add("title");
        titleLabel.textProperty().bind(title);

        mainContainer.setTop(titleLabel);

        mainContainer.setCenter(controlPropertiesContainer);
        controlPropertiesContainer.getChildren().add(sectionPropertiesPane);
        controlPropertiesContainer.getChildren().add(patternPropertiesPane);

        getChildren().add(mainContainer);
    }

    public void setCurrentPropertiesPane(ControlBasePropertiesPane controlBasePropertiesPane) {
        for (Node child : controlPropertiesContainer.getChildren()) {
            child.setVisible(child == controlBasePropertiesPane);
            child.setManaged(child == controlBasePropertiesPane);
        }
        currentPropertiesPane = controlBasePropertiesPane;
    }

    public void init(SelectionManager selectionManager) {
        selectionManager.selectedControlProperty().subscribe(() -> {
            EditorWindowBaseControl control = selectionManager.getSelectedControl();
            switch (control) {
                case SectionViewControl sectionView -> {
                    setTitle(sectionView.getTagText());
                    setCurrentPropertiesPane(sectionPropertiesPane);
                }
                case PatternViewControl patternView -> {
                    setTitle("Pattern");
                    setCurrentPropertiesPane(patternPropertiesPane);
                }
                default -> System.out.println("TODO...");
            }
            currentPropertiesPane.initControl(control);

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

        mainContainer.resizeRelocate(leftInsets, topInsets,
                width - leftInsets - rightInsets, height - topInsets - bottomInsets);
    }

    // -- title
    private final StringProperty title = new SimpleStringProperty();
    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    public void setTitle(String title) { this.title.set(title); }
}