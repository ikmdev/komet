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
package dev.ikm.tinkar.integration;

import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Type-safe JUnit 5 extension for creating a NEW SpinedArray store.
 * <p>
 * Prefer this class when you want discoverable, IDE-friendly configuration via
 * {@code @ExtendWith(NewSpinedArrayKeyValueProvider.class)}. You can still
 * refine behavior with {@link WithKeyValueProvider} on the test class
 * (e.g., to set {@code dataPath}, {@code cleanOnStart}, or {@code importPath}).
 * <p>
 * Defaults:
 * - Forces controller {@code TestConstants.NEW_SPINED_ARRAY_STORE}
 * - Does not set a specific dataPath; falls back to {@code WithKeyValueProvider} or
 *   {@link KeyValueProviderExtension} default {@code target/key-value-store} unless overridden.
 */
public class NewSpinedArrayKeyValueProvider extends KeyValueProviderExtension {

    @Override
    protected Config resolveConfig(ExtensionContext context) {
        Config cfg = super.resolveConfig(context);
        cfg.controllerName = TestConstants.NEW_SPINED_ARRAY_STORE;
        return cfg;
    }
}