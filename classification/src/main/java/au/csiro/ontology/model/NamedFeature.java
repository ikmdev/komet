/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com).
 * All rights reserved. Use is subject to license terms and conditions.
 */
package au.csiro.ontology.model;


/**
 * This class represents a feature (also referred to as a data property in OWL).
 *
 * @author Alejandro Metke
 */

public class NamedFeature extends Feature {

    private static final long serialVersionUID = 1L;

    /**
     * String identifier of this concept.
     */
    private String id;

    /**
     *
     */
    public NamedFeature() {

    }

    /**
     * Creates a new Concept.
     *
     * @param id The concept's identifier.
     */
    public NamedFeature(String id) {
        assert (id != null);
        this.id = id;
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
        NamedFeature other = (NamedFeature) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public int compareTo(Feature o) {
        if (!(o instanceof NamedFeature)) {
            return -1;
        } else {
            return id.compareTo(((NamedFeature) o).getId());
        }
    }

    /**
     * Returns this concept's identifier.
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
