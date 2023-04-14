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

/**
 * Extends the given {@link RepairListener} to an
 * {@link AxiomPinpointingListener} in which all remaining methods fail.
 * 
 * @author Yevgeny Kazakov
 *
 * @param <A>
 *                the type of axioms appearing in repairs reported by this
 *                listener
 */
public class RepairListenerAdapter<A>
		extends FailingAxiomPinpointingListener<A> {

	private final RepairListener<A> delegate_;

	public RepairListenerAdapter(RepairListener<A> delegate) {
		this.delegate_ = delegate;
	}

	@Override
	public void usefulAxiom(A axiom) {
		delegate_.usefulAxiom(axiom);
	}

	@Override
	public void computesRepairs() {
		delegate_.computesRepairs();
	}

	@Override
	public void computationComplete() {
		delegate_.computationComplete();
	}

	@Override
	public void newRepairFound() {
		delegate_.newRepairFound();
	}

	@Override
	public void newRepairComplete() {
		delegate_.newRepairComplete();
	}

}
