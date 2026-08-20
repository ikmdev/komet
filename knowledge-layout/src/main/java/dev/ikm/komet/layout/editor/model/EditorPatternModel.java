package dev.ikm.komet.layout.editor.model;

import dev.ikm.komet.layout.KlPatternSemanticsFactories;
import dev.ikm.komet.layout.KlPatternSemanticsFactory;
import dev.ikm.komet.layout.editor.property.KlPropertySet;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.FieldDefinitionRecord;
import dev.ikm.tinkar.entity.PatternVersionRecord;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.preferences.KLEditorPreferences.ListKey.FIELDS_LIST;
import static dev.ikm.komet.preferences.KLEditorPreferences.ListKey.PATTERN_LIST;
import static dev.ikm.komet.preferences.KLEditorPreferences.PatternKey.PATTERN_COMPONENT;
import static dev.ikm.komet.preferences.KLEditorPreferences.PatternKey.PATTERN_REQUIREMENTS;
import static dev.ikm.komet.preferences.KLEditorPreferences.PatternKey.PATTERN_SEMANTICS_FACTORY;
import static dev.ikm.komet.preferences.KLEditorPreferences.PatternKey.PATTERN_SEMANTIC_FILTERS;
import static dev.ikm.komet.preferences.KLEditorPreferences.PatternKey.PATTERN_TITLE;
import static dev.ikm.komet.preferences.KLEditorPreferences.PatternKey.PATTERN_TITLE_VISIBLE;

/**
 * Represents a Pattern. It has properties like the title of the Pattern, the fields inside it (EditorFieldModel instances),
 * its nid.
 */
public class EditorPatternModel extends EditorGridNodeModel {
    private static final Logger LOG = LoggerFactory.getLogger(EditorPatternModel.class);

    /** Fully qualified name of the factory used by default when a pattern has no factory stored. */
    private static final String DEFAULT_FACTORY_CLASS_NAME =
            KlPatternSemanticsFactories.STANDARD_FACTORY_CLASS_NAME;

    private final ViewCalculator viewCalculator;
    private final PatternFacade patternFacade;
    private final int nid;
    private final ImmutableList<FieldDefinitionRecord> fieldDefinitions;

    /**
     * This placement's identity, and the name of the sub-node its details are stored under. Identifies
     * the placement rather than the Pattern, so the same Pattern placed twice in a Section is two
     * placements with their own positions, titles and rules.
     */
    private final String id;

    /*=============================================================================*
     *                                                                             *
     * Constructors                                                                *
     *                                                                             *
     *=============================================================================*/

    /*=============================================================================*
     *                                                                             *
     * Constructors                                                                *
     *                                                                             *
     *=============================================================================*/

    /**
     * Creates a EditorPatternModel given the passed in nid of the Pattern.
     *
     * @param viewCalculator the view calculator
     * @param patternNid the nid of the Pattern
     */
    public EditorPatternModel(ViewCalculator viewCalculator, int patternNid) {
        this(viewCalculator, patternNid, UUID.randomUUID().toString());
    }

