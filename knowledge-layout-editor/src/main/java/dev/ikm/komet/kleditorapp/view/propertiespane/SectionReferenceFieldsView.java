package dev.ikm.komet.kleditorapp.view.propertiespane;

import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.model.EditorSectionModel;
import dev.ikm.tinkar.entity.FieldDefinitionRecord;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Subscription;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.ArrayList;
import java.util.List;

/**
 * The "Reference label" area of the {@link SectionPropertiesPane}'s INTERACTION group, shown while
 * the Section has a reference component: the fields of that pattern, one per row, in the order
 * they name the semantic a user of the window chooses for the Section (see
 * {@link EditorSectionModel#getReferenceFieldOrder()}). Until the author sets an order the window
 * works one out from the semantics' values, and the list shows the fields in pattern order,
 * dimmed, saying so. The author fixes an order by dragging a row or with its arrows; each row then
 * says where its position is used (the first is the combobox's value, the first two lead a
 * dropdown option, every one is in the reference strip). A link goes back to automatic.
 */
public class SectionReferenceFieldsView extends VBox {
    public static final String DEFAULT_STYLE_CLASS = "reference-fields";

    private static final String[] POSITION_USES = {"combobox", "option"};
    private static final String STRIP_USE = "strip";

    /** On the row being dragged: its stand-in in the list while its image travels with the cursor. */
    private static final PseudoClass DRAGGING = PseudoClass.getPseudoClass("dragging");
    /** On the row the dragged one would take the place of. */
    private static final PseudoClass DROP_TARGET = PseudoClass.getPseudoClass("drop-target");

    private static final double LIFT_SHADOW_RADIUS = 10;
    private static final double LIFT_SHADOW_OFFSET_Y = 3;

    /** On the view while the Section has no order of its own, so the window orders the fields itself. */
    private static final PseudoClass AUTOMATIC = PseudoClass.getPseudoClass("automatic");

    private static final String AUTOMATIC_HINT = "Automatic: the window orders the fields by how much their values "
            + "differ between the semantics. Reorder to fix the order.";
    private static final String MANUAL_HINT = "Order of the fields naming the chosen semantic. The first shows in the "
            + "combobox; the strip lists them in this order.";

    private final Label hintLabel = new Label();
    private final VBox rowsContainer = new VBox();
    private final Hyperlink resetLink = new Hyperlink("Reset to automatic");

    private EditorSectionModel section;
    private Subscription sectionSubscription = Subscription.EMPTY;

