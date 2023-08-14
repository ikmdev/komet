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
package org.liveontologies.puli;

import java.util.Collection;
import java.util.Collections;

class EmptyProof<I extends Inference<?>> implements DynamicProof<I> {

	@Override
	public Collection<? extends I> getInferences(Object conclusion) {
		return Collections.emptySet();
	}

	@Override
	public void addListener(ChangeListener listener) {
		// the set never changes
	}

	@Override
	public void removeListener(ChangeListener listener) {
		// the set never changes
	}

	@Override
	public void dispose() {
		// no-op
	}

}
