package dev.ikm.komet.kleditorapp.view;

import dev.ikm.komet.kleditorapp.view.control.EditorWindowControl;
import dev.ikm.komet.kleditorapp.view.control.FieldViewControl;
import dev.ikm.komet.kleditorapp.view.control.PatternViewControl;
import dev.ikm.komet.kleditorapp.view.control.SectionViewControl;
import dev.ikm.komet.kleditorapp.view.control.WindowControlFactory;
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

import java.util.List;

import static dev.ikm.komet.kleditorapp.view.control.PatternBrowserCell.KL_EDITOR_VERSION_PROXY;

public class KLEditorWindowController {

    private final EditorWindowControl editorWindowControl;

    private final ViewCalculator viewCalculator;

    private final EditorWindowModel editorWindowModel;

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
        SectionViewControl sectionViewControl = WindowControlFactory.createSectionView(editorSectionModel);

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
        PatternViewControl patternViewControl = WindowControlFactory.createPatternView(patternModel);

        addFieldViews(patternModel, patternModel.getFields());
        patternModel.getFields().addListener((ListChangeListener<? super EditorFieldModel>) change -> onPatternModelFieldsChanged(patternModel, change));

        SectionViewControl sectionViewControl = (SectionViewControl) WindowControlFactory.getView(editorSectionModel);
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
        FieldViewControl fieldViewControl = WindowControlFactory.createFieldView(fieldModel);

        PatternViewControl patternViewControl = (PatternViewControl) WindowControlFactory.getView(patternModel);
        patternViewControl.getFields().add(fieldViewControl);
    }

    private void setupDragAndDrop(SectionViewControl sectionViewControl) {
        EditorSectionModel editorSectionModel = (EditorSectionModel) WindowControlFactory.getModel(sectionViewControl);

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
        SelectionManager.instance().setSelectedControl(WindowControlFactory.getView(editorWindowModel.getMainSection()));
    }

    public void shutdown() {
        EditorWindowManager.shutdown();
    }
}