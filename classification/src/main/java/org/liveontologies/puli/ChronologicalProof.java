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

/**
 * An implementation of modifiable proof that forbids production of inferences
 * whose premises are not conclusions of some inferences stored in this
 * {@link Proof}.
 * 
 * @author Peter Skocovsky
 * 
 * @param <I>
 *            the type of inferences provided by this proof
 */
public class ChronologicalProof<I extends Inference<?>> extends BaseProof<I> {

	@Override
	public void produce(final I inference) {
		check(inference);
		super.produce(inference);
	}

	/**
	 * Checks whether all premises of the given {@link Inference} are
	 * conclusions of some inferences stored in this {@link Proof} and throws a
	 * {@link RuntimeException} if not.
	 * 
	 * @param inference
	 */
	private void check(final I inference) {
		for (final Object premise : inference.getPremises()) {
			if (getInferences(premise).isEmpty()) {
				throw new RuntimeException(
						inference + ": premise not derived: " + premise);
			}
		}
	}

}
