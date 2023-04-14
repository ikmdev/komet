/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com).
 * All rights reserved. Use is subject to license terms and conditions.
 */
package au.csiro.ontology.model;


/**
 * This class represents a concept inclusion axiom (also known as a SubClassOf axiom in OWL).
 *
 * @author Alejandro Metke
 */

public class ConceptInclusion extends Axiom {

    /**
     * The left hand side of the expression.
     */
    private Concept lhs;

    /**
     * The right hand side of the expression.
     */
    private Concept rhs;

    /**
     *
     */
    public ConceptInclusion() {

    }

    /**
     * Creates a new ConceptInclusion.
     *
     * @param lhs
     * @param rhs
     */
    public ConceptInclusion(final Concept lhs, final Concept rhs) {
        if (null == lhs) {
            throw new IllegalArgumentException("LHS cannot be null (RHS = " + rhs + ")");
        }
        this.lhs = lhs;
        if (null == rhs) {
            throw new IllegalArgumentException("RHS cannot be null (LHS = " + lhs + ")");
        }
        this.rhs = rhs;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((lhs == null) ? 0 : lhs.hashCode());
        result = prime * result + ((rhs == null) ? 0 : rhs.hashCode());
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
        ConceptInclusion other = (ConceptInclusion) obj;
        if (lhs == null) {
            if (other.lhs != null)
                return false;
        } else if (!lhs.equals(other.lhs))
            return false;
        if (rhs == null) {
            if (other.rhs != null)
                return false;
        } else if (!rhs.equals(other.rhs))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return lhs + " \u2291 " + rhs;
    }

    public int compareTo(Axiom o) {
        if (!(o instanceof ConceptInclusion)) {
            return -1;
        } else {
            ConceptInclusion otherCi = (ConceptInclusion) o;
            int lhsRes = lhs.compareTo(otherCi.getLhs());
            int rhsRes = rhs.compareTo(otherCi.getRhs());
            if (lhsRes == 0 && rhsRes == 0)
                return 0;
            else if (lhsRes != 0)
                return lhsRes;
            else
                return rhsRes;
        }
    }

    /**
     * @return the lhs
     */
    public Concept getLhs() {
        return lhs;
    }

    /**
     * @param lhs the lhs to set
     */
    public void setLhs(Concept lhs) {
        this.lhs = lhs;
    }

    /**
     * @return the rhs
     */
    public Concept getRhs() {
        return rhs;
    }

    /**
     * @param rhs the rhs to set
     */
    public void setRhs(Concept rhs) {
        this.rhs = rhs;
    }

}
