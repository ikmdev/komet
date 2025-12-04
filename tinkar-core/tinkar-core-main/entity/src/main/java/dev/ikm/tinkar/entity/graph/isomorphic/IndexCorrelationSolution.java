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
package dev.ikm.tinkar.entity.graph.isomorphic;

import dev.ikm.tinkar.common.util.Symbols;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.impl.factory.primitive.IntLists;

import java.util.Objects;

public record IndexCorrelationSolution(int score, int hashcode, ImmutableIntList solution) implements Comparable<IndexCorrelationSolution> {


    public IndexCorrelationSolution(int[] correlation) {
        this(IntLists.immutable.of(correlation));
    }
    public IndexCorrelationSolution(ImmutableIntList solution) {
        this(IsomorphicResultsLeafHash.score(solution), solution);
    }
    private IndexCorrelationSolution(int score, ImmutableIntList solution) {
        this(score, 31 * Objects.hash(score) + solution.hashCode() , solution);
    }

    //~--- methods -------------------------------------------------------------

    /**
     * Compare to.
     *
     * @param o the o
     * @return the int
     */
    @Override
    public int compareTo(IndexCorrelationSolution o) {
        int comparison = Integer.compare(this.score, o.score);

        if (comparison != 0) {
            return comparison;
        }

        comparison = Integer.compare(this.hashcode, o.hashcode);

        if (comparison != 0) {
            return comparison;
        }

        return compare(this.solution, o.solution);
    }

    /**
     * Equals.
     *
     * @param o the o
     * @return true, if successful
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        final IndexCorrelationSolution that = (IndexCorrelationSolution) o;

        if (this.hashcode != that.hashcode) {
            return false;
        }

        if (this.score != that.score) {
            return false;
        }

        return this.solution.equals(that.solution);
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return this.hashcode;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("solution{").append("  s:");
        sb.append(this.score).append(", [");
        for (int i = 0; i < this.solution.size(); i++) {
            if (this.solution.get(i) == -1) {
                sb.append(i).append(":").append(Symbols.NULL_SIGN);
            } else {
                sb.append(i).append(":").append(this.solution.get(i));
            }
            if (i < this.solution.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]}");
        return sb.toString();
    }

    /**
     * Compare.
     *
     * @param o1 the o 1
     * @param o2 the o 2
     * @return the int
     */
    int compare(ImmutableIntList o1, ImmutableIntList o2) {
        for (int i = 0; i < o1.size(); i++) {
            if (o1.get(i) != o2.get(i)) {
                return (o1.get(i) < o2.get(i)) ? -1
                        : ((o1.get(i) == o2.get(i)) ? 0
                        : 1);
            }
        }

        return 0;
    }

    //~--- get methods ---------------------------------------------------------

    /**
     * Gets the solution.
     *
     * @return the solution
     */
    public ImmutableIntList getIntList() {
        return this.solution;
    }
}