    public SectionReferenceFieldsView() {
        Label titleLabel = new Label("Reference label");
        titleLabel.getStyleClass().add("fields-title");

        hintLabel.getStyleClass().add("fields-hint");
        hintLabel.setWrapText(true);

        rowsContainer.getStyleClass().add("rows-container");

        resetLink.getStyleClass().add("reset-link");
        resetLink.setOnAction(_ -> section.getReferenceFieldOrder().clear());

        getChildren().addAll(titleLabel, hintLabel, rowsContainer, resetLink);

        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    /**
     * Shows (and edits) the reference field order of the passed in Section, following its reference
     * component as that changes; null shows nothing.
     *
     * @param section the Section whose order to show
     */
    public void setSection(EditorSectionModel section) {
        sectionSubscription.unsubscribe();
        this.section = section;
        if (section == null) {
            sectionSubscription = Subscription.EMPTY;
            rowsContainer.getChildren().clear();
            show(false);
            return;
        }
        sectionSubscription = section.referenceComponentProperty().subscribe(this::rebuildRows)
                .and(section.getReferenceFieldOrder().subscribe(this::rebuildRows));
        rebuildRows();
    }

    private void show(boolean shown) {
        setVisible(shown);
        setManaged(shown);
    }

    private void rebuildRows() {
        rowsContainer.getChildren().clear();
        EditorPatternModel pattern = section.getReferenceComponent();
        show(pattern != null);
        if (pattern == null) {
            return;
        }

        // With no order of its own the Section's fields are shown in pattern order, dimmed and
        // without positions or uses: the window works those out from the semantics' values.
        boolean automatic = section.getReferenceFieldOrder().isEmpty();
        pseudoClassStateChanged(AUTOMATIC, automatic);
        hintLabel.setText(automatic ? AUTOMATIC_HINT : MANUAL_HINT);
        resetLink.setDisable(automatic);

        ImmutableList<FieldDefinitionRecord> fields = pattern.getFieldDefinitions();
        List<Integer> order = EditorSectionModel.fieldIndexesInOrder(section.getReferenceFieldOrder(), fields.size());
        for (int position = 0; position < order.size(); position++) {
            rowsContainer.getChildren().add(createRow(fields.get(order.get(position)), position, order, automatic));
        }
    }

    private HBox createRow(FieldDefinitionRecord field, int position, List<Integer> order, boolean automatic) {
        Label fieldNameLabel = new Label(field.meaning().description());
        fieldNameLabel.getStyleClass().add("field-name");
        fieldNameLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(fieldNameLabel, Priority.ALWAYS);

        Button upButton = createMoveButton("chevron-up", position, position - 1, order);
        upButton.setDisable(position == 0);
        Button downButton = createMoveButton("chevron-down", position, position + 1, order);
        downButton.setDisable(position == order.size() - 1);

        HBox row = new HBox(fieldNameLabel, upButton, downButton);
        row.getStyleClass().add("reference-field-row");
        row.setAlignment(Pos.CENTER_LEFT);

        if (!automatic) {
            Label positionLabel = new Label(String.valueOf(position + 1));
            positionLabel.getStyleClass().add("position");
            row.getChildren().addFirst(positionLabel);

            boolean leads = position < POSITION_USES.length;
            Label useLabel = new Label(leads ? POSITION_USES[position] : STRIP_USE);
            useLabel.getStyleClass().addAll("use", leads ? "lead" : "strip");
            row.getChildren().add(row.getChildren().indexOf(upButton), useLabel);
            if (leads) {
                row.getStyleClass().add("lead");
            }
        }

        // Drag a row onto another to put it in that one's place. The row lifts off with the cursor
        // as a shadowed image of itself, leaving a dimmed stand-in in the list; the row under the
        // cursor marks where it would land.
        row.setOnDragDetected(event -> {
            Dragboard dragboard = row.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(position));
            dragboard.setContent(content);
            dragboard.setDragView(liftedImage(row), event.getX(), event.getY());
            row.pseudoClassStateChanged(DRAGGING, true);
            event.consume();
        });
        row.setOnDragDone(_ -> row.pseudoClassStateChanged(DRAGGING, false));
        row.setOnDragOver(event -> {
            if (isRowOfThisList(event.getGestureSource(), row)) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        row.setOnDragEntered(event -> row.pseudoClassStateChanged(DROP_TARGET, isRowOfThisList(event.getGestureSource(), row)));
        row.setOnDragExited(_ -> row.pseudoClassStateChanged(DROP_TARGET, false));
        row.setOnDragDropped(event -> {
            move(order, Integer.parseInt(event.getDragboard().getString()), position);
            event.setDropCompleted(true);
            event.consume();
        });
        return row;
    }

    /** Whether the drag's source is another row of this list, the only thing a row accepts. */
    private boolean isRowOfThisList(Object gestureSource, HBox row) {
        return gestureSource != row && gestureSource instanceof HBox source && source.getParent() == rowsContainer;
    }

    /** The row as it looks lifted off the list: itself, with a shadow under it, on a transparent ground. */
    private static Image liftedImage(HBox row) {
        row.setEffect(new DropShadow(LIFT_SHADOW_RADIUS, 0, LIFT_SHADOW_OFFSET_Y, Color.rgb(0, 0, 0, 0.3)));
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        Image image = row.snapshot(parameters, null);
        row.setEffect(null);
        return image;
    }

    private Button createMoveButton(String iconStyleClass, int from, int to, List<Integer> order) {
        Region icon = new Region();
        icon.getStyleClass().addAll("icon", iconStyleClass);

        Button button = new Button();
        button.setGraphic(icon);
        button.getStyleClass().add("icon-button");
        button.setOnAction(_ -> move(order, from, to));
        return button;
    }

    /** Stores the shown order with the field at one position moved to another; the rows follow through the model. */
    private void move(List<Integer> order, int from, int to) {
        List<Integer> moved = new ArrayList<>(order);
        moved.add(to, moved.remove(from));
        section.getReferenceFieldOrder().setAll(moved);
    }
}
