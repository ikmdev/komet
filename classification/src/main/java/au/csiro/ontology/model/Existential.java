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


/**
 * This class represents an existential (also known as an ObjectSomeValuesFrom
 * in OWL).
 *
 * @author Alejandro Metke
 */

public class Existential extends Concept {

    private static final long serialVersionUID = 1L;

    private Role role;

    private Concept concept;

    /**
     *
     */
    public Existential() {

    }

    /**
     * @param role
     * @param concept
     */
    public Existential(Role role, Concept concept) {
        this.role = role;
        this.concept = concept;
    }

    @Override
    public String toString() {
        return role + "." + concept;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((concept == null) ? 0 : concept.hashCode());
        result = prime * result + ((role == null) ? 0 : role.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Existential other = (Existential) obj;
        if (concept == null) {
            if (other.concept != null)
                return false;
        } else if (!concept.equals(other.concept))
            return false;
        if (role == null) {
            if (other.role != null)
                return false;
        } else if (!role.equals(other.role))
            return false;
        return true;
    }

    public Role getRole() {
        return role;
    }

    /**
     * @param role the role to set
     */
    public void setRole(Role role) {
        this.role = role;
    }

    public Concept getConcept() {
        return concept;
    }

    /**
     * @param concept the concept to set
     */
    public void setConcept(Concept concept) {
        this.concept = concept;
    }

    @SuppressWarnings({"rawtypes"})
    public int compareTo(Concept o) {
        Class thisClass = this.getClass();
        Class otherClass = o.getClass();
        if (thisClass.equals(otherClass)) {
            Existential other = (Existential) o;
            int res = 0;
            res = role.compareTo(other.role);
            if (res != 0) return res;
            try {
                res = concept.compareTo(other.concept);
            } catch (ClassCastException e) {
                // Need to catch this because elements in the conjunction might
                // be of different types
                res = concept.getClass().toString().compareTo(
                        other.concept.getClass().toString());
            }
            if (res != 0) return res;
            return 0;
        } else {
            return thisClass.toString().compareTo(otherClass.toString());
        }
    }

}
