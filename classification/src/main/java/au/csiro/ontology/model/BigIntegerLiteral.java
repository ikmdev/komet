/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com).
 * All rights reserved. Use is subject to license terms and conditions.
 */
package au.csiro.ontology.model;

import java.math.BigInteger;


/**
 * This class represents a double literal.
 *
 * @author Alejandro Metke
 */

public class BigIntegerLiteral extends Literal {

    private static final long serialVersionUID = 1L;

    private BigInteger value;

    /**
     *
     */
    public BigIntegerLiteral() {

    }

    /**
     * @param type
     * @param value
     */
    public BigIntegerLiteral(BigInteger value) {
        this.value = value;
    }

    /**
     * @param value
     */
    public BigIntegerLiteral(long value) {
        this.value = BigInteger.valueOf(value);
    }

    /**
     * @return the value
     */
    public BigInteger getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(BigInteger value) {
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
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        BigIntegerLiteral other = (BigIntegerLiteral) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    public int compareTo(Literal o) {
        return value.compareTo(((BigIntegerLiteral) o).value);
    }

}
