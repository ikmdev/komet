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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;

import org.liveontologies.puli.AxiomPinpointingInference;
import org.liveontologies.puli.Delegator;
import org.liveontologies.puli.Inference;
import org.liveontologies.puli.Prover;
import org.liveontologies.puli.collections.BloomTrieCollection2;
import org.liveontologies.puli.collections.Collection2;


/**
 * 
 * @author Yevgeny Kazakov
 *
 * @param <Q>
 *                the type of the query
 * @param <A>
 *                the type of axioms used by the inferences
 * @param <I>
 *                the type of inferences used in the proof
 */
public class TopDownRepairComputation<Q, A, I extends AxiomPinpointingInference<?, ? extends A>>
		extends AbstractProofAxiomPinpointingEnumerator<Q, A, I> {

	/**
	 * Returns a {@link ProverAxiomPinpointingEnumerationFactory} for
	 * enumerating repairs using the provided {@link PriorityComparator}. The
	 * {@link PriorityComparator} specifies the order in which repairs are
	 * reported by the {@link AxiomPinpointingListener}: the repairs for which
	 * the value of {@link PriorityComparator#getPriority(Object)} is smaller
	 * according to {@link PriorityComparator#compare(Object, Object)} are
	 * reported first. This function must be compatible with the subset
	 * ordering, that is: <blockquote>If
	 * {@link Set#containsAll(java.util.Collection) set2.containsAll(set1) ==
	 * true} and {@link Set#containsAll(java.util.Collection)
	 * set1.containsAll(set2) == false}, then
	 * {@link Comparator#compare(Object, Object)
	 * priorityComparator.compare(priorityComparator.getPriority(set1),
	 * priorityComparator.getPriority(set2)) < 0}.</blockquote>
	 * 
	 * @author Yevgeny Kazakov
	 * @author Peter Skocovsky
	 * 
	 * @param <Q>
	 *                               the type of the query
	 * @param <A>
	 *                               the type of axioms in repairs
	 * 
	 * @param priorityComparator
	 *                               The comparator that defines the order in
	 *                               which repairs are reported to the listener.
	 * @param statusListener
	 *                               a listener to notify about the different
	 *                               stages of the computation
	 * 
	 * @return a {@link AbstractProofAxiomPinpointingEnumerator} that can
	 *         perform computations of repairs from proofs according to the
	 *         specified parameters
	 */
	public static <Q, A> ProverAxiomPinpointingEnumerationFactory<Q, A> getFactory(
			final PriorityComparator<? super Set<A>, ?> priorityComparator,
			final StatusListener statusListener) {
		return new ProverAxiomPinpointingEnumerationFactory<Q, A>() {

			@Override
			public <I extends AxiomPinpointingInference<?, ? extends A>> AxiomPinpointingEnumerator<Q, A> create(
					Prover<? super Q, ? extends I> prover,
					AxiomPinpointingInterruptMonitor monitor) {
				return new TopDownRepairComputation<Q, A, I>(prover, monitor,
						priorityComparator, statusListener);
			}
		};
	}

	/**
	 * Returns a factory for creating
	 * {@link AbstractProofAxiomPinpointingEnumerator} computations for
	 * enumerating repairs. The repairs are reported by the
	 * {@link AxiomPinpointingListener} in the order of increasing cardinality,
	 * that is, justifications that contain smaller number of axioms are
	 * reported first.
	 * 
	 * @param <C>
	 *                the type of conclusions used in inferences
	 * @param <A>
	 *                the type of axioms in repairs
	 * @return a {@link AbstractProofAxiomPinpointingEnumerator} that can
	 *         perform computations of repairs from proofs according to the
	 *         default parameters
	 */
	public static <C, A> ProverAxiomPinpointingEnumerationFactory<C, A> getFactory() {
		return getFactory(PriorityComparators.<A> cardinality(),
				new DummyStatusListener());
	}

	private final PriorityComparator<? super Set<A>, ?> priorityComparator_;

	private final StatusListener statusListener_;

	private TopDownRepairComputation(Prover<? super Q, ? extends I> prover,
			final AxiomPinpointingInterruptMonitor monitor,
			final PriorityComparator<? super Set<A>, ?> priorityComparator,
			StatusListener statusListener) {
		super(prover, monitor, statusListener);
		this.priorityComparator_ = priorityComparator;
		this.statusListener_ = statusListener;
	}

	@Override
	protected AbstractProofAxiomPinpointingEnumerator<Q, A, I>.QueryEnumerator getQueryEnumerator(
			Q query) {
		return new JobProcessor<>(query, priorityComparator_);
	}

	private class JobProcessor<P> extends
			AbstractProofAxiomPinpointingEnumerator<Q, A, I>.QueryEnumerator {

		final PriorityComparator<? super Set<A>, P> priorityComparator_;

		/**
		 * jobs to be processed
		 */
		final Queue<Job> toDoJobs_ = new PriorityQueue<Job>();

		/**
		 * Used to collect the result and prune jobs
		 */
		final Collection2<Set<A>> minimalRepairs_ = new BloomTrieCollection2<Set<A>>();

		/**
		 * Used to filter out redundant jobs
		 */
		final Collection2<Job> minimalJobs_ = new BloomTrieCollection2<Job>();

		JobProcessor(Q query,
				final PriorityComparator<? super Set<A>, P> priorityComparator) {
			super(query);
			this.priorityComparator_ = priorityComparator;
		}

		@Override
		public void enumerate(AxiomPinpointingListener<A> listener) {
			listener.computesRepairs();
			initialize();
			process(listener);
			listener.computationComplete();
		}

		void initialize() {
			produce(newJob(getQuery()));
		}

		void process(AxiomPinpointingListener<A> listener) {
			for (;;) {
				if (getInterruptMonitor().isInterrupted()) {
					break;
				}
				final Job job = toDoJobs_.poll();
				if (job == null) {
					break;
				}
				// else
				if (!minimalRepairs_.isMinimal(job.repair_)) {
					continue;
				}
				// else
				if (!minimalJobs_.isMinimal(job)) {
					continue;
				}
				// else
				minimalJobs_.add(job);
				final I nextToBreak = chooseToBreak(job.toBreak_);
				if (nextToBreak == null) {
					minimalRepairs_.add(job.repair_);
					if (listener != null) {
						listener.newRepairFound();
						for (A axiom : job.repair_) {
							listener.usefulAxiom(axiom);
						}
						listener.newRepairComplete();
					}
					continue;
				}
				for (Object premise : nextToBreak.getPremises()) {
					produce(doBreak(job.repair_, job.toBreak_, job.broken_,
							premise));
				}
				for (A axiom : nextToBreak.getJustification()) {
					produce(repair(job.repair_, job.toBreak_, job.broken_,
							axiom));
				}
			}
		}

		I chooseToBreak(final Collection<I> inferences) {
			// select the smallest conclusion according to the comparator
			I result = null;
			for (I inf : inferences) {
				if (result == null
						|| inferenceComparator.compare(inf, result) < 0) {
					result = inf;
				}
			}
			return result;
		}

		void produce(final Job job) {
			toDoJobs_.add(job);
			statusListener_.newPartialRepair();
		}

		Job newJob(final Object conclusion) {
			return doBreak(Collections.<A> emptySet(),
					Collections.<I> emptySet(), Collections.<Object> emptySet(),
					conclusion);
		}

		Job doBreak(final Set<A> repair, final Collection<I> toBreak,
				final Set<Object> broken, final Object conclusion) {

			final Set<A> newRepair = repair.isEmpty() ? new HashSet<A>(1)
					: new HashSet<A>(repair);
			final Set<I> newToBreak = toBreak.isEmpty() ? new HashSet<I>(3)
					: new HashSet<I>(toBreak.size());
			final Set<Object> newBroken = broken.isEmpty()
					? new HashSet<Object>(1)
					: new HashSet<Object>(broken);

			newBroken.add(conclusion);
			for (final I inf : toBreak) {
				if (!inf.getPremises().contains(conclusion)) {
					newToBreak.add(inf);
				}
			}
			infLoop: for (final I inf : getProof().getInferences(conclusion)) {
				for (final Object premise : inf.getPremises()) {
					if (broken.contains(premise)) {
						continue infLoop;
					}
				}
				for (final A axiom : inf.getJustification()) {
					if (repair.contains(axiom)) {
						continue infLoop;
					}
				}
				newToBreak.add(inf);
			}
			return new Job(newRepair, newToBreak, newBroken,
					priorityComparator_.getPriority(newRepair));
		}

		Job repair(final Set<A> repair, final Collection<I> toBreak,
				final Set<Object> broken, final A axiom) {

			final Set<A> newRepair = new HashSet<A>(repair);
			final Set<I> newToBreak = new HashSet<I>(toBreak.size());
			final Set<Object> newBroken = new HashSet<Object>(broken);

			newRepair.add(axiom);
			for (final I inf : toBreak) {
				if (!inf.getJustification().contains(axiom)) {
					newToBreak.add(inf);
				}
			}
			return new Job(newRepair, newToBreak, newBroken,
					priorityComparator_.getPriority(newRepair));
		}

		/**
		 * A simple state for computing a repair;
		 * 
		 * @author Peter Skocovsky
		 * @author Yevgeny Kazakov
		 */
		private class Job extends AbstractSet<JobMember<I, A>>
				implements Comparable<Job> {

			final Set<A> repair_;
			final Set<I> toBreak_;
			/**
			 * the cached set of conclusions not derivable without using
			 * {@link #repair_} and {@link #toBreak_}
			 */
			final Set<Object> broken_;
			final P priority_;

			Job(final Set<A> repair, final Set<I> toBreak,
					final Set<Object> broken, final P priority) {
				this.repair_ = repair;
				this.toBreak_ = toBreak;
				this.broken_ = broken;
				this.priority_ = priority;
			}

			@Override
			public boolean containsAll(final Collection<?> c) {
				if (c instanceof TopDownRepairComputation<?, ?, ?>.JobProcessor<?>.Job) {
					final TopDownRepairComputation<?, ?, ?>.JobProcessor<?>.Job other = (TopDownRepairComputation<?, ?, ?>.JobProcessor<?>.Job) c;
					return repair_.containsAll(other.repair_)
							&& toBreak_.containsAll(other.toBreak_);
				}
				// else
				return super.containsAll(c);
			}

			@Override
			public String toString() {
				return repair_.toString() + "; " + broken_.toString() + "; "
						+ toBreak_.toString();
			}

			@Override
			public Iterator<JobMember<I, A>> iterator() {
				Stream<JobMember<I, A>> jobMemberStream = Stream.concat(
						repair_.stream().map(axiom -> new Axiom<I, A>(axiom)),
						toBreak_.stream().map(inf -> new Inf<I, A>(inf))
				);
				return jobMemberStream.iterator();

/*
				return Iterators.<JobMember<I, A>> concat(Iterators.transform(
						repair_.iterator(), new Function<A, Axiom<I, A>>() {

							@Override
							public Axiom<I, A> apply(final A axiom) {
								return new Axiom<I, A>(axiom);
							}

						}), Iterators.transform(toBreak_.iterator(),
								new Function<I, Inf<I, A>>() {

									@Override
									public Inf<I, A> apply(I inf) {
										return new Inf<I, A>(inf);
									}

								}));

 */
			}

			@Override
			public int size() {
				return repair_.size() + toBreak_.size();
			}

			@Override
			public int compareTo(final Job other) {
				final int result = priorityComparator_.compare(priority_,
						other.priority_);
				if (result != 0) {
					return result;
				}
				// else
				return toBreak_.size() - other.toBreak_.size();
			}

		}

	}

	private final Comparator<AxiomPinpointingInference<?, ?>> inferenceComparator = new Comparator<AxiomPinpointingInference<?, ?>>() {

		@Override
		public int compare(final AxiomPinpointingInference<?, ?> inf1,
				final AxiomPinpointingInference<?, ?> inf2) {
			return inf1.getPremises().size() + inf1.getJustification().size()
					- inf2.getPremises().size()
					- inf2.getJustification().size();
		}

	};

	private interface JobMember<C, A> {

	}

	private final static class Inf<I extends Inference<?>, A>
			extends Delegator<I> implements JobMember<I, A> {

		public Inf(I delegate) {
			super(delegate);
		}

	}

	private final static class Axiom<C, A> extends Delegator<A>
			implements JobMember<C, A> {

		public Axiom(A delegate) {
			super(delegate);
		}

	}

	public interface StatusListener
			extends AbstractProofAxiomPinpointingEnumerator.StatusListener {

		void newPartialRepair();

	}

	public static class DummyStatusListener
			extends AbstractProofAxiomPinpointingEnumerator.DummyStatusListener
			implements StatusListener {

		@Override
		public void newPartialRepair() {
			// no-op
		}

	}

}
