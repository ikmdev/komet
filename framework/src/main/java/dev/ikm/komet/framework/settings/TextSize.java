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
 * The text-size step a user can choose in Settings. Every text in Komet scales by the same
 * factor, so the user picks a step rather than a point size and the type hierarchy is preserved.
 * The percentages are tunable without touching stored preferences, which record the step name.
 */
public enum TextSize {
    SMALL("Small", 85),
    DEFAULT("Default", 100),
    LARGE("Large", 115),
    EXTRA_LARGE("Extra large", 130);

    private final String displayName;
    private final int percent;

    TextSize(String displayName, int percent) {
        this.displayName = displayName;
        this.percent = percent;
    }

    /** The label shown in the Settings dialog. */
    public String displayName() {
        return displayName;
    }

    /** The scale of this step relative to {@link #DEFAULT}, in percent. */
    public int percent() {
        return percent;
    }

    /** The scale of this step relative to {@link #DEFAULT}, as a factor ({@code 1.0} for default). */
    public double scale() {
        return percent / 100.0;
    }
}
