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
package org.semanticweb.elk.reasoner.query;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import org.eclipse.collections.api.factory.Maps;
import org.junit.runner.RunWith;
import org.semanticweb.elk.owl.interfaces.ElkClassExpression;
import org.semanticweb.elk.reasoner.config.ReasonerConfiguration;
import org.semanticweb.elk.reasoner.incremental.ElkIncrementalReasoningTestDelegate;
import org.semanticweb.elk.testing.PolySuite;
import org.semanticweb.elk.testing.PolySuite.Config;
import org.semanticweb.elk.testing.PolySuite.Configuration;
import org.semanticweb.elk.testing.TestManifest;


@RunWith(PolySuite.class)
public class ElkIncrementalClassExpressionSubClassesQueryTest extends
		ElkIncrementalClassExpressionQueryTest<ElkDirectSubClassesTestOutput> {

	public ElkIncrementalClassExpressionSubClassesQueryTest(
			final TestManifest<QueryTestInput<ElkClassExpression>> manifest) {
		super(manifest,
				new ElkIncrementalReasoningTestDelegate<ElkDirectSubClassesTestOutput>(
						manifest) {

					@Override
					public ElkDirectSubClassesTestOutput getExpectedOutput()
							throws Exception {
						return new ElkDirectSubClassesTestOutput(
								getStandardReasoner(),
								manifest.getInput().getQuery());
					}

					@Override
					public ElkDirectSubClassesTestOutput getActualOutput()
							throws Exception {
						return new ElkDirectSubClassesTestOutput(
								getIncrementalReasoner(),
								manifest.getInput().getQuery());
					}

					@Override
					protected Map<String, String> additionalConfigIncremental() {
						return Maps.immutable.of(
								ReasonerConfiguration.CLASS_EXPRESSION_QUERY_EVICTOR,
								"NQEvictor(0, 0.75)").castToMap();
					}

					@Override
					protected Map<String, String> additionalConfigWithInterrupts() {
						return Maps.immutable.of(
								ReasonerConfiguration.CLASS_EXPRESSION_QUERY_EVICTOR,
								"NQEvictor(0, 0.75)").castToMap();
					}

				});
	}

	@Config
	public static Configuration getConfig()
			throws IOException, URISyntaxException {
		return getConfig("getDirectSubClasses");
	}

}
