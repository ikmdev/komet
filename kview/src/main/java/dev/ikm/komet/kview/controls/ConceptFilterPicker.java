package dev.ikm.komet.kview.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.css.PseudoClass;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.shape.SVGPath;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * The picker the concept navigator's "Show" pill opens: it asks for a pattern, which is a filter
 * by itself. A link under the pattern opens the part that narrows the filter to one of the
 * pattern's fields and the value that field must have; that part lists the values in use so one
 * can be chosen without knowing it beforehand.
 * <p>
 * The picker only presents and collects choices. The patterns, a pattern's fields, the values in
 * use and the summary line are handed to it; it reports the pattern, whether the filter is
 * narrowed, the field and value that were chosen, and when Apply, Clear or Close is pressed.
 */
public class ConceptFilterPicker extends VBox {

    private static final PseudoClass OPEN_PSEUDO_CLASS = PseudoClass.getPseudoClass("open");

    /** The fields of a pattern are shown as one joined control up to this many, as chips beyond. */
    private static final int MAX_JOINED_FIELDS = 3;
    private static final double ROW_HEIGHT = 28;
    private static final int MAX_VISIBLE_ROWS = 5;
    private static final double VALUES_COUNT_TOOLTIP_WIDTH = 260;

    private final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.concept-filter");
    private final NumberFormat countFormat = NumberFormat.getIntegerInstance();

    private final TextField patternField = new TextField();
    private final Button patternClearButton = iconButton("M6 6l12 12M18 6L6 18", "picker-row-action");
    private final FilteredList<Choice> shownPatterns;
    private final ListView<Choice> patternList;
    private final Button narrowButton = new Button(resources.getString("picker.narrow"));
    private final VBox narrowPane = new VBox();
    private final VBox fieldsPane = new VBox();
    private final ToggleGroup fieldsGroup = new ToggleGroup();
    private final TextField valueField = new TextField();
    private final FilteredList<Choice> shownValues;
    private final ListView<Choice> valueList;
    private final Label valuesMessageLabel = new Label();
    private final HBox valuesHeader = new HBox();
    private final TextFlow summaryFlow = new TextFlow();

