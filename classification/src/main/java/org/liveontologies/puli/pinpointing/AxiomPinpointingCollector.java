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
package org.liveontologies.puli.pinpointing;

import dev.ikm.tinkar.common.validation.Validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import java.util.Objects;

/**
 * An {@link AxiomPinpointingListener} that collects the found justifications,
 * repairs and useful axioms that belong to repairs and justifications
 * 
 * @author Yevgeny Kazakov
 *
 * @param <A>
 *            the type of pinpointed axioms
 */
public class AxiomPinpointingCollector<A>
		implements AxiomPinpointingListener<A> {

	private Collection<Set<? extends A>> justifications_;

	private Collection<Set<? extends A>> repairs_;

	private final Set<A> usefulAxioms_;

	private Collection<A> current_;

	private Set<A> currentJustification_, currentRepair_;

	public AxiomPinpointingCollector() {
		current_ = usefulAxioms_ = new HashSet<>();
	}

	@Override
	public void computesJustifications() {
		justifications_ = new ArrayList<>();
	}

	@Override
	public void computesRepairs() {
		repairs_ = new ArrayList<>();
	}

	@Override
	public void newJustificationFound() {
		Validate.isTrue(currentJustification_ == null);
		current_ = currentJustification_ = new HashSet<>();
	}

	@Override
	public void newJustificationComplete() {
		Validate.isTrue(current_ == currentJustification_);
		justifications_.add(currentJustification_);
		current_ = currentJustification_ = null; // fail fast
	}

	@Override
	public void newRepairFound() {
		Validate.isTrue(currentRepair_ == null);
		current_ = currentRepair_ = new HashSet<>();
	}

	@Override
	public void newRepairComplete() {
		Validate.isTrue(current_ == currentRepair_);
		repairs_.add(currentRepair_);
		current_ = currentRepair_ = null; // fail fast
	}

	@Override
	public void usefulAxiom(A axiom) {
		usefulAxioms_.add(axiom);
		if (current_ != null) {
			current_.add(axiom);
		}
	}

	/**
	 * @return all found justifications or {@code null} if justification
	 *         computation not supported
	 */
	public Collection<Set<? extends A>> getJustifications() {
		return justifications_;
	}

	/**
	 * @return all found repairs or {@code null} if repair computation not
	 *         supported
	 */
	public Collection<Set<? extends A>> getRepairs() {
		return repairs_;
	}

	/**
	 * @return the set of all axioms that appear in justifications and repairs.
	 *         The result cannot be {@code null}.
	 */
	public Set<? extends A> getUsefulAxioms() {
		return usefulAxioms_;
	}

	@Override
	public void computationComplete() {
		// no-op
	}

}
