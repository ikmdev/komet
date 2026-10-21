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
package dev.ikm.komet.kview.mvvm.view.changes;

/**
 * Two texts split at the words they share: the run of words both start with, what each has in
 * the middle, and the run of words both end with. The middles are what the review window
 * marks; an edit that only inserts or only removes words leaves one middle empty.
 *
 * @param prefix       the words both texts start with, with the separator that follows them
 * @param beforeMiddle the old text's own words
 * @param afterMiddle  the new text's own words
 * @param suffix       the words both texts end with, with the separator before them
 */
public record TextDiff(String prefix, String beforeMiddle, String afterMiddle, String suffix) {

    /**
     * Splits two texts at the words they share. The split falls on whitespace, never inside a
     * word, so a changed word is marked whole.
     */
    public static TextDiff of(String before, String after) {
        int prefix = commonPrefix(before, after);
        while (prefix > 0 && !(isBoundary(before, prefix) && isBoundary(after, prefix))) {
            prefix--;
        }
        int suffix = commonSuffix(before, after, prefix);
        while (suffix > 0 && !(isBoundary(before, before.length() - suffix)
                && isBoundary(after, after.length() - suffix))) {
            suffix--;
        }
        int beforeEnd = before.length() - suffix;
        int afterEnd = after.length() - suffix;
        return new TextDiff(before.substring(0, prefix),
                before.substring(prefix, beforeEnd),
                after.substring(prefix, afterEnd),
                before.substring(beforeEnd));
    }

    private static int commonPrefix(String a, String b) {
        int limit = Math.min(a.length(), b.length());
        int i = 0;
        while (i < limit && a.charAt(i) == b.charAt(i)) {
            i++;
        }
        return i;
    }

    /** The longest common suffix that does not reach into the first {@code prefix} characters. */
    private static int commonSuffix(String a, String b, int prefix) {
        int limit = Math.min(a.length(), b.length()) - prefix;
        int i = 0;
        while (i < limit && a.charAt(a.length() - 1 - i) == b.charAt(b.length() - 1 - i)) {
            i++;
        }
        return i;
    }

    /** Whether a split before the character at {@code index} falls between words, or at either end. */
    private static boolean isBoundary(String text, int index) {
        return index == 0 || index == text.length()
                || Character.isWhitespace(text.charAt(index - 1))
                || Character.isWhitespace(text.charAt(index));
    }
}
