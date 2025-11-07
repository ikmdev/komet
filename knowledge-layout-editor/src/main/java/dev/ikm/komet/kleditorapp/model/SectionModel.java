package dev.ikm.komet.kleditorapp.model;

import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.preferences.KLEditorPreferences.KL_ADDITIONAL_SECTIONS;

public class SectionModel {
    private static final Logger LOG = LoggerFactory.getLogger(SectionModel.class);

    public static List<SectionModel> load(KometPreferences editorWindowPreferences, ViewCalculator viewCalculator) {
        List<SectionModel> sectionModels = new ArrayList<>();

        List<String> sectionNames = editorWindowPreferences.getList(KL_ADDITIONAL_SECTIONS);

        for (String sectionName : sectionNames) {
            SectionModel sectionModel = new SectionModel();
            sectionModel.setName(sectionName);

            final KometPreferences sectionPreferences = editorWindowPreferences.node(sectionName);
            sectionModel.loadSectionDetails(sectionPreferences, viewCalculator);

            sectionModels.add(sectionModel);
        }

        return sectionModels;
    }

    public void loadSectionDetails(KometPreferences sectionPreferences, ViewCalculator viewCalculator) {
        List<PatternModel> patternModels = PatternModel.load(sectionPreferences, viewCalculator);
        getPatterns().setAll(patternModels);
    }

    public void save(KometPreferences editorWindowPreferences) {
        String sectionName = getName();

        List<String> sections = editorWindowPreferences.getList(KL_ADDITIONAL_SECTIONS);
        if (!sections.contains(sectionName)) {
            sections.add(sectionName);
        }

        editorWindowPreferences.putList(KL_ADDITIONAL_SECTIONS, sections);

        saveSectionDetails(editorWindowPreferences);

        try {
            editorWindowPreferences.flush();

        } catch (BackingStoreException e) {
            LOG.error("Error writing Section to preferences", e);
        }
    }

    public void saveSectionDetails(KometPreferences editorWindowPreferences) {
        final KometPreferences sectionPreferences = editorWindowPreferences.node(getName());

        for (PatternModel patternModel : getPatterns()) {
            patternModel.save(sectionPreferences);
        }

        try {
            sectionPreferences.flush();
        } catch (BackingStoreException e) {
            LOG.error("Error writing Section details to preferences", e);
        }
    }

    /*******************************************************************************
     *                                                                             *
     * Properties                                                                  *
     *                                                                             *
     ******************************************************************************/

    // -- name
    private final StringProperty name = new SimpleStringProperty("Untitled");
    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }
    public void setName(String name) { this.name.set(name);}

    // -- patterns
    private final ObservableList<PatternModel> patterns = FXCollections.observableArrayList();
    public ObservableList<PatternModel> getPatterns() { return patterns; }

}