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
package dev.ikm.tinkar.common.util.text;

import java.util.Comparator;

/**
 * Looking for more efficient implementation than AplphanumComparator which allocates memory,
 * and breaks strings into parts.
 * Solution found at: https://stackoverflow.com/questions/104599/sort-on-a-string-that-may-contain-a-number
 *
 * 
 */
public class NaturalOrder<T extends Object> implements Comparator<T> {

   private static class StringNaturalOrder implements Comparator<String> {

      @Override
      public int compare(String s1, String s2) {
         return compareStrings(s1, s2);
      }
   }

   private NaturalOrder() {
   }

   private static final Comparator<Object> objectNaturalOrder = new NaturalOrder<>();
   private static final Comparator<String> stringNaturalOrder = new StringNaturalOrder();

   public static final Comparator<Object> getObjectComparator() {
      return objectNaturalOrder;
   }

   public static final Comparator<String> getStringComparator() {
      return stringNaturalOrder;
   }

   /**
    * From stack overflow...
    * The implementation I propose here is simple and efficient. 
    * It does not allocate any extra memory, directly or indirectly 
    * by using regular expressions or methods such as substring(), 
    * split(), toCharArray(), etc.
    *
    * This implementation first goes across both strings to search for the 
    * first characters that are different, at maximal speed, without doing 
    * any special processing during this. Specific number comparison is 
    * triggered only when these characters are both digits. A side-effect 
    * of this implementation is that a digit is considered as greater than 
    * other letters, contrarily to default lexicographic order.
    * @param s1 first string
    * @param s2 second string
    * @return result of comparison
    */
   public static final int compareStrings(String s1, String s2) {
      // Skip all identical characters
      int  len1 = s1.length();
      int  len2 = s2.length();
      int  i;
      char c1, c2;

      for (i = 0, c1 = 0, c2 = 0; (i < len1) && (i < len2) && ((c1 = s1.charAt(i)) == (c2 = s2.charAt(i)) || Character.toLowerCase(s1.charAt(i)) == Character.toLowerCase(s2.charAt(i))); i++) {}

      if (c1 == '.' && c2 == ' ') {
         return -1;
      }
      if (c1 == ' ' && c2 == '.') {
         return 1;
      }


      // Check end of string
      if (Character.toLowerCase(c1) == Character.toLowerCase(c2)) {
         return (len1 - len2);
      }

      // Check digit in first string
      if (Character.isDigit(c1)) {
         // Check digit only in first string
         if (!Character.isDigit(c2)) {
            return (1);
         }

         // Scan all integer digits
         int x1, x2;

         for (x1 = i + 1; (x1 < len1) && Character.isDigit(s1.charAt(x1)); x1++) {}

         for (x2 = i + 1; (x2 < len2) && Character.isDigit(s2.charAt(x2)); x2++) {}

         // Longer integer wins, first digit otherwise
         return ((x2 == x1) ? c1 - c2
                            : x1 - x2);
      }

      // Check digit only in second string
      if (Character.isDigit(c2)) {
         return (-1);
      }

      // No digits
      return (Character.toLowerCase(c1) - Character.toLowerCase(c2));
   }
   @Override
   public int compare(Object o1, Object o2) {
      return compareStrings(o1.toString(), o2.toString());
   }
}

