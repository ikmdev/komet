/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.kview.klwindows.concept;

import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.framework.window.WindowSettings;
import dev.ikm.komet.kview.klwindows.AbstractEntityChapterKlWindowFactory;
import dev.ikm.komet.layout.context.KlContextFactory;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout.window.KlJournalWindow;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.terms.EntityFacade;

import java.util.UUID;

/**
 * A factory for creating {@link ConceptKlWindow} instances to display and manage
 * concept details within a Komet-based application.
 * <p>
 * This class extends {@link AbstractEntityChapterKlWindowFactory} to build
 * specialized windows for concept creation and editing. It also associates
 * relevant view settings to the resulting windows.
 */
public class ConceptKlWindowFactory extends AbstractEntityChapterKlWindowFactory {

    @Override
    public ConceptKlWindow create(UUID journalTopic,
                                  EntityFacade entityFacade,
                                  ViewProperties viewProperties,
                                  KometPreferences preferences) {
        WindowSettings windowSettings = new WindowSettings(preferences);
        ObservableViewNoOverride windowView = windowSettings.getView();
        return new ConceptKlWindow(journalTopic, entityFacade, windowView, viewProperties, preferences);
    }

    /**
     * Creates a new {@link ConceptKlWindow} instance using an existing {@link ObservableViewNoOverride}.
     *
     * @param journalTopic   a {@link UUID} representing the topic or context of the journal
     * @param entityFacade   the {@link EntityFacade} representing the concept; may be {@code null} for create mode
     * @param windowView     an existing {@link ObservableViewNoOverride} to be used in the window
     * @param viewProperties the {@link ViewProperties} for configuring the UI
     * @param preferences    the {@link KometPreferences} for storing and retrieving user/system preferences
     * @return a new {@link ConceptKlWindow} instance
     */
    public ConceptKlWindow create(UUID journalTopic,
                                  EntityFacade entityFacade,
                                  ObservableViewNoOverride windowView,
                                  ViewProperties viewProperties,
                                  KometPreferences preferences) {
        return new ConceptKlWindow(journalTopic, entityFacade, windowView, viewProperties, preferences);
    }

    @Override
    public KlJournalWindow create(KlPreferencesFactory preferencesFactory) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public KlJournalWindow createWithContext(KlPreferencesFactory preferencesFactory, KlContextFactory contextFactory) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public KlJournalWindow restore(KometPreferences preferences) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Class<? extends KlJournalWindow> klImplementationClass() {
        return ConceptKlWindow.class;
    }

    @Override
    public String klDescription() {
        return "Concept Details Chapter Window are displayed inside of the Journal Window desktop workspace";
    }
}
