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

import org.semanticweb.elk.owl.interfaces.ElkEntity;
import org.semanticweb.elk.owl.interfaces.ElkObjectProperty;
import org.semanticweb.elk.owl.iris.ElkIri;
import org.semanticweb.elk.owl.predefined.ElkEntityType;
import org.semanticweb.elk.owl.visitors.ElkEntityVisitor;
import org.semanticweb.elk.owl.visitors.ElkObjectPropertyExpressionVisitor;
import org.semanticweb.elk.owl.visitors.ElkObjectPropertyVisitor;
import org.semanticweb.elk.owl.visitors.ElkObjectVisitor;
import org.semanticweb.elk.owl.visitors.ElkSubObjectPropertyExpressionVisitor;

/**
 * Corresponds to an <a href=
 * "http://www.w3.org/TR/owl2-syntax/#Object_Properties">Object Property<a> in
 * the OWL 2 specification.
 * 
 * @author Yevgeny Kazakov
 * @author Markus Kroetzsch
 */
public class ElkObjectPropertyImpl extends ElkIriObject implements ElkEntity,
		ElkObjectProperty {

	ElkObjectPropertyImpl(ElkIri iri) {
		super(iri);
	}

	@Override
	public ElkEntityType getEntityType() {
		return ElkEntityType.OBJECT_PROPERTY;
	}

	@Override
	public <O> O accept(ElkObjectVisitor<O> visitor) {
		return accept((ElkObjectPropertyVisitor<O>) visitor);
	}

	@Override
	public <O> O accept(ElkObjectPropertyExpressionVisitor<O> visitor) {
		return accept((ElkObjectPropertyVisitor<O>) visitor);
	}

	@Override
	public <O> O accept(ElkSubObjectPropertyExpressionVisitor<O> visitor) {
		return accept((ElkObjectPropertyVisitor<O>) visitor);
	}

	@Override
	public <O> O accept(ElkObjectPropertyVisitor<O> visitor) {
		return visitor.visit(this);
	}

	@Override
	public <O> O accept(ElkEntityVisitor<O> visitor) {
		return accept((ElkObjectPropertyVisitor<O>) visitor);
	}

}
