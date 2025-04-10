package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.AutoCompleteTextField;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class AutoCompleteTextFieldSkin<T> extends TextFieldSkin {
    public static final Duration COMPLETER_WAIT_DURATION = Duration.ZERO;

    private final AutoCompletePopup autoCompletePopup;

    private Timeline timeline;

    private boolean wasTextChangedFromPopup = false;

    private String lastTypedText;

    /*=*************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     ************************************************************************=*/

    public AutoCompleteTextFieldSkin(AutoCompleteTextField control) {
        super(control);

        timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(COMPLETER_WAIT_DURATION));
        timeline.setOnFinished(event -> onSearch());

        control.textProperty().addListener(this::onTextChanged);

        control.addEventHandler(ActionEvent.ACTION, this::onAction);

        autoCompletePopup = new AutoCompletePopup(control);
        autoCompletePopup.getStyleClass().add("auto-complete-popup");
        autoCompletePopup.setAutoFix(true);
    }

    /***************************************************************************
     *                                                                         *
     * Private Implementation                                                  *
     *                                                                         *
     **************************************************************************/

    private void onAction(ActionEvent actionEvent) {
        if (autoCompletePopup != null) {
            autoCompletePopup.hide();
        }
    }

    private void onTextChanged(Observable observable, String oldValue, String newValue) {
        if (wasTextChangedFromPopup) {
            wasTextChangedFromPopup = false;
            return;
        }

        lastTypedText = newValue;
        timeline.playFromStart();
    }

    private void onSearch() {
        AutoCompleteTextField<T> textField = (AutoCompleteTextField<T>) getSkinnable();
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                List<T> results = textField.getCompleter().apply(textField.getText());
                Platform.runLater(() -> showAutoComplete(textField, results));
                return null;
            }
        };
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void showAutoComplete(AutoCompleteTextField<T> autoCompleteTextField, List<T> results) {
        double borderLeft = autoCompleteTextField.getBorder().getInsets().getLeft();
        double borderRight = autoCompleteTextField.getBorder().getInsets().getRight();
        double width = autoCompleteTextField.getBoundsInLocal().getWidth() - borderLeft - borderRight;
        autoCompletePopup.setPrefWidth(width);

        autoCompletePopup.getItems().setAll(results);

        Point2D textFieldScreenCoords = autoCompleteTextField.localToScreen(autoCompleteTextField.getBoundsInLocal().getMinX(), autoCompleteTextField.getBoundsInLocal().getMaxY());
        autoCompletePopup.show(autoCompleteTextField.getScene().getWindow(), textFieldScreenCoords.getX() + borderLeft, textFieldScreenCoords.getY());
    }

    /***************************************************************************
     *                                                                         *
     * Support Classes                                                         *
     *                                                                         *
     **************************************************************************/

    /**********************    AutoComplete Popup    **************************/

    private static class AutoCompletePopup<T> extends PopupControl {
        private final AutoCompleteTextField<T> autoCompleteTextField;

        public AutoCompletePopup(AutoCompleteTextField<T> autoCompleteTextField) {
            this.autoCompleteTextField = autoCompleteTextField;


        }

        @Override
        protected Skin<?> createDefaultSkin() {
            return new AutoCompletePopupSkin<>(this, autoCompleteTextField);
        }

        public void moveSelectionDown() {
            if (selectedItemIndex.get() == items.size() - 1) {
                return;
            }
            setSelectedItemIndex(selectedItemIndex.get() + 1);
        }

        public void moveSelectionUp() {
            if (selectedItemIndex.get() == -1) {
                return; // The min value we can go is -1
            }
            setSelectedItemIndex(selectedItemIndex.get() - 1);
        }

        // -- items
        private final ObservableList<T> items = FXCollections.observableArrayList();
        public ObservableList<T> getItems() { return items; }

        // -- selected item index
        private final IntegerProperty selectedItemIndex = new SimpleIntegerProperty(-1);
        public int getSelectedItemIndex() { return selectedItemIndex.get(); }
        public void setSelectedItemIndex(int value) { selectedItemIndex.set(value); }
        public IntegerProperty selectedItemIndexProperty() { return selectedItemIndex; }
    }

    /**********************    AutoComplete Popupskin    *************************/

    private static class AutoCompletePopupSkin<T> implements Skin<AutoCompletePopup<T>> {
        public static final String DEFAULT_STYLE_CLASS = "auto-complete-popup";
        public static final String DEFAULT_STYLE_SHEET = AutoCompleteTextField.class.getResource("auto-complete-popup.css").toExternalForm();

        private final AutoCompleteTextField<T> autoCompleteTextField;
        private final AutoCompletePopup<T> control;

        private final ListView autoCompleteListView;

        private final List<Node> nodes = new ArrayList<>();

        /**
         * Constructor for AutoCompletePopup Skin instances.
         *
         * @param control The AutoCompletePopup control for which this Skin should attach to.
         */
        public AutoCompletePopupSkin(AutoCompletePopup<T> control, AutoCompleteTextField<T> autoCompleteTextField) {
            this.autoCompleteTextField = autoCompleteTextField;
            this.control = control;

            autoCompleteListView = new ListView<>();
            autoCompleteListView.getStyleClass().add(DEFAULT_STYLE_CLASS);
            autoCompleteListView.getStylesheets().add(DEFAULT_STYLE_SHEET);

            autoCompleteListView.prefWidthProperty().bind(control.prefWidthProperty());

            autoCompleteListView.prefHeightProperty().bind(new DoubleBinding() {
               {
                   super.bind(control.getItems());
               }

               @Override
               protected double computeValue() {
                   double cellHeight = nodes.isEmpty() ? 26 : nodes.get(0).prefHeight(-1) + 9;
                   return control.getItems().size() * cellHeight;
               }
            });

            autoCompleteListView.setFocusTraversable(false);

            control.getItems().addListener(((ListChangeListener.Change<? extends T> _) -> updateItems()));
            updateItems();

            autoCompleteTextField.addEventFilter(KeyEvent.KEY_PRESSED, this::onKeyPressed);

            control.selectedItemIndexProperty().addListener(this::onSelectedItemChanged);

            control.setOnHidden(this::onHidden);
        }

        private void onHidden(WindowEvent windowEvent) {
            control.setSelectedItemIndex(-1);
        }

        private void onSelectedItemChanged(Observable observable, Number oldValue, Number newValue) {
            AutoCompleteTextFieldSkin<?> textFieldSkin = ((AutoCompleteTextFieldSkin)autoCompleteTextField.getSkin());

            String newText = null;
            if (newValue.intValue() >= 0) {
                autoCompleteListView.getSelectionModel().select(control.getSelectedItemIndex());
                newText = control.getItems().get(newValue.intValue()).toString();
            } else {
                autoCompleteListView.getSelectionModel().clearSelection();

                if (oldValue.intValue() == 0 && newValue.intValue() == -1) {
                    newText = textFieldSkin.lastTypedText;
                } else {
                    return;
                }
            }

            textFieldSkin.wasTextChangedFromPopup = true;
            autoCompleteTextField.setText(newText);
            autoCompleteTextField.positionCaret(newText.length());
        }

        private void onKeyPressed(KeyEvent keyEvent) {
            if (!control.isShowing()
                || (keyEvent.getCode() != KeyCode.UP && keyEvent.getCode() != KeyCode.DOWN)) {
                return;
            }

            switch (keyEvent.getCode()) {
                case DOWN -> control.moveSelectionDown();
                case UP -> {
                    control.moveSelectionUp();
                    keyEvent.consume();
                }
                case ESCAPE -> control.hide();
            }
        }

        private void updateItems() {
            autoCompleteListView.getItems().clear();

            for (T result: control.getItems()) {
                Node node = autoCompleteTextField.getResultNodeFactory().apply(result);
                node.getStyleClass().add("auto-suggest-node");
                nodes.add(node);

                node.setOnMousePressed(event -> {
                    autoCompleteTextField.setText(result.toString());
                    control.hide();
                    autoCompleteTextField.fireEvent(new ActionEvent());
                });
                autoCompleteListView.getItems().add(node);
            }
        }

        @Override
        public AutoCompletePopup<T> getSkinnable() {
            return control;
        }

        @Override
        public Node getNode() {
            return autoCompleteListView;
        }

        @Override
        public void dispose() { }
    }
}