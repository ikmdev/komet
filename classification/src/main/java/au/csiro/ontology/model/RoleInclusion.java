/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com).
 * All rights reserved. Use is subject to license terms and conditions.
 */
package au.csiro.ontology.model;

import java.util.Arrays;


/**
 * This class represents a role inclusion axiom (also known as a SubObjectPropertyOf axiom in OWL).
 *
 * @author Alejandro Metke
 */

public class RoleInclusion extends Axiom {

    /**
     * The left hand side of the expression.
     */
    protected Role[] lhs;

    /**
     * The right hand side of the expression.
     */
    protected Role rhs;

    /**
     *
     */
    public RoleInclusion() {

    }

    /**
     * Creates a new {@link RoleInclusion}.
     *
     * @param lhs
     * @param rhs
     */
    public RoleInclusion(final Role[] lhs, final Role rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    /**
     * Creates a new {@link RoleInclusion}.
     *
     * @param lhs
     * @param rhs
     */
    public RoleInclusion(final Role lhs, final Role rhs) {
        this.lhs = new Role[]{lhs};
        this.rhs = rhs;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (lhs.length > 0) {
            sb.append(lhs[0]);
            for (int i = 1; i < lhs.length; i++) {
                sb.append(" o ");
                sb.append(lhs[i]);
            }
        } else {
            sb.append("\u03B5");
        }
        sb.append(" \u2291 ");
        sb.append(rhs);
        return sb.toString();
    }

    @Override
    public int compareTo(Axiom o) {
        if (o instanceof ConceptInclusion) {
            return 1;
        } else if (o instanceof RoleInclusion) {
            RoleInclusion otherRi = (RoleInclusion) o;
            int lhsRes = 0;
            Role[] oLhs = otherRi.getLhs();
            if (!Arrays.equals(lhs, oLhs)) {
                // If length is different put the shortest one first
                if (lhs.length < oLhs.length) {
                    lhsRes = -1;
                } else if (lhs.length > oLhs.length) {
                    lhsRes = 1;
                } else {
                    for (int i = 0; i < lhs.length; i++) {
                        int res = lhs[i].compareTo(oLhs[i]);
                        if (res < 0) {
                            lhsRes = -1;
                            break;
                        } else if (res > 0) {
                            lhsRes = 1;
                            break;
                        }
                    }
                }
            }
            int rhsRes = rhs.compareTo(otherRi.getRhs());
            if (lhsRes == 0 && rhsRes == 0)
                return 0;
            else if (lhsRes != 0)
                return lhsRes;
            else
                return rhsRes;
        } else {
            return -1;
        }
    }

    /**
     * @return the lhs
     */
    public Role[] getLhs() {
        return lhs;
    }

    /**
     * @param lhs the lhs to set
     */
    public void setLhs(Role[] lhs) {
        this.lhs = lhs;
    }

    /**
     * @return the rhs
     */
    public Role getRhs() {
        return rhs;
    }

    /**
     * @param rhs the rhs to set
     */
    public void setRhs(Role rhs) {
        this.rhs = rhs;
    }

}
