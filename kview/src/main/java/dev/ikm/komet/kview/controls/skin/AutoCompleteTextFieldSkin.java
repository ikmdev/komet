package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.AutoCompleteTextField;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
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
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.util.List;

public class AutoCompleteTextFieldSkin<T> extends FXTextFieldSkin {
    private final AutoCompletePopup autoCompletePopup;

    private Timeline timeline;

    private boolean wasTextChangedFromPopup = false;

    private String lastTypedText;

    /*=*************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     ************************************************************************=*/

    public AutoCompleteTextFieldSkin(AutoCompleteTextField<T> control) {
        super(control);

        timeline = new Timeline();

        control.completerWaitTimeProperty().subscribe(this::onCompleterWaitTimeChanged);

        timeline.setOnFinished(_ -> onSearch());

        control.textProperty().addListener(this::onTextChanged);

        control.addEventHandler(ActionEvent.ACTION, this::onAction);

        autoCompletePopup = new AutoCompletePopup<>(control);

        control.getPopupStyleClasses().add("auto-complete-popup");
        Bindings.bindContent(autoCompletePopup.getStyleClass(), control.getPopupStyleClasses());
    }

    /***************************************************************************
     *                                                                         *
     * Private Implementation                                                  *
     *                                                                         *
     **************************************************************************/

    private void onAction(ActionEvent actionEvent) {
        AutoCompleteTextField<T> textField = (AutoCompleteTextField<T>) getSkinnable();
        int selectedIndex = autoCompletePopup.getSelectedItemIndex();
        if (selectedIndex >= 0) {
            textField.setValue((T) autoCompletePopup.getItems().get(selectedIndex));
        }

        timeline.stop();

        if (autoCompletePopup != null && autoCompletePopup.isShowing()) {
            autoCompletePopup.hide();
        }
    }

    private void onCompleterWaitTimeChanged(Duration newValue) {
        timeline.getKeyFrames().setAll(
                new KeyFrame(newValue)
        );
    }

