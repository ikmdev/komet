/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com).
 * All rights reserved. Use is subject to license terms and conditions.
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
