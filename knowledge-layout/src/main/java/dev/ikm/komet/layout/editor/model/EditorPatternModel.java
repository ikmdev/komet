package dev.ikm.komet.layout.editor.model;

import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.FieldDefinitionRecord;
import dev.ikm.tinkar.entity.PatternVersionRecord;
import dev.ikm.tinkar.terms.PatternFacade;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.stream.Collectors;

import static dev.ikm.komet.preferences.KLEditorPreferences.GridLayoutKey.KL_GRID_NUMBER_COLUMNS;
import static dev.ikm.komet.preferences.KLEditorPreferences.ListKey.PATTERN_LIST;

public class EditorPatternModel extends EditorGridNodeModel {
    private static final Logger LOG = LoggerFactory.getLogger(EditorPatternModel.class);

    private final ViewCalculator viewCalculator;
    private final PatternFacade patternFacade;
    private final int nid;

    public EditorPatternModel(ViewCalculator viewCalculator, int patternNid) {
        this.viewCalculator = viewCalculator;
        this.nid = patternNid;
        patternFacade = PatternFacade.make(patternNid);

        setTitle(retrieveDisplayName(patternFacade));

        // -- add fields if they exist
        Entity<EntityVersion> entity = EntityService.get().getEntityFast(patternFacade);
        Latest<EntityVersion> optionalLatest = viewCalculator.latest(entity);
        optionalLatest.ifPresent(latest -> {
            PatternVersionRecord patternVersionRecord = (PatternVersionRecord) latest;
            ImmutableList<FieldDefinitionRecord> fieldDefinitionRecords = patternVersionRecord.fieldDefinitions();

            fieldDefinitionRecords.stream().forEachOrdered(fieldDefinitionForEntity -> {
                EditorFieldModel editorFieldModel = new EditorFieldModel(viewCalculator, fieldDefinitionForEntity);
                fields.add(editorFieldModel);
                editorFieldModel.setRowIndex(fields.indexOf(editorFieldModel));
            });
        });

    }

    public static List<EditorPatternModel> load(KometPreferences sectionPreferences, ViewCalculator viewCalculator) {
        List<EditorPatternModel> editorPatternModels = new ArrayList<>();

        List<PatternFacade> patternFacades = sectionPreferences.getPatternList(PATTERN_LIST);

        for (PatternFacade patternFacade : patternFacades) {
            EditorPatternModel editorPatternModel = new EditorPatternModel(viewCalculator, patternFacade.nid());
            editorPatternModels.add(editorPatternModel);

            final KometPreferences patternPreferences = sectionPreferences.node(patternFacadeToPrefsDirName(patternFacade));

            editorPatternModel.loadPatternDetails(patternPreferences, viewCalculator);
        }

        return editorPatternModels;
    }

    private static String patternFacadeToPrefsDirName(PatternFacade patternFacade) {
        return patternFacade.publicId().asUuidList().stream()
                .map(UUID::toString)
                .collect(Collectors.joining("_"));
    }

    private void loadPatternDetails(KometPreferences patternPreferences, ViewCalculator viewCalculator) {
        patternPreferences.getInt(KL_GRID_NUMBER_COLUMNS).ifPresent(this::setNumberColumns);
        loadGridNodeDetails(patternPreferences);

        for (EditorFieldModel fieldModel : getFields()) {
            fieldModel.load(patternPreferences, viewCalculator);
        }
    }

    public void save(KometPreferences sectionPreferences) {
        List<PatternFacade> patterns = sectionPreferences.getPatternList(PATTERN_LIST);
        if (!patterns.contains(patternFacade)) {
            patterns.add(patternFacade);
        }

        sectionPreferences.putComponentList(PATTERN_LIST, patterns);

        savePatternDetails(sectionPreferences);

        try {
            sectionPreferences.flush();
        } catch (BackingStoreException e) {
            LOG.error("Error writing Section to preferences", e);
        }
    }

    private void savePatternDetails(KometPreferences sectionPreferences) {
        KometPreferences patternPreferences = sectionPreferences.node(patternFacadeToPrefsDirName(patternFacade));

        patternPreferences.putInt(KL_GRID_NUMBER_COLUMNS, getNumberColumns());

        saveGridNodeDetails(patternPreferences);

        for (EditorFieldModel fieldModel : getFields()) {
            fieldModel.save(patternPreferences);
        }
    }

    private String retrieveDisplayName(PatternFacade patternFacade) {
        Optional<String> optionalStringRegularName = viewCalculator.getRegularDescriptionText(patternFacade);
        Optional<String> optionalStringFQN = viewCalculator.getFullyQualifiedNameText(patternFacade);
        return optionalStringRegularName.orElseGet(optionalStringFQN::get);
    }

    // -- title
    private StringProperty title = new SimpleStringProperty();
    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    public void setTitle(String title) { this.title.set(title); }

    // -- fields
    private final ObservableList<EditorFieldModel> fields = FXCollections.observableArrayList();
    public ObservableList<EditorFieldModel> getFields() { return fields; }

    // -- nid
    public int getNid() { return nid; }

    // -- number columns
    private final IntegerProperty numberColumns = new SimpleIntegerProperty(1);
    public int getNumberColumns() { return numberColumns.get(); }
    public IntegerProperty numberColumnsProperty() { return numberColumns; }
    public void setNumberColumns(int number) { numberColumns.set(number); }
}
