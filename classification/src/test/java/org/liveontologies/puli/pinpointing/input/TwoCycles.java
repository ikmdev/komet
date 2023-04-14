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

public class TwoCycles extends BaseEnumeratorTestInput {

	@Override
	protected void build() {
		conclusion("A").premise("B").axiom(1).add();
		conclusion("B").premise("C").axiom(2).add();
		conclusion("A").premise("D").axiom(3).add();
		conclusion("D").premise("C").axiom(4).add();
		conclusion("C").premise("E").axiom(5).add();
		conclusion("E").premise("B").axiom(6).add();
		conclusion("B").premise("D").axiom(7).add();
		conclusion("C").axiom(8).add();
		query("A");
	}

}
