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

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.tinkar.common.service.PrimitiveData;

public class SolorAxiomDataBuilderTest extends AxiomDataBuilderTest {

	private static final Logger LOG = LoggerFactory.getLogger(SolorAxiomDataBuilderTest.class);

	static {
		stated_count = 548180; // 422857;
		active_count = 488602; // 362667;
		inactive_count = 59578; // 60190;
		test_case = "solor";
	}

	@BeforeAll
	public static void startPrimitiveData() throws IOException {
//		setupPrimitiveData("solor-us-test-sa");
//		setupPrimitiveData("solor-ids-512-sa");
		setupPrimitiveData("solor-08-27-256-sa");
		PrimitiveData.start();
	}

}
