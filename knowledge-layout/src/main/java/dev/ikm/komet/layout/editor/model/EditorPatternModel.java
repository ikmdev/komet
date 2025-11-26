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
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.preferences.KLEditorPreferences.PatternKey.PATTERN_LIST;

public class EditorPatternModel {
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
                fields.add(fieldDefinitionForEntity.meaning().description());
            });
        });

    }

    public static List<EditorPatternModel> load(KometPreferences sectionPreferences, ViewCalculator viewCalculator) {
        List<EditorPatternModel> editorPatternModels = new ArrayList<>();

        List<PatternFacade> patternFacades = sectionPreferences.getPatternList(PATTERN_LIST);

        for (PatternFacade patternFacade : patternFacades) {
            EditorPatternModel editorPatternModel = new EditorPatternModel(viewCalculator, patternFacade.nid());
            editorPatternModels.add(editorPatternModel);
        }

        return editorPatternModels;
    }

    public void save(KometPreferences sectionPreferences) {
        List<PatternFacade> patterns = sectionPreferences.getPatternList(PATTERN_LIST);
        if (!patterns.contains(patternFacade)) {
            patterns.add(patternFacade);
        }

        sectionPreferences.putComponentList(PATTERN_LIST, patterns);

        try {
            sectionPreferences.flush();
        } catch (BackingStoreException e) {
            LOG.error("Error writing Section to preferences", e);
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
    private final ObservableList<String> fields = FXCollections.observableArrayList();
    public ObservableList<String> getFields() { return fields; }

    // -- nid
    public int getNid() { return nid; }

}
