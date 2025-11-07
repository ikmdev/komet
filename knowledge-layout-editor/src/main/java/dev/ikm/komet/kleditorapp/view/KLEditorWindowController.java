package dev.ikm.komet.kleditorapp.view;

import dev.ikm.komet.kleditorapp.model.PatternModel;
import dev.ikm.komet.kleditorapp.model.SectionModel;
import dev.ikm.komet.kleditorapp.model.WindowModel;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;

import static dev.ikm.komet.kleditorapp.view.PatternBrowserCell.KL_EDITOR_VERSION_PROXY;

public class KLEditorWindowController {

    @FXML
    private TitledPane section1TitledPane;

    @FXML
    private VBox windowContent;

    @FXML
    private Label titleLabel;

    private ViewCalculator viewCalculator;

    public void initialize() {
    }

    public void init(ViewCalculator viewCalculator) {
        this.viewCalculator = viewCalculator;

        WindowModel windowModel = WindowModel.instance();
        titleLabel.textProperty().bind(windowModel.titleProperty());

        // We only have 1 section for now
        SectionModel mainSection = windowModel.getMainSection();
        section1TitledPane.textProperty().bindBidirectional(mainSection.nameProperty());

        // Listen to changes on Main Section
        mainSection.getPatterns().addListener(this::onMainSectionPatternsChanged);

        setupDragAndDrop();
    }

    private void onMainSectionPatternsChanged(ListChangeListener.Change<? extends PatternModel> change) {
        while(change.next()) {
            if (change.wasAdded()) {
                for (PatternModel patternModel : change.getAddedSubList()) {
                    PatternViewControl patternViewControl = new PatternViewControl();
                    patternViewControl.titleProperty().bind(patternModel.titleProperty());
                    Bindings.bindContent(patternViewControl.getFields(), patternModel.getFields());

                    windowContent.getChildren().add(patternViewControl);
                }
            }
        }
    }

    private void setupDragAndDrop() {
        windowContent.setOnDragOver(event -> {
            if (event.getDragboard().hasContent(KL_EDITOR_VERSION_PROXY)) {
                event.acceptTransferModes(TransferMode.COPY);
            }

            event.consume();
        });

        windowContent.setOnDragDropped(event -> {
            if (!event.getDragboard().hasContent(KL_EDITOR_VERSION_PROXY)) {
                event.setDropCompleted(false);
                event.consume();
                return;
            }

            doPatternDrop(event);
        });
    }

    private void doPatternDrop(DragEvent event) {
        Dragboard dragboard = event.getDragboard();

        Integer patternNid = (Integer) dragboard.getContent(KL_EDITOR_VERSION_PROXY);

        PatternModel patternModel = new PatternModel(viewCalculator, patternNid);

        SectionModel sectionModel = WindowModel.instance().getMainSection();
        sectionModel.getPatterns().add(patternModel);

        event.setDropCompleted(true);

        event.consume();
    }
}