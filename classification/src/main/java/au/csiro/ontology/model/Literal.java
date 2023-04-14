/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com).
 * All rights reserved. Use is subject to license terms and conditions.
 */
package au.csiro.ontology.model;

import java.io.Serializable;

/**
 * This interface represents a literal (also known as a Literal in OWL).
 * 
 * @author Alejandro Metke
 * 
 */
public abstract class Literal implements Comparable<Literal>, Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public abstract String toString();

    public abstract int hashCode();

    public abstract boolean equals(Object o);

}
