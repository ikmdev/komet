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
package dev.ikm.komet.framework.settings;

/**
 * The language of Komet's own user interface (menus, dialogs, messages), as chosen in Settings.
 * This is distinct from the language coordinate that selects concept descriptions, which is a
 * per-view setting. English is the only translation that exists today; new entries are added here
 * as translations arrive.
 */
public enum DisplayLanguage {
    SYSTEM_DEFAULT("System default (English)"),
    ENGLISH("English");

    private final String displayName;

    DisplayLanguage(String displayName) {
        this.displayName = displayName;
    }

    /** The label shown in the Settings dialog. */
    public String displayName() {
        return displayName;
    }
}
