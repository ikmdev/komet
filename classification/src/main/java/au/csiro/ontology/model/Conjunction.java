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
package au.csiro.ontology.model;

import java.util.*;


/**
 * This class represents a conjunction (also referred to as an ObjectIntersectionOf in OWL).
 *
 * @author Alejandro Metke
 */

public class Conjunction extends Concept {

    private static final long serialVersionUID = 1L;

    private Concept[] concepts;

    private int hashCode;

    /**
     *
     */
    @SuppressWarnings("unchecked")
    public Conjunction() {
        this(Collections.EMPTY_SET);
    }

    public Conjunction(final Collection<? extends Concept> concepts) {
        // Store the concepts in hashCode order so that equals() is order
        // independent, i.e. conjunctions are reflexive (should also be
        // transitive, but Agile says STTCPW)

        final SortedSet<Concept> sorted = new TreeSet<>(concepts);
        this.concepts = sorted.toArray(new Concept[sorted.size()]);
        hashCode = sorted.hashCode();
    }

    /**
     * @param concepts
     */
    public Conjunction(final Concept[] concepts) {
        setConcepts(concepts);
    }

    public Concept[] getConcepts() {
        return concepts;
    }

    /**
     * @param concepts the concepts to set
     */
    public void setConcepts(Concept[] concepts) {
        if (concepts.length == 1) {
            this.concepts = concepts;
            this.hashCode = this.concepts[0].hashCode();
        } else {
            final SortedSet<Concept> sorted = new TreeSet<>();
            for (Concept concept : concepts) {
                sorted.add(concept);
            }
            this.concepts = sorted.toArray(new Concept[sorted.size()]);
            hashCode = sorted.hashCode();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        if (concepts.length > 0) {
            sb.append(concepts[0]);
            for (int i = 1; i < concepts.length; i++) {
                sb.append(" + ");
                sb.append(concepts[i]);
            }
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Conjunction other = (Conjunction) obj;
        return hashCode == other.hashCode
                && Arrays.equals(concepts, other.concepts);
    }

    @Override
    public int compareTo(Concept o) {
        Class<? extends Conjunction> thisClass = this.getClass();
        Class<? extends Concept> otherClass = o.getClass();
        if (thisClass.equals(otherClass)) {
            Conjunction other = (Conjunction) o;
            // Equal if all concepts equal
            // Otherwise order depends on the length and then on the order of
            // first different concept

            int res = 0;
            res = concepts.length - other.concepts.length;
            if (res != 0) return res;

            for (int i = 0; i < concepts.length; i++) {
                try {
                    res = concepts[i].compareTo(other.concepts[i]);
                } catch (ClassCastException e) {
                    // Need to catch this because elements in the conjunction
                    // might be of different types
                    res = concepts[i].getClass().toString().compareTo(
                            other.concepts[i].getClass().toString());
                }

                if (res != 0) return res;
            }

            return 0;
        } else {
            return thisClass.toString().compareTo(otherClass.toString());
        }
    }

}
