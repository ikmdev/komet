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

public class SemiCycle extends BaseEnumeratorTestInput {

	@Override
	protected void build() {
		conclusion("A").premise("B").premise("C").add();
		conclusion("B").axiom(1).add();
		conclusion("C").axiom(2).add();
		conclusion("C").premise("C").add();
		query("A");
	}

}
