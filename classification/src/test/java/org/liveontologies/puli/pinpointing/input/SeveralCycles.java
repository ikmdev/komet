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
package org.liveontologies.puli.pinpointing.input;

public class SeveralCycles extends BaseEnumeratorTestInput {

	@Override
	protected void build() {
		conclusion("A").premise("B").axiom(1).add();
		conclusion("A").premise("C").axiom(1).add();
		conclusion("B").premise("C").axiom(3).add();
		conclusion("C").premise("D").axiom(4).add();
		conclusion("D").premise("E").axiom(5).add();
		conclusion("E").premise("F").axiom(6).add();
		conclusion("F").premise("C").axiom(7).add();
		conclusion("F").premise("K").premise("L").premise("G").axiom(8).add();
		conclusion("G").premise("E").axiom(9).add();
		conclusion("B").premise("I").axiom(12).add();
		conclusion("I").premise("J").premise("K").axiom(13).add();
		conclusion("J").premise("B").axiom(13).add();
		conclusion("D").premise("C").axiom(8).axiom(9).add();
		conclusion("G").axiom(10).axiom(11).add();
		conclusion("E").axiom(1).axiom(9).add();
		conclusion("C").axiom(8).axiom(9).add();
		conclusion("K").add();
		conclusion("L").add();
		query("A");
	}

}
