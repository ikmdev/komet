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
package dev.ikm.komet.terms;

import dev.ikm.tinkar.terms.EntityProxy;

import java.util.UUID;

/**
 * Komet's window, pane, configuration and user settings. Each names a JavaFX property (by its
 * {@code toXmlFragment()}); none is a component of any set, and nothing here reaches a store, so
 * these are hand-written, apart from the generated {@link KometTerm}. They keep the UUIDs they
 * have always had; {@code LEFT_PANE_DEFAULTS} was {@code LEFT_PANE_DAFAULTS}.
 */
public final class KometSettingTerm {

    private KometSettingTerm() {
    }

    /**
     * Window configuration name (SOLOR)
     */
    public static final EntityProxy.Concept WINDOW_CONFIGURATION_NAME =
            EntityProxy.Concept.make("Window configuration name (SOLOR)",
                    UUID.fromString("a93cf6c2-8cc1-5cb7-9af9-7d27d6dbc29e"));

    /**
     * Window height (SOLOR)
     */
    public static final EntityProxy.Concept WINDOW_HEIGHT =
            EntityProxy.Concept.make("Window height (SOLOR)",
                    UUID.fromString("42a7d496-c6fd-542d-8980-425d738090a7"));

    /**
     * Window width (SOLOR)
     */
    public static final EntityProxy.Concept WINDOW_WIDTH =
            EntityProxy.Concept.make("Window width (SOLOR)",
                    UUID.fromString("f65a21ff-66a1-5b0d-a871-6f66b7601ce2"));

    /**
     * Window x position (SOLOR)
     */
    public static final EntityProxy.Concept WINDOW_X_POSITION =
            EntityProxy.Concept.make("Window x position (SOLOR)",
                    UUID.fromString("2ac1bb5c-f68c-5d32-a436-51fc4e75d308"));

    /**
     * Window y position (SOLOR)
     */
    public static final EntityProxy.Concept WINDOW_Y_POSITION =
            EntityProxy.Concept.make("Window y position (SOLOR)",
                    UUID.fromString("6ce4ecd7-d9fb-5a66-8bab-46ae8bb4b282"));

    /**
     * Enable left pane (SOLOR)
     */
    public static final EntityProxy.Concept ENABLE_LEFT_PANE =
            EntityProxy.Concept.make("Enable left pane (SOLOR)",
                    UUID.fromString("83a2580f-c7fe-5727-8046-cda4b4756617"));

    /**
     * Enable center pane (SOLOR)
     */
    public static final EntityProxy.Concept ENABLE_CENTER_PANE =
            EntityProxy.Concept.make("Enable center pane (SOLOR)",
                    UUID.fromString("cf719bfb-f1ed-52df-aac2-fb7dfd5441a9"));

    /**
     * Enable right pane (SOLOR)
     */
    public static final EntityProxy.Concept ENABLE_RIGHT_PANE =
            EntityProxy.Concept.make("Enable right pane (SOLOR)",
                    UUID.fromString("6c613e68-534f-5380-ac14-62ccad2fb2cb"));

    /**
     * Left pane defaults (SOLOR)
     */
    public static final EntityProxy.Concept LEFT_PANE_DEFAULTS =
            EntityProxy.Concept.make("Left pane defaults (SOLOR)",
                    UUID.fromString("559f9d01-9435-53da-ac85-6021a80e6353"));

    /**
     * Center pane defaults (SOLOR)
     */
    public static final EntityProxy.Concept CENTER_PANE_DEFAULTS =
            EntityProxy.Concept.make("Center pane defaults (SOLOR)",
                    UUID.fromString("7fb52e9d-f77c-5ef9-8191-677365e02b4b"));

    /**
     * Right pane defaults (SOLOR)
     */
    public static final EntityProxy.Concept RIGHT_PANE_DEFAULTS =
            EntityProxy.Concept.make("Right pane defaults (SOLOR)",
                    UUID.fromString("58b4b6d2-1a91-51d0-8e3f-7476d60e6888"));

    /**
     * Configuration name (SOLOR)
     */
    public static final EntityProxy.Concept CONFIGURATION_NAME =
            EntityProxy.Concept.make("Configuration name (SOLOR)",
                    UUID.fromString("2debbffc-e145-565b-acb4-94011e06dfb9"));

    /**
     * Enable editing (SOLOR)
     */
    public static final EntityProxy.Concept ENABLE_EDITING =
            EntityProxy.Concept.make("Enable editing (SOLOR)",
                    UUID.fromString("6b4067f1-26fa-51e0-8af5-316924e715ce"));

    /**
     * KOMET user list (SOLOR)
     */
    public static final EntityProxy.Concept KOMET_USER_LIST =
            EntityProxy.Concept.make("KOMET user list (SOLOR)",
                    UUID.fromString("5e77558d-97d0-52b6-adf0-d54beb97b3a6"));

    /**
     * Module for user (SOLOR)
     */
    public static final EntityProxy.Concept MODULE_FOR_USER =
            EntityProxy.Concept.make("Module for user (SOLOR)",
                    UUID.fromString("c8fd4f1b-d842-5245-9a7d-a58dc0ac1c11"));

    /**
     * Path for user (SOLOR)
     */
    public static final EntityProxy.Concept PATH_FOR_USER =
            EntityProxy.Concept.make("Path for user (SOLOR)",
                    UUID.fromString("12131382-1535-5a77-928b-6eacad221ea2"));
}
