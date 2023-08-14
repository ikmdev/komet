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

import java.util.Map;

import org.semanticweb.elk.owl.interfaces.ElkAxiom;

/**
 * The test output for entailment checking of {@link ElkAxiom}s
 * 
 * @author Yevgeny Kazakov
 */
public class ElkEntailmentQueryTestOutput extends
		IncompleteEntailmentTestOutput<ElkAxiom, ElkEntailmentQueryTestOutput> {

	public ElkEntailmentQueryTestOutput(
			Map<ElkAxiom, ? extends QueryResult> reasoningResult)
			throws ElkQueryException {
		for (QueryResult queryResult : reasoningResult.values()) {
			try {
				ElkAxiom query = queryResult.getQuery();
				if (queryResult.entailmentProved()) {
					addPositiveEntailment(query);
				} else if (!queryResult.getIncompletenessMonitor()
						.isIncompletenessDetected()) {
					addNegativeEntailment(query);
				}
			} finally {
				queryResult.unlock();
			}
		}
	}

}
