package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.RangeCalendarControl;
import dev.ikm.komet.kview.controls.DateFilterTitledPane;
import dev.ikm.komet.kview.controls.DateRange;
import dev.ikm.komet.kview.controls.IconRegion;
import dev.ikm.komet.kview.controls.TruncatedTextFlow;
import javafx.css.PseudoClass;
import javafx.scene.Parent;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.skin.TitledPaneSkin;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Subscription;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static dev.ikm.komet.kview.controls.RangeCalendarControl.DATE_FORMATTER;

public class DateFilterTitledPaneSkin extends TitledPaneSkin {

    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.filter-options");
    private static final PseudoClass MODIFIED_TITLED_PANE = PseudoClass.getPseudoClass("modified");
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");
    private static final PseudoClass TALLER_TITLE_AREA = PseudoClass.getPseudoClass("taller");

    private final Region arrow;
    private final VBox titleBox;
    private final TruncatedTextFlow selectedOption;
    private final ComboBox<String> comboBox;
    private final VBox contentBox;
    private Subscription subscription;
    private RangeCalendarControl calendarControl;

    public DateFilterTitledPaneSkin(DateFilterTitledPane control) {
        super(control);

        Label titleLabel = new Label(control.getText(), new IconRegion("circle"));
        titleLabel.textProperty().bind(control.titleProperty());
        titleLabel.getStyleClass().add("title-label");

        selectedOption = new TruncatedTextFlow();
        selectedOption.setMaxContentHeight(44); // 2 lines
        selectedOption.setMaxWidth(240);
        selectedOption.getStyleClass().add("option");
        selectedOption.textProperty().bind(control.optionProperty());
        subscription = selectedOption.boundsInParentProperty().subscribe(b ->
                pseudoClassStateChanged(TALLER_TITLE_AREA, b.getHeight() > 30));

        comboBox = new ComboBox<>();
        comboBox.getItems().addAll(control.getAvailableOptions());
        comboBox.getSelectionModel().select(0);
        comboBox.getStyleClass().add("date-combo-box");

        titleBox = new VBox(titleLabel, selectedOption, comboBox);
        titleBox.getStyleClass().add("title-box");
        control.setGraphic(titleBox);

        Region titleRegion = (Region) control.lookup(".title");
        arrow = (Region) titleRegion.lookup(".arrow-button");

        arrow.translateXProperty().bind(titleRegion.widthProperty().subtract(arrow.widthProperty().add(arrow.layoutXProperty())));
        arrow.translateYProperty().bind(titleBox.layoutYProperty().subtract(arrow.layoutYProperty()));
        titleBox.translateXProperty().bind(arrow.widthProperty().multiply(-1));

        contentBox = new VBox();
        contentBox.getStyleClass().add("content-box");

        control.setContent(contentBox);

        control.setOption(resources.getString("date.option.all"));

        titleRegion.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (!control.isExpanded()) {
                boolean active = !comboBox.isVisible() || comboBox.localToScene(comboBox.getBoundsInLocal()).contains(e.getSceneX(), e.getSceneY());
                control.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, active);
                e.consume();
            }
        });

        if (control.getParent() instanceof Accordion accordion) {
            subscription = subscription.and(accordion.expandedPaneProperty().subscribe(pane -> {
                if (!(pane instanceof DateFilterTitledPane)) {
                    control.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, false);
                } else {
                    Parent parent = accordion.getParent();
                    while (!(parent instanceof ScrollPane)) {
                        parent = parent.getParent();
                    }

                    ScrollPane scrollPane = (ScrollPane) parent;
                    control.heightProperty().subscribe(_ -> scrollPane.setVvalue(scrollPane.getVmax()));
                }
            }));
        }

        subscription = subscription.and(control.expandedProperty().subscribe(expanded -> {
            if (!expanded) {
                control.getSelectedOptions().clear();
                control.getExcludedOptions().clear();
                if (calendarControl != null && calendarControl.getDate() != null) {
                    control.getSelectedOptions().add(DATE_FORMATTER.format(calendarControl.getDate()));
                    control.setOption(MessageFormat.format(resources.getString("date.option.specific"),
                            DATE_FORMATTER.format(calendarControl.getDate())));
                } else if (calendarControl != null && !calendarControl.dateRangeList().isEmpty()) {
                    String including = calendarControl.dateRangeList().stream()
                            .filter(r -> !r.exclude())
                            .map(dr -> {
                                control.getSelectedOptions().add(DATE_FORMATTER.format(dr.startDate()) + ":" + DATE_FORMATTER.format(dr.endDate()));
                                return MessageFormat.format(resources.getString("date.range.text"), dr.startDate(), dr.endDate());
                            })
                            .collect(Collectors.joining(", "));
                    String excluding = calendarControl.dateRangeList().stream()
                            .filter(DateRange::exclude)
                            .map(dr -> {
                                control.getExcludedOptions().add(DATE_FORMATTER.format(dr.startDate()) + ":" + DATE_FORMATTER.format(dr.endDate()));
                                return MessageFormat.format(resources.getString("date.range.text"), dr.startDate(), dr.endDate());
                            })
                            .collect(Collectors.joining(", "));
                    if (excluding.isEmpty()) {
                        control.setOption(MessageFormat.format(resources.getString("date.option.range"), including));
                    } else {
                        control.setOption(MessageFormat.format(resources.getString("date.option.range.excluding"), including, excluding));
                    }
                } else {
                    control.setOption(control.getDefaultOption());
                }
            }
        }));
        subscription = subscription.and(comboBox.showingProperty().subscribe((_, showing) -> {
            if (showing && !contentBox.getChildren().isEmpty() && !control.isExpanded()) {
                comboBox.hide();
                control.setExpanded(true);
            }
        }));
        subscription = subscription.and(comboBox.getSelectionModel().selectedIndexProperty().subscribe(value -> {
            if (value.intValue() == 0) {
                contentBox.getChildren().clear();
                calendarControl = null;
                control.setExpanded(false);
                control.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, false);
            } else {
                if (value.intValue() == 1) {
                    createSpecificDatePane();
                } else {
                    createDateRangePane();
                }
                control.setExpanded(true);
                control.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, true);
            }
        }));

        subscription = subscription.and(selectedOption.textProperty().subscribe((_, t) ->
                pseudoClassStateChanged(MODIFIED_TITLED_PANE, !control.getDefaultOption().equals(t))));
    }

    @Override
    public void dispose() {
        selectedOption.textProperty().unbind();
        arrow.translateXProperty().unbind();
        arrow.translateYProperty().unbind();
        titleBox.translateXProperty().unbind();
        if (subscription != null) {
            subscription.unsubscribe();
        }
        super.dispose();
    }

    private void createSpecificDatePane() {
        StackPane separatorRegion = new StackPane(new IconRegion("line"));
        separatorRegion.getStyleClass().add("separator-region");

        calendarControl = new RangeCalendarControl();
        contentBox.getChildren().setAll(separatorRegion, calendarControl);
    }

    private void createDateRangePane() {
        StackPane separatorRegion = new StackPane(new IconRegion("line"));
        separatorRegion.getStyleClass().add("separator-region");

        calendarControl = new RangeCalendarControl();
        calendarControl.setMode(RangeCalendarControl.MODE.RANGE);
        Button additionButton = new Button(resources.getString("date.range.additional.button"));
        additionButton.getStyleClass().add("additional");
        additionButton.setOnAction(_ -> calendarControl.addRange(false));

        Button excludeButton = new Button(resources.getString("date.range.exclude.button"));
        excludeButton.getStyleClass().add("exclude");
        excludeButton.setOnAction(_ -> calendarControl.addRange(true));

        VBox bottomBox = new VBox(additionButton, excludeButton);
        bottomBox.getStyleClass().add("bottom-box");

        bottomBox.disableProperty().bind(calendarControl.canAddNewRangeProperty().not());
        contentBox.getChildren().setAll(separatorRegion, calendarControl, bottomBox);
    }

}
