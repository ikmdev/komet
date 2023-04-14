/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com).
 * All rights reserved. Use is subject to license terms and conditions.
 */
package au.csiro.ontology.model;


/**
 * This class represents a long literal.
 *
 * @author Alejandro Metke
 */

public class LongLiteral extends Literal {

    private static final long serialVersionUID = 1L;

    private long value;

    /**
     *
     */
    public LongLiteral() {

    }

    public LongLiteral(long value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public long getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (value ^ (value >>> 32));
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
        LongLiteral other = (LongLiteral) obj;
        if (value != other.value)
            return false;
        return true;
    }

    public int compareTo(Literal o) {
        return ((Long) value).compareTo(((LongLiteral) o).value);
    }

}
