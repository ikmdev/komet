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

import dev.ikm.tinkar.provider.ephemeral.constants.EphemeralStoreControllerName;
import dev.ikm.tinkar.provider.spinedarray.constants.SpinedArrayControllerNames;

import java.io.File;
import java.util.function.Function;

public class TestConstants {
    public static final String LOAD_EPHEMERAL_STORE = EphemeralStoreControllerName.NEW_CONTROLLER_NAME;
    public static final String OPEN_SPINED_ARRAY_STORE = SpinedArrayControllerNames.OPEN_CONTROLLER_NAME;
    public static final String NEW_SPINED_ARRAY_STORE = SpinedArrayControllerNames.NEW_CONTROLLER_NAME;

    public static final Function<String,File> createFilePathInTarget = (pathName) -> new File("%s/target/%s".formatted(System.getProperty("user.dir"), pathName));

    public static final Function<Class,File>
            createFilePathInTargetFromClassName = (clazz) -> createFilePathInTarget.apply("generated-datastores/%s".formatted(clazz.getSimpleName()));

    // IKM Test Data Files
    public static final File PB_STARTER_DATA = createFilePathInTarget.apply("data/tinkar-starter-data-unreasoned-pb.zip");
    public static final File PB_STARTER_DATA_REASONED = createFilePathInTarget.apply("data/tinkar-starter-data-reasoned-pb.zip");
    public static final File PB_EXAMPLE_DATA_REASONED = createFilePathInTarget.apply("data/tinkar-example-data-reasoned-pb.zip");

    // Other Helper Files
    public static final File PB_TEST_FILE = createFilePathInTarget.apply("data/tinkar-export-test.pb.zip");
    public static final File PB_ROUNDTRIP_TEST_FILE = createFilePathInTarget.apply("data/tinkar-export-test-roundtrip.pb.zip");
    public static final File PB_PERFORMANCE_TEST_FILE = createFilePathInTarget.apply("data/tinkar-export-test-performance.pb.zip");
}
