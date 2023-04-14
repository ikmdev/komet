/*-
 * #%L
 * Proof Utility Library
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2017 Live Ontologies Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.liveontologies.puli.pinpointing.input.justifications;

import org.liveontologies.puli.pinpointing.BaseAxiomPinpointingTestInput;
import org.liveontologies.puli.pinpointing.input.Cycles_2;

public class Cycles_2Justifications extends BaseAxiomPinpointingTestInput {

	@Override
	protected void build() {
		input(new Cycles_2());

		justification(1, 17, 10, 4);
		justification(16, 2);
		justification(14, 18, 3);
		justification(2, 18, 12, 6);
		justification(16, 1, 10, 11, 4, 13);
		justification(1, 6, 18);
		justification(16, 1, 9, 11, 4);
		justification(17, 2, 10, 12, 4);
	}

}
