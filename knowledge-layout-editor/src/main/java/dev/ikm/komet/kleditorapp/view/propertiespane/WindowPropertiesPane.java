package dev.ikm.komet.kleditorapp.view.propertiespane;

import dev.ikm.komet.kleditorapp.view.ControlBasePropertiesPane;
import dev.ikm.komet.kview.controls.ToggleSwitch;
import dev.ikm.komet.layout.editor.model.EditorWindowModel;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Subscription;

/**
 * Properties pane shown when the whole Window is selected. Exposes the Window's view size (width and
 * height) and the control-bar options (Coordinate and Timeline) that toggle the matching icons in
 * the Window header.
 *
 * <p>Width and Height are editable combo boxes. They start at {@code Auto} (content-driven sizing);
 * the user can type an integer pixel value or pick {@code Auto} from the drop-down to revert.</p>
 */
public class WindowPropertiesPane extends ControlBasePropertiesPane<EditorWindowModel> {
    public static final String DEFAULT_STYLE_CLASS = "window-properties";

    /** Drop-down entry / display text for a content-driven dimension. */
    private static final String AUTO_LABEL = "Auto";

    private final ComboBox<String> widthComboBox;
    private final ComboBox<String> heightComboBox;

    private final ToggleSwitch coordinateToggleSwitch;
    private final ToggleSwitch timelineToggleSwitch;

    private Subscription widthSubscription;
    private Subscription heightSubscription;

    // Guards against the model -> combo display update being mistaken for a user edit (which would
    // otherwise re-commit and loop).
    private boolean syncing;

    public WindowPropertiesPane() {
        // The Window can't be deleted, so no DELETE button.
        super(false);

        VBox windowMainContainer = new VBox();

        // "WINDOW SIZE" group
        Label sizeTitleLabel = new Label("WINDOW SIZE");
        sizeTitleLabel.getStyleClass().add("group-title");

        GridPane sizeGridPane = createGridPane();

        Label widthLabel = new Label("Width");
        GridPane.setHalignment(widthLabel, HPos.RIGHT);
        sizeGridPane.add(widthLabel, 0, 0);

        widthComboBox = createSizeComboBox();
        wireSizeComboBox(widthComboBox, this::commitWidth);
        sizeGridPane.add(widthComboBox, 1, 0);

        Label heightLabel = new Label("Height");
        GridPane.setHalignment(heightLabel, HPos.RIGHT);
        sizeGridPane.add(heightLabel, 0, 1);

        heightComboBox = createSizeComboBox();
        wireSizeComboBox(heightComboBox, this::commitHeight);
        sizeGridPane.add(heightComboBox, 1, 1);

        Separator separator = new Separator();
        separator.setPrefWidth(200);

        // "CONTROL BAR OPTIONS" group
        Label controlBarTitleLabel = new Label("CONTROL BAR OPTIONS");
        controlBarTitleLabel.getStyleClass().add("group-title");

        GridPane controlBarGridPane = createGridPane();

        Label coordinateLabel = new Label("Coordinate");
        GridPane.setHalignment(coordinateLabel, HPos.RIGHT);
        controlBarGridPane.add(coordinateLabel, 0, 0);

        coordinateToggleSwitch = new ToggleSwitch();
        controlBarGridPane.add(coordinateToggleSwitch, 1, 0);

        Label timelineLabel = new Label("Timeline");
        GridPane.setHalignment(timelineLabel, HPos.RIGHT);
        controlBarGridPane.add(timelineLabel, 0, 1);

        timelineToggleSwitch = new ToggleSwitch();
        controlBarGridPane.add(timelineToggleSwitch, 1, 1);

        windowMainContainer.getChildren().addAll(
                sizeTitleLabel,
                sizeGridPane,
                separator,
                controlBarTitleLabel,
                controlBarGridPane
        );

        mainContainer.setCenter(windowMainContainer);

        // CSS
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        windowMainContainer.getStyleClass().add("window-main-container");
    }

