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
package dev.ikm.komet.app.test;

import dev.ikm.komet.amplify.viewmodels.StampViewModel;
import dev.ikm.komet.amplify.mvvm.ViewModel;
import dev.ikm.komet.amplify.viewmodels.ConceptViewModel;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.framework.window.WindowSettings;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;

import static dev.ikm.komet.amplify.viewmodels.StampViewModel.*;
import static dev.ikm.komet.amplify.viewmodels.StampViewModel.PATHS_PROPERTY;
import static dev.ikm.komet.amplify.viewmodels.ConceptViewModel.*;
import static dev.ikm.komet.preferences.JournalWindowPreferences.MAIN_KOMET_WINDOW;

public class ConceptViewModelTest {
    public static void main(String[] args) {
        // point to a file directory
        // start datbase
        // TODO this throws an error because database is not started.
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        KometPreferences windowPreferences = appPreferences.node(MAIN_KOMET_WINDOW);

        WindowSettings windowSettings = new WindowSettings(windowPreferences);
        ViewProperties viewProperties = windowSettings.getView().makeOverridableViewProperties();


        StampViewModel stampViewModel = new StampViewModel();
        stampViewModel
                .setPropertyValue(STATUS_PROPERTY, "Incomplete")
                .setPropertyValue(TIME_PROPERTY, System.currentTimeMillis())
                .setPropertyValue(MODULE_PROPERTY, 0)
                .setPropertyValue(PATH_PROPERTY, 0)
                .addProperty(MODULES_PROPERTY, stampViewModel.findAllModules(viewProperties), true)
                .addProperty(PATHS_PROPERTY, stampViewModel.findAllPaths(viewProperties), true);
        log("--------------");
        log("Creation stampViewModel \n" + stampViewModel);
        log("--------------");

        ViewModel conceptViewModel = new ConceptViewModel()
                .setPropertyValue(MODE, CREATE)
                .setPropertyValue(AXIOM, SUFFICIENT_SET)
                .setPropertyValue(CONCEPT_STAMP_VIEW_MODEL, stampViewModel);


        log("--------------");
        log("Creation conceptViewModel \n" + conceptViewModel);
        log("--------------");

        // stop database
    }
    public static void log(String message) {
        System.out.println(message);
    }
}
