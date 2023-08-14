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
 * @author Markus Kroetzsch, Aug 8, 2011
 */
package org.semanticweb.elk.owl.implementation;

import org.semanticweb.elk.owl.interfaces.ElkDataPropertyExpression;
import org.semanticweb.elk.owl.interfaces.ElkFunctionalDataPropertyAxiom;
import org.semanticweb.elk.owl.visitors.ElkDataPropertyAxiomVisitor;
import org.semanticweb.elk.owl.visitors.ElkFunctionalDataPropertyAxiomVisitor;
import org.semanticweb.elk.owl.visitors.ElkPropertyAxiomVisitor;

/**
 * Implements {@link ElkFunctionalDataPropertyAxiom}
 * 
 * @author Markus Kroetzsch
 * @author "Yevgeny Kazakov"
 * 
 */
public class ElkFunctionalDataPropertyAxiomImpl extends
		ElkPropertyAxiomImpl<ElkDataPropertyExpression> implements
		ElkFunctionalDataPropertyAxiom {

	ElkFunctionalDataPropertyAxiomImpl(ElkDataPropertyExpression property) {
		super(property);
	}

	@Override
	public <O> O accept(ElkDataPropertyAxiomVisitor<O> visitor) {
		return accept((ElkFunctionalDataPropertyAxiomVisitor<O>) visitor);
	}

	@Override
	public <O> O accept(ElkPropertyAxiomVisitor<O> visitor) {
		return accept((ElkFunctionalDataPropertyAxiomVisitor<O>) visitor);
	}

	@Override
	public <O> O accept(ElkFunctionalDataPropertyAxiomVisitor<O> visitor) {
		return visitor.visit(this);
	}

}
