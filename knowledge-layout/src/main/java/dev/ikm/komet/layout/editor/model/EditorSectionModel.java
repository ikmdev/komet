package dev.ikm.komet.layout.editor.model;

import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.preferences.KLEditorPreferences.GridLayoutKey.KL_GRID_NUMBER_COLUMNS;
import static dev.ikm.komet.preferences.KLEditorPreferences.KL_ADDITIONAL_SECTIONS;
import static dev.ikm.komet.preferences.KLEditorPreferences.KL_REFERENCE_COMPONENT;
import static dev.ikm.komet.preferences.KLEditorPreferences.KL_SUPPLEMENTAL_AREAS;

/**
 * Represents a Section. It has properties like the Section name, the patterns inside it (EditorPatternModel instances),
 * number of columns, tag text (e.g. "Section 1").
 */
public class EditorSectionModel extends EditorModelBase implements ParentGridModel {
    private static final Logger LOG = LoggerFactory.getLogger(EditorSectionModel.class);

    public static final String UNTITLED_SECTION_NAME = "Untitled";

    /** Guards {@link #name} updates made by this class so they aren't mistaken for a manual (user) rename. */
    private boolean settingNameInternally = false;

    /**
     * True once the user has manually renamed this Section. A manually assigned name is preserved and is
     * never auto-reset when the Section is emptied.
     */
    private boolean nameManuallyAssigned = false;

    public EditorSectionModel() {
        patterns.addListener(this::patternsChanged);
        supplementalAreas.addListener(this::supplementalAreasChanged);
        // Detect manual (user) renames so they can be preserved; internal auto-naming is guarded.
        name.subscribe(() -> {
            if (!settingNameInternally) {
                nameManuallyAssigned = true;
            }
        });
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

        sectionPreferences.getEntity(KL_REFERENCE_COMPONENT).ifPresent(referenceComponent ->
                setReferenceComponent(new EditorPatternModel(viewCalculator, referenceComponent.nid())));

        List<EditorPatternModel> editorPatternModels = EditorPatternModel.load(sectionPreferences, viewCalculator);
        getPatterns().setAll(editorPatternModels);

        List<String> areaIds = sectionPreferences.getList(KL_SUPPLEMENTAL_AREAS);
        List<EditorSupplementalAreaModel> areas = new ArrayList<>();
        for (String areaId : areaIds) {
            areas.add(EditorSupplementalAreaModel.load(sectionPreferences, UUID.fromString(areaId)));
        }
        getSupplementalAreas().setAll(areas);
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
                autoNameSection(change.getAddedSubList());
                change.getAddedSubList().forEach(pattern -> pattern.setParentSection(this));
            }
            // When the Section is emptied, revert an auto-assigned name so the next Pattern added can
            // name the Section again (see issue #93). A name the user set manually is left untouched.
            if (change.wasRemoved() && patterns.isEmpty() && !nameManuallyAssigned) {
                setNameInternally(UNTITLED_SECTION_NAME);
            }
        }
    }

    /**
     * Auto names this Section if it's still untitled.
     * This implementation names the Section based of the first Pattern in the list that is passed
     * into this method. It uses the Patterns name with the  "Pattern" part stripped of the name of the Pattern
     * if it's there.
     *
     * @param addedPatterns the Patterns that were added
     */
    private void autoNameSection(List<? extends EditorPatternModel> addedPatterns) {
        if (isUntitledSection()){
            String patternTitle = addedPatterns.getFirst().getTitle();
            String newName = patternTitle.endsWith("Pattern")
                    ? patternTitle.substring(0, patternTitle.length() - "Pattern".length()).trim()
                    : patternTitle.stripTrailing();
            setNameInternally(newName);
        }
    }

    /**
     * Sets the Section name on behalf of the application (auto-naming or reset), without marking the name
     * as manually assigned by the user.
     *
     * @param newName the name to set
     */
    private void setNameInternally(String newName) {
        settingNameInternally = true;
        name.set(newName);
        settingNameInternally = false;
    }

    private void supplementalAreasChanged(ListChangeListener.Change<? extends EditorSupplementalAreaModel> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                change.getAddedSubList().forEach(area -> area.setParentSection(this));
            }
        }
    }

    /**
     * Checks if the Section is Unitlted (Section's are initially named "Untitled" + a number).
     *
     * @return returns true if this Section is untitled
     */
    private boolean isUntitledSection() {
        return name.get().matches("^Untitled\\s*\\d*$");
    }

    private void saveSectionDetails(KometPreferences editorWindowPreferences) {
        final KometPreferences sectionPreferences = editorWindowPreferences.node(getName());

        sectionPreferences.putInt(KL_GRID_NUMBER_COLUMNS, getNumberColumns());
        if (getReferenceComponent() != null) {
            sectionPreferences.putEntity(KL_REFERENCE_COMPONENT, getReferenceComponent().getPatternFacade());
        }

        for (EditorPatternModel editorPatternModel : getPatterns()) {
            editorPatternModel.save(sectionPreferences);
        }

        List<String> areaIds = new ArrayList<>();
        for (EditorSupplementalAreaModel area : getSupplementalAreas()) {
            area.save(sectionPreferences);
            areaIds.add(area.getId().toString());
        }
        sectionPreferences.putList(KL_SUPPLEMENTAL_AREAS, areaIds);

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

    // -- supplemental areas
    /**
     * The collection of placed supplemental areas inside this Section.
     */
    private final ObservableList<EditorSupplementalAreaModel> supplementalAreas = FXCollections.observableArrayList();
    public ObservableList<EditorSupplementalAreaModel> getSupplementalAreas() { return supplementalAreas; }

    // -- number columns
    /**
     * The number of columns that this Section has.
     */
    private final IntegerProperty numberColumns = new SimpleIntegerProperty(1);
    @Override
    public IntegerProperty numberColumnsProperty() { return numberColumns; }

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

    // -- reference component
    private final ObjectProperty<EditorPatternModel> referenceComponent = new SimpleObjectProperty<>();
    public EditorPatternModel getReferenceComponent() { return referenceComponent.get(); }
    public ObjectProperty<EditorPatternModel> referenceComponentProperty() { return referenceComponent; }
    public void setReferenceComponent(EditorPatternModel editorPatternModel) { referenceComponent.set(editorPatternModel); }
}