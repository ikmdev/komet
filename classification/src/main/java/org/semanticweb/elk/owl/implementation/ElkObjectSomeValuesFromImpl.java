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
import org.semanticweb.elk.owl.interfaces.ElkObjectPropertyExpression;
import org.semanticweb.elk.owl.interfaces.ElkObjectSomeValuesFrom;
import org.semanticweb.elk.owl.visitors.ElkObjectSomeValuesFromVisitor;
import org.semanticweb.elk.owl.visitors.ElkPropertyRestrictionQualifiedVisitor;

/**
 * Implementation of {@link ElkObjectSomeValuesFrom}
 * 
 * @author Yevgeny Kazakov
 * @author Markus Kroetzsch
 */
public class ElkObjectSomeValuesFromImpl
		extends
		ElkPropertyRestrictionQualifiedImpl<ElkObjectPropertyExpression, ElkClassExpression>
		implements ElkObjectSomeValuesFrom {

	ElkObjectSomeValuesFromImpl(
			ElkObjectPropertyExpression property,
			ElkClassExpression filler) {
		super(property, filler);
	}

	@Override
	public <O> O accept(ElkPropertyRestrictionQualifiedVisitor<O> visitor) {
		return accept((ElkObjectSomeValuesFromVisitor<O>) visitor);
	}

	@Override
	public <O> O accept(ElkObjectSomeValuesFromVisitor<O> visitor) {
		return visitor.visit(this);
	}
}
