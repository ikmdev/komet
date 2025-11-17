package dev.ikm.komet.kleditorapp.model;

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

public class WindowModel {
    private static final Logger LOG = LoggerFactory.getLogger(WindowModel.class);
    private static final WindowModel INSTANCE = new WindowModel();

    private final ObservableList<SectionModel> additionalSections = FXCollections.observableArrayList();

    private WindowModel() {
        additionalSections.addListener(this::additionalSectionsChanged);
        mainSection.setTagText("Section 1");
    }

    private void additionalSectionsChanged(ListChangeListener.Change<? extends SectionModel> change) {
        for (int i = 0 ; i < additionalSections.size(); ++i) {
            // Update Tag Text
            SectionModel sectionModel = additionalSections.get(i);
            sectionModel.setTagText("Section " + (i + 2));
        }

        // Check for duplicate Section name's and if they exist change them so they're not duplicate
        while(change.next()) {
            if (change.wasAdded()) {
                for (SectionModel addedSection : change.getAddedSubList()) {
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
     * @param excludedSectionModel exclude this section from the search.
     * @return true if there's already a Section in this WindowModel that has the name passed in.
     */
    private boolean nameExists(String name, SectionModel excludedSectionModel) {
        if (getMainSection().getName().equals(name)) {
            return true;
        }

        for (SectionModel additionalSection : additionalSections) {
            if (additionalSection == excludedSectionModel) {
                continue;
            }
            if (additionalSection.getName().equals(name)) {
                return true;
            }
        }
        return false;
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

        // Additional Sections
        List<SectionModel> additionalSections = getAdditionalSections();

        List<String> additionalSectionsToSave = new ArrayList<>();
        for (SectionModel sectionModel : additionalSections) {
            additionalSectionsToSave.add(sectionModel.getName());
        }
        editorWindowPreferences.putList(KL_ADDITIONAL_SECTIONS, additionalSectionsToSave);

        for (SectionModel sectionModel : additionalSections) {
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
        getAdditionalSections().setAll(sectionModels);
    }

    public void reset() {
        additionalSections.clear();
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
    public ObservableList<SectionModel> getAdditionalSections() { return additionalSections; }
}