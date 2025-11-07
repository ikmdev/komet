package dev.ikm.komet.kleditorapp.model;

import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.preferences.KLEditorPreferences.KL_EDITOR_WINDOWS;
import static dev.ikm.komet.preferences.KLEditorPreferences.KL_MAIN_SECTION;

public class WindowModel {
    private static final Logger LOG = LoggerFactory.getLogger(WindowModel.class);
    private static final WindowModel INSTANCE = new WindowModel();

    private ObservableList<SectionModel> aditionalSections = FXCollections.observableArrayList();

    private WindowModel() {
    }

    public static WindowModel instance() { return INSTANCE; }

    public void save(KometPreferences klEditorAppPreferences) {
        final KometPreferences editorWindowPreferences = klEditorAppPreferences.node(getTitle());

        List<String> editorWindows = klEditorAppPreferences.getList(KL_EDITOR_WINDOWS);
        if (!editorWindows.contains(getTitle())) {
            editorWindows.add(getTitle());
        }

        klEditorAppPreferences.putList(KL_EDITOR_WINDOWS, editorWindows);

        // Main Section
        editorWindowPreferences.put(KL_MAIN_SECTION, getMainSection().getName());
        mainSection.save(editorWindowPreferences);

        // Aditional Sections
        for (SectionModel sectionModel : getAditionalSections()) {
            sectionModel.save(editorWindowPreferences);
        }

        try {
            editorWindowPreferences.flush();
            klEditorAppPreferences.flush();
        } catch (BackingStoreException e) {
            LOG.error("Error writing KL Editor Window to preferences", e);
        }
    }

    public void load(KometPreferences klEditorAppPreferences, ViewCalculator viewCalculator) {
        final KometPreferences editorWindowPreferences = klEditorAppPreferences.node(getTitle());

        loadMainSection(editorWindowPreferences, viewCalculator);

        // Load aditional sections
        List<SectionModel> sectionModels = SectionModel.load(editorWindowPreferences, viewCalculator);
        getAditionalSections().setAll(sectionModels);
    }

    private void loadMainSection(KometPreferences editorWindowPreferences, ViewCalculator viewCalculator) {
        Optional<String> mainSectionName = editorWindowPreferences.get(KL_MAIN_SECTION);
        mainSectionName.ifPresentOrElse(sectionName -> {
            SectionModel mainSection = getMainSection();

            mainSection.setName(sectionName);
            final KometPreferences sectionPreferences = editorWindowPreferences.node(sectionName);
            mainSection.loadSectionDetails(sectionPreferences, viewCalculator);
        }, () -> {
            throw new RuntimeException("Can't load section (main section) from preferences");
        });
    }

    /*******************************************************************************
     *                                                                             *
     * Properties                                                                  *
     *                                                                             *
     ******************************************************************************/

    // -- main section
    private final SectionModel mainSection = new SectionModel();
    public SectionModel getMainSection() { return mainSection; }

    // -- title
    private final StringProperty title = new SimpleStringProperty("Untitled");
    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    public void setTitle(String title) { this.title.set(title);}

    // -- sections
    public ObservableList<SectionModel> getAditionalSections() { return aditionalSections; }
}