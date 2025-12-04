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

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Parameterized JUnit 5 annotation to initialize a key-value provider for tests.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * @WithKeyValueProvider(
 *     controllerName = "OPEN_SPINED_ARRAY_STORE",
 *     dataPath = "target/key-value-store",
 *     cleanOnStart = true,
 *     importPath = "target/data/*-pb.zip"
 * )
 * class MyTest { ... }
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(KeyValueProviderExtension.class)
public @interface WithKeyValueProvider {
    /**
     * Name of the controller to use. If set to "default" the extension will
     * choose an appropriate controller based on other parameters and filesystem state.
     */
    String controllerName() default "default";

    /**
     * Optional filesystem location for non-ephemeral providers. If empty, a default of
     * "target/key-value-store" is used. For ephemeral providers this value is ignored.
     */
    String dataPath() default "";

    /**
     * When true, delete any existing data at {@link #dataPath()} before starting.
     */
    boolean cleanOnStart() default false;

    /**
     * Optional path or glob pattern(s) pointing to protobuf files to import after startup.
     * Multiple patterns can be separated by commas (","). Examples:
     * - "target/data/tinkar-starter-data-reasoned-pb.zip"
     * - "target/data/*-pb.zip"
     * - "target/data/*-pb.zip,target/extra/*.zip"
     */
    String importPath() default "";
}
