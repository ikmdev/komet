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
 * This class represents a role (also referred to as an object property in OWL).
 *
 * @author Alejandro Metke
 */

public class NamedRole extends Role {

    private static final long serialVersionUID = 1L;

    /**
     * String identifier of this concept.
     */
    protected String id;

    /**
     *
     */
    public NamedRole() {

    }

    /**
     * Creates a new Role.
     *
     * @param id The role's identifier.
     */
    public NamedRole(String id) {
        assert (id != null);
        this.id = id;
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        NamedRole other = (NamedRole) obj;
        if (id == null) {
            if (other.id != null)
                return false;
            else
                assert (false);
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    public int compareTo(Role o) {
        if (!(o instanceof NamedRole)) {
            return -1;
        } else {
            return id.compareTo(((NamedRole) o).getId());
        }
    }

    /**
     * Returns this role's identifier.
     *
     * @return The identifier.
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

}
