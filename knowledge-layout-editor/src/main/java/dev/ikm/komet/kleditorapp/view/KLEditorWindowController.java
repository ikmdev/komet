package dev.ikm.komet.kleditorapp.view;

import dev.ikm.komet.kleditorapp.view.control.EditorWindowControl;
import dev.ikm.komet.kleditorapp.view.control.PatternViewControl;
import dev.ikm.komet.kleditorapp.view.control.SectionViewControl;
import dev.ikm.komet.layout.editor.EditorWindowManager;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.model.EditorSectionModel;
import dev.ikm.komet.layout.editor.model.EditorWindowModel;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;

public class KLEditorWindowController {

    private EditorWindowControl editorWindowControl;

    private ViewCalculator viewCalculator;

    private final HashMap<SectionViewControl, EditorSectionModel> sectionViewToModel = new HashMap<>();
    private final HashMap<EditorSectionModel, SectionViewControl> sectionModelToView = new HashMap<>();

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

    private void onSectionPatternsChanged(EditorSectionModel editorSectionModel, ListChangeListener.Change<? extends EditorPatternModel> change) {
        while(change.next()) {
            if (change.wasAdded()) {
                addPatternViews(editorSectionModel, change.getAddedSubList());
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

        sectionViewToModel.put(sectionViewControl, editorSectionModel);
        sectionModelToView.put(editorSectionModel, sectionViewControl);

        setupDragAndDrop(sectionViewControl);

        VBox.setVgrow(sectionViewControl, Priority.ALWAYS);
        editorWindowControl.getSectionViews().add(sectionViewControl);
    }

    private void addPatternViews(EditorSectionModel editorSectionModel, List<? extends EditorPatternModel> patternModels) {
        for (EditorPatternModel editorPatternModel : patternModels) {
            PatternViewControl patternViewControl = new PatternViewControl();
            patternViewControl.titleProperty().bind(editorPatternModel.titleProperty());
            Bindings.bindContent(patternViewControl.getFields(), editorPatternModel.getFields());

            SectionViewControl sectionViewControl = sectionModelToView.get(editorSectionModel);
            sectionViewControl.getPatterns().add(patternViewControl);
        }
    }

    private void setupDragAndDrop(SectionViewControl sectionViewControl) {
        EditorSectionModel editorSectionModel = sectionViewToModel.get(sectionViewControl);

        sectionViewControl.setOnPatternDropped((event, patternNid) -> {
            EditorPatternModel editorPatternModel = new EditorPatternModel(viewCalculator, patternNid);
            editorSectionModel.getPatterns().add(editorPatternModel);

            event.setDropCompleted(true);
            event.consume();
        });

        // Listen to changes on Section Patterns
        editorSectionModel.getPatterns().addListener((ListChangeListener<? super EditorPatternModel>) change -> onSectionPatternsChanged(editorSectionModel, change));
    }

    @FXML
    private void onAddSectionAction(ActionEvent actionEvent) {
        EditorSectionModel editorSectionModel = new EditorSectionModel();
        editorWindowModel.getAdditionalSections().add(editorSectionModel);
    }

    public void shutdown() {
        EditorWindowManager.shutdown();
    }
}