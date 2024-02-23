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
package dev.ikm.komet.reasoner.elkowl;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.tinkar.common.service.PrimitiveData;

public class SampleClassifierTest extends ClassifierTest {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(SampleClassifierTest.class);

	static {
		stated_count = 83547;
		active_count = 83547;
		inactive_count = 0;
		test_case = "sample";
	}

	@BeforeAll
	public static void startPrimitiveData() throws IOException {
		Path source = Paths.get("target", "db", "sample-data-3-sa");
		Path target = Paths.get("target", "db", "sample-data-classifier");
		// Temp until test data artifacts are in maven repo
		assumeTrue(Files.exists(source));
		PrimitiveDataTestBase.copyDirectory(source, target);
		setupPrimitiveData("sample-data-classifier");
		PrimitiveData.start();
	}

}
