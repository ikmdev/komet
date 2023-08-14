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
package org.semanticweb.elk.owl.implementation;

import org.semanticweb.elk.owl.interfaces.ElkClassExpression;
import org.semanticweb.elk.owl.interfaces.ElkSubClassOfAxiom;
import org.semanticweb.elk.owl.visitors.ElkAxiomVisitor;
import org.semanticweb.elk.owl.visitors.ElkClassAxiomVisitor;
import org.semanticweb.elk.owl.visitors.ElkObjectVisitor;
import org.semanticweb.elk.owl.visitors.ElkSubClassOfAxiomVisitor;

/**
 * Corresponds to a <a href=
 * "http://www.w3.org/TR/owl2-syntax/#Subclass_Axioms">Subclass Axiom<a> in the
 * OWL 2 specification.
 * 
 * @author Yevgeny Kazakov
 * 
 */
public class ElkSubClassOfAxiomImpl extends ElkObjectImpl implements
		ElkSubClassOfAxiom {

	private final ElkClassExpression subClassExpression_,
			superClassExpression_;

	ElkSubClassOfAxiomImpl(ElkClassExpression subClassExpression,
			ElkClassExpression superClassExpression) {
		this.subClassExpression_ = subClassExpression;
		this.superClassExpression_ = superClassExpression;
	}

	@Override
	public ElkClassExpression getSubClassExpression() {
		return subClassExpression_;
	}

	@Override
	public ElkClassExpression getSuperClassExpression() {
		return superClassExpression_;
	}

	@Override
	public <O> O accept(ElkClassAxiomVisitor<O> visitor) {
		return accept((ElkSubClassOfAxiomVisitor<O>) visitor);
	}

	@Override
	public <O> O accept(ElkAxiomVisitor<O> visitor) {
		return accept((ElkSubClassOfAxiomVisitor<O>) visitor);
	}

	@Override
	public <O> O accept(ElkObjectVisitor<O> visitor) {
		return accept((ElkSubClassOfAxiomVisitor<O>) visitor);
	}

	@Override
	public <O> O accept(ElkSubClassOfAxiomVisitor<O> visitor) {
		return visitor.visit(this);
	}

}
