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
package dev.ikm.tinkar.collection;

import dev.ikm.tinkar.common.util.ArrayUtil;

import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.function.LongConsumer;
import java.util.function.LongUnaryOperator;
import java.util.function.Supplier;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

/**
 *
 *
 */
public class SpinedIntLongMap {
  private static final int DEFAULT_SPINE_SIZE = 1024;
   private final int spineSize;
   private final ConcurrentMap<Integer, AtomicLongArray> spines = new ConcurrentHashMap<>();
   private final long INITIAL_VALUE = Long.MAX_VALUE;

   public SpinedIntLongMap() {
      this.spineSize = DEFAULT_SPINE_SIZE;
   }

   public int sizeInBytes() {
      int sizeInBytes = 0;
      sizeInBytes = sizeInBytes + ((spineSize * 8) * spines.size()); // 8 bytes = bytes of 64 bit integer
      return sizeInBytes;
   }

   private int getSpineCount() {
      int spineCount = 0;
      for (Integer spineKey:  spines.keySet()) {
         spineCount = Math.max(spineCount, spineKey + 1);
      }
      return spineCount; 
   }
   
   
   private AtomicLongArray newSpine(Integer spineKey) {
      return new AtomicLongArray(ArrayUtil.createAndFill(spineSize, INITIAL_VALUE));
   }

   public void put(int index, long element) {
       if (index < 0) {
           index = Integer.MAX_VALUE + index;
       }
      int spineIndex = index/spineSize;
      int indexInSpine = index % spineSize;
      this.spines.computeIfAbsent(spineIndex, this::newSpine).set(indexInSpine, element);
   }

   public long get(int index) {
       if (index < 0) {
           index = Integer.MAX_VALUE + index;
       }
      int spineIndex = index/spineSize;
      int indexInSpine = index % spineSize;
      return this.spines.computeIfAbsent(spineIndex, this::newSpine).get(indexInSpine);
   }

   public boolean containsKey(int index) {
       if (index < 0) {
           index = Integer.MAX_VALUE + index;
       }
      int spineIndex = index/spineSize;
      int indexInSpine = index % spineSize;
      return this.spines.computeIfAbsent(spineIndex, this::newSpine).get(indexInSpine) != INITIAL_VALUE;
   }
   
   public long getAndUpdate(int index, LongUnaryOperator generator) {
       if (index < 0) {
           index = Integer.MAX_VALUE + index;
       }
      int spineIndex = index/spineSize;
      int indexInSpine = index % spineSize;
      return this.spines.computeIfAbsent(spineIndex, this::newSpine).updateAndGet(indexInSpine, generator);
   }
   
   public void forEach(Processor processor) {
      int currentSpineCount = getSpineCount();
      int key = 0;
      for (int spineIndex = 0; spineIndex < currentSpineCount; spineIndex++) {
         AtomicLongArray spine = this.spines.computeIfAbsent(spineIndex, this::newSpine);
         for (int indexInSpine = 0; indexInSpine < spineSize; indexInSpine++) {
            long value = spine.get(indexInSpine);
            if (value != INITIAL_VALUE) {
               processor.process(key, value);
            }
         }
         key++;
      }
   } 
   
   public interface Processor {
      public void process(int key, long value);
   }   

   public LongStream stream() {
      final Supplier<? extends Spliterator.OfLong> streamSupplier = this.get();

      return StreamSupport.longStream(streamSupplier, streamSupplier.get()
              .characteristics(), false);
   }

   /**
    * Gets the value spliterator supplier.
    *
    * @return the {@code Supplier<? extends Spliterator.OfLong}}
    */
   protected Supplier<? extends Spliterator.OfLong> get() {
      return new SpliteratorSupplier();
   }

   /**
    * The Class SpliteratorSupplier.
    */
   private class SpliteratorSupplier
           implements Supplier<Spliterator.OfLong> {

      /**
       * Gets the.
       *
       * @return the spliterator of long
       */
      @Override
      public Spliterator.OfLong get() {
         return new SpinedValueSpliterator();
      }
   }

   private class SpinedValueSpliterator implements Spliterator.OfLong {
      int end;
      int currentPosition;

      public SpinedValueSpliterator() {
          this.end = DEFAULT_SPINE_SIZE * getSpineCount();
        this.currentPosition = 0;
      }
      
      public SpinedValueSpliterator(int start, int end) {
         this.currentPosition = start;
         this.end = end;
      }
      
      @Override
      public Spliterator.OfLong trySplit() {
         int splitEnd = end;
         int split = end - currentPosition;
         int half = split / 2;
         this.end = currentPosition + half;
         return new SpinedValueSpliterator(currentPosition + half + 1, splitEnd);
      }

      @Override
      public boolean tryAdvance(LongConsumer action) {
         while (currentPosition < end) {
            long value = get(currentPosition++);
            if (value != INITIAL_VALUE) {
               action.accept(value);
               return true;
            }
         }
         return false;
      }

      @Override
      public long estimateSize() {
         return end - currentPosition;
      }

      @Override
      public int characteristics() {
          return Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED
                | Spliterator.SIZED;
     }

   }
}
