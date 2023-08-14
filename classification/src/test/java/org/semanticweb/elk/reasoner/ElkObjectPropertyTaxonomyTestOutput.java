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
package org.semanticweb.elk.reasoner;

import org.semanticweb.elk.exceptions.ElkException;

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

import org.semanticweb.elk.owl.interfaces.ElkAxiom;
import org.semanticweb.elk.owl.interfaces.ElkObjectProperty;
import org.semanticweb.elk.reasoner.completeness.IncompleteResult;
import org.semanticweb.elk.reasoner.taxonomy.model.Taxonomy;

public class ElkObjectPropertyTaxonomyTestOutput extends
		AbstractTaxonomyTestOutput<ElkObjectProperty, ElkObjectPropertyTaxonomyTestOutput> {

	public ElkObjectPropertyTaxonomyTestOutput(
			IncompleteResult<? extends Taxonomy<ElkObjectProperty>> incompleteTaxonomy) {
		super(incompleteTaxonomy);
	}

	public ElkObjectPropertyTaxonomyTestOutput(
			Taxonomy<ElkObjectProperty> taxonomy) {
		super(taxonomy);
	}

	public ElkObjectPropertyTaxonomyTestOutput(Reasoner reasoner)
			throws ElkException {
		this(reasoner.getObjectPropertyTaxonomyQuietly());
	}

	@Override
	TaxonomyEntailment.Listener<ElkObjectProperty> adapt(
			Listener<ElkAxiom> listener) {
		return new ElkObjectPropertyTaxonomyEntailmentAdapter(listener);
	}

}
