package dev.ikm.komet.kview.klwindows;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.layout.KlFactory;
import dev.ikm.komet.layout.KlWidget;
import dev.ikm.komet.layout.window.KlJournalWindow;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.scene.control.MenuItem;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.UUID;

/**
 * This abstract class represents a factory to create an entity chapter type window to be managed and displayed in the Journal View.
 */
public abstract class AbstractEntityChapterKlWindowFactory implements KlFactory<KlJournalWindow> {

    /**
     * This chapter window excepts the following:
     * <pre>
     *     - journal topic owning journal window will communicated events.
     *     - entity facade when not null usually this will load and display the current details.
     *     - desktop surface is a JavaFX Parent uses to apply the window title area drag support.
     *     - view properties is access to view calculators to query data.
     *     - komet preferences assists on reading and writing data to preferences user.home/Solor/database_folder/preferences
     * </pre>
     * @param journalTopic the UUID representing the journal topic the owning Journal Window uses to communicate events.
     * @param entityFacade entity facade when not null usually this will load and display the current details.
     * @param viewProperties view properties is access to view calculators to query data.
     * @param preferences komet preferences assists on reading and writing data to preferences user.home/Solor/database_folder/preferences
     * @return AbstractEntityChapterKlWindow Returns a KlWindow representing a detail view of an entity. e.g. concept window, pattern window, semantic window.
     */
    abstract public AbstractEntityChapterKlWindow create(UUID journalTopic, EntityFacade entityFacade, ViewProperties viewProperties, KometPreferences preferences);
}
