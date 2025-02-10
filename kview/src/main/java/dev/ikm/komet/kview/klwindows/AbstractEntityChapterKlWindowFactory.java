package dev.ikm.komet.kview.klwindows;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.layout.KlWidget;
import dev.ikm.komet.layout.window.KlSceneFactory;
import dev.ikm.komet.layout.window.KlWindow;
import dev.ikm.komet.layout.window.KlWindowFactory;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.scene.control.MenuItem;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.UUID;

/**
 * This abstract class represents a factory to create an entity chapter type window to be managed and displayed in the Journal View.
 */
public abstract class AbstractEntityChapterKlWindowFactory implements KlWindowFactory {

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

    @Override
    public WindowType factoryWindowType() {
        return WindowType.JOURNAL;
    }

    /**
     * TODO: Question: should we move this method or make it default and returns null? Journal type Chapter windows don't have JavaFX Menus.
     * @return
     */
    @Override
    public ImmutableList<MenuItem> createMenuItems() {
        throw new UnsupportedOperationException("Not supported. Journal windows do not contain JavaFX menu items.");
    }

    /**
     * TODO: Question: Not sure if this should be moved in another interface or abstract class for JavaFX type Windows (aka Stage).
     *       The second parameter is expecting a scene. A chapter window does not have a scene.
     * @param preferences The KometPreferences object that specifies where two write configuration
     *                    the default configuration preferences.
     * @param sceneFactory The KlSceneFactory object used to produce the scene
     *                     for the KlWindow. It provides the mechanism to create
     *                     and configure the scene that will be hosted within the window.
     * @return
     */
    @Override
    public KlWindow create(KometPreferences preferences, KlSceneFactory sceneFactory) {
        throw new UnsupportedOperationException("Not supported. Journal windows do not contain JavaFX scene similar to a Stage window.");
    }

    /**
     * TODO: Question: Not sure if this should be moved in another interface or abstract class for JavaFX type Windows (aka Stage).
     *       may need more information to create an entity type chapter window, such as desktop surface, etc.
     * @param preferences The KometPreferences object that specifies where two write configuration
     *                    the default configuration preferences.
     */
     @Override
    public KlWindow create(KometPreferences preferences) {
        throw new UnsupportedOperationException("Not supported. Creating Chapter windows involves additional attributes.");
    }

    @Override
    public Class<? extends KlWidget> klWidgetInterfaceClass() {
        return null;
    }
}
