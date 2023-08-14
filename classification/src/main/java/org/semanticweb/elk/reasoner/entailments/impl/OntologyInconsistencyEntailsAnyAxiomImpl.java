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
package org.semanticweb.elk.reasoner.entailments.impl;

import java.util.Collections;
import java.util.List;

import org.semanticweb.elk.owl.interfaces.ElkAxiom;
import org.semanticweb.elk.reasoner.entailments.model.AxiomEntailment;
import org.semanticweb.elk.reasoner.entailments.model.EntailmentInference;
import org.semanticweb.elk.reasoner.entailments.model.OntologyInconsistency;
import org.semanticweb.elk.reasoner.entailments.model.OntologyInconsistencyEntailsAnyAxiom;

public class OntologyInconsistencyEntailsAnyAxiomImpl extends
		AbstractAxiomEntailmentInference<ElkAxiom, AxiomEntailment<? extends ElkAxiom>>
		implements OntologyInconsistencyEntailsAnyAxiom {

	public OntologyInconsistencyEntailsAnyAxiomImpl(
			final AxiomEntailment<? extends ElkAxiom> conclusion) {
		super(conclusion);
	}

	@Override
	public <O> O accept(final EntailmentInference.Visitor<O> visitor) {
		return visitor.visit(this);
	}

	@Override
	public List<? extends OntologyInconsistency> getPremises() {
		return Collections.singletonList(OntologyInconsistencyImpl.INSTANCE);
	}

}
