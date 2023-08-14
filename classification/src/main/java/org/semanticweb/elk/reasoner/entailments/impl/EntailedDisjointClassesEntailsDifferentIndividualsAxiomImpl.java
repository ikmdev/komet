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

import org.semanticweb.elk.owl.interfaces.ElkDifferentIndividualsAxiom;
import org.semanticweb.elk.reasoner.entailments.model.DifferentIndividualsAxiomEntailment;
import org.semanticweb.elk.reasoner.entailments.model.DisjointClassesAxiomEntailment;
import org.semanticweb.elk.reasoner.entailments.model.EntailedDisjointClassesEntailsDifferentIndividualsAxiom;
import org.semanticweb.elk.reasoner.entailments.model.EntailmentInference;

public class EntailedDisjointClassesEntailsDifferentIndividualsAxiomImpl extends
		AbstractAxiomEntailmentInference<ElkDifferentIndividualsAxiom, DifferentIndividualsAxiomEntailment>
		implements EntailedDisjointClassesEntailsDifferentIndividualsAxiom {

	private final DisjointClassesAxiomEntailment premise_;

	public EntailedDisjointClassesEntailsDifferentIndividualsAxiomImpl(
			final DifferentIndividualsAxiomEntailment conclusion,
			final DisjointClassesAxiomEntailment premise) {
		super(conclusion);
		this.premise_ = premise;
	}

	@Override
	public List<? extends DisjointClassesAxiomEntailment> getPremises() {
		return Collections.singletonList(premise_);
	}

	@Override
	public <O> O accept(final EntailmentInference.Visitor<O> visitor) {
		return visitor.visit(this);
	}

}