    private void onTextChanged(Observable observable, String oldValue, String newValue) {
        if (newValue.isEmpty()) {
            lastTypedText = "";
            wasTextChangedFromPopup = false;
            timeline.stop();
            autoCompletePopup.hide();
            return;
        }

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
                Platform.runLater(() -> updateAutoCompletePopupVisibility(textField, results));
                return null;
            }
        };
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void updateAutoCompletePopupVisibility(AutoCompleteTextField<T> autoCompleteTextField, List<T> results) {
        if (results.isEmpty()) {
            autoCompletePopup.hide();
        }

        autoCompletePopup.setPrefWidth(getPopupWidth());

        autoCompletePopup.getItems().setAll(results);

        Point2D textFieldScreenCoords = autoCompleteTextField.localToScreen(autoCompleteTextField.getBoundsInLocal().getMinX(), autoCompleteTextField.getBoundsInLocal().getMaxY());

        if (!autoCompletePopup.isShowing()) {
            autoCompletePopup.setSelectedItemIndex(-1); // When showing popup we initially don't want any suggestion selected
            double x = textFieldScreenCoords.getX() + autoCompleteTextField.getBorder().getInsets().getLeft();
            autoCompletePopup.show(autoCompleteTextField.getScene().getWindow(), x, textFieldScreenCoords.getY());
        }
    }

    protected double getPopupWidth() {
        AutoCompleteTextField<T> autoCompleteTextField = (AutoCompleteTextField<T>) getSkinnable();
        double borderLeft = autoCompleteTextField.getBorder().getInsets().getLeft();
        double borderRight = autoCompleteTextField.getBorder().getInsets().getRight();
        return autoCompleteTextField.getBoundsInLocal().getWidth() - borderLeft - borderRight;
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

            setAutoFix(true);
            setAutoHide(true);
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
        public static final String DEFAULT_STYLE_SHEET = AutoCompleteTextField.class.getResource("auto-complete-popup.css").toExternalForm();

        private final VBox mainContainer;
        private final AutoCompleteTextField<T> autoCompleteTextField;
        private final AutoCompletePopup<T> control;

        protected final ListView<T> autoCompleteListView;

        /**
         * Constructor for AutoCompletePopup Skin instances.
         *
         * @param control The AutoCompletePopup control for which this Skin should attach to.
         */
        public AutoCompletePopupSkin(AutoCompletePopup<T> control, AutoCompleteTextField<T> autoCompleteTextField) {
            this.autoCompleteTextField = autoCompleteTextField;
            this.control = control;

            mainContainer = new VBox();
            autoCompleteListView = new ListView<>();

            mainContainer.getStylesheets().add(DEFAULT_STYLE_SHEET);

            autoCompleteListView.setCellFactory(listView -> {
                ListCell<T> listCell = autoCompleteTextField.getSuggestionsCellFactory().call(listView);
                listCell.setOnMousePressed(event -> {
                    T result = listCell.getItem();

                    setTextFieldText(convertSuggestedObjectToString(result));

                    control.setSelectedItemIndex(control.getItems().indexOf(result));

                    autoCompleteTextField.fireEvent(new ActionEvent());
                });

                listCell.getStyleClass().add("auto-suggest-node");
                return listCell;
            });

            autoCompleteListView.prefWidthProperty().bind(control.prefWidthProperty());

            DoubleBinding heightBinding = new DoubleBinding() {
                {
                    super.bind(control.getItems(), autoCompleteTextField.maxNumberOfSuggestionsProperty());
                }

                @Override
                protected double computeValue() {
                    double cellHeight = autoCompleteTextField.getSuggestionsNodeHeight();
                    return Math.min(autoCompleteTextField.getMaxNumberOfSuggestions(), control.getItems().size()) * cellHeight + 8;
                }
            };

            autoCompleteListView.prefHeightProperty().bind(heightBinding);
            autoCompleteListView.minHeightProperty().bind(heightBinding);
            autoCompleteListView.maxHeightProperty().bind(heightBinding);

            autoCompleteListView.setFocusTraversable(false);

            Bindings.bindContent(autoCompleteListView.getItems(), control.getItems());

            // sync header when suggestions change
            control.getItems().addListener((ListChangeListener<? super T>) _ -> {
                updateHeader(control.getItems());
            });
            updateHeader(control.getItems());


            autoCompleteTextField.addEventFilter(KeyEvent.KEY_PRESSED, this::onKeyPressed);

            control.selectedItemIndexProperty().addListener(this::onSelectedItemChanged);

            // add content to suggestions popup
            if (autoCompleteTextField.getPopupHeaderPane() != null) {
                mainContainer.getChildren().add(autoCompleteTextField.getPopupHeaderPane().createContent());
            }
            mainContainer.getChildren().add(autoCompleteListView);

            // css
            mainContainer.getStyleClass().add("main-container");
        }

        private void updateHeader(List<T> items) {
            if (autoCompleteTextField.getPopupHeaderPane() != null) {
                autoCompleteTextField.getPopupHeaderPane().updateContent(items);
            }
        }

        private void onSelectedItemChanged(Observable observable, Number oldValue, Number newValue) {
            AutoCompleteTextFieldSkin<?> textFieldSkin = ((AutoCompleteTextFieldSkin)autoCompleteTextField.getSkin());

            String newText;
            if (newValue.intValue() >= 0) {
                autoCompleteListView.getSelectionModel().select(control.getSelectedItemIndex());
                T selectedPopupObject = control.getItems().get(newValue.intValue());

                newText = convertSuggestedObjectToString(selectedPopupObject);
            } else {
                autoCompleteListView.getSelectionModel().clearSelection();

                if (oldValue.intValue() == 0 && newValue.intValue() == -1) {
                    newText = textFieldSkin.lastTypedText;
                } else {
                    return;
                }
            }

            setTextFieldText(newText);
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

        private void setTextFieldText(String newText) {
            AutoCompleteTextFieldSkin<?> textFieldSkin = ((AutoCompleteTextFieldSkin)autoCompleteTextField.getSkin());
            textFieldSkin.wasTextChangedFromPopup = true;

            autoCompleteTextField.setText(newText);
        }

        private String convertSuggestedObjectToString(T popupObject) {
            StringConverter<T> stringConverter = autoCompleteTextField.getConverter();

            if (stringConverter != null) {
                return stringConverter.toString(popupObject);
            } else {
                return popupObject.toString();
            }
        }

        @Override
        public AutoCompletePopup<T> getSkinnable() {
            return control;
        }

        @Override
        public Node getNode() {
            return mainContainer;
        }

        @Override
        public void dispose() { }
    }
}