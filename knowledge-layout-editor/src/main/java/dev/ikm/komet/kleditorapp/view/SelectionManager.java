package dev.ikm.komet.kleditorapp.view;

import dev.ikm.komet.layout.editor.EditorWindowBaseControl;
import dev.ikm.komet.kleditorapp.view.control.EditorWindowControl;
import dev.ikm.komet.kleditorapp.view.control.PatternViewControl;
import dev.ikm.komet.kleditorapp.view.control.PatternViewControlBase;
import dev.ikm.komet.kleditorapp.view.control.SectionViewControl;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.input.MouseEvent;

public class SelectionManager {
    private static SelectionManager INSTANCE;

    public static SelectionManager init(EditorWindowControl editorWindowControl) {
        INSTANCE = new SelectionManager(editorWindowControl);
        return INSTANCE;
    }

    public static SelectionManager instance() { return INSTANCE; }

    private SelectionManager(EditorWindowControl editorWindowControl) {
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
        sectionViewControl.getPatterns().addListener((ListChangeListener<? super PatternViewControlBase>) sectionChange -> onPatternViewsChanged(sectionChange));

        setupListenersForSelection(sectionViewControl);
    }

    private void onPatternViewsChanged(ListChangeListener.Change<? extends PatternViewControlBase> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                change.getAddedSubList().forEach(this::setupPatternForSelection);
            }
        }
    }

    private void setupPatternForSelection(PatternViewControlBase pattern) {
        setupListenersForSelection(pattern);
        // Only the standard pattern view has selectable field tiles; the table renders fields as columns.
        if (pattern instanceof PatternViewControl patternViewControl) {
            patternViewControl.getFields().forEach(this::setupListenersForSelection);
        }
    }

    private void setupListenersForSelection(EditorWindowBaseControl editorWindowBaseControl) {
        editorWindowBaseControl.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            EditorWindowBaseControl selectedControl = getSelectedControl();
            if (selectedControl == editorWindowBaseControl) {
                return;
            }
            if (selectedControl != null) {
                selectedControl.setSelected(false);
            }
            setSelectedControl(editorWindowBaseControl);
            mouseEvent.consume(); // Consume the event so it doesn't bubble up to the parent
        });
    }

    // -- selected Control
    private final ObjectProperty<EditorWindowBaseControl> selectedControl = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            get().setSelected(true);
        }
    };
    public EditorWindowBaseControl getSelectedControl() { return selectedControl.get(); }
    public ObjectProperty<EditorWindowBaseControl> selectedControlProperty() { return selectedControl; }
    public void setSelectedControl(EditorWindowBaseControl editorWindowBaseControl) { selectedControl.set(editorWindowBaseControl); }
}