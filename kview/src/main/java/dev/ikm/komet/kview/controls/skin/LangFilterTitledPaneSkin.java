package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.FilterOptionsPopup;
import dev.ikm.komet.kview.controls.FilterOptionsUtils;
import dev.ikm.komet.kview.controls.LangFilterTitledPane;
import dev.ikm.komet.kview.controls.FilterOptions;
import dev.ikm.komet.kview.controls.IconRegion;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    private FilterOptions.LanguageFilterCoordinates currentLangCoordinates;

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

        FilterOptions.LanguageFilterCoordinates langCoordinates = control.getLangCoordinates();

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
                updateModifiedState(currentLangCoordinates);
            }
        }));

        subscription = subscription.and(control.langCoordinatesProperty().subscribe((_, _) -> setupTitledPane()));
        updateModifiedState(currentLangCoordinates);
    }

    private void updateModifiedState(FilterOptions.LanguageFilterCoordinates currentLangCoordinates) {
        boolean langModified = false;
        List<FilterOptions.Option<EntityFacade>> currentOptions = currentLangCoordinates.getOptions();
        List<FilterOptions.Option<EntityFacade>> defaultOptions = control.getDefaultLangCoordinates().getOptions();
        for (int i = 0; i < currentOptions.size(); i++) {
            boolean modified = !Objects.equals(currentOptions.get(i), defaultOptions.get(i));
            if (modified && !currentOptions.get(i).isInOverride()) {
                currentOptions.get(i).setInOverride(true);
            }
            if (currentOptions.get(i).isInOverride() || modified) {
                langModified = true;
            }
        }
        pseudoClassStateChanged(MODIFIED_TITLED_PANE, langModified);
        revertButton.setDisable(!langModified);
    }

    private <T> String getDescription(T t) {
        return FilterOptionsUtils.getDescription(control.getNavigator() == null ? null : control.getNavigator().getViewCalculator(), t);
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

    private class LangGridPane extends GridPane {

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
        private final ObjectProperty<FilterOptions.LanguageFilterCoordinates> langOptionProperty = new SimpleObjectProperty<>(this, "langOption") {
            @Override
            protected void invalidated() {
                FilterOptions.LanguageFilterCoordinates languageOptions = get();
                if (languageOptions != null) {
                    List<EntityFacade> langOptions = languageOptions.getLanguage().selectedOptions();
                    langOption.setText(langOptions.isEmpty() || langOptions.getFirst() == null ?
                        resources.getString("language.option.empty") : getDescription(langOptions.getFirst()));
                    ObservableList<EntityFacade> dialectOptions = languageOptions.getDialect().selectedOptions();
                    // TODO: Dynamically load valid dialects for the selected language
                    dialectOption.setText(dialectOptions.isEmpty() || dialectOptions.getFirst() == null ||
                            (!langOptions.isEmpty() && !TinkarTerm.ENGLISH_LANGUAGE.equals(langOptions.getFirst())) ?
                            resources.getString("dialect.option.empty") : String.join(", ", dialectOptions.stream().map(LangFilterTitledPaneSkin.this::getDescription).toList()));
                    ObservableList<EntityFacade> patternOptions = languageOptions.getPattern().selectedOptions();
                    patternOption.setText(patternOptions.isEmpty() || patternOptions.getFirst() == null ?
                            resources.getString("pattern.option.empty") : patternOptions.isEmpty() ? "" : getDescription(patternOptions.getFirst()));
                    ObservableList<EntityFacade> descriptionOptions = languageOptions.getDescriptionType().selectedOptions();
                    descriptionOption.setText(descriptionOptions.isEmpty() || descriptionOptions.getFirst() == null ?
                            resources.getString("description.option.empty") : String.join(", ", descriptionOptions.stream().map(LangFilterTitledPaneSkin.this::getDescription).toList()));
                    initialized.set(!(langOptions.isEmpty() || langOptions.getFirst() == null));
                } else {
                    initialized.set(false);
                    langOption.setText(null);
                    dialectOption.setText(null);
                    patternOption.setText(null);
                    descriptionOption.setText(null);
                }
            }
        };
        public final ObjectProperty<FilterOptions.LanguageFilterCoordinates> langOptionProperty() {
            return langOptionProperty;
        }
        public final FilterOptions.LanguageFilterCoordinates getLangOption() {
            return langOptionProperty.get();
        }
        public final void setLangOption(FilterOptions.LanguageFilterCoordinates value) {
            langOptionProperty.set(value);
        }

    }

    private class LangContentBox extends VBox {

        private static final int MAX_ORDER = 5;
        private static final PseudoClass PROMPT_PSEUDO_CLASS = PseudoClass.getPseudoClass("prompt");

        private final ComboBox<EntityFacade> comboBox;

        private final SortedBox dialectBox;
        private final VBox patternBox;
        private final ToggleGroup patternGroup;
        private final SortedBox descriptionBox;
        private List<EntityFacade> disabledLanguages;
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
            comboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(EntityFacade item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && !empty) {
                        setText(getDescription(item));
                    } else {
                        setText(null);
                    }
                }
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
                protected void updateItem(EntityFacade item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && !empty) {
                        label.setText(getDescription(item));
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

        private Subscription comboSubscription;

        // langOptionProperty
        private final ObjectProperty<FilterOptions.LanguageFilterCoordinates> langOptionProperty = new SimpleObjectProperty<>(this, "langOption") {
            @Override
            protected void invalidated() {
                if (comboSubscription != null) {
                    comboSubscription.unsubscribe();
                }
                FilterOptions.LanguageFilterCoordinates languageOptions = get();
                if (languageOptions != null) {
                    comboBox.setItems(FXCollections.observableArrayList(languageOptions.getLanguage().availableOptions()));
                    disabledLanguages = new ArrayList<>(languageOptions.getLanguage().excludedOptions());
                    comboBox.setValue(languageOptions.getLanguage().selectedOptions().isEmpty() ?
                            null : languageOptions.getLanguage().selectedOptions().getFirst());

                    comboSubscription = comboBox.getSelectionModel().selectedItemProperty().subscribe(item -> {
                        // TODO: Dynamically load valid dialects for the selected language
                        dialectBox.setVisible(TinkarTerm.ENGLISH_LANGUAGE.equals(item));
                        dialectBox.setManaged(dialectBox.isVisible());
                        if (item != null) {
                            dialectBox.setOption(languageOptions.getDialect());
                        }
                    });
                    patternBox.getChildren().clear();
                    patternBox.getChildren().addAll(languageOptions.getPattern().availableOptions().stream()
                            .map(entity -> {
                                ToggleButton tb = new ToggleButton(getDescription(entity), new IconRegion("check"));
                                tb.setUserData(entity);
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
                                        entity.equals(languageOptions.getPattern().selectedOptions().getFirst()));
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
        public final ObjectProperty<FilterOptions.LanguageFilterCoordinates> langOptionProperty() {
            return langOptionProperty;
        }
        public final FilterOptions.LanguageFilterCoordinates getLangOption() {
            return langOptionProperty.get();
        }
        public final void setLangOption(FilterOptions.LanguageFilterCoordinates value) {
            langOptionProperty.set(value);
        }

        public FilterOptions.LanguageFilterCoordinates getLangCoordinates() {
            FilterOptions.LanguageFilterCoordinates languageCoordinates = getLangOption().copy();

            languageCoordinates.getLanguage().selectedOptions().clear();
            languageCoordinates.getLanguage().selectedOptions().add(comboBox.getSelectionModel().getSelectedItem());

            languageCoordinates.getDialect().selectedOptions().clear();
            languageCoordinates.getDialect().selectedOptions().addAll(dialectBox.getSelection());

            languageCoordinates.getPattern().selectedOptions().clear();
            if (patternGroup.getSelectedToggle() != null) {
                languageCoordinates.getPattern().selectedOptions().add((EntityFacade) ((ToggleButton) patternGroup.getSelectedToggle()).getUserData());
            }

            languageCoordinates.getDescriptionType().selectedOptions().clear();
            languageCoordinates.getDescriptionType().selectedOptions().addAll(descriptionBox.getSelection());

            return languageCoordinates;
        }
    }

    private class SortedBox extends VBox {

        private boolean lock;

        public SortedBox() {
        }

        // optionProperty
        private final ObjectProperty<FilterOptions.Option<EntityFacade>> optionProperty = new SimpleObjectProperty<>(this, "option") {
            @Override
            protected void invalidated() {
                setupBox(get());
            }
        };
        public final ObjectProperty<FilterOptions.Option<EntityFacade>> optionProperty() {
           return optionProperty;
        }
        public final FilterOptions.Option<EntityFacade> getOption() {
           return optionProperty.get();
        }
        public final void setOption(FilterOptions.Option<EntityFacade> value) {
            optionProperty.set(value);
        }

        public List<EntityFacade> getSelection() {
            return getChildren().stream()
                    .filter(OrderedToggleBox.class::isInstance)
                    .map(OrderedToggleBox.class::cast)
                    .filter(OrderedToggleBox::isSelected)
                    .map(OrderedToggleBox::getDialect)
                    .toList();
        }

        private void setupBox(FilterOptions.Option<EntityFacade> option) {
            if (lock) {
                return;
            }
            getChildren().clear();
            if (option == null) {
                return;
            }
            lock = true;
            AtomicInteger counter = new AtomicInteger();
            List<EntityFacade> selected = option.selectedOptions().stream().toList();
            List<EntityFacade> rest = new ArrayList<>(option.availableOptions().stream().toList());
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

    private class OrderedToggleBox extends HBox {

        private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");
        private static final String DRAG_KEY = "DraggedOrderedToggleBox";
        private final EntityFacade dialect;
        private final Label orderLabel;
        private boolean dragging;

        public OrderedToggleBox(int order, EntityFacade dialect, Consumer<EntityFacade> onDragged) {
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

            ToggleButton toggleButton = new ToggleButton(getDescription(dialect), new IconRegion("check"));
            toggleButton.setUserData(dialect);
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

        public EntityFacade getDialect() {
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
