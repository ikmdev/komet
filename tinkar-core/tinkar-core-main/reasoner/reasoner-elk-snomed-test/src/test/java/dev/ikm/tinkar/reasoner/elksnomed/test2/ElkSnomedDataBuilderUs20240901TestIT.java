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
package dev.ikm.tinkar.reasoner.elksnomed.test2;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.test.SnomedVersionUs;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.reasoner.elksnomed.test.ElkSnomedDataBuilderTestBase;
import dev.ikm.tinkar.reasoner.elksnomed.test.PrimitiveDataTestUtil;

public class ElkSnomedDataBuilderUs20240901TestIT extends ElkSnomedDataBuilderTestBase implements SnomedVersionUs {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(ElkSnomedDataBuilderUs20240901TestIT.class);

	static {
		test_case = "snomed-us-20240901";
	}

	{
		inactive_count = 26923;
	}

	@Override
	public String getVersion() {
		return "20240901";
	}

	@Override
	public String getInternationalVersion() {
		return "20240701";
	}

	@BeforeAll
	public static void startPrimitiveData() throws IOException {
		PrimitiveDataTestUtil.setupPrimitiveData(test_case + "-sa");
		PrimitiveData.start();
	}

	@AfterAll
	public static void stopPrimitiveData() {
		PrimitiveDataTestUtil.stopPrimitiveData();
	}

}
