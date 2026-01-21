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
import static dev.ikm.komet.preferences.KLEditorPreferences.PatternKey.PATTERN_TITLE_VISIBLE;

/**
 * Represents a Pattern. It has properties like the title of the Pattern, the fields inside it (EditorFieldModel instances),
 * number of columns, its nid.
 */
public class EditorPatternModel extends EditorGridNodeModel {
    private static final Logger LOG = LoggerFactory.getLogger(EditorPatternModel.class);

    private final ViewCalculator viewCalculator;
    private final PatternFacade patternFacade;
    private final int nid;

    /**
     * Creates a EditorPatternModel given the passed in nid of the Pattern.
     *
     * @param viewCalculator the view calculator
     * @param patternNid the nid of the Pattern
     */
    public EditorPatternModel(ViewCalculator viewCalculator, int patternNid) {
        this.viewCalculator = viewCalculator;
        this.nid = patternNid;
        patternFacade = PatternFacade.make(patternNid);

        setTitle(retrieveDisplayName(patternFacade));

        fields.addListener(this::fieldsChanged);

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

    private void fieldsChanged(ListChangeListener.Change<? extends EditorFieldModel> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                change.getAddedSubList().forEach(field -> field.setParentPattern(this));
            }
        }
    }

    /**
     * Loads and sets up the Pattern given an instance of KometPreferences (stored preferences).
     * It returns the list of PatternModels that are inside the preferences folder that's passed in to this method (the
     * passed in folder points to a Section).
     *
     * @param sectionPreferences the stored preferences pointing to a Section
     * @param viewCalculator the view calculator
     */
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
        patternPreferences.getBoolean(PATTERN_TITLE_VISIBLE).ifPresent(this::setTitleVisible);
        loadGridNodeDetails(patternPreferences);

        for (EditorFieldModel fieldModel : getFields()) {
            fieldModel.load(patternPreferences, viewCalculator);
        }
    }

    /**
     * Saves the Pattern into KometPreferences (stored preferences).
     *
     * @param sectionPreferences the stored preferences pointing to the Section
     */
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

        // Grid
        patternPreferences.putInt(KL_GRID_NUMBER_COLUMNS, getNumberColumns());

        // title visible
        patternPreferences.putBoolean(PATTERN_TITLE_VISIBLE, isTitleVisible());

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

    @Override
    public void delete() {
        getParentSection().getPatterns().remove(this);
    }

    /*******************************************************************************
     *                                                                             *
     * Properties                                                                  *
     *                                                                             *
     ******************************************************************************/

    // -- title
    /**
     * The Pattern's title.
     */
    private StringProperty title = new SimpleStringProperty();
    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    public void setTitle(String title) { this.title.set(title); }

    // -- title visible
    private BooleanProperty titleVisible = new SimpleBooleanProperty(false);
    public boolean isTitleVisible() { return titleVisible.get(); }
    public BooleanProperty titleVisibleProperty() { return titleVisible; }
    public void setTitleVisible(boolean titleVisible) { this.titleVisible.set(titleVisible); }

    // -- fields
    /**
     * The collection of EditorFieldModel (fields) this Pattern has.
     */
    private final ObservableList<EditorFieldModel> fields = FXCollections.observableArrayList();
    public ObservableList<EditorFieldModel> getFields() { return fields; }

    // -- nid
    /**
     * The Pattern's nid.
     */
    public int getNid() { return nid; }

    // -- number columns
    /**
     * The number of columns the Grid layout inside this Pattern should have.
     */
    private final IntegerProperty numberColumns = new SimpleIntegerProperty(1);
    public int getNumberColumns() { return numberColumns.get(); }
    public IntegerProperty numberColumnsProperty() { return numberColumns; }
    public void setNumberColumns(int number) { numberColumns.set(number); }

    // -- parent section
    private ReadOnlyObjectWrapper<EditorSectionModel> parentSection = new ReadOnlyObjectWrapper<>();
    public EditorSectionModel getParentSection() { return parentSection.get(); }
    public ReadOnlyObjectProperty<EditorSectionModel> parentSectionProperty() { return parentSection.getReadOnlyProperty(); }
    void setParentSection(EditorSectionModel parentSection) { this.parentSection.set(parentSection); }
}
