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
package org.semanticweb.elk.reasoner.taxonomy.model;

import org.semanticweb.elk.owl.interfaces.ElkEntity;

/**
 * Instantiates {@link Node}s based on the supplied members and an additional
 * parameter.
 * 
 * @author Peter Skocovsky
 *
 * @param <T>
 *            The type of members of the created nodes.
 * @param <N>
 *            The type of the created nodes.
 * @param <P>
 *            The type of the additional parameter.
 */
public interface TaxonomyNodeFactory<
				T extends ElkEntity,
				N extends Node<T>,
				P
		> {

	/**
	 * Instantiates {@link Node} based on the supplied members and an additional
	 * parameter.
	 * 
	 * @param members
	 *            The members the node should contain.
	 * @param size
	 *            The number of the members the node should contain.
	 * @param param
	 *            The additional parameter for the node creation.
	 * @return The new {@link Node}
	 */
	N createNode(Iterable<? extends T> members, int size, P param);
	
}
