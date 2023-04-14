/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com).
 * All rights reserved. Use is subject to license terms and conditions.
 */
package au.csiro.ontology.model;

import java.io.Serializable;

/**
 * Base interface for concepts.
 * 
 * @author Alejandro Metke
 *
 */
public abstract class Concept implements Comparable<Concept>, Serializable {
    
    private static final long serialVersionUID = 1L;

    public abstract String toString();

    public abstract int hashCode();

    public abstract boolean equals(Object o);
    
}
