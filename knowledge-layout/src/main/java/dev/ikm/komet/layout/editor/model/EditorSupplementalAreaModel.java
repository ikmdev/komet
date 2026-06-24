package dev.ikm.komet.layout.editor.model;

import dev.ikm.komet.layout.KlRestorable;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.preferences.KLEditorPreferences.KL_AREA_FACTORY_CLASS_NAME;

/**
 * Represents a placed supplemental area within a Section — a {@code KlSupplementalArea} contributed
 * through the editor's "Controls" palette (for example a Claude check area or an embeddable chat).
 *
 * <p>It carries the area's {@code KlSupplementalArea.Factory} class name (persisted so the runtime
 * can restore the area via the standard {@code FACTORY_CLASS_NAME} mechanism) and inherits grid
 * placement (row/column/span) from {@link EditorGridNodeModel}. Each placed area has a stable
 * {@link #getId() id} used as its preferences sub-node name.
 */
public class EditorSupplementalAreaModel extends EditorGridNodeModel {
    private static final Logger LOG = LoggerFactory.getLogger(EditorSupplementalAreaModel.class);

    private final UUID id;
    private final String areaFactoryClassName;

    /**
     * Creates a new placed area for the given factory class name, assigning a fresh id.
     *
     * @param areaFactoryClassName the {@code KlSupplementalArea.Factory} class name
     */
    public EditorSupplementalAreaModel(String areaFactoryClassName) {
        this(UUID.randomUUID(), areaFactoryClassName);
    }

    private EditorSupplementalAreaModel(UUID id, String areaFactoryClassName) {
        this.id = id;
        this.areaFactoryClassName = areaFactoryClassName;
        setTitle(deriveTitle(areaFactoryClassName));
        parentGridProperty().bind(parentSectionProperty());
    }

    /**
     * Loads a placed supplemental area from a Section's preferences.
     *
     * @param sectionPreferences the Section's preferences node
     * @param id                 the area's identifier
     * @return the loaded model
     */
    public static EditorSupplementalAreaModel load(KometPreferences sectionPreferences, UUID id) {
        KometPreferences areaPreferences = sectionPreferences.node(id.toString());
        String factoryClassName = areaPreferences.get(KL_AREA_FACTORY_CLASS_NAME, "");
        EditorSupplementalAreaModel model = new EditorSupplementalAreaModel(id, factoryClassName);
        model.loadGridNodeDetails(areaPreferences);
        return model;
    }

    /**
     * Saves this placed supplemental area into the Section's preferences.
     *
     * @param sectionPreferences the Section's preferences node
     */
    public void save(KometPreferences sectionPreferences) {
        KometPreferences areaPreferences = sectionPreferences.node(id.toString());
        areaPreferences.put(KL_AREA_FACTORY_CLASS_NAME, areaFactoryClassName);
        saveGridNodeDetails(areaPreferences);
        try {
            areaPreferences.flush();
        } catch (BackingStoreException e) {
            LOG.error("Error writing supplemental area to preferences", e);
        }
    }

    private static String deriveTitle(String factoryClassName) {
        if (factoryClassName == null || factoryClassName.isBlank()) {
            return "Area";
        }
        String simple = factoryClassName.substring(factoryClassName.lastIndexOf('.') + 1);
        int dollar = simple.indexOf('$');
        if (dollar >= 0) {
            simple = simple.substring(0, dollar);
        }
        return KlRestorable.camelCaseToWords(simple);
    }

    @Override
    public void delete() {
        if (getParentSection() != null) {
            getParentSection().getSupplementalAreas().remove(this);
        }
    }

    /**
     * Returns this area's stable identifier (its preferences sub-node name).
     *
     * @return the id
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns the {@code KlSupplementalArea.Factory} class name backing this area.
     *
     * @return the factory class name
     */
    public String getAreaFactoryClassName() {
        return areaFactoryClassName;
    }

    // -- title
    private final StringProperty title = new SimpleStringProperty();
    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    public void setTitle(String title) { this.title.set(title); }

    // -- parent section
    private final ReadOnlyObjectWrapper<EditorSectionModel> parentSection = new ReadOnlyObjectWrapper<>();
    public EditorSectionModel getParentSection() { return parentSection.get(); }
    public ReadOnlyObjectProperty<EditorSectionModel> parentSectionProperty() { return parentSection.getReadOnlyProperty(); }
    void setParentSection(EditorSectionModel section) { this.parentSection.set(section); }
}