    /** Set while the picker itself writes into a text field, so the write is not read as typing. */
    private boolean updatingText;

    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    public ConceptFilterPicker() {
        shownPatterns = new FilteredList<>(patterns);
        patternList = createList(shownPatterns);
        shownValues = new FilteredList<>(values);
        valueList = createList(shownValues);

        // header
        Label title = new Label(resources.getString("picker.title"));
        title.getStyleClass().add("picker-title");
        Region headSpacer = new Region();
        HBox.setHgrow(headSpacer, Priority.ALWAYS);
        Button closeButton = iconButton("M6 6l12 12M18 6L6 18", "picker-icon-button");
        closeButton.setOnAction(_ -> run(getOnClose()));
        Group funnel = lineIcon("M3 5h18l-7 8.5V19l-4 2v-7.5z", "accent");
        HBox head = new HBox(funnel, title, headSpacer, closeButton);
        head.getStyleClass().add("picker-head");

        Label strip = new Label(resources.getString("picker.hint"));
        strip.setWrapText(true);
        strip.setMaxWidth(Double.MAX_VALUE);
        strip.getStyleClass().add("picker-strip");

        // pattern
        Label patternLabel = new Label(resources.getString("picker.pattern"));
        patternLabel.getStyleClass().add("picker-label");
        patternField.setPromptText(resources.getString("picker.pattern.prompt"));
        HBox.setHgrow(patternField, Priority.ALWAYS);
        patternClearButton.setOnAction(_ -> {
            setSelectedPattern(null);
            patternField.requestFocus();
        });
        HBox patternBox = new HBox(lineIcon("M11 4.5a6.5 6.5 0 1 0 0 13a6.5 6.5 0 1 0 0-13M16 16l4.5 4.5"), patternField, patternClearButton);
        patternBox.getStyleClass().add("picker-input");
        patternField.textProperty().subscribe(() -> {
            if (!updatingText) {
                setSelectedPattern(null);
                filter(shownPatterns, patternField.getText());
            }
        });
        patternList.getSelectionModel().selectedItemProperty().subscribe(choice -> {
            if (choice != null) {
                setSelectedPattern(choice);
            }
        });

        // the link that opens the field and value part
        Region narrowArrow = new Region();
        narrowArrow.getStyleClass().add("picker-disclosure-arrow");
        narrowButton.setGraphic(narrowArrow);
        narrowButton.getStyleClass().setAll("picker-disclosure");
        narrowButton.setOnAction(_ -> setNarrowed(!isNarrowed()));

        // field
        Label fieldLabel = new Label(resources.getString("picker.field"));
        fieldLabel.getStyleClass().add("picker-label");
        fieldsGroup.selectedToggleProperty().subscribe(toggle ->
                setSelectedFieldIndex(toggle == null ? -1 : (Integer) toggle.getUserData()));

        // value
        Label valueLabel = new Label(resources.getString("picker.value"));
        valueLabel.getStyleClass().add("picker-label");
        Label valueOperator = new Label(resources.getString("picker.value.is"));
        valueOperator.getStyleClass().add("picker-operator");
        HBox valueLabels = new HBox(valueLabel, valueOperator);
        valueLabels.getStyleClass().add("picker-label-row");
        valueField.setPromptText(resources.getString("picker.value.prompt"));
        HBox.setHgrow(valueField, Priority.ALWAYS);
        HBox valueBox = new HBox(lineIcon("M11 4.5a6.5 6.5 0 1 0 0 13a6.5 6.5 0 1 0 0-13M16 16l4.5 4.5"), valueField);
        valueBox.getStyleClass().add("picker-input");
        valueField.textProperty().subscribe(() -> {
            if (!updatingText) {
                setSelectedValue(null);
                setValueText(valueField.getText());
                filter(shownValues, valueField.getText());
            }
        });
        valueList.getSelectionModel().selectedItemProperty().subscribe(choice -> {
            if (choice != null) {
                setSelectedValue(choice);
            }
        });

        Label valuesTitle = new Label(resources.getString("picker.values.title"));
        Region valuesSpacer = new Region();
        HBox.setHgrow(valuesSpacer, Priority.ALWAYS);
        Label valuesCountTitle = new Label(resources.getString("picker.values.count"));
        valuesCountTitle.getStyleClass().add("picker-values-count-title");
        Tooltip valuesCountTooltip = new Tooltip(resources.getString("picker.values.count.tooltip"));
        valuesCountTooltip.setWrapText(true);
        valuesCountTooltip.setMaxWidth(VALUES_COUNT_TOOLTIP_WIDTH);
        valuesCountTitle.setTooltip(valuesCountTooltip);
        valuesHeader.getChildren().addAll(valuesTitle, valuesSpacer, valuesCountTitle);
        valuesHeader.getStyleClass().add("picker-values-header");
        valuesMessageLabel.setWrapText(true);
        valuesMessageLabel.setMaxWidth(Double.MAX_VALUE);
        valuesMessageLabel.getStyleClass().add("picker-values-message");

        narrowPane.getChildren().addAll(fieldLabel, fieldsPane, valueLabels, valueBox, valuesHeader, valueList, valuesMessageLabel);
        narrowPane.getStyleClass().add("picker-narrow-pane");
        VBox body = new VBox(patternLabel, patternBox, patternList, narrowButton, narrowPane);
        body.getStyleClass().add("picker-body");

        // footer
        summaryFlow.getStyleClass().add("picker-summary");
        summaryFlow.setMaxHeight(Region.USE_PREF_SIZE);
        Region footSpacer = new Region();
        HBox.setHgrow(footSpacer, Priority.ALWAYS);
        Button clearButton = new Button(resources.getString("picker.clear"));
        clearButton.getStyleClass().setAll("picker-link");
        clearButton.setOnAction(_ -> run(getOnClear()));
        Button applyButton = new Button(resources.getString("picker.apply"));
        applyButton.getStyleClass().setAll("picker-apply");
        applyButton.disableProperty().bind(applyDisabled);
        applyButton.setOnAction(_ -> run(getOnApply()));
        HBox foot = new HBox(summaryFlow, footSpacer, clearButton, applyButton);
        foot.getStyleClass().add("picker-foot");

        getChildren().addAll(head, strip, body, foot);
        getStyleClass().add("concept-filter-picker");
        getStylesheets().add(ConceptFilterPicker.class.getResource("concept-filter.css").toExternalForm());

        selectedPattern.subscribe(this::showSelectedPattern);
        selectedValue.subscribe(this::showSelectedValue);
        selectedPattern.subscribe(this::showNarrowing);
        narrowed.subscribe(this::showNarrowing);
        fields.subscribe(this::showNarrowing);
        fields.subscribe(this::rebuildFields);
        selectedFieldIndex.subscribe(index -> selectFieldToggle(index.intValue()));
        values.subscribe(() -> filter(shownValues, valueField.getText()));
        valuesMessage.subscribe(this::showValuesMessage);
        summary.subscribe(this::showSummary);
        valueText.subscribe(text -> {
            if (!valueField.getText().equals(text)) {
                setFieldText(valueField, text);
            }
        });
        rebuildFields();
        showNarrowing();
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    /**
     * Puts the keyboard focus where the next choice is made.
     */
    public void focusNextChoice() {
        if (getSelectedPattern() == null) {
            patternField.requestFocus();
        } else if (narrowPane.isVisible()) {
            valueField.requestFocus();
        }
    }

    /***************************************************************************
     *                                                                         *
     * Private Implementation                                                  *
     *                                                                         *
     **************************************************************************/

    private static void run(Runnable action) {
        if (action != null) {
            action.run();
        }
    }

    /** A line icon: a round 1.8-unit stroke on a 24-unit grid, drawn at 16px. */
    private static Group lineIcon(String content, String... styleClasses) {
        SVGPath path = new SVGPath();
        path.setContent(content);
        path.setScaleX(16 / 24d);
        path.setScaleY(16 / 24d);
        path.getStyleClass().add("picker-line-icon");
        path.getStyleClass().addAll(styleClasses);
        return new Group(path);
    }

    private static Button iconButton(String content, String styleClass) {
        Button button = new Button();
        button.setGraphic(lineIcon(content));
        button.getStyleClass().setAll(styleClass);
        return button;
    }

    private static void filter(FilteredList<Choice> list, String text) {
        String needle = text == null ? "" : text.strip().toLowerCase(Locale.ROOT);
        list.setPredicate(needle.isEmpty() ? null : choice -> choice.label().toLowerCase(Locale.ROOT).contains(needle));
    }

    /** A list of choices: the name at the left, the count at the right, as tall as its rows up to five. */
    private ListView<Choice> createList(FilteredList<Choice> items) {
        ListView<Choice> list = new ListView<>(items);
        list.setFixedCellSize(ROW_HEIGHT);
        list.getStyleClass().add("picker-list");
        list.setCellFactory(_ -> new ListCell<>() {
            private final Label name = new Label();
            private final Label count = new Label();
            private final HBox row = new HBox(name, spacer(), count);

            {
                name.getStyleClass().add("picker-choice-name");
                count.getStyleClass().add("picker-choice-count");
                row.getStyleClass().add("picker-choice");
            }

            @Override
            protected void updateItem(Choice choice, boolean empty) {
                super.updateItem(choice, empty);
                if (empty || choice == null) {
                    setGraphic(null);
                } else {
                    name.setText(choice.label());
                    count.setText(choice.count() < 0 ? "" : countFormat.format(choice.count()));
                    setGraphic(row);
                }
            }
        });
        items.subscribe(() -> sizeList(list));
        sizeList(list);
        return list;
    }

    private static Region spacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private static void sizeList(ListView<Choice> list) {
        double height = Math.min(list.getItems().size(), MAX_VISIBLE_ROWS) * ROW_HEIGHT + 2;
        list.setMinHeight(height);
        list.setPrefHeight(height);
        list.setMaxHeight(height);
    }

    private static void show(Region node, boolean shown) {
        node.setVisible(shown);
        node.setManaged(shown);
    }

    /** A chosen pattern is named in the box and its list is put away; none brings the list back. */
    private void showSelectedPattern(Choice choice) {
        if (choice != null) {
            setFieldText(patternField, choice.label());
        } else if (!patternField.isFocused()) {
            setFieldText(patternField, "");
        }
        if (choice == null) {
            patternList.getSelectionModel().clearSelection();
            filter(shownPatterns, patternField.getText());
        }
        show(patternList, choice == null);
        show(patternClearButton, choice != null);
    }

    /** The link shows once a pattern that has fields is chosen; the field and value part while the link is open. */
    private void showNarrowing() {
        boolean canNarrow = getSelectedPattern() != null && !fields.isEmpty();
        show(narrowButton, canNarrow);
        show(narrowPane, canNarrow && isNarrowed());
        narrowButton.pseudoClassStateChanged(OPEN_PSEUDO_CLASS, isNarrowed());
    }

    private void showSelectedValue(Choice choice) {
        if (choice != null) {
            setFieldText(valueField, choice.label());
            setValueText(choice.label());
            filter(shownValues, null);
            valueList.getSelectionModel().select(choice);
        } else {
            valueList.getSelectionModel().clearSelection();
        }
    }

    private void setFieldText(TextField field, String text) {
        updatingText = true;
        field.setText(text);
        field.positionCaret(text.length());
        updatingText = false;
    }

    /** Up to three fields make one joined control across the picker; more wrap as separate chips. */
    private void rebuildFields() {
        fieldsGroup.getToggles().clear();
        boolean joined = fields.size() <= MAX_JOINED_FIELDS;
        Pane container = joined ? new HBox() : new FlowPane();
        container.getStyleClass().add(joined ? "picker-segments" : "picker-chips");
        for (int index = 0; index < fields.size(); index++) {
            ToggleButton toggle = new ToggleButton(fields.get(index));
            toggle.setUserData(index);
            toggle.setToggleGroup(fieldsGroup);
            toggle.getStyleClass().setAll("picker-field");
            if (joined) {
                toggle.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(toggle, Priority.ALWAYS);
                if (index > 0) {
                    toggle.getStyleClass().add("following");
                }
            }
            container.getChildren().add(toggle);
        }
        if (fields.isEmpty()) {
            Label none = new Label(resources.getString("picker.field.none"));
            none.getStyleClass().add("picker-values-message");
            container.getChildren().add(none);
        }
        fieldsPane.getChildren().setAll(container);
        fieldsPane.getStyleClass().setAll("picker-fields");
        selectFieldToggle(getSelectedFieldIndex());
    }

    private void selectFieldToggle(int index) {
        fieldsGroup.getToggles().stream()
                .filter(toggle -> (Integer) toggle.getUserData() == index)
                .findFirst()
                .ifPresentOrElse(fieldsGroup::selectToggle, () -> fieldsGroup.selectToggle(null));
    }

    /** The summary as runs of text: the parts between asterisks in bold, the asterisks left out. */
    private void showSummary(String text) {
        summaryFlow.getChildren().clear();
        if (text == null) {
            return;
        }
        String[] parts = text.split("\\*", -1);
        for (int index = 0; index < parts.length; index++) {
            if (!parts[index].isEmpty()) {
                Text part = new Text(parts[index]);
                part.getStyleClass().add(index % 2 == 1 ? "picker-summary-strong" : "picker-summary-text");
                summaryFlow.getChildren().add(part);
            }
        }
    }

    private void showValuesMessage(String message) {
        valuesMessageLabel.setText(message);
        show(valuesMessageLabel, message != null);
        show(valueList, message == null);
        show(valuesHeader, message == null);
    }

    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/

    // -- patterns
    /**
     * The patterns that can be chosen. A choice's key is whatever identifies the pattern.
     */
    private final ObservableList<Choice> patterns = FXCollections.observableArrayList();
    public ObservableList<Choice> getPatterns() { return patterns; }

    // -- fields
    /**
     * The names of the chosen pattern's fields, in the pattern's order.
     */
    private final ObservableList<String> fields = FXCollections.observableArrayList();
    public ObservableList<String> getFields() { return fields; }

    // -- values
    /**
     * The values the chosen field has, each with the number of components that carry it.
     */
    private final ObservableList<Choice> values = FXCollections.observableArrayList();
    public ObservableList<Choice> getValues() { return values; }

    // -- selectedPattern
    /**
     * The pattern that was chosen, or null while none is.
     */
    private final ObjectProperty<Choice> selectedPattern = new SimpleObjectProperty<>(this, "selectedPattern");
    public ObjectProperty<Choice> selectedPatternProperty() { return selectedPattern; }
    public Choice getSelectedPattern() { return selectedPattern.get(); }
    public void setSelectedPattern(Choice value) { selectedPattern.set(value); }

    // -- narrowed
    /**
     * Whether the part that narrows the filter to a field value is open. While it is closed, or
     * the pattern has no fields, the pattern alone is the filter.
     */
    private final BooleanProperty narrowed = new SimpleBooleanProperty(this, "narrowed");
    public BooleanProperty narrowedProperty() { return narrowed; }
    public boolean isNarrowed() { return narrowed.get(); }
    public void setNarrowed(boolean value) { narrowed.set(value); }

    // -- selectedFieldIndex
    /**
     * The index of the field that was chosen among {@link #getFields()}, or -1 while none is.
     */
    private final IntegerProperty selectedFieldIndex = new SimpleIntegerProperty(this, "selectedFieldIndex", -1);
    public IntegerProperty selectedFieldIndexProperty() { return selectedFieldIndex; }
    public int getSelectedFieldIndex() { return selectedFieldIndex.get(); }
    public void setSelectedFieldIndex(int value) { selectedFieldIndex.set(value); }

    // -- selectedValue
    /**
     * The value that was chosen from the values in use, or null while none is.
     */
    private final ObjectProperty<Choice> selectedValue = new SimpleObjectProperty<>(this, "selectedValue");
    public ObjectProperty<Choice> selectedValueProperty() { return selectedValue; }
    public Choice getSelectedValue() { return selectedValue.get(); }
    public void setSelectedValue(Choice value) { selectedValue.set(value); }

    // -- valueText
    /**
     * The text typed in the value box. It narrows the values in use; when they cannot be listed,
     * it is the value itself.
     */
    private final StringProperty valueText = new SimpleStringProperty(this, "valueText", "");
    public StringProperty valueTextProperty() { return valueText; }
    public String getValueText() { return valueText.get(); }
    public void setValueText(String value) { valueText.set(value); }

    // -- valuesMessage
    /**
     * A line shown in place of the values in use, when there are none to show yet or they cannot
     * be listed; null shows the list.
     */
    private final StringProperty valuesMessage = new SimpleStringProperty(this, "valuesMessage");
    public StringProperty valuesMessageProperty() { return valuesMessage; }
    public String getValuesMessage() { return valuesMessage.get(); }
    public void setValuesMessage(String value) { valuesMessage.set(value); }

    // -- summary
    /**
     * The line at the left of the footer: what applying the current choices would show. Text
     * between asterisks is shown in bold.
     */
    private final StringProperty summary = new SimpleStringProperty(this, "summary");
    public StringProperty summaryProperty() { return summary; }
    public String getSummary() { return summary.get(); }
    public void setSummary(String value) { summary.set(value); }

    // -- applyDisabled
    /**
     * Whether Apply cannot be pressed, because the choices do not make a filter yet.
     */
    private final BooleanProperty applyDisabled = new SimpleBooleanProperty(this, "applyDisabled", true);
    public BooleanProperty applyDisabledProperty() { return applyDisabled; }
    public boolean isApplyDisabled() { return applyDisabled.get(); }
    public void setApplyDisabled(boolean value) { applyDisabled.set(value); }

    // -- onApply
    /**
     * What pressing Apply does.
     */
    private final ObjectProperty<Runnable> onApply = new SimpleObjectProperty<>(this, "onApply");
    public ObjectProperty<Runnable> onApplyProperty() { return onApply; }
    public Runnable getOnApply() { return onApply.get(); }
    public void setOnApply(Runnable value) { onApply.set(value); }

    // -- onClear
    /**
     * What pressing Clear does.
     */
    private final ObjectProperty<Runnable> onClear = new SimpleObjectProperty<>(this, "onClear");
    public ObjectProperty<Runnable> onClearProperty() { return onClear; }
    public Runnable getOnClear() { return onClear.get(); }
    public void setOnClear(Runnable value) { onClear.set(value); }

    // -- onClose
    /**
     * What pressing the header's cross does.
     */
    private final ObjectProperty<Runnable> onClose = new SimpleObjectProperty<>(this, "onClose");
    public ObjectProperty<Runnable> onCloseProperty() { return onClose; }
    public Runnable getOnClose() { return onClose.get(); }
    public void setOnClose(Runnable value) { onClose.set(value); }

    /***************************************************************************
     *                                                                         *
     * Supporting Classes                                                      *
     *                                                                         *
     **************************************************************************/

    /**
     * One thing that can be chosen in the picker.
     *
     * @param key   what the choice stands for, for whoever fills the picker
     * @param label the text shown
     * @param count the number shown at the right of the row, or -1 for none
     */
    public record Choice(Object key, String label, long count) {
    }
}
