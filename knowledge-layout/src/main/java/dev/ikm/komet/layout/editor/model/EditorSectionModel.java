package dev.ikm.komet.layout.editor.model;

import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.preferences.KLEditorPreferences.GridLayoutKey.KL_GRID_NUMBER_COLUMNS;
import static dev.ikm.komet.preferences.KLEditorPreferences.KL_ADDITIONAL_SECTIONS;

/**
 * Represents a Section. It has properties like the Section name, the patterns inside it (EditorPatternModel instances),
 * number of columns, tag text (e.g. "Section 1").
 */
public class EditorSectionModel extends EditorModelBase {
    private static final Logger LOG = LoggerFactory.getLogger(EditorSectionModel.class);

    public static final String UNTITLED_SECTION_NAME = "Untitled";

    public EditorSectionModel() {
        patterns.addListener(this::patternsChanged);
    }

    /*******************************************************************************
     *                                                                             *
     * Public API                                                                  *
     *                                                                             *
     ******************************************************************************/

    /**
     * Loads and sets up the Section given an instance of KometPreferences (stored preferences).
     * It returns the list of SectionModels that are inside the preferences folder that's passed in to this method (the
     * passed in folder points to a window).
     *
     * @param editorWindowPreferences the stored preferences pointing to the window
     * @param viewCalculator the view calculator
     */
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

    /**
     * Loads and sets up the Section given an instance of KometPreferences (stored preferences).
     *
     * @param sectionPreferences the stored preferences pointing to section
     * @param viewCalculator the view calculator
     */
    public void loadSectionDetails(KometPreferences sectionPreferences, ViewCalculator viewCalculator) {
        sectionPreferences.getInt(KL_GRID_NUMBER_COLUMNS).ifPresent(this::setNumberColumns);

        List<EditorPatternModel> editorPatternModels = EditorPatternModel.load(sectionPreferences, viewCalculator);
        getPatterns().setAll(editorPatternModels);
    }

    /**
     * Saves the Section into KometPreferences (stored preferences).
     *
     * @param editorWindowPreferences the stored preferences pointing to the window
     */
    public void save(KometPreferences editorWindowPreferences) {
        saveSectionDetails(editorWindowPreferences);

        try {
            editorWindowPreferences.flush();

        } catch (BackingStoreException e) {
            LOG.error("Error writing Section to preferences", e);
        }
    }

    @Override
    public void delete() {
        getParentWindow().getAdditionalSections().remove(this);
    }

    /*******************************************************************************
     *                                                                             *
     * Private Implementation                                                      *
     *                                                                             *
     ******************************************************************************/

    private void patternsChanged(ListChangeListener.Change<? extends EditorPatternModel> change) {
        while(change.next()) {
            if (change.wasAdded()) {
                change.getAddedSubList().forEach(pattern -> pattern.setParentSection(this));
            }
        }
    }

    private void saveSectionDetails(KometPreferences editorWindowPreferences) {
        final KometPreferences sectionPreferences = editorWindowPreferences.node(getName());

        sectionPreferences.putInt(KL_GRID_NUMBER_COLUMNS, getNumberColumns());

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
    /**
     * The name of the Section.
     */
    private final StringProperty name = new SimpleStringProperty(UNTITLED_SECTION_NAME);
    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }
    public void setName(String name) { this.name.set(name);}

    // -- tag text
    /**
     * The tag text, like "Section 1".
     */
    private final StringProperty tagText = new SimpleStringProperty();
    public String getTagText() { return tagText.get(); }
    public StringProperty tagTextProperty() { return tagText; }
    public void setTagText(String text) { tagText.set(text); }

    // -- patterns
    /**
     * The collection of Pattern's inside this Section.
     */
    private final ObservableList<EditorPatternModel> patterns = FXCollections.observableArrayList();
    public ObservableList<EditorPatternModel> getPatterns() { return patterns; }

    // -- number columns
    /**
     * The number of columns that this Section should have.
     */
    private final IntegerProperty numberColumns = new SimpleIntegerProperty(1);
    public int getNumberColumns() { return numberColumns.get(); }
    public IntegerProperty numberColumnsProperty() { return numberColumns; }
    public void setNumberColumns(int number) { numberColumns.set(number); }

    // -- parent window
    ReadOnlyObjectWrapper<EditorWindowModel> parentWindow = new ReadOnlyObjectWrapper<>();
    public EditorWindowModel getParentWindow() { return parentWindow.get(); }
    public ReadOnlyObjectProperty<EditorWindowModel> parentWindowProperty() { return parentWindow; }
    void setParentWindow(EditorWindowModel editorWindowModel) { parentWindow.set(editorWindowModel); }

    // -- start collapsed
    private final BooleanProperty startCollapsed = new SimpleBooleanProperty(false);
    public boolean isStartCollapsed() { return startCollapsed.get(); }
    public BooleanProperty startCollapsedProperty() { return startCollapsed; }
    public void setStartCollapsed(boolean value) { startCollapsed.set(value); }
}