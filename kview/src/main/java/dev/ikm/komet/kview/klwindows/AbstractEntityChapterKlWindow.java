package dev.ikm.komet.kview.klwindows;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.layout.KlGadget;
import dev.ikm.komet.layout.context.KlContext;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.scene.layout.Pane;

import java.util.UUID;

/**
 * An abstract entity chapter window (Abstract chapter window) maintaining the following:
 * <pre>
 *      journalTopic - journal topic owning journal window will communicated events.
 *      entityFacade - entity facade when not null usually this will load and display the current details.
 *      viewProperties - view properties is access to view calculators to query data.
 *      preferences - komet preferences assists on reading and writing data to preferences user.home/Solor/database_folder/preferences
 *
 * </pre>
 *
 * @see AbstractChapterKlWindow
 * @see EntityFacade
 */
public abstract class AbstractEntityChapterKlWindow extends AbstractChapterKlWindow<Pane> {

    /**
     * The UUID for the journal topic used by the owning Journal Window to communicate events.
     */
    private UUID journalTopic;

    /**
     * The {@link EntityFacade} representing the entity to display or edit.
     */
    private EntityFacade entityFacade;

    /**
     * Constructs a new entity-focused chapter window with references to the journal topic,
     * entity facade, view properties, and user preferences.
     *
     * @param journalTopic the UUID representing the journal topic the owning Journal Window uses to communicate events.
     * @param entityFacade entity facade when not null usually this will load and display the current details.
     * @param viewProperties view properties is access to view calculators to query data.
     * @param preferences komet preferences assists on reading and writing data to preferences user.home/Solor/database_folder/preferences.
     */
    public AbstractEntityChapterKlWindow(UUID journalTopic,
                                         EntityFacade entityFacade,
                                         ViewProperties viewProperties,
                                         KometPreferences preferences) {
        super(viewProperties, preferences);
        this.journalTopic = journalTopic;
        this.entityFacade = entityFacade;
    }

    /**
     * @return The current {@link EntityFacade} being displayed or edited.
     */
    public EntityFacade getEntityFacade() {
        return entityFacade;
    }

    /**
     * Updates the {@link EntityFacade} for this window.
     *
     * @param entityFacade The new {@link EntityFacade} to display or edit.
     */
    protected void setEntityFacade(EntityFacade entityFacade) {
        this.entityFacade = entityFacade;
    }

    /**
     * @return The UUID for the journal topic used by the owning Journal Window.
     */
    public UUID getJournalTopic() {
        return journalTopic;
    }

    /**
     * Sets a new journal topic UUID.
     *
     * @param journalTopic The updated UUID representing the journal topic.
     */
    protected void setJournalTopic(UUID journalTopic) {
        this.journalTopic = journalTopic;
    }
}