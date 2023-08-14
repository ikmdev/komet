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
package org.semanticweb.elk.owl.implementation;

import org.semanticweb.elk.owl.interfaces.ElkClassExpression;
import org.semanticweb.elk.owl.interfaces.ElkObjectMaxCardinalityQualified;
import org.semanticweb.elk.owl.interfaces.ElkObjectPropertyExpression;
import org.semanticweb.elk.owl.visitors.ElkCardinalityRestrictionQualifiedVisitor;
import org.semanticweb.elk.owl.visitors.ElkObjectMaxCardinalityQualifiedVisitor;
import org.semanticweb.elk.owl.visitors.ElkObjectMaxCardinalityVisitor;

/**
 * Implementation of {@link ElkObjectMaxCardinalityQualified}.
 * 
 * @author Markus Kroetzsch
 * @author "Yevgeny Kazakov"
 * 
 */
public class ElkObjectMaxCardinalityQualifiedImpl
		extends
		ElkCardinalityRestrictionQualifiedImpl<ElkObjectPropertyExpression, ElkClassExpression>
		implements ElkObjectMaxCardinalityQualified {

	ElkObjectMaxCardinalityQualifiedImpl(
			ElkObjectPropertyExpression property,
			int cardinality, ElkClassExpression filler) {
		super(property, cardinality, filler);
	}

	@Override
	public <O> O accept(ElkCardinalityRestrictionQualifiedVisitor<O> visitor) {
		return accept((ElkObjectMaxCardinalityQualifiedVisitor<O>) visitor);
	}

	@Override
	public <O> O accept(ElkObjectMaxCardinalityVisitor<O> visitor) {
		return accept((ElkObjectMaxCardinalityQualifiedVisitor<O>) visitor);
	}

	@Override
	public <O> O accept(ElkObjectMaxCardinalityQualifiedVisitor<O> visitor) {
		return visitor.visit(this);
	}

}
