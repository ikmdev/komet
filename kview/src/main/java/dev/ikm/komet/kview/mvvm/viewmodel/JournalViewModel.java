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
package dev.ikm.komet.kview.mvvm.viewmodel;

import dev.ikm.komet.framework.window.WindowSettings;

/**
 * Manages journal entry state and actions, building on common form behavior provided by {@link FormViewModel}.
 */
public class JournalViewModel extends FormViewModel {

    public static String WINDOW_SETTINGS = "windowSettings";

    /**
     * Initializes a new JournalViewModel with default form configuration.
     */
    public JournalViewModel() {
        super();

        addProperty(WINDOW_SETTINGS, (WindowSettings) null);
    }

    // Add journal-specific properties and commands as needed
}
