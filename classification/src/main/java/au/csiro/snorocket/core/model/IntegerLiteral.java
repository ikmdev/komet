/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com).
 * All rights reserved. Use is subject to license terms and conditions. 
 */
package au.csiro.snorocket.core.model;


/**
 * An integer literal.
 * 
 * @author Alejandro Metke
 * 
 */
public class IntegerLiteral extends AbstractLiteral {

    private final int value;

    /**
     * Constructor.
     * 
     * @param type
     */
    public IntegerLiteral(int value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public int getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + value;
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
        IntegerLiteral other = (IntegerLiteral) obj;
        if (value != other.value)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public int compareTo(AbstractLiteral o) {
        int thisVal = this.value;
        int anotherVal = ((IntegerLiteral) o).value;
        return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
    }

}
