package dev.ikm.komet.layout.editor.model;

import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.Region;
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
import static dev.ikm.komet.preferences.KLEditorPreferences.KL_WINDOW_COORDINATE_VISIBLE;
import static dev.ikm.komet.preferences.KLEditorPreferences.KL_WINDOW_PREF_HEIGHT;
import static dev.ikm.komet.preferences.KLEditorPreferences.KL_WINDOW_PREF_WIDTH;
import static dev.ikm.komet.preferences.KLEditorPreferences.KL_WINDOW_TIMELINE_VISIBLE;

/**
 * Represents a Window. It has properties like its title, the main Section (EditorSectionModel instance)
 * the additional Sections inside it (EditorSectionModel instances).
 * Every window has to have at least one section that's why there is a main Section property with a getter.
 */
public class EditorWindowModel extends EditorModelBase {
    private static final Logger LOG = LoggerFactory.getLogger(EditorWindowModel.class);

    /**
     * Sentinel for an "Auto" (content-driven) Window dimension. New Windows start out Auto in both
     * dimensions so the Window hugs its content (no extra blank space); it's also the value
     * stored/restored when the user hasn't pinned an explicit size.
     */
    public static final double AUTO_SIZE = Region.USE_COMPUTED_SIZE;

    private final ObservableList<EditorSectionModel> additionalSections = FXCollections.observableArrayList();

    public EditorWindowModel() {
        additionalSections.addListener(this::additionalSectionsChanged);
        mainSection.setTagText("Section 1");
    }

    /**
     * The Window can't be deleted from the properties pane (it's the root of the layout), so deletion
     * is a no-op. {@link EditorModelBase} requires this so the Window participates in the same
     * properties-pane lifecycle as the other selectable elements.
     */
    @Override
    public void delete() {
        // no-op: the Window is the root of the layout and cannot be deleted.
    }

    /*******************************************************************************
     *                                                                             *
     * Public API                                                                  *
     *                                                                             *
     ******************************************************************************/

    /**
     * Loads and sets up the Window given an instance of KometPreferences (stored preferences).
     *
     * @param editorWindowPreferences the stored preferences pointing to the Window
     * @param viewCalculator the view calculator
     */
    public static EditorWindowModel load(KometPreferences editorWindowPreferences, ViewCalculator viewCalculator, String title) {
        EditorWindowModel editorWindowModel = new EditorWindowModel();

        editorWindowModel.setTitle(title);

        // Window (view) size and control-bar options.
        editorWindowPreferences.getDouble(KL_WINDOW_PREF_WIDTH).ifPresent(editorWindowModel::setPrefWidth);
        editorWindowPreferences.getDouble(KL_WINDOW_PREF_HEIGHT).ifPresent(editorWindowModel::setPrefHeight);
        editorWindowPreferences.getBoolean(KL_WINDOW_COORDINATE_VISIBLE).ifPresent(editorWindowModel::setCoordinateVisible);
        editorWindowPreferences.getBoolean(KL_WINDOW_TIMELINE_VISIBLE).ifPresent(editorWindowModel::setTimelineVisible);

        editorWindowModel.loadMainSection(editorWindowPreferences, viewCalculator);

        // Load additional sections
        List<EditorSectionModel> editorSectionModels = EditorSectionModel.load(editorWindowPreferences, viewCalculator);
        editorWindowModel.getAdditionalSections().setAll(editorSectionModels);

        return editorWindowModel;
    }

    /**
     * Saves the Window into KometPreferences (stored preferences).
     *
     * @param klEditorAppPreferences the stored preferences pointing to the kl editor app
     */
    public void save(KometPreferences klEditorAppPreferences) {
        final KometPreferences editorWindowPreferences = klEditorAppPreferences.node(getTitle());

        List<String> editorWindows = klEditorAppPreferences.getList(KL_EDITOR_WINDOWS);
        if (!editorWindows.contains(getTitle())) {
            editorWindows.add(getTitle());
        }

        klEditorAppPreferences.putList(KL_EDITOR_WINDOWS, editorWindows);

        // Window (view) size and control-bar options.
        editorWindowPreferences.putDouble(KL_WINDOW_PREF_WIDTH, getPrefWidth());
        editorWindowPreferences.putDouble(KL_WINDOW_PREF_HEIGHT, getPrefHeight());
        editorWindowPreferences.putBoolean(KL_WINDOW_COORDINATE_VISIBLE, isCoordinateVisible());
        editorWindowPreferences.putBoolean(KL_WINDOW_TIMELINE_VISIBLE, isTimelineVisible());

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

                    // update parent window on Section model
                    addedSection.setParentWindow(this);
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
    /**
     * The main section. Every Window needs to at least have one.
     */
    private final EditorSectionModel mainSection = new EditorSectionModel();
    public EditorSectionModel getMainSection() { return mainSection; }

    // -- title
    /**
     * The title of the Section.
     */
    private final StringProperty title = new SimpleStringProperty();
    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    public void setTitle(String title) { this.title.set(title);}

    // -- sections
    /**
     * The additional Sections (besides the main section) inside this Window.
     */
    public ObservableList<EditorSectionModel> getAdditionalSections() { return additionalSections; }

    // -- preferred width
    /**
     * The Window's preferred (view) width in pixels.
     */
    private final DoubleProperty prefWidth = new SimpleDoubleProperty(this, "prefWidth", AUTO_SIZE);
    public double getPrefWidth() { return prefWidth.get(); }
    public DoubleProperty prefWidthProperty() { return prefWidth; }
    public void setPrefWidth(double width) { prefWidth.set(width); }

    // -- preferred height
    /**
     * The Window's preferred (view) height in pixels.
     */
    private final DoubleProperty prefHeight = new SimpleDoubleProperty(this, "prefHeight", AUTO_SIZE);
    public double getPrefHeight() { return prefHeight.get(); }
    public DoubleProperty prefHeightProperty() { return prefHeight; }
    public void setPrefHeight(double height) { prefHeight.set(height); }

    // -- coordinate icon visible (control bar option)
    /**
     * Whether the Coordinate control-bar icon is shown in the Window header.
     */
    private final BooleanProperty coordinateVisible = new SimpleBooleanProperty(this, "coordinateVisible", true);
    public boolean isCoordinateVisible() { return coordinateVisible.get(); }
    public BooleanProperty coordinateVisibleProperty() { return coordinateVisible; }
    public void setCoordinateVisible(boolean visible) { coordinateVisible.set(visible); }

    // -- timeline icon visible (control bar option)
    /**
     * Whether the Timeline control-bar icon is shown in the Window header.
     */
    private final BooleanProperty timelineVisible = new SimpleBooleanProperty(this, "timelineVisible", true);
    public boolean isTimelineVisible() { return timelineVisible.get(); }
    public BooleanProperty timelineVisibleProperty() { return timelineVisible; }
    public void setTimelineVisible(boolean visible) { timelineVisible.set(visible); }
}