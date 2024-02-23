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
package dev.ikm.komet.reasoner.elksnomed;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolorElkSnomedClassifierTest extends SolorElkSnomedDataBuilderTest {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(SolorElkSnomedClassifierTest.class);

	@Test
	public void supercs() throws Exception {
		runSnomedReasoner();
		Path target_path = getWritePath("supercs");
		assumeTrue(Files.exists(target_path));
		compare("supercs");
	}

	@Test
	public void supercsService() throws Exception {
		runSnomedReasonerService();
		Path target_path = getWritePath("supercs");
		assumeTrue(Files.exists(target_path));
		compare("supercs");
	}

}
