/*-
 * #%L
 * Proof Utility Library
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2017 Live Ontologies Project
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
package org.liveontologies.puli;

class RemoveAssertedProofNode<C> extends ConvertedProofNode<C> {

	RemoveAssertedProofNode(ProofNode<C> delegate) {
		super(delegate);
	}

	@Override
	protected final void convert(ConvertedProofStep<C> step) {
		if (Inferences.isAsserted(step)) {
			// ignore asserted inferences
			return;
		}
		// else
		convert(new RemoveAssertedProofStep<C>(step.getDelegate()));
	}

	void convert(RemoveAssertedProofStep<C> step) {
		super.convert(step);
	}

}
