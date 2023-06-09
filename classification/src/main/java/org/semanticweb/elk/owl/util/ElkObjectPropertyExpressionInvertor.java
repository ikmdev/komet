package org.semanticweb.elk.owl.util;

/*
 * #%L
 * ELK OWL Object Interfaces
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2016 Department of Computer Science, University of Oxford
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.semanticweb.elk.owl.interfaces.ElkObjectInverseOf;
import org.semanticweb.elk.owl.interfaces.ElkObjectProperty;
import org.semanticweb.elk.owl.interfaces.ElkObjectPropertyExpression;
import org.semanticweb.elk.owl.visitors.ElkObjectPropertyExpressionVisitor;

/**
 * An {@link ElkObjectPropertyExpressionVisitor} that computes the inverse of
 * the visited {@link ElkObjectPropertyExpression} using the provided
 * {@link ElkObjectInverseOf.Factory}
 * 
 * @author Yevgeny Kazakov
 */
@SuppressWarnings("javadoc")
public class ElkObjectPropertyExpressionInvertor implements
		ElkObjectPropertyExpressionVisitor<ElkObjectPropertyExpression> {

	private final ElkObjectInverseOf.Factory factory_;

	ElkObjectPropertyExpressionInvertor(ElkObjectInverseOf.Factory factory) {
		this.factory_ = factory;
	}

	@Override
	public ElkObjectPropertyExpression visit(ElkObjectInverseOf expression) {
		return expression.getObjectProperty();
	}

	@Override
	public ElkObjectPropertyExpression visit(ElkObjectProperty expression) {
		return factory_.getObjectInverseOf(expression);
	}

	public static ElkObjectPropertyExpression invert(
			ElkObjectPropertyExpression expression,
			ElkObjectInverseOf.Factory factory) {
		return expression
				.accept(new ElkObjectPropertyExpressionInvertor(factory));
	}

}
