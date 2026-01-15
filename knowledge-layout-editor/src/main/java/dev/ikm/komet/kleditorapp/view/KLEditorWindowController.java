package dev.ikm.komet.kleditorapp.view;

import dev.ikm.komet.kleditorapp.view.control.EditorWindowControl;
import dev.ikm.komet.kleditorapp.view.control.FieldViewControl;
import dev.ikm.komet.kleditorapp.view.control.PatternViewControl;
import dev.ikm.komet.kleditorapp.view.control.SectionViewControl;
import dev.ikm.komet.layout.editor.EditorWindowManager;
import dev.ikm.komet.layout.editor.model.EditorFieldModel;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.model.EditorSectionModel;
import dev.ikm.komet.layout.editor.model.EditorWindowModel;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;

import static dev.ikm.komet.kleditorapp.view.control.PatternBrowserCell.KL_EDITOR_VERSION_PROXY;

public class KLEditorWindowController {

    private EditorWindowControl editorWindowControl;

    private ViewCalculator viewCalculator;

    private final HashMap<SectionViewControl, EditorSectionModel> sectionViewToModel = new HashMap<>();
    private final HashMap<EditorSectionModel, SectionViewControl> sectionModelToView = new HashMap<>();

    private final HashMap<EditorPatternModel, PatternViewControl> patternModelToView = new HashMap<>();

    private EditorWindowModel editorWindowModel;

    public KLEditorWindowController(EditorWindowModel editorWindowModel, EditorWindowControl editorWindowControl, ViewCalculator viewCalculator) {
        this.viewCalculator = viewCalculator;
        this.editorWindowModel = editorWindowModel;
        this.editorWindowControl = editorWindowControl;

        editorWindowControl.titleProperty().bind(editorWindowModel.titleProperty());

        editorWindowControl.setOnAddSectionAction(this::onAddSectionAction);

        // Main Section
        addSectionViewAndPatterns(List.of(editorWindowModel.getMainSection()));

        // Additional Sections
        editorWindowModel.getAdditionalSections().addListener(this::onAdditionalSectionsChanged);
        addSectionViewAndPatterns(editorWindowModel.getAdditionalSections());
    }

