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

public class Cycles_2 extends BaseEnumeratorTestInput {

	@Override
	protected void build() {
		conclusion("A").premise("B").axiom(1).add();
		conclusion("A").premise("E").axiom(2).add();
		conclusion("A").premise("H").axiom(3).add();
		conclusion("B").premise("C").axiom(4).add();
		conclusion("B").premise("G").axiom(5).add();
		conclusion("B").premise("I").axiom(6).add();
		conclusion("C").premise("A").axiom(7).add();
		conclusion("C").premise("B").axiom(8).add();
		conclusion("C").premise("D").axiom(9).add();
		conclusion("C").premise("F").axiom(10).add();
		conclusion("D").premise("E").axiom(11).add();
		conclusion("E").premise("B").axiom(12).add();
		conclusion("F").premise("D").axiom(13).add();
		conclusion("H").premise("I").axiom(14).add();
		conclusion("I").premise("H").axiom(15).add();
		conclusion("E").axiom(16).add();
		conclusion("F").axiom(17).add();
		conclusion("I").axiom(18).add();
		query("A");
	}

}
