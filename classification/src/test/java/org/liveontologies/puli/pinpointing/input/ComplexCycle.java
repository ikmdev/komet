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
package org.liveontologies.puli.pinpointing.input;

public class ComplexCycle extends BaseEnumeratorTestInput {

	@Override
	protected void build() {
		conclusion("A").premise("B").axiom(1).add();
		conclusion("A").premise("C").axiom(2).add();
		conclusion("B").premise("C").axiom(3).add();
		conclusion("C").premise("D").axiom(4).add();
		conclusion("D").premise("B").axiom(5).add();
		conclusion("D").axiom(6).axiom(7).add();
		conclusion("A").premise("E").axiom(8).add();
		conclusion("E").axiom(1).axiom(9).add();
		conclusion("B").axiom(8).axiom(9).add();
		query("A");
	}

}
