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
package dev.ikm.komet.kview.klwindows.lidr;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.klwindows.AbstractEntityChapterKlWindowFactory;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.terms.EntityFacade;

import java.util.UUID;

/**
 * A factory for creating {@link LidrKlWindow} instances, which display LIDR (Logical Instrumentation,
 * Devices, and Records) details within a Komet-based application.
 * <p>
 * This class extends {@link AbstractEntityChapterKlWindowFactory} to provide implementations
 * for constructing specialized LIDR windows. It also supplies a description of the widget
 * and its implementing class for use within a desktop workspace or user interface.
 */
public class LidrKlWindowFactory extends AbstractEntityChapterKlWindowFactory {

    @Override
    public LidrKlWindow create(UUID journalTopic, EntityFacade entityFacade,
                               ViewProperties viewProperties, KometPreferences preferences) {
        return new LidrKlWindow(journalTopic, entityFacade, null, viewProperties, preferences);
    }

    /**
     * Creates a new {@link LidrKlWindow} instance, optionally specifying a device concept.
     *
     * @param journalTopic   the UUID representing the journal topic the owning Journal Window uses to communicate events.
     * @param entityFacade   entity facade when not null usually this will load and display the current details.
     * @param deviceConcept  an optional entity facade representing a device, or null for creation mode.
     * @param viewProperties view properties is access to view calculators to query data.
     * @param preferences    komet preferences assists on reading and writing data to preferences user.home/Solor/database_folder/preferences
     * @return a new {@link LidrKlWindow} instance
     */
    public LidrKlWindow create(UUID journalTopic, EntityFacade entityFacade, EntityFacade deviceConcept,
                               ViewProperties viewProperties, KometPreferences preferences) {
        return new LidrKlWindow(journalTopic, entityFacade, deviceConcept, viewProperties, preferences);
    }

    @Override
    public String klWidgetDescription() {
        return "Lidr Details Chapter Window are displayed inside of the Journal Window desktop workspace";
    }

    @Override
    public Class<?> klWidgetImplementationClass() {
        return LidrKlWindow.class;
    }
}
