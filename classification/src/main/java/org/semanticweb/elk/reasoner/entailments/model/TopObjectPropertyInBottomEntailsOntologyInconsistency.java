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
package org.semanticweb.elk.reasoner.entailments.model;

import org.semanticweb.elk.reasoner.saturation.conclusions.model.SubPropertyChain;

/**
 * {@link OntologyInconsistency} is entailed because inclusion of
 * {@code owl:topObjectProperty} in {@code owl:bottomObjectProperty} was
 * derived.
 * <p>
 * {@link #getReason()} returns a {@link SubPropertyChain} with
 * {@link SubPropertyChain#getSubChain()} = {@code owl:topObjectProperty} and
 * {@link SubPropertyChain#getSuperChain()} = {@code owl:bottomObjectProperty}.
 * 
 * @author Peter Skocovsky
 */
public interface TopObjectPropertyInBottomEntailsOntologyInconsistency extends
		OntologyInconsistencyEntailmentInference, HasReason<SubPropertyChain> {

	public static interface Visitor<O> {
		O visit(TopObjectPropertyInBottomEntailsOntologyInconsistency topObjectPropertyInBottomEntailsOntologyInconsistency);
	}

}
