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
package org.semanticweb.elk.owl.managers;

import java.lang.ref.ReferenceQueue;

import org.semanticweb.elk.owl.interfaces.ElkObjectProperty;
import org.semanticweb.elk.util.hashing.HashGenerator;

class WeakElkObjectPropertyWrapper extends WeakWrapper<ElkObjectProperty> {

	WeakElkObjectPropertyWrapper(ElkObjectProperty referent,
			ReferenceQueue<? super ElkObjectProperty> q) {
		super(referent, q);
	}

	@Override
	protected int hashCode(ElkObjectProperty referent) {
		return HashGenerator.combinedHashCode("ElkObjectProperty", referent.getIri());
	}

	@Override
	protected boolean equal(ElkObjectProperty referent, Object obj) {
		if (obj instanceof ElkObjectProperty)
			return referent.getIri().equals(((ElkObjectProperty) obj).getIri());
		return false;
	}

}