    private void onAdditionalSectionsChanged(ListChangeListener.Change<? extends EditorSectionModel> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                addSectionViewAndPatterns(change.getAddedSubList());
            }
        }
    }

    private void onSectionModelPatternsChanged(EditorSectionModel sectionModel, ListChangeListener.Change<? extends EditorPatternModel> change) {
        while(change.next()) {
            if (change.wasAdded()) {
                addPatternViews(sectionModel, change.getAddedSubList());
            }
        }
    }

    private void addSectionViewAndPatterns(List<? extends EditorSectionModel> sectionModels) {
        for (EditorSectionModel sectionModel : sectionModels) {
            addSectionView(sectionModel);
            addPatternViews(sectionModel, sectionModel.getPatterns());
        }
    }

    private void addSectionView(EditorSectionModel editorSectionModel) {
        SectionViewControl sectionViewControl = new SectionViewControl();

        sectionViewControl.nameProperty().bindBidirectional(editorSectionModel.nameProperty());
        sectionViewControl.tagTextProperty().bind(editorSectionModel.tagTextProperty());

        sectionViewControl.numberColumnsProperty().bindBidirectional(editorSectionModel.numberColumnsProperty());

        sectionViewToModel.put(sectionViewControl, editorSectionModel);
        sectionModelToView.put(editorSectionModel, sectionViewControl);

        setupDragAndDrop(sectionViewControl);

        VBox.setVgrow(sectionViewControl, Priority.ALWAYS);
        editorWindowControl.getSectionViews().add(sectionViewControl);
    }

    private void addPatternViews(EditorSectionModel editorSectionModel, List<? extends EditorPatternModel> patternModels) {
        for (EditorPatternModel patternModel : patternModels) {
            addPatternView(editorSectionModel, patternModel);
        }
    }

    private void addPatternView(EditorSectionModel editorSectionModel, EditorPatternModel patternModel) {
        PatternViewControl patternViewControl = new PatternViewControl();

        patternModelToView.put(patternModel, patternViewControl);

        patternViewControl.titleProperty().bind(patternModel.titleProperty());
        patternViewControl.titleVisibleProperty().bindBidirectional(patternModel.titleVisibleProperty());

        patternViewControl.numberColumnsProperty().bindBidirectional(patternModel.numberColumnsProperty());

        patternViewControl.columnIndexProperty().bindBidirectional(patternModel.columnIndexProperty());
        patternViewControl.rowIndexProperty().bindBidirectional(patternModel.rowIndexProperty());
        patternViewControl.columnSpanProperty().bindBidirectional(patternModel.columnSpanProperty());

        addFieldViews(patternModel, patternModel.getFields());
        patternModel.getFields().addListener((ListChangeListener<? super EditorFieldModel>) change -> onPatternModelFieldsChanged(patternModel, change));

        SectionViewControl sectionViewControl = sectionModelToView.get(editorSectionModel);
        sectionViewControl.getPatterns().add(patternViewControl);
    }

    private void onPatternModelFieldsChanged(EditorPatternModel patternModel, ListChangeListener.Change<? extends EditorFieldModel> change) {
        while(change.next()) {
            if (change.wasAdded()) {
                addFieldViews(patternModel, change.getAddedSubList());
            }
        }
    }

    private void addFieldViews(EditorPatternModel patternModel, List<? extends EditorFieldModel> fieldModels) {
        for (EditorFieldModel fieldModel : fieldModels) {
            addFieldView(patternModel, fieldModel);
        }
    }

    private void addFieldView(EditorPatternModel patternModel, EditorFieldModel fieldModel) {
        FieldViewControl fieldViewControl = new FieldViewControl();
        fieldViewControl.titleProperty().bind(fieldModel.titleProperty());
        fieldViewControl.fieldNumberProperty().bind(fieldModel.indexProperty().add(1));

        fieldViewControl.columnIndexProperty().bindBidirectional(fieldModel.columnIndexProperty());
        fieldViewControl.rowIndexProperty().bindBidirectional(fieldModel.rowIndexProperty());
        fieldViewControl.columnSpanProperty().bindBidirectional(fieldModel.columnSpanProperty());

        PatternViewControl patternViewControl = patternModelToView.get(patternModel);
        patternViewControl.getFields().add(fieldViewControl);
    }

    private void setupDragAndDrop(SectionViewControl sectionViewControl) {
        EditorSectionModel editorSectionModel = sectionViewToModel.get(sectionViewControl);

        sectionViewControl.setOnDragOverIntoTile(dragEvent -> {
            if (dragEvent.getDragboard().hasContent(KL_EDITOR_VERSION_PROXY)) {
                dragEvent.acceptTransferModes(TransferMode.COPY);
            }

            dragEvent.consume();
        });

        sectionViewControl.setOnDragDroppedIntoTile((event, gridDropInfo) -> {
            if (!event.getDragboard().hasContent(KL_EDITOR_VERSION_PROXY)) {
                event.setDropCompleted(false);
                event.consume();
                return;
            }

            Dragboard dragboard = event.getDragboard();

            Integer patternNid = (Integer) dragboard.getContent(KL_EDITOR_VERSION_PROXY);

            EditorPatternModel editorPatternModel = new EditorPatternModel(viewCalculator, patternNid);
            editorPatternModel.setRowIndex(gridDropInfo.getRowIndex());
            editorPatternModel.setColumnIndex(gridDropInfo.getColumnIndex());

            editorSectionModel.getPatterns().add(editorPatternModel);

            event.setDropCompleted(true);
            event.consume();
        });

        // Listen to changes on Section Patterns
        editorSectionModel.getPatterns().addListener((ListChangeListener<? super EditorPatternModel>) change -> onSectionModelPatternsChanged(editorSectionModel, change));
    }

    @FXML
    private void onAddSectionAction(ActionEvent actionEvent) {
        EditorSectionModel editorSectionModel = new EditorSectionModel();
        editorWindowModel.getAdditionalSections().add(editorSectionModel);
    }

    /**
     * Perform any initial configuration. This method is called after everything has been setup.
     */
    public void start() {
        // Select main section initially
        SelectionManager.instance().setSelectedControl(sectionModelToView.get(editorWindowModel.getMainSection()));
    }

    public void shutdown() {
        EditorWindowManager.shutdown();
    }
}