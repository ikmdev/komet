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

import org.semanticweb.elk.owl.interfaces.ElkAnnotationProperty;
import org.semanticweb.elk.owl.interfaces.ElkSubAnnotationPropertyOfAxiom;
import org.semanticweb.elk.owl.visitors.ElkAnnotationAxiomVisitor;
import org.semanticweb.elk.owl.visitors.ElkAxiomVisitor;
import org.semanticweb.elk.owl.visitors.ElkObjectVisitor;
import org.semanticweb.elk.owl.visitors.ElkSubAnnotationPropertyOfAxiomVisitor;

/**
 * ELK implementation of {@link ElkSubAnnotationPropertyOfAxiom}.
 * 
 * @author Yevgeny Kazakov
 * @author Markus Kroetzsch
 * 
 */
public class ElkSubAnnotationPropertyOfAxiomImpl extends ElkObjectImpl
		implements ElkSubAnnotationPropertyOfAxiom {

	private final ElkAnnotationProperty subProperty_;
	private final ElkAnnotationProperty superProperty_;

	ElkSubAnnotationPropertyOfAxiomImpl(ElkAnnotationProperty subProperty,
			ElkAnnotationProperty superProperty) {
		this.subProperty_ = subProperty;
		this.superProperty_ = superProperty;
	}

	@Override
	public ElkAnnotationProperty getSubAnnotationProperty() {
		return subProperty_;
	}

	@Override
	public ElkAnnotationProperty getSuperAnnotationProperty() {
		return superProperty_;
	}

	@Override
	public <O> O accept(ElkObjectVisitor<O> visitor) {
		return accept((ElkSubAnnotationPropertyOfAxiomVisitor<O>) visitor);
	}

	@Override
	public <O> O accept(ElkAxiomVisitor<O> visitor) {
		return accept((ElkSubAnnotationPropertyOfAxiomVisitor<O>) visitor);
	}

	@Override
	public <O> O accept(ElkAnnotationAxiomVisitor<O> visitor) {
		return accept((ElkSubAnnotationPropertyOfAxiomVisitor<O>) visitor);
	}

	@Override
	public <O> O accept(ElkSubAnnotationPropertyOfAxiomVisitor<O> visitor) {
		return visitor.visit(this);
	}

}
