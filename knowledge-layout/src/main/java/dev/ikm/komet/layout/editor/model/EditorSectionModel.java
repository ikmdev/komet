package dev.ikm.komet.layout.editor.model;

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

public class EditorSectionModel {
    private static final Logger LOG = LoggerFactory.getLogger(EditorSectionModel.class);

    public static final String UNTITLED_SECTION_NAME = "Untitled";

    /*******************************************************************************
     *                                                                             *
     * Public API                                                                  *
     *                                                                             *
     ******************************************************************************/

    public static List<EditorSectionModel> load(KometPreferences editorWindowPreferences, ViewCalculator viewCalculator) {
        List<EditorSectionModel> editorSectionModels = new ArrayList<>();

        List<String> sectionNames = editorWindowPreferences.getList(KL_ADDITIONAL_SECTIONS);

        for (String sectionName : sectionNames) {
            EditorSectionModel editorSectionModel = new EditorSectionModel();
            editorSectionModel.setName(sectionName);

            final KometPreferences sectionPreferences = editorWindowPreferences.node(sectionName);
            editorSectionModel.loadSectionDetails(sectionPreferences, viewCalculator);

            editorSectionModels.add(editorSectionModel);
        }

        return editorSectionModels;
    }

    public void loadSectionDetails(KometPreferences sectionPreferences, ViewCalculator viewCalculator) {
        List<EditorPatternModel> editorPatternModels = EditorPatternModel.load(sectionPreferences, viewCalculator);
        getPatterns().setAll(editorPatternModels);
    }

    public void save(KometPreferences editorWindowPreferences) {
        saveSectionDetails(editorWindowPreferences);

        try {
            editorWindowPreferences.flush();

        } catch (BackingStoreException e) {
            LOG.error("Error writing Section to preferences", e);
        }
    }

    public void saveSectionDetails(KometPreferences editorWindowPreferences) {
        final KometPreferences sectionPreferences = editorWindowPreferences.node(getName());

        for (EditorPatternModel editorPatternModel : getPatterns()) {
            editorPatternModel.save(sectionPreferences);
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
    private final StringProperty name = new SimpleStringProperty(UNTITLED_SECTION_NAME);
    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }
    public void setName(String name) { this.name.set(name);}

    // -- tag text
    private final StringProperty tagText = new SimpleStringProperty();
    public String getTagText() { return tagText.get(); }
    public StringProperty tagTextProperty() { return tagText; }
    public void setTagText(String text) { tagText.set(text); }

    // -- patterns
    private final ObservableList<EditorPatternModel> patterns = FXCollections.observableArrayList();
    public ObservableList<EditorPatternModel> getPatterns() { return patterns; }

}