    private ComboBox<String> createSizeComboBox() {
        ComboBox<String> comboBox = new ComboBox<>(FXCollections.observableArrayList(AUTO_LABEL));
        comboBox.setEditable(true);
        comboBox.setMaxWidth(Double.MAX_VALUE);
        return comboBox;
    }

    /**
     * Commits the combo's text to the model on Enter / drop-down selection and on focus loss, so an
     * edit takes effect whether the user presses Enter or just clicks away.
     */
    private void wireSizeComboBox(ComboBox<String> comboBox, Runnable commit) {
        comboBox.setOnAction(event -> commit.run());
        comboBox.getEditor().focusedProperty().subscribe(focused -> {
            if (!focused) {
                commit.run();
            }
        });
    }

    private static GridPane createGridPane() {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(8);
        gridPane.setVgap(8);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(10);
        col1.setPrefWidth(100);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setMinWidth(10);
        col2.setHgrow(Priority.ALWAYS);

        gridPane.getColumnConstraints().addAll(col1, col2);

        return gridPane;
    }

    @Override
    protected void doInit() {
        if (widthSubscription != null) {
            widthSubscription.unsubscribe();
        }
        if (heightSubscription != null) {
            heightSubscription.unsubscribe();
        }
        if (previouslyShownModel != null) {
            coordinateToggleSwitch.selectedProperty().unbindBidirectional(previouslyShownModel.coordinateVisibleProperty());
            timelineToggleSwitch.selectedProperty().unbindBidirectional(previouslyShownModel.timelineVisibleProperty());
        }

        // Keep the combo boxes showing the model's current size (fires immediately to set the
        // initial "Auto"/value display).
        widthSubscription = currentlyShownModel.prefWidthProperty().subscribe(value -> displaySize(widthComboBox, value));
        heightSubscription = currentlyShownModel.prefHeightProperty().subscribe(value -> displaySize(heightComboBox, value));

        coordinateToggleSwitch.selectedProperty().bindBidirectional(currentlyShownModel.coordinateVisibleProperty());
        timelineToggleSwitch.selectedProperty().bindBidirectional(currentlyShownModel.timelineVisibleProperty());
    }

    @Override
    protected void onSessionEnding() {
        if (widthSubscription != null) {
            widthSubscription.unsubscribe();
        }
        if (heightSubscription != null) {
            heightSubscription.unsubscribe();
        }
        coordinateToggleSwitch.selectedProperty().unbindBidirectional(currentlyShownModel.coordinateVisibleProperty());
        timelineToggleSwitch.selectedProperty().unbindBidirectional(currentlyShownModel.timelineVisibleProperty());
    }

    private void commitWidth() {
        commitSize(widthComboBox, currentlyShownModel == null ? null : currentlyShownModel.prefWidthProperty());
    }

    private void commitHeight() {
        commitSize(heightComboBox, currentlyShownModel == null ? null : currentlyShownModel.prefHeightProperty());
    }

    private void commitSize(ComboBox<String> comboBox, DoubleProperty sizeProperty) {
        if (syncing || sizeProperty == null) {
            return;
        }
        double value = parseSize(comboBox.getEditor().getText(), sizeProperty.get());
        sizeProperty.set(value);
        // Reflect the canonical form back (e.g. invalid input snaps to the previous value, "12" -> "12").
        displaySize(comboBox, value);
    }

    private void displaySize(ComboBox<String> comboBox, Number value) {
        syncing = true;
        String text = sizeToText(value);
        comboBox.setValue(text);
        comboBox.getEditor().setText(text);
        syncing = false;
    }

    private static String sizeToText(Number value) {
        if (value == null || value.doubleValue() < 0) {
            return AUTO_LABEL;
        }
        return Integer.toString((int) Math.round(value.doubleValue()));
    }

    private static double parseSize(String text, double fallback) {
        if (text == null) {
            return fallback;
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty() || trimmed.equalsIgnoreCase(AUTO_LABEL)) {
            return EditorWindowModel.AUTO_SIZE;
        }
        try {
            int value = Integer.parseInt(trimmed);
            return value < 0 ? EditorWindowModel.AUTO_SIZE : value;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}