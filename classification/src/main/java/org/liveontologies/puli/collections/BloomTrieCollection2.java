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
package org.liveontologies.puli.collections;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A {@link Collection2} that stores collections in a trie. The key for a
 * collection is a Bloom filter represented as a 64 bit (long) integer: every
 * element sets one bit to 1. Another Bloom filter (likewise represented as long
 * integer) is used to prevent subset tests. The iterators returned by
 * 
 * @author Yevgeny Kazakov
 *
 * @param <C>
 *            the type of collections maintained by this {@link Collection2}
 */
public class BloomTrieCollection2<C extends Collection<?>>
		extends AbstractCollection2<C> {

	private final static short FILTER_SHIFT_ = 6; // 2^6 = 64
	// enough
	private final static int FILTER_MASK_ = getMask(FILTER_SHIFT_);

	private final static long LONG_MASK_ = -1L; // all bits set to 1

	private Node<C> root_ = new LeafNode<C>();

	private int size_ = 0;

	/**
	 * @param bits
	 * @return 11..1 bits times
	 */
	private static int getMask(short bits) {
		return (1 << bits) - 1;
	}

	// used as a key in the trie
	private static long getFilter(Collection<?> s) {
		long result = 0;
		for (Object e : s) {
			// use low 12 bits of hash
			int hash = e.hashCode();
			int pos = hash & FILTER_MASK_;
			hash >>>= FILTER_SHIFT_;
			pos ^= hash & FILTER_MASK_;
			result |= 1L << pos;
		}
		return result;
	}

	// used to further prune subset test
	private static long getFilter2(Collection<?> s) {
		long result = 0;
		for (Object e : s) {
			// use low 13-24 bits of hash
			int hash = e.hashCode() >>> 12;
			int pos = hash & FILTER_MASK_;
			hash >>>= FILTER_SHIFT_;
			pos ^= hash & FILTER_MASK_;
			result |= 1L << pos;
		}
		return result;
	}

	@Override
	public boolean add(C s) {
		Node<C> newRoot = root_.add(s, LONG_MASK_, getFilter(s), getFilter2(s));
		if (newRoot != null) {
			root_ = newRoot;
		}
		size_++;
		return true;
	}

	@Override
	public boolean contains(Object o) {
		if (o instanceof Collection<?>) {
			Collection<?> s = (Collection<?>) o;
			return root_.contains(s, LONG_MASK_, getFilter(s), getFilter2(s));
		} else {
			return false;
		}
	}

	@Override
	public void clear() {
		root_ = new LeafNode<C>();
		size_ = 0;
	}

	@Override
	public boolean isMinimal(Collection<?> s) {
		return root_.isMinimal(s, LONG_MASK_, getFilter(s), getFilter2(s));
	}

	@Override
	public boolean isMaximal(Collection<?> s) {
		return root_.isMaximal(s, LONG_MASK_, getFilter(s), getFilter2(s));
	}

	@Override
	public Iterable<C> subCollectionsOf(final Collection<?> s) {
		return new Iterable<C>() {
			final Condition<C> subsetCondition = new Condition<C>() {

				@Override
				public boolean holds(C o) {
					return s.containsAll(o);
				}
			};
			final long filter = getFilter(s), filter2 = getFilter2(s);

			@Override
			public Iterator<C> iterator() {
				return new DelegatingIterator<C>(root_.subCollectionsOf(
						subsetCondition, LONG_MASK_, filter, filter2)) {

					@Override
					public void remove() {
						super.remove();
						size_--;
					}

				};
			}
		};
	}

	@Override
	public Iterable<C> superCollectionsOf(final Collection<?> s) {
		return new Iterable<C>() {
			final Condition<C> supersetCondition = new Condition<C>() {

				@Override
				public boolean holds(C o) {
					return o.containsAll(s);
				}
			};
			final long filter = getFilter(s), filter2 = getFilter2(s);

			@Override
			public Iterator<C> iterator() {
				return new DelegatingIterator<C>(root_.superCollectionsOf(
						supersetCondition, LONG_MASK_, filter, filter2)) {

					@Override
					public void remove() {
						super.remove();
						size_--;
					}

				};
			}

		};
	}

	@Override
	public Iterator<C> iterator() {
		return new DelegatingIterator<C>(root_.iterator(LONG_MASK_)) {

			@Override
			public void remove() {
				super.remove();
				size_--;
			}

		};
	}

	@Override
	public int size() {
		return size_;
	}

	interface Node<C extends Collection<?>> {

		/**
		 * Tries to add the given collection to this node if the node capacity
		 * permits, or otherwise creates a copy of the node with the larger
		 * capacity and adds the collection there
		 * 
		 * @param s
		 * @param mask
		 * @param fragment
		 * @param filter2
		 * @return {@code null} if the capacity of this node was sufficient or
		 *         the newly created node otherwise
		 */
		Node<C> add(C s, long mask, long fragment, long filter2);

		boolean contains(Collection<?> s, long mask, long fragment,
				long filter2);

		boolean isMinimal(Collection<?> s, long mask, long fragment,
				long filter2);

		boolean isMaximal(Collection<?> s, long mask, long fragment,
				long filter2);

		Iterator<C> iterator(long mask);

		Iterator<C> subCollectionsOf(Condition<? super C> subsetCondition,
				long mask, long fragment, long filter2);

		Iterator<C> superCollectionsOf(Condition<? super C> supersetCondition,
				long mask, long fragment, long filter2);

	}

	static class InternalNode<C extends Collection<?>> implements Node<C> {

		private final static short BUCKET_SHIFT_ = 6;

		private final static int BUCKET_MASK_ = getMask(BUCKET_SHIFT_);

		private final static Iterator<?> EMPTY_ITERATOR_ = Collections.EMPTY_LIST
				.iterator();

		private final Node<C>[] children_;

		@SuppressWarnings("unchecked")
		InternalNode(long mask) {
			if (mask == 0L) {
				throw new IllegalArgumentException();
			}
			children_ = new Node[(int) (BUCKET_MASK_ & mask) + 1];
		}

		@Override
		public Node<C> add(C s, long mask, long fragment, long filter2) {
			int pos = (int) (fragment & BUCKET_MASK_ & mask);
			mask >>>= BUCKET_SHIFT_;
			fragment >>>= BUCKET_SHIFT_;
			if (children_[pos] == null) {
				children_[pos] = new LeafNode<C>(mask);
			}
			Node<C> updated = children_[pos].add(s, mask, fragment, filter2);
			if (updated != null) {
				children_[pos] = updated;
			}
			return null;
		}

		@Override
		public boolean contains(Collection<?> s, long mask, long fragment,
				long filter2) {
			int pos = (int) (fragment & BUCKET_MASK_ & mask);
			mask >>>= BUCKET_SHIFT_;
			fragment >>>= BUCKET_SHIFT_;
			if (children_[pos] == null) {
				return false;
			}
			// else
			return children_[pos].contains(s, fragment, mask, filter2);
		}

		@Override
		public boolean isMinimal(Collection<?> s, long mask, long fragment,
				long filter2) {
			int fragmentMask = (int) (fragment & mask & BUCKET_MASK_);
			mask >>>= BUCKET_SHIFT_;
			fragment >>>= BUCKET_SHIFT_;
			int pos = 0;
			for (;;) {
				Node<C> child = children_[pos];
				if (child != null
						&& !child.isMinimal(s, mask, fragment, filter2)) {
					return false;
				}
				if (pos == fragmentMask) {
					// no subset is found
					return true;
				}
				pos |= ~fragmentMask;
				pos++;
				pos &= fragmentMask;
			}
		}

		@Override
		public boolean isMaximal(Collection<?> s, long mask, long fragment,
				long filter2) {
			int fragmentMask = (int) (fragment & mask & BUCKET_MASK_);
			int pos = (int) (mask & BUCKET_MASK_);
			mask >>>= BUCKET_SHIFT_;
			fragment >>>= BUCKET_SHIFT_;
			for (;;) {
				Node<C> child = children_[pos];
				if (child != null
						&& !child.isMaximal(s, mask, fragment, filter2)) {
					return false;
				}
				if (pos == fragmentMask) {
					// no superset is found
					return true;
				}
				pos &= ~fragmentMask;
				pos--;
				pos |= fragmentMask;
			}
		}

		@Override
		public Iterator<C> iterator(long mask) {
			return new BaseIterator(mask);
		}

		@Override
		public Iterator<C> subCollectionsOf(
				Condition<? super C> subsetCondition, long mask, long fragment,
				long filter2) {
			return new SubIterator(subsetCondition, mask, fragment, filter2);
		}

		@Override
		public Iterator<C> superCollectionsOf(
				Condition<? super C> supersetCondition, long mask,
				long fragment, long filter2) {
			return new SuperIterator(supersetCondition, mask, fragment,
					filter2);
		}

		// TODO: removal of elements by iterator should clear empty nodes
		class BaseIterator implements Iterator<C> {

			final long nextMask;
			int pos = 0;
			@SuppressWarnings("unchecked")
			Iterator<C> iter = (Iterator<C>) EMPTY_ITERATOR_;

			/**
			 * {@code true} if the last element returned by this iterator is the
			 * last element returned by {@link #iter}; this is needed to
			 * implement {@link #remove()}
			 */
			boolean iterInSync = true;

			BaseIterator(long mask) {
				this.nextMask = mask >>> BUCKET_SHIFT_;
			}

			void advancePos() {
				pos++;
			}

			boolean noMorePos() {
				return pos == children_.length;
			}

			Iterator<C> getChildIterator(Node<C> child) {
				return child.iterator(nextMask);
			}

			@Override
			public void remove() {
				if (iterInSync) {
					iter.remove();
				} else {
					throw new NoSuchElementException();
				}
			}

			@Override
			public boolean hasNext() {
				for (;;) {
					if (iter.hasNext()) {
						return true;
					}
					for (;;) {
						if (noMorePos()) {
							return false;
						}
						Node<C> child = children_[pos];
						advancePos();
						if (child == null) {
							continue;
						}
						// else
						iterInSync = false;
						iter = getChildIterator(child);
						break;
					}
				}
			}

			@Override
			public C next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				C result = iter.next();
				iterInSync = true;
				return result;
			}

		}

		class SubIterator extends BaseIterator {

			final Condition<? super C> subsetCondition;

			final long nextFragment, filter2;

			final int fragmentMask;

			boolean noMorePos = false;

			SubIterator(Condition<? super C> subsetCondition, long mask,
					long fragment, long filter2) {
				super(mask);
				this.subsetCondition = subsetCondition;
				this.nextFragment = fragment >>> BUCKET_SHIFT_;
				this.filter2 = filter2;
				this.fragmentMask = (int) (fragment & mask & BUCKET_MASK_);
			}

			@Override
			void advancePos() {
				if (pos == fragmentMask) {
					noMorePos = true;
					return;
				}
				pos |= ~fragmentMask;
				pos++;
				pos &= fragmentMask;
			}

			@Override
			boolean noMorePos() {
				return noMorePos;
			}

			@Override
			Iterator<C> getChildIterator(Node<C> child) {
				return child.subCollectionsOf(subsetCondition, nextMask,
						nextFragment, filter2);
			}

		}

		class SuperIterator extends BaseIterator {

			final Condition<? super C> supersetCondition;

			final long nextFragment, filter2;

			final int fragmentMask;

			boolean noMorePos = false;

			SuperIterator(Condition<? super C> supersetCondition, long mask,
					long fragment, long filter2) {
				super(mask);
				this.supersetCondition = supersetCondition;
				this.nextFragment = fragment >>> BUCKET_SHIFT_;
				this.filter2 = filter2;
				this.fragmentMask = (int) (fragment & mask & BUCKET_MASK_);
				pos = (int) (mask & BUCKET_MASK_);
			}

			@Override
			void advancePos() {
				if (pos == fragmentMask) {
					noMorePos = true;
					return;
				}
				// else
				pos &= ~fragmentMask;
				pos--;
				pos |= fragmentMask;
			}

			@Override
			boolean noMorePos() {
				return noMorePos;
			}

			@Override
			Iterator<C> getChildIterator(Node<C> child) {
				return child.superCollectionsOf(supersetCondition, nextMask,
						nextFragment, filter2);
			}

		}

	}

	static class LeafNode<C extends Collection<?>> implements Node<C> {

		private final static int INIT_CAPACITY_ = 8;

		/**
		 * the maximal number of element in a node after which it is split to an
		 * internal node, if possible
		 */
		private final static int SPLIT_CAPACITY_ = 64;

		private final Object[] collections_;

		private final long[] fragments_, // can be null
				filters2_;

		private int size_ = 0;

		LeafNode() {
			this(LONG_MASK_);
		}

		LeafNode(long mask) {
			this(mask, INIT_CAPACITY_);
		}

		LeafNode(long mask, int capacity) {
			collections_ = new Object[capacity];
			fragments_ = mask == 0L ? null : new long[capacity];
			filters2_ = new long[capacity];
		}

		LeafNode(LeafNode<C> from) {
			// resize
			int capacity = from.collections_.length << 1;
			size_ = from.size_;
			collections_ = new Object[capacity];
			System.arraycopy(from.collections_, 0, collections_, 0, size_);
			filters2_ = new long[capacity];
			System.arraycopy(from.filters2_, 0, filters2_, 0, size_);
			if (from.fragments_ != null) {
				fragments_ = new long[capacity];
				System.arraycopy(from.fragments_, 0, fragments_, 0, size_);
			} else {
				fragments_ = null;
			}
		}

		@SuppressWarnings("unchecked")
		C getCollection(int index) {
			return (C) collections_[index];
		}

		long getFragment(int index) {
			if (fragments_ == null) {
				return 0L;
			}
			// else
			return fragments_[index];
		}

		@Override
		public Node<C> add(C s, long mask, long fragment, long filter2) {
			if (size_ < collections_.length) {
				collections_[size_] = s;
				if (fragments_ != null) {
					fragments_[size_] = fragment;
				}
				filters2_[size_] = filter2;
				size_++;
				return null;
			}
			// else
			Node<C> replacement;
			if (mask == 0L || size_ < SPLIT_CAPACITY_) {
				replacement = new LeafNode<C>(this);
			} else {
				replacement = new InternalNode<C>(mask);
				for (int i = 0; i < collections_.length; i++) {
					replacement.add(getCollection(i), mask, getFragment(i),
							filters2_[i]);
				}
			}
			replacement.add(s, mask, fragment, filter2);
			return replacement;
		}

		void remove(int pos) {
			if (pos < 0 || pos >= size_) {
				throw new IndexOutOfBoundsException("Position " + pos
						+ " must be between 0 " + (size_ - 1));
			}
			size_--;
			collections_[pos] = collections_[size_];
			if (fragments_ != null) {
				fragments_[pos] = fragments_[size_];
			}
			filters2_[pos] = filters2_[size_];
		}

		@Override
		public boolean contains(Collection<?> s, long mask, long fragment,
				long filter2) {
			for (int i = 0; i < size_; i++) {
				if (filter2 == filters2_[i] && s.equals(collections_[i])) {
					return true;
				}
			}
			// else not found
			return false;
		}

		@Override
		public boolean isMinimal(Collection<?> s, long mask, long fragment,
				long filter2) {
			for (int i = 0; i < size_; i++) {
				if ((fragment | getFragment(i)) == fragment
						&& (filter2 | filters2_[i]) == filter2
						&& s.containsAll(getCollection(i))) {
					return false;
				}
			}
			// else no subset is found
			return true;
		}

		@Override
		public boolean isMaximal(Collection<?> s, long mask, long fragment,
				long filter2) {
			for (int i = 0; i < size_; i++) {
				if ((fragment & getFragment(i)) == fragment
						&& (filter2 & filters2_[i]) == filter2
						&& getCollection(i).containsAll(s)) {
					return false;
				}
			}
			// else no subset is found
			return true;
		}

		@Override
		public Iterator<C> iterator(long mask) {
			return new Iterator<C>() {
				int pos = 0;

				@Override
				public boolean hasNext() {
					return pos < size_;
				}

				@Override
				public C next() {
					if (!hasNext()) {
						throw new NoSuchElementException();
					}
					// else
					C result = getCollection(pos);
					pos++;
					return result;
				}

				@Override
				public void remove() {
					if (pos == 0) {
						throw new IllegalStateException();
					}
					pos--;
					LeafNode.this.remove(pos);
				}

			};
		}

		@Override
		public Iterator<C> subCollectionsOf(
				Condition<? super C> subsetCondition, long mask, long fragment,
				long filter2) {
			return new FilteredIterator<C>(iterator(mask), subsetCondition);
		}

		@Override
		public Iterator<C> superCollectionsOf(
				Condition<? super C> supersetCondition, long mask,
				long fragment, long filter2) {
			return new FilteredIterator<C>(iterator(mask), supersetCondition);
		}

	}

}
