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
 * An {@link AxiomPinpointingListener} that does not do anything. Useful as a
 * skeleton implementation for listeners that need to implement just a subset of
 * the methods.
 * 
 * @author Yevgeny Kazakov
 *
 * @param <E>
 */
public class DummyAxiomPinpointingListener<E>
		implements AxiomPinpointingListener<E> {

	@Override
	public void computesJustifications() {
		// no-op
	}

	@Override
	public void computesRepairs() {
		// no-op
	}

	@Override
	public void newJustificationFound() {
		// no-op
	}

	@Override
	public void newJustificationComplete() {
		// no-op
	}

	@Override
	public void newRepairFound() {
		// no-op
	}

	@Override
	public void newRepairComplete() {
		// no-op
	}

	@Override
	public void usefulAxiom(E axiom) {
		// no-op
	}

	@Override
	public void computationComplete() {
		// no-op
	}

}
