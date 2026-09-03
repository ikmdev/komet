package dev.ikm.komet.kleditorapp.view;

import dev.ikm.komet.layout.editor.Selectable;
import dev.ikm.komet.kleditorapp.view.control.EditorWindowControl;
import dev.ikm.komet.kleditorapp.view.control.FieldColumnControl;
import dev.ikm.komet.kleditorapp.view.control.PatternStandardEditorControl;
import dev.ikm.komet.kleditorapp.view.control.PatternEditorControlBase;
import dev.ikm.komet.kleditorapp.view.control.PatternTableEditorControl;
import dev.ikm.komet.kleditorapp.view.control.SectionViewControl;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class SelectionManager {
    private static SelectionManager INSTANCE;

    public static SelectionManager init(EditorWindowControl editorWindowControl) {
        INSTANCE = new SelectionManager(editorWindowControl);
        return INSTANCE;
    }

    public static SelectionManager instance() { return INSTANCE; }

    private SelectionManager(EditorWindowControl editorWindowControl) {
        // The window itself is selectable. Its handler runs only for presses that
        // aren't consumed by an inner control (sections, patterns, etc.), i.e. clicks on the window
        // header or empty areas, so clicking inside a section still selects that section.
        setupListenersForSelection(editorWindowControl);

        editorWindowControl.getSectionViews().forEach(this::setupSectionView);
        editorWindowControl.getSectionViews().addListener((ListChangeListener<? super SectionViewControl>) c -> onSectionViewsChanged(c));
    }

    private void onSectionViewsChanged(ListChangeListener.Change<? extends SectionViewControl> change) {
        while(change.next()) {
            if (change.wasAdded()) {
                change.getAddedSubList().forEach(this::setupSectionView);
            }
        }
    }

    private void setupSectionView(SectionViewControl sectionViewControl) {
        sectionViewControl.getPatterns().forEach(this::setupPatternForSelection);
        sectionViewControl.getPatterns().addListener((ListChangeListener<? super PatternEditorControlBase>) sectionChange -> onPatternViewsChanged(sectionChange));

        setupListenersForSelection(sectionViewControl);
    }

    private void onPatternViewsChanged(ListChangeListener.Change<? extends PatternEditorControlBase> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                change.getAddedSubList().forEach(this::setupPatternForSelection);
            }
        }
    }

    private void setupPatternForSelection(PatternEditorControlBase pattern) {
        switch (pattern) {
            // The standard pattern view has selectable field tiles.
            case PatternStandardEditorControl standardPattern -> {
                setupListenersForSelection(standardPattern);
                standardPattern.getFields().forEach(this::setupListenersForSelection);
            }
            // The table renders its fields as columns, selected through their headers.
            case PatternTableEditorControl tablePattern -> setupTablePatternForSelection(tablePattern);
            default -> setupListenersForSelection(pattern);
        }
    }

    /**
     * A table pattern's fields are its columns, so a press on a column header selects that column's
     * field; a press anywhere else in the pattern (title, table body...) selects the pattern itself.
     */
    private void setupTablePatternForSelection(PatternTableEditorControl tablePatternControl) {
        // A column header consumes the press it receives (that is how the table arms column dragging),
        // so it never bubbles up to the pattern: catch it in the capturing phase instead, and leave it
        // unconsumed so the header still gets it and dragging a column to reorder keeps working.
        tablePatternControl.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            FieldColumnControl field = tablePatternControl.fieldAt((Node) mouseEvent.getTarget());
            if (field != null) {
                select(field);
            }
        });
        tablePatternControl.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            if (tablePatternControl.fieldAt((Node) mouseEvent.getTarget()) == null) {
                select(tablePatternControl);
            }
            mouseEvent.consume(); // Consume the event so it doesn't bubble up to the parent
        });
    }

    private <T extends Node & Selectable> void setupListenersForSelection(T selectableControl) {
        selectableControl.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            select(selectableControl);
            mouseEvent.consume(); // Consume the event so it doesn't bubble up to the parent
        });
    }

    private void select(Selectable selectableControl) {
        Selectable selectedControl = getSelectedControl();
        if (selectedControl == selectableControl) {
            return;
        }
        if (selectedControl != null) {
            selectedControl.setSelected(false);
        }
        setSelectedControl(selectableControl);
    }

    // -- selected Control
    private final ObjectProperty<Selectable> selectedControl = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            get().setSelected(true);
        }
    };
    public Selectable getSelectedControl() { return selectedControl.get(); }
    public ObjectProperty<Selectable> selectedControlProperty() { return selectedControl; }
    public void setSelectedControl(Selectable selectableControl) { selectedControl.set(selectableControl); }
}
