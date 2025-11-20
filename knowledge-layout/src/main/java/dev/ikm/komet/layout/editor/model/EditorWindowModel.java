package dev.ikm.komet.layout.editor.model;

import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.preferences.KLEditorPreferences.KL_ADDITIONAL_SECTIONS;
import static dev.ikm.komet.preferences.KLEditorPreferences.KL_EDITOR_WINDOWS;
import static dev.ikm.komet.preferences.KLEditorPreferences.KL_MAIN_SECTION;

public class EditorWindowModel {
    public static final String EMPTY_WINDOW_TITLE = "Untitled";

    private static final Logger LOG = LoggerFactory.getLogger(EditorWindowModel.class);
    private final ObservableList<EditorSectionModel> additionalSections = FXCollections.observableArrayList();

    public EditorWindowModel() {
        additionalSections.addListener(this::additionalSectionsChanged);
        mainSection.setTagText("Section 1");
    }

    /*******************************************************************************
     *                                                                             *
     * Public API                                                                  *
     *                                                                             *
     ******************************************************************************/

    public static EditorWindowModel load(KometPreferences editorWindowPreferences, ViewCalculator viewCalculator, String title) {
        EditorWindowModel editorWindowModel = new EditorWindowModel();
        editorWindowModel.setTitle(title);

        editorWindowModel.loadMainSection(editorWindowPreferences, viewCalculator);

        // Load additional sections
        List<EditorSectionModel> editorSectionModels = EditorSectionModel.load(editorWindowPreferences, viewCalculator);
        editorWindowModel.getAdditionalSections().setAll(editorSectionModels);

        return editorWindowModel;
    }

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

        // Additional Sections
        List<EditorSectionModel> additionalSections = getAdditionalSections();

        List<String> additionalSectionsToSave = new ArrayList<>();
        for (EditorSectionModel editorSectionModel : additionalSections) {
            additionalSectionsToSave.add(editorSectionModel.getName());
        }
        editorWindowPreferences.putList(KL_ADDITIONAL_SECTIONS, additionalSectionsToSave);

        for (EditorSectionModel editorSectionModel : additionalSections) {
            editorSectionModel.save(editorWindowPreferences);
        }

        try {
            editorWindowPreferences.flush();
            klEditorAppPreferences.flush();
        } catch (BackingStoreException e) {
            LOG.error("Error writing KL Editor Window to preferences", e);
        }
    }

    /*******************************************************************************
     *                                                                             *
     * Private API                                                                 *
     *                                                                             *
     ******************************************************************************/

    private void loadMainSection(KometPreferences editorWindowPreferences, ViewCalculator viewCalculator) {
        Optional<String> mainSectionName = editorWindowPreferences.get(KL_MAIN_SECTION);
        mainSectionName.ifPresentOrElse(sectionName -> {
            EditorSectionModel mainSection = getMainSection();

            mainSection.setName(sectionName);
            final KometPreferences sectionPreferences = editorWindowPreferences.node(sectionName);
            mainSection.loadSectionDetails(sectionPreferences, viewCalculator);
        }, () -> {
            throw new RuntimeException("Can't load section (main section) from preferences");
        });
    }

    private void additionalSectionsChanged(ListChangeListener.Change<? extends EditorSectionModel> change) {
        for (int i = 0 ; i < additionalSections.size(); ++i) {
            // Update Tag Text
            EditorSectionModel editorSectionModel = additionalSections.get(i);
            editorSectionModel.setTagText("Section " + (i + 2));
        }

        // Check for duplicate Section name's and if they exist change them so they're not duplicate
        while(change.next()) {
            if (change.wasAdded()) {
                for (EditorSectionModel addedSection : change.getAddedSubList()) {
                    String originalSectionName = addedSection.getName();
                    String newSectionName = originalSectionName;
                    int i = 2;
                    while (nameExists(newSectionName, addedSection)) {
                        newSectionName = originalSectionName + " " + i;
                        ++i;
                    }
                    addedSection.setName(newSectionName);
                }
            }
        }
    }

    /**
     * Is there a Section in this WindowModel that has the name passed in as the parameter.
     *
     * @param name the name we're checking to see if already exists.
     * @param excludedEditorSectionModel exclude this section from the search.
     * @return true if there's already a Section in this WindowModel that has the name passed in.
     */
    private boolean nameExists(String name, EditorSectionModel excludedEditorSectionModel) {
        if (getMainSection().getName().equals(name)) {
            return true;
        }

        for (EditorSectionModel additionalSection : additionalSections) {
            if (additionalSection == excludedEditorSectionModel) {
                continue;
            }
            if (additionalSection.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /*******************************************************************************
     *                                                                             *
     * Properties                                                                  *
     *                                                                             *
     ******************************************************************************/

    // -- main section
    private final EditorSectionModel mainSection = new EditorSectionModel();
    public EditorSectionModel getMainSection() { return mainSection; }

    // -- title
    private final StringProperty title = new SimpleStringProperty(EMPTY_WINDOW_TITLE);
    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    public void setTitle(String title) { this.title.set(title);}

    // -- sections
    public ObservableList<EditorSectionModel> getAdditionalSections() { return additionalSections; }
}