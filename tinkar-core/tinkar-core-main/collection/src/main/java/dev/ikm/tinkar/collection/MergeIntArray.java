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

import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntSets;

import java.util.Arrays;

/**
 *
 * 
 */
public class MergeIntArray {

   public static int[] merge(int[] currentArray, int[] updateArray) {
      if (currentArray == null || currentArray.length == 0) {
         return updateArray;
      }
      if (updateArray == null) {
         throw new IllegalStateException("Update value is null");
      }
      if (updateArray.length == 1) {
         int updateValue = updateArray[0];
         int searchResult = Arrays.binarySearch(currentArray, updateValue);
         if (searchResult >= 0) {
            return currentArray; // already there. 
         }
         int[] array2 = new int[currentArray.length + 1];
         int insertIndex = -searchResult - 1;
         System.arraycopy(currentArray, 0, array2, 0, insertIndex);
         System.arraycopy(currentArray, insertIndex, array2, insertIndex + 1, currentArray.length - insertIndex);
         array2[insertIndex] = updateValue;
         return array2;
      }
      MutableIntSet mergedSet = IntSets.mutable.of(currentArray);
      mergedSet.addAll(updateArray);

      return mergedSet.toSortedArray();
   }

}
