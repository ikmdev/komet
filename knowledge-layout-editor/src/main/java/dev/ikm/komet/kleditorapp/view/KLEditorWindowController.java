package dev.ikm.komet.kleditorapp.view;

import dev.ikm.komet.kleditorapp.model.SectionModel;
import dev.ikm.komet.kleditorapp.model.WindowModel;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.FieldDefinitionRecord;
import dev.ikm.tinkar.entity.PatternVersionRecord;
import dev.ikm.tinkar.terms.PatternFacade;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

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
        WindowModel windowModel = WindowModel.instance();
        titleLabel.textProperty().bind(windowModel.titleProperty());

        // We only have 1 section for now
        SectionModel sectionModel = windowModel.getSections().getFirst();
        section1TitledPane.textProperty().bindBidirectional(sectionModel.nameProperty());

        setupDragAndDrop();
    }

    public void init(ViewCalculator viewCalculator) {
        this.viewCalculator = viewCalculator;
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

        PatternFacade patternFacade = PatternFacade.make(patternNid);

        // Pattern container Node
        VBox patternContainer = new VBox();
        patternContainer.getStyleClass().add("pattern-container");
        Label patternLabel = new Label(retrieveDisplayName(patternFacade));
        patternContainer.getChildren().add(patternLabel);

        // -- add fields if they exist
        Entity<EntityVersion> entity = EntityService.get().getEntityFast(patternFacade);
        Latest<EntityVersion> optionalLatest = viewCalculator.latest(entity);
        optionalLatest.ifPresent(latest -> {
            PatternVersionRecord patternVersionRecord = (PatternVersionRecord) latest;
            ImmutableList<FieldDefinitionRecord> fieldDefinitionRecords = patternVersionRecord.fieldDefinitions();

            AtomicInteger i = new AtomicInteger(1);
            fieldDefinitionRecords.stream().forEachOrdered( fieldDefinitionForEntity -> {
                HBox patternFieldContainer = new HBox();
                patternFieldContainer.getStyleClass().add("field-container");
                Label patternFieldLabel = new Label("Field " + i + ": ");
                Label patternFieldText = new Label(fieldDefinitionForEntity.meaning().description());
                patternFieldContainer.getChildren().addAll(patternFieldLabel, patternFieldText);

                patternContainer.getChildren().add(patternFieldContainer);

                i.incrementAndGet();
            });

            windowContent.getChildren().add(patternContainer);

            event.setDropCompleted(true);
        });

        event.consume();
    }

    private String retrieveDisplayName(PatternFacade patternFacade) {
        Optional<String> optionalStringRegularName = viewCalculator.getRegularDescriptionText(patternFacade);
        Optional<String> optionalStringFQN = viewCalculator.getFullyQualifiedNameText(patternFacade);
        return optionalStringRegularName.orElseGet(optionalStringFQN::get);
    }
}