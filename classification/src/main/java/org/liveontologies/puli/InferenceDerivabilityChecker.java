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
package org.liveontologies.puli;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.eclipse.collections.api.multimap.list.MutableListMultimap;
import org.eclipse.collections.api.multimap.set.MutableSetMultimap;
import org.eclipse.collections.impl.factory.Multimaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * A utility to check derivability of conclusions by inferences (in the presence
 * of blocked conclusions). A conclusion is derivable if it is not blocked and a
 * conclusion of an inference whose all premises are (recursively) derivable.
 * 
 * @author Yevgeny Kazakov
 *
 * @param <C>
 *            the type of conclusions in inferences
 * @param <I>
 *            the type of inferences in proofs
 */
public class InferenceDerivabilityChecker<C, I extends Inference<? extends C>>
		implements DerivabilityCheckerWithBlocking<C, I>, Proof<I> {

	// logger for this class
	private static final Logger LOGGER_ = LoggerFactory
			.getLogger(InferenceDerivabilityChecker.class);

	/**
	 * the inferences that can be used for deriving conclusions
	 */
	private final Proof<? extends I> proof_;

	/**
	 * conclusions that cannot be used in the derivations
	 */
	private final Set<C> blocked_ = new HashSet<C>();

	private final Queue<C> toBlock_ = new ArrayDeque<C>(32),
			toUnblock_ = new ArrayDeque<C>(32);

	/**
	 * {@link #goals_} that that were found derivable
	 */
	private final Set<C> derivable_ = new HashSet<C>();

	/**
	 * a map from a conclusion not in {@link #derivable_} to {@link #proof_}
	 * that have this conclusion as one of the premises; intuitively, these
	 * inferences are "waiting" for this conclusion to be derived
	 */
	private final MutableListMultimap<C, I> watchedInferences_ = Multimaps.mutable.list.empty();

	/**
	 * a mirror map corresponding to {@link #watchedInferences_} that points to
	 * the positions of premises in the respective inferences
	 */
	private final MutableListMultimap<C, Integer> watchPremisePositions_ = Multimaps.mutable.list.empty();

	/**
	 * a map from a {@link #derivable_} conclusion to {@link #proof_} whose all
	 * premises are also in {@link #derivable_}; intuitively, these inferences
	 * are used in the derivations
	 */
	private final MutableSetMultimap<C, I> firedInferencesByPremises_ = Multimaps.mutable.set.empty();

	/**
	 * a map containing inferences in {@link #firedInferencesByPremises_} with a
	 * key for every premise of such inference
	 */
	private final Map<C, I> firedInferencesByConclusions_ = new HashMap<C, I>();

	/**
	 * a map from conclusions to iterators over all inferences in
	 * {@link #proof_} with these conclusions that are neither present in
	 * {@link #watchedInferences_} nor in {@link #firedInferencesByConclusions_}
	 */
	private final Map<C, Queue<I>> remainingInferences_ = new HashMap<C, Queue<I>>();

	/**
	 * conclusions for which a derivability test was initiated or finished
	 */
	private final Set<C> goals_ = new HashSet<C>();

	/**
	 * {@link #goals_} that needs to be checked for derivability; they should
	 * not be in {@link #blocked_}
	 */
	private final Deque<C> toCheck_ = new ArrayDeque<C>(128);

	private final Deque<C> toSetUnknown_ = new ArrayDeque<C>(128);

	/**
	 * {@link #derivable_} goals which may have some {@link #watchedInferences_}
	 */
	private final Queue<C> toPropagate_ = new LinkedList<C>();

	public InferenceDerivabilityChecker(Proof<? extends I> proof) {
		Objects.requireNonNull(proof);
		this.proof_ = proof;
	}

	@Override
	public boolean isDerivable(C conclusion) {
		LOGGER_.trace("{}: checking derivability", conclusion);
		initBlocking();
		toCheck(conclusion);
		process();
		boolean derivable = derivable_.contains(conclusion)
				&& !blocked_.contains(conclusion);
		LOGGER_.trace("{}: derivable: {}", conclusion, derivable);
		return derivable;
	}

	@Override
	public Proof<I> getDerivation(C conclusion) {
		if (!isDerivable(conclusion)) {
			return null;
		}
		// else construct proof of fired inferences
		return new Proof<I>() {

			@Override
			public Collection<? extends I> getInferences(Object conclusion) {
				return Collections.singleton(
						firedInferencesByConclusions_.get(conclusion));
			}
		};
	}

	@Override
	public Set<C> getBlockedConclusions() {
		return blocked_;
	}

	@Override
	public boolean block(C conclusion) {
		if (blocked_.add(conclusion)) {
			LOGGER_.trace("{}: blocked", conclusion);
			toBlock_.add(conclusion);
			return true;
		}
		// else
		return false;
	}

	@Override
	public boolean unblock(C conclusion) {
		if (blocked_.remove(conclusion)) {
			LOGGER_.trace("{}: unblocked", conclusion);
			toUnblock_.add(conclusion);
			return true;
		}
		// else
		return false;
	}

	/**
	 * @return all conclusions that could not be derived in tests for
	 *         derivability. It guarantees to contain all conclusions for which
	 *         {@link #isDerivable(Object)} returns {@code false} and also at
	 *         least one premise for each inference producing an element in this
	 *         set. But this set may also grow if {@link #isDerivable(Object)}
	 *         returns {@code true} (e.g., if the conclusion is derivable by one
	 *         inference but has another inference in which some premise is not
	 *         derivable). This set is mostly useful for debugging issues with
	 *         derivability.
	 */
	public Set<? extends C> getNonDerivableConclusions() {
		return watchedInferences_.keySet().toSet();
	}

	private void initBlocking() {
		for (;;) {
			C next = toBlock_.poll();
			if (next == null) {
				break;
			}
			if (!blocked_.contains(next)) {
				// was unblocked later
				continue;
			}
			setUnknown(next);
		}
		for (;;) {
			C next = toUnblock_.poll();
			if (next == null) {
				break;
			}
			if (blocked_.contains(next)) {
				// was blocked later
				continue;
			}
			if (derivable_.contains(next)) {
				toPropagate_.add(next);
			} else if (goals_.contains(next)) {
				toCheck_.addFirst(next);
			}
		}
	}

	private void toCheck(C conclusion) {
		if (goals_.add(conclusion)) {
			LOGGER_.trace("{}: new goal", conclusion);
			if (blocked_.contains(conclusion)) {
				LOGGER_.trace("{}: goal blocked", conclusion);
				return;
			}
			toCheck_.addFirst(conclusion);
		}
	}

	private boolean derive(C conclusion) {
		if (!derivable_.add(conclusion)) {
			return false;
		}
		// else propagate
		LOGGER_.trace("{}: derived", conclusion);
		if (!blocked_.contains(conclusion)) {
			toPropagate_.add(conclusion);
		}
		return true;
	}

	private void process() {
		for (;;) {
			// propagating derivable inferences with the highest priority
			C derivable = toPropagate_.poll();

			if (derivable != null) {
				List<I> watched = watchedInferences_.removeAll(derivable);
				List<Integer> positions = watchPremisePositions_
						.removeAll(derivable);
				for (int i = 0; i < watched.size(); i++) {
					I inf = watched.get(i);
					int pos = positions.get(i);
					check(pos, inf);
				}
				continue;
			}

			// expanding inferences if there is nothing to propagate
			C unknown = toCheck_.peek();
			if (unknown != null) {
				if (derivable_.contains(unknown)) {
					toCheck_.poll();
					continue;
				}
				Queue<I> inferences = getRemainingInferences(unknown);
				I inf = inferences.poll();
				if (inf == null) {
					toCheck_.poll();
					continue;
				}
				LOGGER_.trace("{}: expanding", inf);
				check(0, inf);
				continue;
			}

			// all done
			return;
		}

	}

	private Queue<I> getRemainingInferences(C conclusion) {
		Queue<I> result = remainingInferences_.get(conclusion);
		if (result == null) {
			result = new ArrayDeque<I>(proof_.getInferences(conclusion));
			remainingInferences_.put(conclusion, result);
		}
		return result;
	}

	private void check(int pos, I inf) {
		List<? extends C> premises = inf.getPremises();
		int premiseCount = premises.size();
		int premisesChecked = 0;
		for (;;) {
			if (premisesChecked == premiseCount) {
				// all premises are derived
				fire(inf);
				return;
			}
			C premise = premises.get(pos);
			if (!derivable_.contains(premise)) {
				addWatch(premise, pos, inf);
				return;
			}
			pos++;
			if (pos == premiseCount) {
				pos = 0;
			}
			premisesChecked++;
		}
	}

	private void fire(I inf) {
		LOGGER_.trace("{}: fire", inf);
		C conclusion = inf.getConclusion();
		getRemainingInferences(conclusion).add(inf);
		if (!derive(conclusion)) {
			return;
		}
		firedInferencesByConclusions_.put(inf.getConclusion(), inf);
		List<? extends C> premises = inf.getPremises();
		for (int pos = 0; pos < premises.size(); pos++) {
			firedInferencesByPremises_.put(premises.get(pos), inf);
		}
	}

	private void addWatch(C premise, int pos, I inf) {
		LOGGER_.trace("{}: watching position {}", inf, pos);
		watchedInferences_.put(premise, inf);
		watchPremisePositions_.put(premise, pos);
		toCheck(premise);
	}

	void setUnknown(C conclusion) {
		toSetUnknown_.add(conclusion);
		for (;;) {
			conclusion = toSetUnknown_.poll();
			if (conclusion == null) {
				break;
			}
			if (!derivable_.remove(conclusion)) {
				continue;
			}
			// else was derivable
			LOGGER_.trace("{}: unknown goal", conclusion);
			if (!blocked_.contains(conclusion)) {
				toCheck_.addLast(conclusion);
			}
			I fired = firedInferencesByConclusions_.remove(conclusion);
			for (C premise : fired.getPremises()) {
				firedInferencesByPremises_.remove(premise, fired);
			}
			for (I inf : firedInferencesByPremises_.get(conclusion)) {
				toSetUnknown_.add(inf.getConclusion());
			}
		}
	}

	@Override
	public Collection<? extends I> getInferences(Object conclusion) {
		I inference = firedInferencesByConclusions_.get(conclusion);
		return inference == null ? Collections.<I> emptySet()
				: Collections.singleton(inference);
	}

}
