package org.liveontologies.puli.pinpointing;

/*-
 * #%L
 * Proof Utility Library
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2021 Live Ontologies Project
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

class AxiomPinpointingCombinedListener<A>
		implements AxiomPinpointingListener<A> {

	private final AxiomPinpointingListener<A>[] listeners_;

	@SafeVarargs
	AxiomPinpointingCombinedListener(AxiomPinpointingListener<A>... listeners) {
		this.listeners_ = listeners;
	}

	@Override
	public void computesJustifications() {
		for (int i = 0; i < listeners_.length; i++) {
			listeners_[i].computesJustifications();
		}
	}

	@Override
	public void newJustificationFound() {
		for (int i = 0; i < listeners_.length; i++) {
			listeners_[i].newJustificationFound();
		}
	}

	@Override
	public void newJustificationComplete() {
		for (int i = 0; i < listeners_.length; i++) {
			listeners_[i].newJustificationComplete();
		}
	}

	@Override
	public void usefulAxiom(A axiom) {
		for (int i = 0; i < listeners_.length; i++) {
			listeners_[i].usefulAxiom(axiom);
		}

	}

	@Override
	public void computationComplete() {
		for (int i = 0; i < listeners_.length; i++) {
			listeners_[i].computationComplete();
		}

	}

	@Override
	public void computesRepairs() {
		for (int i = 0; i < listeners_.length; i++) {
			listeners_[i].computesRepairs();
		}
	}

	@Override
	public void newRepairFound() {
		for (int i = 0; i < listeners_.length; i++) {
			listeners_[i].newRepairFound();
		}
	}

	@Override
	public void newRepairComplete() {
		for (int i = 0; i < listeners_.length; i++) {
			listeners_[i].newRepairComplete();
		}
	}

}
