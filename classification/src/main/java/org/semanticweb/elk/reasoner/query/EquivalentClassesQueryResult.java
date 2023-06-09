package org.semanticweb.elk.reasoner.query;

/*-
 * #%L
 * ELK Reasoner Core
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2020 Department of Computer Science, University of Oxford
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

import org.semanticweb.elk.owl.interfaces.ElkClass;
import org.semanticweb.elk.owl.interfaces.ElkClassExpression;
import org.semanticweb.elk.reasoner.taxonomy.model.Node;

public class EquivalentClassesQueryResult {

	private final ElkClassExpression query_;

	private final Node<ElkClass> equivalentClasses_;

	private final boolean isComplete_;

	public EquivalentClassesQueryResult(ElkClassExpression query,
			Node<ElkClass> equivalentClasses, boolean isComplete) {
		this.query_ = query;
		this.equivalentClasses_ = equivalentClasses;
		this.isComplete_ = isComplete;
	}

	public ElkClassExpression getQuery() {
		return query_;
	}

	public Node<ElkClass> getEquivalentClasses() {
		return equivalentClasses_;
	}

	public boolean isComplete() {
		return isComplete_;
	}

}
