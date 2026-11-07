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
package dev.ikm.komet.layout;

/**
 * Selects Monocle's headless JavaFX platform for a test that needs the FX thread and controls but
 * asserts nothing the native toolkit renders. Call it before {@code Platform.startup}; each test
 * class runs in a JVM of its own. The same properties as the framework's
 * {@code JavaFXThreadExtension}.
 *
 * <p>Why: the native macOS toolkit started by a user with no login session (a CI agent run as a
 * LaunchDaemon) makes macOS 26 print an {@code NSXPCSharedListener ... 'ClientCallsAuxiliary'}
 * error on stderr for every test JVM, and Monocle puts nothing on the screen of a machine that
 * has one.</p>
 */
public final class HeadlessFx {

    private HeadlessFx() {
    }

    /** Selects Monocle headless when it is on the class path; otherwise leaves the default toolkit. */
    public static void select() {
        try {
            Class.forName("com.sun.glass.ui.monocle.MonoclePlatformFactory");
        } catch (ClassNotFoundException noMonocle) {
            return;
        }
        System.setProperty("glass.platform", "Monocle");
        System.setProperty("monocle.platform", "Headless");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        System.setProperty("java.awt.headless", "true");
    }
}
