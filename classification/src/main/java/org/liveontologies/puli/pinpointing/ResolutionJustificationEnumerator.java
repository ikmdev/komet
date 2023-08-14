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

import java.util.AbstractSet;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.collections.api.multimap.list.MutableListMultimap;
import org.eclipse.collections.impl.factory.Multimaps;
import org.eclipse.collections.impl.factory.Sets;
import org.liveontologies.puli.AxiomPinpointingInference;
import org.liveontologies.puli.Delegator;
import org.liveontologies.puli.Prover;
import org.liveontologies.puli.collections.BloomTrieCollection2;
import org.liveontologies.puli.collections.Collection2;
import org.liveontologies.puli.statistics.NestedStats;

/**
 * Computing justifications by resolving inferences. An inference X can be
 * resolved with an inference Y if the conclusion of X is one of the premises of
 * Y; the resulting inference Z will have the conclusion of Y, all premises of X
 * and Y except for the resolved one and all justificaitons of X and Y.
 * 
 * @author Peter Skocovsky
 * @author Yevgeny Kazakov
 *
 * @param <Q>
 *                the type of the query
 * @param <A>
 *                the type of axioms used by the inferences
 * @param <I>
 *                the type of inferences used in the proof
 */
public class ResolutionJustificationEnumerator<Q, A, I extends AxiomPinpointingInference<?, ? extends A>>
		extends AbstractProofAxiomPinpointingEnumerator<Q, A, I> {

	public enum SelectionType {
		TOP_DOWN, BOTTOM_UP, THRESHOLD
		// TODO: switch to class hierarchy to support threshold with parameter
	}

	/**
	 * Returns a factory for creating
	 * {@link AbstractProofAxiomPinpointingEnumerator} computations for
	 * enumerating justifications using resolution with parameters specified by
	 * the given {@link SelectionType} and {@link PriorityComparator}. The
	 * {@link SelectionType} determines how (resolution) rules are applied to
	 * derive new inferences from existing once. The {@link PriorityComparator}
	 * specifies the order in which justifications are reported by the
	 * {@link AxiomPinpointingListener}: the justifications for which the value
	 * of {@link PriorityComparator#getPriority(Object)} is smaller according to
	 * {@link PriorityComparator#compare(Object, Object)} are reported first.
	 * This function must be compatible with the subset ordering, that is:
	 * <blockquote>If {@link Set#containsAll(java.util.Collection)
	 * set2.containsAll(set1) == true} and
	 * {@link Set#containsAll(java.util.Collection) set1.containsAll(set2) ==
	 * false}, then {@link Comparator#compare(Object, Object)
	 * priorityComparator.compare(priorityComparator.getPriority(set1),
	 * priorityComparator.getPriority(set2)) < 0}.</blockquote>
	 * 
	 * @author Peter Skocovsky
	 * @author Yevgeny Kazakov
	 *
	 * @param <Q>
	 *                               the type of conclusions used in inferences
	 * @param <A>
	 *                               the type of axioms in justifications
	 * 
	 * @param selection
	 *                               The selection type that determines the
	 *                               strategy of resolution rule applications
	 * @param priorityComparator
	 *                               The comparator that defines the order in
	 *                               which justifications are reported to the
	 *                               listener.
	 * @param statusListener
	 *                               a listener to notify about the different
	 *                               stages of the computation
	 * @return a {@link AbstractProofAxiomPinpointingEnumerator} that can
	 *         perform computations of justifications from proofs according to
	 *         the specified parameters
	 */
	public static <Q, A> ProverAxiomPinpointingEnumerationFactory<Q, A> getFactory(
			final SelectionType selection,
			final PriorityComparator<? super Set<A>, ?> priorityComparator,
			final StatusListener statusListener) {
		return new ProverAxiomPinpointingEnumerationFactory<Q, A>() {

			@Override
			public String toString() {
				return ResolutionJustificationEnumerator.class.getSimpleName()
						+ "(" + selection.toString() + ", "
						+ priorityComparator.toString() + ")";
			}

			@Override
			public <I extends AxiomPinpointingInference<?, ? extends A>> AxiomPinpointingEnumerator<Q, A> create(
					Prover<? super Q, ? extends I> prover,
					AxiomPinpointingInterruptMonitor monitor) {

				return new ResolutionJustificationEnumerator<>(prover, monitor,
						selection, priorityComparator, statusListener);
			}

		};
	}

	public static <Q, A> ProverAxiomPinpointingEnumerationFactory<Q, A> getFactory(
			final SelectionType selection,
			final PriorityComparator<? super Set<A>, ?> priorityComparator) {
		return getFactory(selection, priorityComparator,
				new DummyStatusListener());
	}

	public static <Q, A> ProverAxiomPinpointingEnumerationFactory<Q, A> getFactory(
			final PriorityComparator<? super Set<A>, ?> priorityComparator) {
		return getFactory(SelectionType.THRESHOLD, priorityComparator,
				new DummyStatusListener());
	}

	/**
	 * Returns a factory for creating
	 * {@link AbstractProofAxiomPinpointingEnumerator} computations for
	 * enumerating justifications using resolution with parameters specified by
	 * the given {@link SelectionType}. The justifications are reported by the
	 * {@link AxiomPinpointingListener} in the order of increasing cardinality,
	 * that is, justifications that contain smaller number of axioms are
	 * reported first.
	 * 
	 * @param <Q>
	 *                           the type of conclusions used in inferences
	 * @param <A>
	 *                           the type of axioms in justifications
	 * 
	 * @param selection
	 *                           The selection type that determines the strategy
	 *                           of resolution rule applications
	 * @param statusListener
	 *                           a listener to notify about the different stages
	 *                           of the computation
	 * @return a {@link AbstractProofAxiomPinpointingEnumerator} that can
	 *         perform computations of justifications from proofs according to
	 *         the specified parameters
	 * 
	 * @see #getFactory(SelectionType, PriorityComparator, StatusListener)
	 */
	public static <Q, A> ProverAxiomPinpointingEnumerationFactory<Q, A> getFactory(
			final SelectionType selection, StatusListener statusListener) {
		return getFactory(selection, PriorityComparators.<A> cardinality(),
				statusListener);
	}

	public static <Q, A> ProverAxiomPinpointingEnumerationFactory<Q, A> getFactory(
			final SelectionType selection) {
		return getFactory(selection, PriorityComparators.<A> cardinality(),
				new DummyStatusListener());
	}

	/**
	 * Returns a factory for creating
	 * {@link AbstractProofAxiomPinpointingEnumerator} computations for
	 * enumerating justifications using resolution. The justifications are
	 * reported by the {@link AxiomPinpointingListener} in the order of
	 * increasing cardinality, that is, justifications that contain smaller
	 * number of axioms are reported first.
	 * 
	 * @param <Q>
	 *                the type of conclusions used in inferences
	 * @param <A>
	 *                the type of axioms in justifications
	 * @return a {@link AbstractProofAxiomPinpointingEnumerator} that can
	 *         perform computations of justifications from proofs according with
	 *         the default parameters
	 * 
	 * @see #getFactory(SelectionType, PriorityComparator)
	 */
	public static <Q, A> ProverAxiomPinpointingEnumerationFactory<Q, A> getFactory() {
		return getFactory(SelectionType.THRESHOLD);
	}

	public static <Q, A> ProverAxiomPinpointingEnumerationFactory<Q, A> getFactory(
			final StatusListener statusListener) {
		return getFactory(new DummyStatusListener());
	}

	private final SelectionType selectionType_;

	private final PriorityComparator<? super Set<A>, ?> priorityComparator_;

	/**
	 * Conclusions for which computation of justifications has been initialized
	 */
	private final Set<Object> initialized_ = new HashSet<>();

	private final IdMap<Object> conclusionIds_ = HashIdMap.create();

	private final IdMap<A> axiomIds_ = HashIdMap.create();

	/**
	 * a structure used to check inferences for minimality; an inference is
	 * minimal if there was no other inference with the same conclusion, subset
	 * of premises and subset of justirication produced
	 */
	private final Map<Integer, Collection2<DerivedInference>> minimalInferencesByConclusionIds_ = new HashMap<Integer, Collection2<DerivedInference>>();

	private final MutableListMultimap<Integer, DerivedInference>
	// inferences whose conclusions are selected, indexed by this conclusion
	inferencesBySelectedConclusionIds_ = Multimaps.mutable.list.empty(),
			// inferences whose premise is selected, indexed by this premise
			inferencesBySelectedPremiseIds_ = Multimaps.mutable.list.empty();

	/**
	 * inferences that are not necessary for computing the justifications for
	 * the current query; these are (possibly minimal) inferences whose
	 * justification is a superset of a justification for the query
	 */
	private Queue<DerivedInference> blockedInferences_ = new ArrayDeque<DerivedInference>();

	private final StatusListener statusListener_;

	private ResolutionJustificationEnumerator(
			final Prover<? super Q, ? extends I> prover,
			final AxiomPinpointingInterruptMonitor monitor,
			final SelectionType selectionType,
			final PriorityComparator<? super Set<A>, ?> priorityComparator,
			StatusListener statusListener) {
		super(prover, monitor, statusListener);
		this.selectionType_ = selectionType;
		this.priorityComparator_ = priorityComparator;
		this.statusListener_ = statusListener;
	}

	private Collection2<DerivedInference> getMinimalInferences(
			Integer conclusionId) {
		Collection2<DerivedInference> result = minimalInferencesByConclusionIds_
				.get(conclusionId);
		if (result == null) {
			result = new BloomTrieCollection2<DerivedInference>();
			minimalInferencesByConclusionIds_.put(conclusionId, result);
		}
		return result;
	}

	@Override
	protected AbstractProofAxiomPinpointingEnumerator<Q, A, I>.QueryEnumerator getQueryEnumerator(
			Q query) {
		return new JustificationProcessor<>(query, priorityComparator_);
	}

	@NestedStats
	public static Class<?> getNestedStats() {
		return BloomTrieCollection2.class;
	}

	static int[] getIds(Collection<? extends Integer> set) {
		return SortedIdSet.getIds(set);
	}

	Set<A> getJustification(int[] ids) {
		return new SortedIdSet<A>(ids, axiomIds_);
	}

	/**
	 * A derived inference obtained from either original inferences or
	 * resolution between two inferences on the conclusion and a premise.
	 * 
	 * @author Peter Skocovsky
	 * @author Yevgeny Kazakov
	 */
	static class DerivedInference extends AbstractSet<DerivedInferenceMember> {

		private final int conclusionId_;
		private final int[] premiseIds_;
		private final int[] justificationIds_;
		/**
		 * {@code true} if the inference was checked for minimality
		 */
		boolean isMinimal_ = false;

		protected DerivedInference(final int conclusionId,
				final int[] premiseIds, final int[] justificationIds) {
			this.conclusionId_ = conclusionId;
			this.premiseIds_ = premiseIds;
			this.justificationIds_ = justificationIds;
		}

		public Set<Integer> getPremises() {
			return new SortedIntSet(premiseIds_);
		}

		public Set<Integer> getJustification() {
			return new SortedIntSet(justificationIds_);
		}

		public boolean isATautology() {
			return Arrays.binarySearch(premiseIds_, conclusionId_) >= 0;
		}

		@Override
		public Iterator<DerivedInferenceMember> iterator() {
			Stream<DerivedInferenceMember> stream1 = Arrays.stream(new DerivedInferenceMember[] {new Conclusion(conclusionId_)});
			Stream<DerivedInferenceMember> stream2 = Arrays.stream(premiseIds_).mapToObj(premiseId -> new Premise(premiseId));
			Stream<DerivedInferenceMember> stream3 = Arrays.stream(justificationIds_).mapToObj(axiomId -> new Axiom(axiomId));

			return Stream.concat(Stream.concat(stream1,stream2), stream3).iterator();
		}

		@Override
		public boolean containsAll(final Collection<?> c) {
			if (c instanceof ResolutionJustificationEnumerator.DerivedInference) {
				final DerivedInference other = (DerivedInference) c;
				return conclusionId_ == other.conclusionId_
						&& SortedIdSet.containsAll(premiseIds_,
								other.premiseIds_)
						&& SortedIdSet.containsAll(justificationIds_,
								other.justificationIds_);
			}
			// else
			return super.containsAll(c);
		}

		private boolean contains(DerivedInferenceMember other) {
			return other.accept(new DerivedInferenceMember.Visitor<Boolean>() {

				@Override
				public Boolean visit(Axiom axiom) {
					return Arrays.binarySearch(justificationIds_,
							axiom.getDelegate()) >= 0;
				}

				@Override
				public Boolean visit(Conclusion conclusion) {
					return conclusionId_ == conclusion.getDelegate();
				}

				@Override
				public Boolean visit(Premise premise) {
					return Arrays.binarySearch(premiseIds_,
							premise.getDelegate()) >= 0;
				}

			});

		}

		@Override
		public boolean contains(final Object o) {
			if (o instanceof DerivedInferenceMember) {
				return contains((DerivedInferenceMember) o);
			}
			// else
			return false;
		}

		@Override
		public int size() {
			return getPremises().size() + getJustification().size() + 1;
		}

		@Override
		public String toString() {
			return String.valueOf(conclusionId_) + " -| "
					+ Arrays.toString(premiseIds_) + ": "
					+ Arrays.toString(justificationIds_);
		}

	}

	interface DerivedInferenceMember {

		<O> O accept(Visitor<O> visitor);

		interface Visitor<O> {

			O visit(Axiom axiom);

			O visit(Conclusion conclusion);

			O visit(Premise premise);
		}

	}

	static final class Axiom extends Delegator<Integer>
			implements DerivedInferenceMember {

		public Axiom(Integer id) {
			super(id);
		}

		@Override
		public <O> O accept(DerivedInferenceMember.Visitor<O> visitor) {
			return visitor.visit(this);
		}

	}

	static final class Conclusion extends Delegator<Integer>
			implements DerivedInferenceMember {

		public Conclusion(Integer id) {
			super(id);
		}

		@Override
		public <O> O accept(DerivedInferenceMember.Visitor<O> visitor) {
			return visitor.visit(this);
		}

	}

	static final class Premise extends Delegator<Integer>
			implements DerivedInferenceMember {

		public Premise(Integer id) {
			super(id);
		}

		@Override
		public <O> O accept(DerivedInferenceMember.Visitor<O> visitor) {
			return visitor.visit(this);
		}

	}

	private class JustificationProcessor<P> extends QueryEnumerator {

		final int queryId_;

		/**
		 * a function used for selecting conclusions in inferences on which to
		 * resolve
		 */
		final SelectionFunction selectionFunction_;

		/**
		 * to check minimality of justifications
		 */
		final Collection2<Set<Integer>> minimalJustifications_ = new BloomTrieCollection2<Set<Integer>>();

		/**
		 * a temporary queue used to initialize computation of justifications
		 * for conclusions that are not yet {@link #initialized_}
		 */
		final Queue<Object> toInitialize_ = new ArrayDeque<>();

		final PriorityComparator<? super Set<A>, P> priorityComparator_;

		/**
		 * newly computed inferences to be resolved upon
		 */
		final Queue<UnprocessedInference<P>> unprocessedInferences_;

		final InferenceProcessor<P> resolver_;

		JustificationProcessor(Q query,
				PriorityComparator<? super Set<A>, P> priorityComparator) {
			super(query);
			this.queryId_ = conclusionIds_.getId(query);
			this.priorityComparator_ = priorityComparator;
			this.selectionFunction_ = getSelectionFunction(selectionType_);
			this.resolver_ = new InferenceProcessor<P>(priorityComparator);
			this.unprocessedInferences_ = new PriorityQueue<UnprocessedInference<P>>(
					256,
					new UnprocessedInferenceCompatator<P>(priorityComparator));
		}

		@Override
		public void enumerate(AxiomPinpointingListener<A> listener) {
			listener.computesJustifications();
			initialize();
			unblockJobs();
			changeSelection();
			process(listener);
			listener.computationComplete();
		}

		void toInitialize(Object conclusion) {
			if (initialized_.add(conclusion)) {
				toInitialize_.add(conclusion);
			}
		}

		void block(DerivedInference inf) {
			blockedInferences_.add(inf);
		}

		void initialize() {
			toInitialize(getQuery());
			for (;;) {
				Object next = toInitialize_.poll();
				if (next == null) {
					return;
				}
				for (final I inf : getProof().getInferences(next)) {
					produce(newDerivedInference(inf));
					for (Object premise : inf.getPremises()) {
						toInitialize(premise);
					}
				}
			}
		}

		void unblockJobs() {
			for (;;) {
				DerivedInference inf = blockedInferences_.poll();
				if (inf == null) {
					return;
				}
				// else
				produce(newDerivedInference(inf));
			}
		}

		void changeSelection() {
			// selection for inferences with selected query must change
			for (DerivedInference inf : inferencesBySelectedConclusionIds_
					.removeAll(queryId_)) {
				produce(newDerivedInference(inf));
			}
		}

		void process(AxiomPinpointingListener<A> listener) {
			for (;;) {
				checkInterrupt();
				UnprocessedInference<P> next = unprocessedInferences_.poll();
				if (next == null) {
					break;
				}
				DerivedInference inf = next.accept(resolver_);
				if (!minimalJustifications_.isMinimal(inf.getJustification())) {
					block(inf);
					continue;
				}
				// else
				if (inf.premiseIds_.length == 0
						&& queryId_ == inf.conclusionId_) {
					minimalJustifications_.add(inf.getJustification());
					listener.newJustificationFound();
					for (int i = 0; i < inf.justificationIds_.length; i++) {
						listener.usefulAxiom(
								axiomIds_.getElement(inf.justificationIds_[i]));
					}
					listener.newJustificationComplete();
					block(inf);
					continue;
				}
				// else
				if (!inf.isMinimal_) {
					Collection2<DerivedInference> minimalInferences = getMinimalInferences(
							inf.conclusionId_);
					if (!minimalInferences.isMinimal(inf)) {
						continue;
					}
					// else
					inf.isMinimal_ = true;
					minimalInferences.add(inf);
					statusListener_.newMinimalInference();
				}
				Integer selected = selectionFunction_.getResolvingAtomId(inf);
				if (selected == null) {
					// resolve on the conclusions
					selected = inf.conclusionId_;
					if (queryId_ == selected) {
						throw new RuntimeException(
								"Goal conclusion cannot be selected if the inference has premises: "
										+ inf);
					}
					inferencesBySelectedConclusionIds_.put(selected, inf);
					for (DerivedInference other : inferencesBySelectedPremiseIds_
							.get(selected)) {
						produce(newResolvent(inf, other));
					}
				} else {
					// resolve on the selected premise
					inferencesBySelectedPremiseIds_.put(selected, inf);
					for (DerivedInference other : inferencesBySelectedConclusionIds_
							.get(selected)) {
						produce(newResolvent(other, inf));
					}
				}
			}

		}

		void produce(final UnprocessedInference<P> resolvent) {
			if (resolvent.isATautology()) {
				// skip tautologies
				return;
			}
			statusListener_.inferenceDerived();
			unprocessedInferences_.add(resolvent);
		}

		UnprocessedInference<P> newDerivedInference(
				DerivedInference inference) {
			return new InitialInference<P>(inference.conclusionId_,
					inference.premiseIds_, inference.justificationIds_,
					priorityComparator_.getPriority(
							getJustification(inference.justificationIds_)),
					inference.isMinimal_);
		}

		UnprocessedInference<P> newDerivedInference(
				AxiomPinpointingInference<?, ? extends A> inference) {
			int[] justificationIds = getAxiomIds(inference.getJustification());
			return new InitialInference<P>(
					conclusionIds_.getId(inference.getConclusion()),
					getConclusionIds(inference.getPremises()), justificationIds,
					priorityComparator_
							.getPriority(getJustification(justificationIds)));
		}

		UnprocessedInference<P> newResolvent(
				final DerivedInference firstInference,
				final DerivedInference secondInference) {
			return new Resolvent<P>(firstInference, secondInference,
					priorityComparator_.getPriority(getJustification(
							SortedIdSet.union(firstInference.justificationIds_,
									secondInference.justificationIds_))));
		}

		int[] getConclusionIds(Collection<?> conclusions) {
			return SortedIdSet.getIds(conclusions, conclusionIds_);
		}

		int[] getAxiomIds(Collection<? extends A> axioms) {
			return SortedIdSet.getIds(axioms, axiomIds_);
		}

		SelectionFunction getSelectionFunction(SelectionType type) {
			switch (type) {
			case BOTTOM_UP:
				return new SelectionFunction() {

					@Override
					public Integer getResolvingAtomId(
							DerivedInference inference) {
						// select the premise that is derived by the fewest
						// inferences;
						// if there are no premises, select the conclusion
						Integer result = null;
						int minInferenceCount = Integer.MAX_VALUE;
						for (int premiseId : inference.premiseIds_) {
							int inferenceCount = getProof().getInferences(
									conclusionIds_.getElement(premiseId))
									.size();
							if (inferenceCount < minInferenceCount) {
								result = premiseId;
								minInferenceCount = inferenceCount;
							}
						}
						return result;
					}
				};
			case THRESHOLD:
				return new SelectionFunction() {

					static final int THRESHOLD_ = 2;

					@Override
					public Integer getResolvingAtomId(
							DerivedInference inference) {
						// select the premise derived by the fewest inferences
						// unless the number of such inferences is larger than
						// the give threshold and the conclusion is not the
						// query; in this case select the conclusion
						int minInferenceCount = Integer.MAX_VALUE;
						Integer result = null;
						for (int premiseId : inference.premiseIds_) {
							int inferenceCount = getProof().getInferences(
									conclusionIds_.getElement(premiseId))
									.size();
							if (inferenceCount < minInferenceCount) {
								result = premiseId;
								minInferenceCount = inferenceCount;
							}
						}
						if (minInferenceCount > THRESHOLD_
								&& queryId_ != inference.conclusionId_) {
							// resolve on the conclusion
							result = null;
						}
						return result;
					}

				};

			case TOP_DOWN:
				return new SelectionFunction() {

					@Override
					public Integer getResolvingAtomId(
							DerivedInference inference) {
						// select the conclusion, unless it is the query
						// conclusion and there are premises, in which case
						// select the premise derived by the fewest inferences
						Integer result = null;
						if (queryId_ == inference.conclusionId_) {
							int minInferenceCount = Integer.MAX_VALUE;
							for (int premiseId : inference.premiseIds_) {
								int inferenceCount = getProof().getInferences(
										conclusionIds_.getElement(premiseId))
										.size();
								if (inferenceCount < minInferenceCount) {
									result = premiseId;
									minInferenceCount = inferenceCount;
								}
							}
						}
						return result;
					}
				};

			default:
				throw new RuntimeException(
						"Unsupported selection type: " + type);
			}
		}

	}

	/**
	 * An object representing unprocessed {@link DerivedInference}
	 * 
	 * @author Yevgeny Kazakov
	 *
	 * @param <P>
	 */
	interface UnprocessedInference<P> {

		/**
		 * @return the priority in which this inference should be processed
		 */
		P getPriority();

		/**
		 * @return the number of premises of for the inference represented by
		 *         this element
		 */
		int getPremiseCount();

		/**
		 * @return {@code true} if the inference represented by this element is
		 *         a tautology, i.e. its conclusion is one of the premises.
		 */
		boolean isATautology();

		<O> O accept(UnprocessedInference.Visitor<P, O> visitor);

		interface Visitor<P, O> {

			O visit(InitialInference<P> inference);

			O visit(Resolvent<P> inference);

		}

	}

	/**
	 * An {@link UnprocessedInference} representing the initial inference, from
	 * which the resolution computation starts
	 * 
	 * @author Yevgeny Kazakov
	 *
	 * @param <P>
	 */
	static class InitialInference<P> extends DerivedInference
			implements UnprocessedInference<P> {

		private final P priority_;

		private InitialInference(int conclusionId, int[] premiseIds,
				int[] justificationIds, P priority) {
			super(conclusionId, premiseIds, justificationIds);
			this.priority_ = priority;
		}

		private InitialInference(int conclusionId, int[] premiseIds,
				int[] justificationIds, P priority, boolean isMinimal) {
			this(conclusionId, premiseIds, justificationIds, priority);
			this.isMinimal_ = isMinimal;
		}

		@Override
		public int getPremiseCount() {
			return getPremises().size();
		}

		@Override
		public P getPriority() {
			return priority_;
		}

		@Override
		public <O> O accept(UnprocessedInference.Visitor<P, O> visitor) {
			return visitor.visit(this);
		}

	}

	/**
	 * The result of resolution applied to two {@link DerivedInference}s. The
	 * resulting inference is computed only when this
	 * {@link UnprocessedInference} is processed to prevent unnecessary memory
	 * consumption when this object is stored in the queue.
	 * 
	 * @author Yevgeny Kazakov
	 *
	 */
	static class Resolvent<P> implements UnprocessedInference<P> {

		private final DerivedInference firstInference_, secondInference_;

		private final P priority_;

		private final int premiseCount_;

		Resolvent(DerivedInference firstInference,
				DerivedInference secondInference, P priority) {
			if (firstInference.isATautology()
					|| secondInference.isATautology()) {
				throw new IllegalArgumentException(
						"Cannot resolve on tautologies!");
			}
			this.firstInference_ = firstInference;
			this.secondInference_ = secondInference;
			this.priority_ = priority;
			this.premiseCount_ = Sets.union(firstInference_.getPremises(), secondInference_.getPremises()).size() - 1;
		}

		@Override
		public int getPremiseCount() {
			return premiseCount_;
		}

		@Override
		public boolean isATautology() {
			// correct when the second inference is not a tautology
			return Arrays.binarySearch(firstInference_.premiseIds_,
					secondInference_.conclusionId_) >= 0;
		}

		@Override
		public P getPriority() {
			return priority_;
		}

		@Override
		public <O> O accept(UnprocessedInference.Visitor<P, O> visitor) {
			return visitor.visit(this);
		}

	}

	static class UnprocessedInferenceCompatator<P>
			implements Comparator<UnprocessedInference<P>> {

		private final Comparator<P> priorityComparator_;

		UnprocessedInferenceCompatator(Comparator<P> priorityComparator) {
			this.priorityComparator_ = priorityComparator;
		}

		@Override
		public int compare(UnprocessedInference<P> first,
				UnprocessedInference<P> second) {
			final int result = priorityComparator_.compare(first.getPriority(),
					second.getPriority());
			if (result != 0) {
				return result;
			}
			// else
			final int firstPremiseCount = first.getPremiseCount();
			final int secondPremiseCount = second.getPremiseCount();
			return (firstPremiseCount < secondPremiseCount) ? -1
					: ((firstPremiseCount == secondPremiseCount) ? 0 : 1);
		}

	};

	/**
	 * Converts {@link UnprocessedInference}s to {@link DerivedInference}s
	 * 
	 * @author Yevgeny Kazakov
	 *
	 * @param <P>
	 */
	class InferenceProcessor<P>
			implements UnprocessedInference.Visitor<P, DerivedInference> {

		private final PriorityComparator<? super Set<A>, P> priorityComparator_;

		InferenceProcessor(
				PriorityComparator<? super Set<A>, P> priorityComparator) {
			this.priorityComparator_ = priorityComparator;
		}

		@Override
		public DerivedInference visit(InitialInference<P> inference) {
			return inference;
		}

		@Override
		public DerivedInference visit(Resolvent<P> inference) {
			int[] newPremiseIds;
			DerivedInference first = inference.firstInference_;
			DerivedInference second = inference.secondInference_;
			if (second.getPremises().size() == 1) {
				newPremiseIds = first.premiseIds_;
			} else {
				newPremiseIds = getIds(Sets.union(first.getPremises(),
						Sets.difference(second.getPremises(),
								Collections.singleton(first.conclusionId_))));
			}
			int[] newJustificationIds = SortedIdSet
					.union(first.justificationIds_, second.justificationIds_);
			return new InitialInference<P>(second.conclusionId_, newPremiseIds,
					newJustificationIds, priorityComparator_.getPriority(
							getJustification(newJustificationIds)));
		}

	}

	public interface SelectionFunction {

		/**
		 * @param inference
		 * @return the id of the premise of the inference on which the
		 *         resolution rule should be applied or {@code null} if the
		 *         resolution rule should be applied on the conclusion of the
		 *         inference
		 */
		Integer getResolvingAtomId(DerivedInference inference);

	}

	public interface StatusListener
			extends AbstractProofAxiomPinpointingEnumerator.StatusListener {

		void inferenceDerived();

		void newMinimalInference();

	}

	public static class DummyStatusListener
			extends AbstractProofAxiomPinpointingEnumerator.DummyStatusListener
			implements StatusListener {

		@Override
		public void inferenceDerived() {
			// no-op
		}

		@Override
		public void newMinimalInference() {
			// no-op
		}

	}

}
