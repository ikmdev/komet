package dev.ikm.komet.kleditorapp.view;

import dev.ikm.komet.kleditorapp.model.PatternModel;
import dev.ikm.komet.kleditorapp.model.SectionModel;
import dev.ikm.komet.kleditorapp.model.WindowModel;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;

public class KLEditorWindowController {

    @FXML
    private VBox sectionContainer;

    @FXML
    private Label titleLabel;

    @FXML
    private SectionViewControl mainSectionView;

    private ViewCalculator viewCalculator;

    private final HashMap<SectionViewControl, SectionModel> sectionViewToModel = new HashMap<>();
    private final HashMap<SectionModel, SectionViewControl> sectionModelToView = new HashMap<>();

    public void init(ViewCalculator viewCalculator) {
        this.viewCalculator = viewCalculator;

        WindowModel windowModel = WindowModel.instance();
        titleLabel.textProperty().bind(windowModel.titleProperty());

        setupMainSection(windowModel);

        windowModel.getAdditionalSections().addListener(this::onAdditionalSectionsChanged);
    }

    /**
     * Setups the main section which will always need to exist (we always need to have at least 1 Section).
     *
     * @param windowModel the WindowModel object
     */
    private void setupMainSection(WindowModel windowModel) {
        SectionModel mainSection = windowModel.getMainSection();

        addSectionView(mainSection);
    }


    private void onAdditionalSectionsChanged(ListChangeListener.Change<? extends SectionModel> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                for (SectionModel sectionModel : change.getAddedSubList()) {
                    addSectionView(sectionModel);
                    addPatternViews(sectionModel, sectionModel.getPatterns());
                }
            }
        }
    }

    private void addSectionView(SectionModel sectionModel) {
        SectionViewControl sectionViewControl = new SectionViewControl();

        sectionViewControl.nameProperty().bind(sectionModel.nameProperty());
        sectionViewControl.tagTextProperty().bind(sectionModel.tagTextProperty());

        sectionViewToModel.put(sectionViewControl, sectionModel);
        sectionModelToView.put(sectionModel, sectionViewControl);

        setupDragAndDrop(sectionViewControl);

        VBox.setVgrow(sectionViewControl, Priority.ALWAYS);
        sectionContainer.getChildren().add(sectionViewControl);
    }

    private void onSectionPatternsChanged(SectionModel sectionModel, ListChangeListener.Change<? extends PatternModel> change) {
        while(change.next()) {
            if (change.wasAdded()) {
                addPatternViews(sectionModel, change.getAddedSubList());
            }
        }
    }

    private void addPatternViews(SectionModel sectionModel, List<? extends PatternModel> patternModels) {
        for (PatternModel patternModel : patternModels) {
            PatternViewControl patternViewControl = new PatternViewControl();
            patternViewControl.titleProperty().bind(patternModel.titleProperty());
            Bindings.bindContent(patternViewControl.getFields(), patternModel.getFields());

            SectionViewControl sectionViewControl = sectionModelToView.get(sectionModel);
            sectionViewControl.getItems().add(patternViewControl);
        }
    }

    private void setupDragAndDrop(SectionViewControl sectionViewControl) {
        SectionModel sectionModel = sectionViewToModel.get(sectionViewControl);

        sectionViewControl.setOnPatternDropped((event, patternNid) -> {
            PatternModel patternModel = new PatternModel(viewCalculator, patternNid);
            sectionModel.getPatterns().add(patternModel);

            event.setDropCompleted(true);
            event.consume();
        });

        // Listen to changes on Section Patterns
        sectionModel.getPatterns().addListener((ListChangeListener<? super PatternModel>) change -> onSectionPatternsChanged(sectionModel, change));
    }

    @FXML
    private void onAddSectionAction(ActionEvent actionEvent) {
        WindowModel windowModel = WindowModel.instance();
        SectionModel sectionModel = new SectionModel();
        windowModel.getAdditionalSections().add(sectionModel);
    }

    public void shutdown() {
        WindowModel.instance().reset();
    }
}