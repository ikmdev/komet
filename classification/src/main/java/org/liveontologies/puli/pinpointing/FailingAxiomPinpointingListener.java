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
 * An {@link AxiomPinpointingListener} that fails on every method. Useful to
 * prototype listeners some of which methods should not be called.
 * 
 * @author Yevgeny Kazakov
 *
 * @param <A>
 *                the type of axioms appearing in repairs and justifications
 *                reported by this listener
 */
public class FailingAxiomPinpointingListener<A>
		implements AxiomPinpointingListener<A> {

	<T> T fail() {
		throw new UnsupportedOperationException(
				"Listener method should not be called!");
	}

	@Override
	public void computesJustifications() {
		fail();
	}

	@Override
	public void newJustificationFound() {
		fail();
	}

	@Override
	public void newJustificationComplete() {
		fail();
	}

	@Override
	public void usefulAxiom(A axiom) {
		fail();
	}

	@Override
	public void computationComplete() {
		fail();
	}

	@Override
	public void computesRepairs() {
		fail();
	}

	@Override
	public void newRepairFound() {
		fail();
	}

	@Override
	public void newRepairComplete() {
		fail();
	}

}
