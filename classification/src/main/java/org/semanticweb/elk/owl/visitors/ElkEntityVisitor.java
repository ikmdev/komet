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
/**
 * @author Yevgeny Kazakov, Jul 3, 2011
 */
package org.semanticweb.elk.owl.visitors;

import org.semanticweb.elk.owl.interfaces.ElkEntity;

/**
 * Visitor pattern interface for instances of {@link ElkEntity}.
 * 
 * @author "Yevgeny Kazakov"
 * 
 * @param <O>
 *            the type of the output of this visitor
 */
public interface ElkEntityVisitor<O> extends ElkAnnotationPropertyVisitor<O>,
		ElkClassVisitor<O>, ElkDataPropertyVisitor<O>, ElkDatatypeVisitor<O>,
		ElkNamedIndividualVisitor<O>, ElkObjectPropertyVisitor<O> {

	// combined visitor

}
