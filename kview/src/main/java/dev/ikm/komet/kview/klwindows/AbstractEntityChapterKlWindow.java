package dev.ikm.komet.kview.klwindows;

import dev.ikm.komet.framework.view.ViewProperties;
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
 */
public abstract class AbstractEntityChapterKlWindow extends AbstractChapterKlWindow<Pane> {
    private UUID journalTopic;
    private EntityFacade entityFacade;


    public AbstractEntityChapterKlWindow(UUID journalTopic, EntityFacade entityFacade, ViewProperties viewProperties, KometPreferences preferences) {
        super(viewProperties, preferences);
        this.journalTopic = journalTopic;
        this.entityFacade = entityFacade;
    }

    public EntityFacade getEntityFacade() {
        return entityFacade;
    }

    protected void setEntityFacade(EntityFacade entityFacade) {
        this.entityFacade = entityFacade;
    }

    public UUID getJournalTopic() {
        return journalTopic;
    }

    protected void setJournalTopic(UUID journalTopic) {
        this.journalTopic = journalTopic;
    }
}
