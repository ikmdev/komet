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

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.eclipse.collections.api.multimap.list.MutableListMultimap;
import org.eclipse.collections.impl.factory.Multimaps;

class InferenceExpander<C, I extends Inference<? extends C>>
		implements Producer<I> {

	private final Set<C> derivable_;

	private final Proof<? extends I> proof_;

	private final MutableListMultimap<C, I> watchInferences_ = Multimaps.mutable.list.empty();
	private final MutableListMultimap<C, Integer> watchPositions_ = Multimaps.mutable.list.empty();

	private final Queue<C> newlyDerived_ = new ArrayDeque<C>();

	private final Producer<? super I> producer_;

	InferenceExpander(Set<C> derivable, Proof<? extends I> proof, C goal,
			Producer<? super I> producer) {
		this.proof_ = proof;
		this.derivable_ = derivable;
		this.producer_ = producer;
		process(goal);
	}

	public static <C, I extends Inference<? extends C>> void expand(
			Set<C> derivable, Proof<? extends I> proof, C goal,
			Producer<? super I> producer) {
		new InferenceExpander<C, I>(derivable, proof, goal, producer);
	}

	void process(C goal) {
		Proofs.unfoldRecursively(proof_, goal, this);
	}

	@Override
	public void produce(I inf) {
		List<? extends C> premises = inf.getPremises();
		for (int i = 0; i < premises.size(); i++) {
			C premise = premises.get(i);
			if (!derivable_.contains(premise)) {
				watchInferences_.put(premise, inf);
				watchPositions_.put(premise, i);
				return;
			}
		}
		// all premises are derived
		C conclusion = inf.getConclusion();
		if (derivable_.add(conclusion)) {
			producer_.produce(inf);
			newlyDerived_.add(conclusion);
			propagate();
		}
	}

	void propagate() {
		for (;;) {
			C next = newlyDerived_.poll();
			if (next == null) {
				return;
			}
			List<I> watch = watchInferences_.removeAll(next);
			List<Integer> positions = watchPositions_.removeAll(next);
			for (int i = 0; i < watch.size(); i++) {
				I inf = watch.get(i);
				int pos = positions.get(i);
				List<? extends C> premises = inf.getPremises();
				for (;;) {
					pos++;
					if (pos == premises.size()) {
						// all premises are derived
						C conclusion = inf.getConclusion();
						if (derivable_.add(conclusion)) {
							producer_.produce(inf);
							newlyDerived_.add(conclusion);
						}
						break;
					}
					// else
					C premise = premises.get(pos);
					if (derivable_.contains(premise)) {
						continue;
					}
					// else
					watchInferences_.put(premise, inf);
					watchPositions_.put(premise, pos);
					break;
				}
			}
		}

	}

}
