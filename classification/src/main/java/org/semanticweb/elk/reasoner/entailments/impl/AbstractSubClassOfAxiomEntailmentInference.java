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

import org.semanticweb.elk.owl.interfaces.ElkSubClassOfAxiom;
import org.semanticweb.elk.reasoner.entailments.model.Entailment;
import org.semanticweb.elk.reasoner.entailments.model.HasReason;
import org.semanticweb.elk.reasoner.entailments.model.SubClassOfAxiomEntailment;
import org.semanticweb.elk.reasoner.entailments.model.SubClassOfAxiomEntailmentInference;
import org.semanticweb.elk.reasoner.saturation.conclusions.model.ClassConclusion;

abstract class AbstractSubClassOfAxiomEntailmentInference<R extends ClassConclusion>
		extends
		AbstractAxiomEntailmentInference<ElkSubClassOfAxiom, SubClassOfAxiomEntailment>
		implements SubClassOfAxiomEntailmentInference, HasReason<R> {

	private final R reason_;

	public AbstractSubClassOfAxiomEntailmentInference(
			final SubClassOfAxiomEntailment conclusion, final R reason) {
		super(conclusion);
		this.reason_ = reason;
	}

	@Override
	public List<? extends Entailment> getPremises() {
		return Collections.emptyList();
	}

	@Override
	public R getReason() {
		return reason_;
	}

}