    /**
     * Creates a EditorPatternModel for a Pattern already placed in a Section, restoring the placement's
     * identity so its stored details are read from (and written back to) the node they are already in.
     *
     * @param viewCalculator the view calculator
     * @param patternNid the nid of the Pattern
     * @param id the placement's id
     */
    @SuppressWarnings("removal")
    private EditorPatternModel(ViewCalculator viewCalculator, int patternNid, String id) {
        this.id = id;
        this.viewCalculator = viewCalculator;
        this.nid = patternNid;
        patternFacade = PatternFacade.make(patternNid);

        setTitle(retrieveDisplayName(patternFacade));
        setIdentifier(getTitle());

        visibleFields.addListener(this::fieldsChanged);

        // -- the fields the Pattern is defined with in the database
        Entity<EntityVersion> entity = EntityService.get().getEntityFast(patternFacade);
        Latest<EntityVersion> optionalLatest = viewCalculator.latest(entity);
        fieldDefinitions = optionalLatest.isPresent()
                ? ((PatternVersionRecord) optionalLatest.get()).fieldDefinitions()
                : Lists.immutable.empty();

        // -- lay out one field per definition; the author can then remove the ones they don't want shown
        fieldDefinitions.forEachWithIndex((fieldDefinition, index) -> {
            EditorFieldModel editorFieldModel = new EditorFieldModel(viewCalculator, fieldDefinition);
            visibleFields.add(editorFieldModel);
            editorFieldModel.setRowIndex(index);
        });

        parentGridProperty().bind(parentSectionProperty());

        // Refresh the factory-specific property set whenever the factory changes. Fires immediately
        // for the initial (default) factory, so getFactoryProperties() is populated from the start.
        factory.subscribe(currentFactory -> {
            KlPropertySet propertySet = currentFactory == null
                    ? null
                    : currentFactory.createProperties().orElse(null);
            factoryProperties.set(propertySet);
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

        for (String patternId : sectionPreferences.getList(PATTERN_LIST)) {
            PatternFacade patternFacade = getStoredPattern(sectionPreferences, patternId);

            EditorPatternModel editorPatternModel =
                    new EditorPatternModel(viewCalculator, patternFacade.nid(), patternId);
            editorPatternModels.add(editorPatternModel);

            editorPatternModel.loadPatternDetails(sectionPreferences.node(patternId), viewCalculator);
        }

        return editorPatternModels;
    }

    private static PatternFacade getStoredPattern(KometPreferences sectionPreferences, String patternId) {
        EntityFacade pattern = sectionPreferences.node(patternId).getEntity(PATTERN_COMPONENT)
                .orElseThrow(() -> new IllegalStateException(
                        "Placed Pattern '" + patternId + "' has no stored Pattern"));
        return PatternFacade.make(pattern.nid());
    }

    private void loadPatternDetails(KometPreferences patternPreferences, ViewCalculator viewCalculator) {
        // Only an authored title is stored; without one the title stays the Pattern's description,
        // as resolved by the constructor.
        patternPreferences.get(PATTERN_TITLE).ifPresent(this::setTitle);
        patternPreferences.getBoolean(PATTERN_TITLE_VISIBLE).ifPresent(this::setTitleVisible);

        loadFactory(patternPreferences);

        // Load after loadFactory so the property set for the restored factory already exists.
        KlPropertySet factoryPropertySet = getFactoryProperties();
        if (factoryPropertySet != null) {
            factoryPropertySet.load(patternPreferences);
        }

        loadGridNodeDetails(patternPreferences);

        requirements.setAll(patternPreferences.getList(PATTERN_REQUIREMENTS).stream()
                .map(EditorPatternRequirement::fromPreferenceString)
                .toList());

        semanticFilters.setAll(patternPreferences.getList(PATTERN_SEMANTIC_FILTERS).stream()
                .map(EditorPatternSemanticFilter::fromPreferenceString)
                .toList());

        // The fields the author kept. The constructor laid out one per field definition, so this drops
        // the ones that were removed. A layout stored before fields could be removed has no list and
        // keeps them all.
        patternPreferences.getOptionalList(FIELDS_LIST).ifPresent(shownFieldIndexes -> {
            List<Integer> indexes = shownFieldIndexes.stream().map(Integer::valueOf).toList();
            visibleFields.removeIf(field -> !indexes.contains(field.getIndex()));
        });

        for (EditorFieldModel fieldModel : getVisibleFields()) {
            fieldModel.load(patternPreferences, viewCalculator);
        }
    }

    private void loadFactory(KometPreferences patternPreferences) {
        Optional<String> factoryClassName = patternPreferences.get(PATTERN_SEMANTICS_FACTORY);
        factoryClassName.ifPresent(className -> {
            if (className.equals("")) {
                return;
            }

            KlPatternSemanticsFactories.byClassName(className).ifPresentOrElse(
                    this::setFactory,
                    () -> LOG.warn("Unknown pattern semantics factory '{}'; keeping default", className));
        });
    }

    /**
     * Saves the Pattern into KometPreferences (stored preferences). Which Patterns a Section holds, and
     * in what order, is written by the Section itself (see {@code EditorSectionModel}) — this stores
     * only the Pattern's own details.
     *
     * @param sectionPreferences the stored preferences pointing to the Section
     */
    public void save(KometPreferences sectionPreferences) {
        savePatternDetails(sectionPreferences);

        try {
            sectionPreferences.flush();
        } catch (BackingStoreException e) {
            LOG.error("Error writing Section to preferences", e);
        }
    }

    /**
     * Removes the stored details of the Section's placements other than the passed in ones — the
     * placements removed from the Section since it was last saved — so that details don't sit there for
     * a placement nothing points at any more.
     *
     * @param sectionPreferences the stored preferences pointing to the Section
     * @param patternIds the ids of the placements the Section still holds
     */
    static void removePlacementsOtherThan(KometPreferences sectionPreferences, List<String> patternIds) {
        sectionPreferences.getList(PATTERN_LIST).stream()
                .filter(storedPatternId -> !patternIds.contains(storedPatternId))
                .forEach(removedPatternId -> {
                    try {
                        sectionPreferences.node(removedPatternId).removeNode();
                    } catch (BackingStoreException e) {
                        LOG.error("Error removing Pattern from preferences", e);
                    }
                });
    }

    private void savePatternDetails(KometPreferences sectionPreferences) {
        KometPreferences patternPreferences = sectionPreferences.node(id);

        // the Pattern placed, which is what the placement's id stands for
        patternPreferences.putEntity(PATTERN_COMPONENT, patternFacade);

        // title: stored only when the user authored one, so that a title left as the Pattern's
        // description keeps following that description rather than freezing the text it had here.
        if (getTitle().equals(retrieveDisplayName(patternFacade))) {
            patternPreferences.remove(PATTERN_TITLE);
        } else {
            patternPreferences.put(PATTERN_TITLE, getTitle());
        }

        // title visible
        patternPreferences.putBoolean(PATTERN_TITLE_VISIBLE, isTitleVisible());

        // factory
        KlPatternSemanticsFactory klPatternSemanticsFactory = getFactory();
        String className = klPatternSemanticsFactory == null ? "" : klPatternSemanticsFactory.getClass().getName();
        patternPreferences.put(PATTERN_SEMANTICS_FACTORY, className);

        // factory-specific properties
        KlPropertySet factoryPropertySet = getFactoryProperties();
        if (factoryPropertySet != null) {
            factoryPropertySet.save(patternPreferences);
        }

        saveGridNodeDetails(patternPreferences);

        // requirement refinements
        patternPreferences.putList(PATTERN_REQUIREMENTS, requirements.stream()
                .map(EditorPatternRequirement::toPreferenceString)
                .toList());

        // semantic display filters
        patternPreferences.putList(PATTERN_SEMANTIC_FILTERS, semanticFilters.stream()
                .map(EditorPatternSemanticFilter::toPreferenceString)
                .toList());

        // the fields shown, so that the ones the author removed from the layout stay removed
        patternPreferences.putList(FIELDS_LIST, visibleFields.stream()
                .map(field -> String.valueOf(field.getIndex()))
                .toList());

        for (EditorFieldModel fieldModel : getVisibleFields()) {
            fieldModel.save(patternPreferences);
        }
    }

    private String retrieveDisplayName(PatternFacade patternFacade) {
        Optional<String> optionalStringRegularName = viewCalculator.getRegularDescriptionText(patternFacade);
        Optional<String> optionalStringFQN = viewCalculator.getFullyQualifiedNameText(patternFacade);
        // Neither may be present — e.g. a remote-backed provider whose local entity store
        // doesn't have this pattern's descriptions loaded — so fall back to the nid rather
        // than throw NoSuchElementException.
        return optionalStringRegularName.or(() -> optionalStringFQN)
                .orElseGet(() -> "Pattern [nid=" + patternFacade.nid() + "]");
    }

    /**
     * Whether the passed in semantic of this Pattern passes its display filters (see
     * {@link #getSemanticFilters()}), that is whether it should be shown wherever this Pattern's
     * semantics are rendered. A Pattern with no filters displays every semantic.
     *
     * <p>The semantic's latest version is resolved with the passed in calculator — the one the
     * semantic is displayed under rather than this model's authoring calculator — so an uncommitted
     * version counts, as it does in the required-pattern checks.
     *
     * @param semanticNid the nid of a semantic of this Pattern
     * @param viewCalculator the calculator the semantic is displayed under
     * @return whether the semantic should be displayed
     */
    public boolean displaysSemantic(int semanticNid, ViewCalculator viewCalculator) {
        if (semanticFilters.isEmpty()) {
            return true;
        }

        Latest<SemanticEntityVersion> latestVersion = viewCalculator.latest(semanticNid);
        if (latestVersion.isAbsent()) {
            return false;
        }

        ImmutableList<Object> fieldValues = latestVersion.get().fieldValues();
        return semanticFilters.stream().anyMatch(filter -> filter.matches(fieldValues));
    }

    @Override
    public void delete() {
        getParentSection().getPatterns().remove(this);
    }

    /*=============================================================================*
     *                                                                             *
     * Properties                                                                  *
     *                                                                             *
     *=============================================================================*/

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
     * The collection of EditorFieldModel (fields) this Pattern is showing.
     */
    private final ObservableList<EditorFieldModel> visibleFields = FXCollections.observableArrayList();
    public ObservableList<EditorFieldModel> getVisibleFields() { return visibleFields; }

    // -- field definitions
    /**
     * The fields this Pattern is defined with in the database, in the order they are defined in.
     *
     * <p>Unlike {@link #getVisibleFields()} — the fields laid out in the editor, which the author adds to
     * and removes from — this is the Pattern as the database has it, so it stays complete however the
     * layout is edited. It is what anything reasoning about the Pattern's semantics rather than about
     * their layout works from, e.g. the constraints of {@link EditorPatternRequirement} and
     * {@link EditorPatternSemanticFilter}, which can constrain a field whether or not it is shown.
     */
    public ImmutableList<FieldDefinitionRecord> getFieldDefinitions() { return fieldDefinitions; }

    // -- requirements
    /**
     * Refinements of this Pattern's required flag (see {@link EditorPatternRequirement}). Only
     * meaningful while {@link #isRequired()} is true; an empty list keeps the plain required
     * meaning of at least one semantic of any kind.
     */
    private final ObservableList<EditorPatternRequirement> requirements = FXCollections.observableArrayList();
    public ObservableList<EditorPatternRequirement> getRequirements() { return requirements; }

    // -- semantic filters
    /**
     * Filters selecting which of this Pattern's semantics are displayed (see
     * {@link EditorPatternSemanticFilter}). An empty list displays every semantic; several filters
     * display the semantics passing any one of them.
     */
    private final ObservableList<EditorPatternSemanticFilter> semanticFilters = FXCollections.observableArrayList();
    public ObservableList<EditorPatternSemanticFilter> getSemanticFilters() { return semanticFilters; }

    // -- view calculator
    /**
     * The calculator this Pattern's display names (and those of its requirement constraints) are
     * resolved with.
     */
    public ViewCalculator getViewCalculator() { return viewCalculator; }

    // -- nid
    /**
     * The Pattern's nid.
     */
    public int getNid() { return nid; }

    // -- pattern facade
    /**
     * The Pattern's facade. Carries the portable PublicId, so it's what should be persisted (rather than
     * the machine-local nid) when the Pattern needs to be stored in preferences.
     */
    public PatternFacade getPatternFacade() { return patternFacade; }

    // -- id
    /**
     * This placement's id, and the name of the preferences sub-node its details are stored under. It
     * identifies the placement rather than the Pattern, so that the same Pattern placed twice in a
     * Section keeps two sets of details rather than the two placements sharing (and overwriting) one.
     */
    public String getId() { return id; }

    // -- parent section
    private ReadOnlyObjectWrapper<EditorSectionModel> parentSection = new ReadOnlyObjectWrapper<>();
    public EditorSectionModel getParentSection() { return parentSection.get(); }
    public ReadOnlyObjectProperty<EditorSectionModel> parentSectionProperty() { return parentSection.getReadOnlyProperty(); }
    void setParentSection(EditorSectionModel parentSection) { this.parentSection.set(parentSection); }

    // -- identifier
    private StringProperty identifier = new SimpleStringProperty();
    public String getIdentifier() { return identifier.get(); }
    public StringProperty identifierProperty() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier.set(identifier); }

    // -- factory
    private ObjectProperty<KlPatternSemanticsFactory> factory = new SimpleObjectProperty<>(
            KlPatternSemanticsFactories.byClassName(DEFAULT_FACTORY_CLASS_NAME)
                    .orElseThrow(() -> new IllegalStateException(
                            "Default pattern semantics factory not registered: " + DEFAULT_FACTORY_CLASS_NAME)));
    public KlPatternSemanticsFactory getFactory() { return factory.get(); }
    public void setFactory(KlPatternSemanticsFactory factory) { this.factory.set(factory); }
    public ObjectProperty<KlPatternSemanticsFactory> factoryProperty() { return factory; }

    // -- factory properties
    /**
     * The live set of factory-specific properties for the currently selected factory. A fresh set is
     * created whenever the factory changes (so switching factories discards the previous factory's
     * values). This is the single instance the properties pane edits, that {@link #save} persists,
     * and that the journal control binds to.
     */
    private final ObjectProperty<KlPropertySet> factoryProperties = new SimpleObjectProperty<>();
    public KlPropertySet getFactoryProperties() { return factoryProperties.get(); }
    public ObjectProperty<KlPropertySet> factoryPropertiesProperty() { return factoryProperties; }
}
