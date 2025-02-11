package dev.ikm.komet.kview.klwindows.pattern;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.klwindows.AbstractEntityChapterKlWindowFactory;
import dev.ikm.komet.kview.klwindows.lidr.LidrKlWindow;
import dev.ikm.komet.layout.context.KlContextFactory;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout.window.KlJournalWindow;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.terms.EntityFacade;

import java.util.UUID;

/**
 * A factory able to create a pattern details window (entity chapter type window) to be managed and displayed
 * in the Journal View.
 */
public class PatternKlWindowFactory extends AbstractEntityChapterKlWindowFactory {

    /**
     * @param journalTopic the UUID representing the journal topic the owning Journal Window uses to communicate events.
     * @param entityFacade entity facade when not null usually this will load and display the current details.
     * @param viewProperties view properties is access to view calculators to query data.
     * @param preferences komet preferences assists on reading and writing data to preferences user.home/Solor/database_folder/preferences.
     */
    @Override
    public PatternKlWindow create(UUID journalTopic, EntityFacade entityFacade,
                                  ViewProperties viewProperties, KometPreferences preferences) {
        return new PatternKlWindow(journalTopic, entityFacade, viewProperties, preferences);
    }

    @Override
    public Class<KlJournalWindow> klInterfaceClass() {
        return KlJournalWindow.class;
    }

    @Override
    public Class<? extends KlJournalWindow> klImplementationClass() {
        return PatternKlWindow.class;
    }

    @Override
    public String klDescription() {
        return "Pattern Details Chapter Window are displayed inside of the Journal Window desktop workspace";
    }

    @Override
    public LidrKlWindow create(KlPreferencesFactory preferencesFactory) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public LidrKlWindow createWithContext(KlPreferencesFactory preferencesFactory, KlContextFactory contextFactory) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public LidrKlWindow restore(KometPreferences preferences) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
