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
 * @author Yevgeny Kazakov, Apr 8, 2011
 */
package org.semanticweb.elk.owl.visitors;

import org.semanticweb.elk.owl.interfaces.ElkObjectPropertyAxiom;

/**
 * Visitor pattern interface for instances of {@link ElkObjectPropertyAxiom}.
 * 
 * @author Yevgeny Kazakov
 * @author Markus Kroetzsch
 * 
 * @param <O>
 *            the type of the output of this visitor
 */
public interface ElkObjectPropertyAxiomVisitor<O> extends
		ElkAsymmetricObjectPropertyAxiomVisitor<O>,
		ElkDisjointObjectPropertiesAxiomVisitor<O>,
		ElkEquivalentObjectPropertiesAxiomVisitor<O>,
		ElkFunctionalObjectPropertyAxiomVisitor<O>,
		ElkInverseFunctionalObjectPropertyAxiomVisitor<O>,
		ElkInverseObjectPropertiesAxiomVisitor<O>,
		ElkIrreflexiveObjectPropertyAxiomVisitor<O>,
		ElkObjectPropertyDomainAxiomVisitor<O>,
		ElkObjectPropertyRangeAxiomVisitor<O>,
		ElkReflexiveObjectPropertyAxiomVisitor<O>,
		ElkSubObjectPropertyOfAxiomVisitor<O>,
		ElkSymmetricObjectPropertyAxiomVisitor<O>,
		ElkTransitiveObjectPropertyAxiomVisitor<O> {

	// combined visitor
}
