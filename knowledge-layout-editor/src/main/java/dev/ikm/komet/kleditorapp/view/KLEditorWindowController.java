package dev.ikm.komet.kleditorapp.view;

import dev.ikm.komet.kleditorapp.view.control.EditorWindowControl;
import dev.ikm.komet.kleditorapp.view.control.FieldViewControl;
import dev.ikm.komet.kleditorapp.view.control.PatternStandardEditorControl;
import dev.ikm.komet.kleditorapp.view.control.PatternEditorControlBase;
import dev.ikm.komet.kleditorapp.view.control.SectionViewControl;
import dev.ikm.komet.kleditorapp.view.control.SupplementalAreaViewControl;
import dev.ikm.komet.kleditorapp.view.control.KlEditorWindowControlFactory;
import dev.ikm.komet.layout.editor.EditorWindowManager;
import dev.ikm.komet.layout.editor.model.EditorFieldModel;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.model.EditorSectionModel;
import dev.ikm.komet.layout.editor.model.EditorSupplementalAreaModel;
import dev.ikm.komet.layout.editor.model.EditorWindowModel;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.util.List;

import static dev.ikm.komet.kleditorapp.view.control.ControlBrowserCell.KL_EDITOR_AREA_FACTORY;
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

        // Window (view) size and control-bar options are driven by the model (edited via the Window pane).
        editorWindowControl.prefWidthProperty().bind(editorWindowModel.prefWidthProperty());
        editorWindowControl.prefHeightProperty().bind(editorWindowModel.prefHeightProperty());
        editorWindowControl.coordinateVisibleProperty().bind(editorWindowModel.coordinateVisibleProperty());
        editorWindowControl.timelineVisibleProperty().bind(editorWindowModel.timelineVisibleProperty());

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
            if (change.wasRemoved()) {
                change.getRemoved().forEach(sectionModel -> {
                    editorWindowControl.getSectionViews().remove((SectionViewControl) KlEditorWindowControlFactory.getView(sectionModel));
                });
            }
        }
    }

    private void onSectionModelPatternsChanged(EditorSectionModel sectionModel, SectionViewControl sectionViewControl,
                                               ListChangeListener.Change<? extends EditorPatternModel> change) {
        while(change.next()) {
            if (change.wasAdded()) {
                addPatternViews(sectionModel, change.getAddedSubList());
            }
            if (change.wasRemoved()) {
                change.getRemoved().forEach(patternModel -> {
                    PatternEditorControlBase patternViewControl = (PatternEditorControlBase) KlEditorWindowControlFactory.getView(patternModel);
                    sectionViewControl.getPatterns().remove(patternViewControl);
                });
            }
        }
    }

    private void addSectionViewAndPatterns(List<? extends EditorSectionModel> sectionModels) {
        for (EditorSectionModel sectionModel : sectionModels) {
            addSectionView(sectionModel);
            addPatternViews(sectionModel, sectionModel.getPatterns());
            addSupplementalAreaViews(sectionModel, sectionModel.getSupplementalAreas());
        }
    }

    private void addSectionView(EditorSectionModel editorSectionModel) {
        SectionViewControl sectionViewControl = KlEditorWindowControlFactory.createSectionView(editorSectionModel);

        setupDragAndDrop(sectionViewControl);

        editorWindowControl.getSectionViews().add(sectionViewControl);
    }

    private void addPatternViews(EditorSectionModel editorSectionModel, List<? extends EditorPatternModel> patternModels) {
        for (EditorPatternModel patternModel : patternModels) {
            addPatternView(editorSectionModel, patternModel);
        }
    }

    private void addPatternView(EditorSectionModel editorSectionModel, EditorPatternModel patternModel) {
        SectionViewControl sectionViewControl = (SectionViewControl) KlEditorWindowControlFactory.getView(editorSectionModel);
        sectionViewControl.getPatterns().add(createPatternEditorControl(patternModel));

        // Re-render the pattern with the newly selected factory's editor control when the display type changes.
        patternModel.factoryProperty().addListener((obs, oldFactory, newFactory) -> swapPatternView(editorSectionModel, patternModel));

        // Keep the displayed control's field views in sync as the model's fields change.
        patternModel.getFields().addListener(
                (ListChangeListener<? super EditorFieldModel>) change -> onPatternModelFieldsChanged(patternModel, change));
    }

    /**
     * Builds the editor-side control for a pattern from its currently selected factory. The factory both
     * creates and populates the control (its field views included), so no separate field-population pass
     * is needed here.
     */
    private PatternEditorControlBase createPatternEditorControl(EditorPatternModel patternModel) {
        return (PatternEditorControlBase) patternModel.getFactory().createEditorControl(patternModel);
    }

    /**
     * Replaces the displayed pattern control with the one produced by the now-selected factory, keeping its
     * place in the section and preserving selection.
     */
    private void swapPatternView(EditorSectionModel editorSectionModel, EditorPatternModel patternModel) {
        SectionViewControl sectionViewControl = (SectionViewControl) KlEditorWindowControlFactory.getView(editorSectionModel);
        PatternEditorControlBase oldView = (PatternEditorControlBase) KlEditorWindowControlFactory.getView(patternModel);

        boolean wasSelected = SelectionManager.instance().getSelectedControl() == oldView;

        // createEditorControl re-registers the model -> view mapping, so build the replacement first.
        PatternEditorControlBase newView = createPatternEditorControl(patternModel);

        int index = sectionViewControl.getPatterns().indexOf(oldView);
        if (index >= 0) {
            sectionViewControl.getPatterns().set(index, newView);
        } else {
            sectionViewControl.getPatterns().add(newView);
        }

        if (wasSelected) {
            SelectionManager.instance().setSelectedControl(newView);
        }
    }

    private void onSectionModelSupplementalAreasChanged(EditorSectionModel sectionModel, SectionViewControl sectionViewControl,
                                                        ListChangeListener.Change<? extends EditorSupplementalAreaModel> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                addSupplementalAreaViews(sectionModel, change.getAddedSubList());
            }
            if (change.wasRemoved()) {
                change.getRemoved().forEach(areaModel -> {
                    SupplementalAreaViewControl areaView = (SupplementalAreaViewControl) KlEditorWindowControlFactory.getView(areaModel);
                    sectionViewControl.getSupplementalAreas().remove(areaView);
                });
            }
        }
    }

    private void addSupplementalAreaViews(EditorSectionModel editorSectionModel, List<? extends EditorSupplementalAreaModel> areaModels) {
        for (EditorSupplementalAreaModel areaModel : areaModels) {
            addSupplementalAreaView(editorSectionModel, areaModel);
        }
    }

    private void addSupplementalAreaView(EditorSectionModel editorSectionModel, EditorSupplementalAreaModel areaModel) {
        SupplementalAreaViewControl areaView = KlEditorWindowControlFactory.createSupplementalAreaView(areaModel);

        SectionViewControl sectionViewControl = (SectionViewControl) KlEditorWindowControlFactory.getView(editorSectionModel);
        sectionViewControl.getSupplementalAreas().add(areaView);
    }

    private void onPatternModelFieldsChanged(EditorPatternModel patternModel,
                                             ListChangeListener.Change<? extends EditorFieldModel> change) {
        // Field tiles only exist in the standard pattern view; other representations (e.g. the table) build
        // their own columns from the model when (re)rendered. Resolve the current view on each change so this
        // keeps working after the pattern is re-rendered with a different factory's editor control.
        if (!(KlEditorWindowControlFactory.getView(patternModel) instanceof PatternStandardEditorControl patternStandardEditorControl)) {
            return;
        }
        while(change.next()) {
            if (change.wasAdded()) {
                for (EditorFieldModel fieldModel : change.getAddedSubList()) {
                    patternStandardEditorControl.getFields().add(KlEditorWindowControlFactory.createFieldView(fieldModel));
                }
            }
            if (change.wasRemoved()) {
                change.getRemoved().forEach(fieldModel -> {
                    FieldViewControl fieldViewControl = (FieldViewControl) KlEditorWindowControlFactory.getView(fieldModel);
                    patternStandardEditorControl.getFields().remove(fieldViewControl);
                });
            }
        }
    }

    private void setupDragAndDrop(SectionViewControl sectionViewControl) {
        EditorSectionModel editorSectionModel = (EditorSectionModel) KlEditorWindowControlFactory.getModel(sectionViewControl);

        sectionViewControl.setOnDragOverIntoTile(dragEvent -> {
            if (dragEvent.getDragboard().hasContent(KL_EDITOR_VERSION_PROXY)
                    || dragEvent.getDragboard().hasContent(KL_EDITOR_AREA_FACTORY)) {
                dragEvent.acceptTransferModes(TransferMode.COPY);
            }

            dragEvent.consume();
        });

        sectionViewControl.setOnDragDroppedIntoTile((event, gridDropInfo) -> {
            Dragboard dragboard = event.getDragboard();

            if (dragboard.hasContent(KL_EDITOR_VERSION_PROXY)) {
                Integer patternNid = (Integer) dragboard.getContent(KL_EDITOR_VERSION_PROXY);

                EditorPatternModel editorPatternModel = new EditorPatternModel(viewCalculator, patternNid);
                editorPatternModel.setRowIndex(gridDropInfo.getRowIndex());
                editorPatternModel.setColumnIndex(gridDropInfo.getColumnIndex());

                editorSectionModel.getPatterns().add(editorPatternModel);

                event.setDropCompleted(true);
            } else if (dragboard.hasContent(KL_EDITOR_AREA_FACTORY)) {
                String factoryClassName = (String) dragboard.getContent(KL_EDITOR_AREA_FACTORY);

                EditorSupplementalAreaModel areaModel = new EditorSupplementalAreaModel(factoryClassName);
                areaModel.setRowIndex(gridDropInfo.getRowIndex());
                areaModel.setColumnIndex(gridDropInfo.getColumnIndex());

                editorSectionModel.getSupplementalAreas().add(areaModel);

                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }

            event.consume();
        });

        // Listen to changes on Section Patterns
        editorSectionModel.getPatterns().addListener((ListChangeListener<? super EditorPatternModel>) change -> onSectionModelPatternsChanged(editorSectionModel, sectionViewControl, change));

        // Listen to changes on Section supplemental areas
        editorSectionModel.getSupplementalAreas().addListener((ListChangeListener<? super EditorSupplementalAreaModel>) change -> onSectionModelSupplementalAreasChanged(editorSectionModel, sectionViewControl, change));
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
        SelectionManager.instance().setSelectedControl(KlEditorWindowControlFactory.getView(editorWindowModel.getMainSection()));
    }

    public void shutdown() {
        EditorWindowManager.shutdown();
    }
}