package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.FilterOptionsPopup;
import dev.ikm.komet.kview.controls.LangFilterTitledPane;
import dev.ikm.komet.kview.controls.FilterOptions;
import dev.ikm.komet.kview.controls.IconRegion;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.control.Accordion;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.skin.TitledPaneSkin;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.stage.Window;
import javafx.util.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class LangFilterTitledPaneSkin extends TitledPaneSkin {

    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.filter-options");
    private static final String STYLE_SHEETS = FilterOptionsPopup.class.getResource("filter-options-popup.css").toExternalForm();
    private static final PseudoClass MODIFIED_TITLED_PANE = PseudoClass.getPseudoClass("modified");

    private final Region arrow;
    private final VBox titleVBox;
    private final StackPane revertButton;
    private final LangGridPane selectedOptionPane;
    private final LangContentBox contentBox;
    private final LangFilterTitledPane control;
    private Subscription subscription;
    private ScrollPane scrollPane;
    private FilterOptions.LanguageCoordinates currentLangCoordinates;

    public LangFilterTitledPaneSkin(LangFilterTitledPane control) {
        super(control);
        this.control = control;

        Label titleLabel = new Label(control.getText(), new IconRegion("circle"));
        titleLabel.textProperty().bind(control.titleProperty());
        titleLabel.getStyleClass().add("title-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        revertButton = new StackPane(new IconRegion("icon", "revert"));
        revertButton.setDisable(true);
        revertButton.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            if (!revertButton.isDisable()) {
                control.setLangCoordinates(control.getDefaultLangCoordinates().copy());
                e.consume();
            }
        });
        revertButton.getStyleClass().add("revert-pane");

        HBox titleHBox = new HBox(titleLabel, spacer, revertButton);
        titleHBox.getStyleClass().add("title-hbox");

        selectedOptionPane = new LangGridPane();
        selectedOptionPane.getStyleClass().add("grid-option");

        titleVBox = new VBox(titleHBox, selectedOptionPane);
        titleVBox.getStyleClass().add("title-box");
        control.setGraphic(titleVBox);

        Region titleRegion = (Region) control.lookup(".title");
        arrow = (Region) titleRegion.lookup(".arrow-button");

        arrow.translateXProperty().bind(titleRegion.widthProperty().subtract(arrow.widthProperty().add(arrow.layoutXProperty())));
        arrow.translateYProperty().bind(titleVBox.layoutYProperty().subtract(arrow.layoutYProperty()));
        titleVBox.translateXProperty().bind(arrow.widthProperty().multiply(-1));

        contentBox = new LangContentBox();
        control.setContent(contentBox);

        if (control.getParent() instanceof Accordion accordion) {
            Parent parent = accordion.getParent();
            while (!(parent instanceof ScrollPane)) {
                parent = parent.getParent();
            }

            scrollPane = (ScrollPane) parent;
        }

        control.setOnContextMenuRequested(_ -> {
            if (currentLangCoordinates.getOrdinal() == 0) {
                return;
            }
            DeleteContextMenu contextMenu = new DeleteContextMenu(() -> {
                if (control.getParent() instanceof Accordion accordion) {
                    accordion.getPanes().remove(control);
                }
            });
            Bounds bounds = control.localToScreen(control.getLayoutBounds());
            contextMenu.show(control.getScene().getWindow(), bounds.getMaxX(), bounds.getMinY());
        });

        setupTitledPane();
    }

    private void setupTitledPane() {
        if (subscription != null) {
            subscription.unsubscribe();
        }

        FilterOptions.LanguageCoordinates langCoordinates = control.getLangCoordinates();

        currentLangCoordinates = langCoordinates == null ? null : langCoordinates.copy();
        selectedOptionPane.setLangOption(currentLangCoordinates);
        contentBox.setLangOption(currentLangCoordinates);

        subscription = Subscription.EMPTY;

        subscription = subscription.and(control.heightProperty().subscribe(_ -> {
            if (control.isExpanded()) {
                scrollPane.setVvalue(scrollPane.getVmax()); // TODO: make sure this titlePane is visible
            }
        }));

        // confirm changes
        subscription = subscription.and(control.expandedProperty().subscribe((_, expanded) -> {
            if (!expanded) {
                currentLangCoordinates = contentBox.getLangCoordinates().copy();
                control.setLangCoordinates(currentLangCoordinates.copy());
                boolean modified = !Objects.equals(currentLangCoordinates, control.getDefaultLangCoordinates());
                pseudoClassStateChanged(MODIFIED_TITLED_PANE, modified);
                revertButton.setDisable(!modified);
            }
        }));

        subscription = subscription.and(control.langCoordinatesProperty().subscribe((_, _) -> setupTitledPane()));
    }

    @Override
    public void dispose() {
        arrow.translateXProperty().unbind();
        arrow.translateYProperty().unbind();
        titleVBox.translateXProperty().unbind();
        if (subscription != null) {
            subscription.unsubscribe();
        }
        super.dispose();
    }

    private static class LangGridPane extends GridPane {

        private final Label langOption;
        private final Label dialectOption;
        private final Label patternOption;
        private final Label descriptionOption;
        private final BooleanProperty initialized = new SimpleBooleanProperty();

        public LangGridPane() {
            Label langLabel = new Label(resources.getString("language.option.title"));
            langLabel.getStyleClass().add("title-label");
            add(langLabel, 0, 0);

            Region line1 = new Region();
            line1.getStyleClass().add("line");
            add(line1, 0, 1, 2, 1);

            Label dialectLabel = new Label(resources.getString("dialect.option.title"));
            dialectLabel.getStyleClass().add("title-label");
            add(dialectLabel, 0, 2);

            Region line2 = new Region();
            line2.getStyleClass().add("line");
            add(line2, 0, 3, 2, 1);

            Label patternLabel = new Label(resources.getString("pattern.option.title"));
            patternLabel.getStyleClass().add("title-label");
            add(patternLabel, 0, 4);

            Region line3 = new Region();
            line3.getStyleClass().add("line");
            add(line3, 0, 5, 2, 1);

            Label descriptionLabel = new Label(resources.getString("description.option.title"));
            descriptionLabel.getStyleClass().add("title-label");
            add(descriptionLabel, 0, 6);

            Region line4 = new Region();
            line4.getStyleClass().add("line");
            add(line4, 0, 7, 2, 1);

            langOption = new Label();
            langOption.getStyleClass().add("option-label");
            add(langOption, 1, 0);

            dialectOption = new Label();
            dialectOption.getStyleClass().add("option-label");
            add(dialectOption, 1, 2);

            patternOption = new Label();
            patternOption.getStyleClass().add("option-label");
            add(patternOption, 1, 4);

            descriptionOption = new Label();
            descriptionOption.getStyleClass().add("option-label");
            add(descriptionOption, 1, 6);

            initialized.subscribe(v -> {
                dialectOption.setVisible(v);
                dialectOption.setManaged(v);
                patternOption.setVisible(v);
                patternOption.setManaged(v);
                descriptionOption.setVisible(v);
                descriptionOption.setManaged(v);
            });
            ColumnConstraints columnConstraints1 = new ColumnConstraints();
            columnConstraints1.setMinWidth(80);
            columnConstraints1.setPrefWidth(80);
            columnConstraints1.setMaxWidth(80);
            getColumnConstraints().add(columnConstraints1);
            ColumnConstraints columnConstraints2 = new ColumnConstraints();
            columnConstraints2.setFillWidth(true);
            columnConstraints2.setHgrow(Priority.ALWAYS);
            getColumnConstraints().add(columnConstraints2);

            RowConstraints rowConstraints1 = new RowConstraints();
            rowConstraints1.setMinHeight(20);
            rowConstraints1.setVgrow(Priority.SOMETIMES);
            RowConstraints rowConstraints2 = new RowConstraints();
            rowConstraints2.setPrefHeight(9);
            rowConstraints2.setVgrow(Priority.NEVER);
            RowConstraints rowConstraints3 = new RowConstraints();
            rowConstraints3.setMinHeight(20);
            rowConstraints3.setVgrow(Priority.SOMETIMES);
            RowConstraints rowConstraints4 = new RowConstraints();
            rowConstraints4.setPrefHeight(9);
            rowConstraints4.setVgrow(Priority.NEVER);
            RowConstraints rowConstraints5 = new RowConstraints();
            rowConstraints5.setMinHeight(20);
            rowConstraints5.setVgrow(Priority.SOMETIMES);
            RowConstraints rowConstraints6 = new RowConstraints();
            rowConstraints6.setPrefHeight(9);
            rowConstraints6.setVgrow(Priority.NEVER);
            RowConstraints rowConstraints7 = new RowConstraints();
            rowConstraints7.setMinHeight(20);
            rowConstraints7.setVgrow(Priority.SOMETIMES);
            RowConstraints rowConstraints8 = new RowConstraints();
            rowConstraints8.setPrefHeight(9);
            rowConstraints8.setVgrow(Priority.NEVER);

            getRowConstraints().addAll(
                    rowConstraints1, rowConstraints2,
                    rowConstraints3, rowConstraints4,
                    rowConstraints5, rowConstraints6,
                    rowConstraints7, rowConstraints8
            );

        }

        // langOptionProperty
        private final ObjectProperty<FilterOptions.LanguageCoordinates> langOptionProperty = new SimpleObjectProperty<>(this, "langOption") {
            @Override
            protected void invalidated() {
                FilterOptions.LanguageCoordinates languageOptions = get();
                if (languageOptions != null) {
                    List<String> selectedOptions = languageOptions.getLanguage().selectedOptions();
                    langOption.setText(selectedOptions.isEmpty() || selectedOptions.getFirst() == null ?
                        resources.getString("language.option.empty") : selectedOptions.getFirst());
                    dialectOption.setText(String.join(", ", languageOptions.getDialect().selectedOptions()));
                    patternOption.setText(languageOptions.getPattern().selectedOptions().isEmpty() ? "" : languageOptions.getPattern().selectedOptions().getFirst());
                    descriptionOption.setText(String.join(", ", languageOptions.getDescriptionType().selectedOptions()));
                    initialized.set(!(selectedOptions.isEmpty() || selectedOptions.getFirst() == null));
                } else {
                    initialized.set(false);
                    langOption.setText(null);
                    dialectOption.setText(null);
                    patternOption.setText(null);
                    descriptionOption.setText(null);
                }
            }
        };
        public final ObjectProperty<FilterOptions.LanguageCoordinates> langOptionProperty() {
            return langOptionProperty;
        }
        public final FilterOptions.LanguageCoordinates getLangOption() {
            return langOptionProperty.get();
        }
        public final void setLangOption(FilterOptions.LanguageCoordinates value) {
            langOptionProperty.set(value);
        }

    }

    private static class LangContentBox extends VBox {

        private static final int MAX_ORDER = 5;
        private static final PseudoClass PROMPT_PSEUDO_CLASS = PseudoClass.getPseudoClass("prompt");

        private final ComboBox<String> comboBox;

        private final SortedBox dialectBox;
        private final VBox patternBox;
        private final ToggleGroup patternGroup;
        private final SortedBox descriptionBox;
        private List<String> disabledLanguages;
        private final BooleanProperty initialized = new SimpleBooleanProperty();

        public LangContentBox() {
            Label langLabel = new Label(resources.getString("language.option.title"));
            langLabel.getStyleClass().add("title-label");

            comboBox = new ComboBox<>();
            comboBox.getStyleClass().add("lang-combo-box");
            comboBox.setPromptText(resources.getString("language.option.prompt"));
            comboBox.valueProperty().subscribe(v -> {
                comboBox.pseudoClassStateChanged(PROMPT_PSEUDO_CLASS, v == null);
                initialized.set(v != null);
            });
            comboBox.setCellFactory(_ -> new ListCell<>() {
                private final Label label;
                private final HBox box;
                {
                    label = new Label();
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    StackPane region = new StackPane(new IconRegion("icon", "check"));
                    region.getStyleClass().add("region");
                    box = new HBox(label, spacer, region);
                    box.getStyleClass().add("box");
                    setText(null);
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && !empty) {
                        label.setText(item);
                        setGraphic(box);
                        if (disabledLanguages != null) {
                            setDisable(disabledLanguages.contains(item));
                        } else {
                            setDisable(false);
                        }
                    } else {
                        setGraphic(null);
                    }
                }
            });

            Region line1 = new Region();
            line1.getStyleClass().add("line");

            Label dialectLabel = new Label(resources.getString("dialect.option.title"));
            dialectLabel.getStyleClass().add("title-label");

            dialectBox = new SortedBox();
            dialectBox.getStyleClass().add("sorted-toggle-box");

            Region line2 = new Region();
            line2.getStyleClass().add("line");

            Label patternLabel = new Label(resources.getString("pattern.option.title"));
            patternLabel.getStyleClass().add("title-label");

            patternBox = new VBox();
            patternBox.getStyleClass().add("option-toggle-box");
            patternGroup = new ToggleGroup();

            Region line3 = new Region();
            line3.getStyleClass().add("line");

            Label descriptionLabel = new Label(resources.getString("description.option.title"));
            descriptionLabel.getStyleClass().add("title-label");

            descriptionBox = new SortedBox();
            descriptionBox.getStyleClass().add("sorted-toggle-box");

            getChildren().addAll(
                    langLabel, comboBox, line1,
                    dialectLabel, dialectBox, line2,
                    patternLabel, patternBox, line3,
                    descriptionLabel, descriptionBox);
            getStyleClass().add("content-box");

            initialized.subscribe(v -> {
                dialectBox.setVisible(v);
                dialectBox.setManaged(v);
                patternBox.setVisible(v);
                patternBox.setManaged(v);
                descriptionBox.setVisible(v);
                descriptionBox.setManaged(v);
            });
        }

        // langOptionProperty
        private final ObjectProperty<FilterOptions.LanguageCoordinates> langOptionProperty = new SimpleObjectProperty<>(this, "langOption") {
            @Override
            protected void invalidated() {
                FilterOptions.LanguageCoordinates languageOptions = get();
                if (languageOptions != null) {
                    comboBox.setItems(FXCollections.observableArrayList(languageOptions.getLanguage().availableOptions()));
                    disabledLanguages = new ArrayList<>(languageOptions.getLanguage().excludedOptions());
                    comboBox.setValue(languageOptions.getLanguage().selectedOptions().isEmpty() ?
                            null : languageOptions.getLanguage().selectedOptions().getFirst());

                    dialectBox.setOption(languageOptions.getDialect());
                    patternBox.getChildren().clear();
                    patternBox.getChildren().addAll(languageOptions.getPattern().availableOptions().stream()
                            .map(s -> {
                                ToggleButton tb = new ToggleButton(s, new IconRegion("check"));
                                tb.selectedProperty().subscribe((_, selected) -> {
                                    if (selected) {
                                        patternGroup.getToggles().stream()
                                                .filter(t -> t != tb)
                                                .forEach(t -> t.setSelected(false));
                                    }
                                });
                                tb.getStyleClass().add("option-toggle");
                                tb.setToggleGroup(patternGroup);
                                tb.setSelected(!languageOptions.getPattern().selectedOptions().isEmpty() &&
                                        s.equals(languageOptions.getPattern().selectedOptions().getFirst()));
                                return tb;
                            })
                            .toList());
                    descriptionBox.setOption(languageOptions.getDescriptionType());
                } else {
                    comboBox.setValue(null);
                    disabledLanguages = null;
                    comboBox.getItems().clear();
                    dialectBox.setOption(null);
                    patternBox.getChildren().clear();
                    descriptionBox.setOption(null);
                }
            }
        };
        public final ObjectProperty<FilterOptions.LanguageCoordinates> langOptionProperty() {
            return langOptionProperty;
        }
        public final FilterOptions.LanguageCoordinates getLangOption() {
            return langOptionProperty.get();
        }
        public final void setLangOption(FilterOptions.LanguageCoordinates value) {
            langOptionProperty.set(value);
        }

        public FilterOptions.LanguageCoordinates getLangCoordinates() {
            FilterOptions.LanguageCoordinates languageCoordinates = getLangOption().copy();

            languageCoordinates.getLanguage().selectedOptions().clear();
            languageCoordinates.getLanguage().selectedOptions().add(comboBox.getSelectionModel().getSelectedItem());

            languageCoordinates.getDialect().selectedOptions().clear();
            languageCoordinates.getDialect().selectedOptions().addAll(dialectBox.getSelection());

            languageCoordinates.getPattern().selectedOptions().clear();
            languageCoordinates.getPattern().selectedOptions().add(((ToggleButton) patternGroup.getSelectedToggle()).getText());

            languageCoordinates.getDescriptionType().selectedOptions().clear();
            languageCoordinates.getDescriptionType().selectedOptions().addAll(descriptionBox.getSelection());

            return languageCoordinates;
        }
    }

    private static class SortedBox extends VBox {

        private boolean lock;

        public SortedBox() {
        }

        // optionProperty
        private final ObjectProperty<FilterOptions.Option> optionProperty = new SimpleObjectProperty<>(this, "option") {
            @Override
            protected void invalidated() {
                setupBox(get());
            }
        };
        public final ObjectProperty<FilterOptions.Option> optionProperty() {
           return optionProperty;
        }
        public final FilterOptions.Option getOption() {
           return optionProperty.get();
        }
        public final void setOption(FilterOptions.Option value) {
            optionProperty.set(value);
        }

        public List<String> getSelection() {
            return getChildren().stream()
                    .filter(OrderedToggleBox.class::isInstance)
                    .map(OrderedToggleBox.class::cast)
                    .filter(OrderedToggleBox::isSelected)
                    .map(OrderedToggleBox::getDialect)
                    .toList();
        }

        private void setupBox(FilterOptions.Option option) {
            if (lock) {
                return;
            }
            getChildren().clear();
            if (option == null) {
                return;
            }
            lock = true;
            AtomicInteger counter = new AtomicInteger();
            List<String> selected = option.selectedOptions().stream().toList();
            List<String> rest = new ArrayList<>(option.availableOptions().stream().toList());
            rest.removeAll(selected);

            getChildren().addAll(
                    selected.stream()
                            .map(name -> {
                                int index = counter.getAndIncrement();
                                OrderedToggleBox orderedToggleBox = new OrderedToggleBox(index, name,
                                        d -> {
                                            option.selectedOptions().remove(d);
                                            option.selectedOptions().add(index, d);
                                            setOption(option.copy());
                                        });
                                orderedToggleBox.setSelected(true);
                                orderedToggleBox.selectedProperty().subscribe((_, s) -> {
                                   if (!s) {
                                       option.selectedOptions().remove(name);
                                       setOption(option.copy());
                                   }
                                });
                                return orderedToggleBox;
                            })
                            .toList());
            if (!selected.isEmpty() && !rest.isEmpty()) {
                Region line = new Region();
                HBox.setHgrow(line, Priority.ALWAYS);
                line.getStyleClass().add("line");

                HBox box = new HBox(line);
                box.getStyleClass().add("box");

                getChildren().add(box);
            }
            if (!rest.isEmpty()) {
                getChildren().addAll(
                        rest.stream()
                                .map(name -> {
                                    OrderedToggleBox orderedToggleBox = new OrderedToggleBox(-1, name, null);
                                    orderedToggleBox.selectedProperty().subscribe((_, s) -> {
                                        if (s) {
                                            option.selectedOptions().add(name);
                                            setOption(option.copy());
                                        }
                                    });
                                    return orderedToggleBox;
                                })
                                .toList());
            }
            lock = false;
        }
    }

    private static class OrderedToggleBox extends HBox {

        private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");
        private static final String DRAG_KEY = "DraggedOrderedToggleBox";
        private final String dialect;
        private final Label orderLabel;
        private boolean dragging;

        public OrderedToggleBox(int order, String dialect, Consumer<String> onDragged) {
            this.dialect = dialect;
            orderLabel = new Label();
            orderLabel.getStyleClass().add("order-label");

            Line dottedLine = new Line(0, 0, 16, 0);
            dottedLine.getStyleClass().add("dotted-line");

            addEventHandler(MouseEvent.MOUSE_PRESSED, _ -> dragging = false);
            addEventHandler(MouseEvent.MOUSE_DRAGGED, _ -> dragging = true);
            addEventHandler(MouseEvent.MOUSE_RELEASED, _ -> {
                if (!dragging) {
                    setSelected(!isSelected());
                }
            });

            ToggleButton toggleButton = new ToggleButton(dialect, new IconRegion("check"));
            toggleButton.setMouseTransparent(true);
            toggleButton.getStyleClass().add("option-toggle");
            selectedProperty.bindBidirectional(toggleButton.selectedProperty());

            StackPane dragPane = new StackPane(new IconRegion("icon", "drag"));
            dragPane.getStyleClass().add("drag-pane");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            getChildren().addAll(orderLabel, dottedLine, toggleButton, spacer, dragPane);
            getStyleClass().add("ordered-toggle-box");

            setOnDragOver(e -> {
                Dragboard dragboard = e.getDragboard();
                if (dragboard.hasString() && DRAG_KEY.equals(dragboard.getString())) {
                    e.acceptTransferModes(TransferMode.MOVE);
                    e.consume();
                }
            });
            setOnDragDropped(e -> {
                Dragboard dragboard = e.getDragboard();
                boolean success = false;
                if (dragboard.hasString() && DRAG_KEY.equals(dragboard.getString())) {
                    OrderedToggleBox gestureSource = (OrderedToggleBox) e.getGestureSource();
                    if (onDragged != null) {
                        onDragged.accept(gestureSource.dialect);
                    }
                    success = true;
                }
                e.setDropCompleted(success);
                e.consume();
            });
            setOnDragDetected(e -> {
                if (!isSelected()) {
                    return;
                }
                Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(DRAG_KEY);
                dragboard.setContent(content);
                Image snapshot = createSnapshot();
                dragboard.setDragView(snapshot);
                dragboard.setDragViewOffsetX(e.getX());
                dragboard.setDragViewOffsetY(e.getY());
                e.consume();
            });
            setOrder(order);
        }

        // orderProperty
        private final IntegerProperty orderProperty = new SimpleIntegerProperty(this, "order", -1) {
            @Override
            protected void invalidated() {
                int order = get();
                if (order >= 0 && order < LangContentBox.MAX_ORDER) {
                    orderLabel.setText(resources.getString("dialect.order.item" + (order + 1)));
                } else {
                    orderLabel.setText(null);
                }
            }
        };
        public final IntegerProperty orderProperty() {
           return orderProperty;
        }
        public final int getOrder() {
           return orderProperty.get();
        }
        public final void setOrder(int value) {
            orderProperty.set(value);
        }

        // selectedProperty
        private final BooleanProperty selectedProperty = new SimpleBooleanProperty(this, "selected") {
            @Override
            protected void invalidated() {
                pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, get());
            }
        };
        public final BooleanProperty selectedProperty() {
           return selectedProperty;
        }
        public final boolean isSelected() {
           return selectedProperty.get();
        }
        public final void setSelected(boolean value) {
            selectedProperty.set(value);
        }

        public String getDialect() {
            return dialect;
        }

        private Image createSnapshot() {
            getStyleClass().add("snapshot");
            WritableImage snapshot = snapshot(null, null);
            getStyleClass().remove("snapshot");
            return snapshot;
        }
    }

    private static class DeleteContextMenu extends ContextMenu {

        public DeleteContextMenu(Runnable runnable) {
            setAutoHide(true);

            MenuItem menuItem = new MenuItem(resources.getString("language.menu.remove"), new IconRegion("icon", "delete"));
            menuItem.setOnAction(_ -> {
                if (runnable != null) {
                    runnable.run();
                }
            });
            getItems().addAll(menuItem);
        }

        @Override
        public void show(Window ownerWindow, double anchorX, double anchorY) {
            super.show(ownerWindow, anchorX, anchorY);
            if (!getScene().getStylesheets().contains(STYLE_SHEETS)) {
                getScene().getStylesheets().add(STYLE_SHEETS);
            }
        }
    }
}
