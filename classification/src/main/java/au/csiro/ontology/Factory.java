/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com).
 * All rights reserved. Use is subject to license terms and conditions.
 */
package au.csiro.ontology;

import au.csiro.ontology.model.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;

/**
 * Concrete implementation of {@link IFactory}.
 *
 * @author Alejandro Metke
 */
public class Factory {

    public static Concept createNamedConcept(String id) {
        return new NamedConcept(id);
    }

    public static Role createNamedRole(String id) {
        return new NamedRole(id);
    }

    public static Feature createNamedFeature(String id) {
        return new NamedFeature(id);
    }

    public static Concept createConjunction(Concept... concepts) {
        return new Conjunction(concepts);
    }

    public static Concept createExistential(Role role, Concept filler) {
        return new Existential(role, filler);
    }

    public static Concept createDatatype(Feature feature, Operator operator, Literal literal) {
        return new Datatype(feature, operator, literal);
    }

    public static Axiom createConceptInclusion(Concept lhs, Concept rhs) {
        return new ConceptInclusion(lhs, rhs);
    }

    public static Axiom createRoleInclusion(Role[] lhs, Role rhs) {
        return new RoleInclusion(lhs, rhs);
    }

    public static Literal createIntegerLiteral(int value) {
        return new IntegerLiteral(value);
    }

    public static Literal createFloatLiteral(float value) {
        return new FloatLiteral(value);
    }

    public static Literal createDecimalLiteral(double value) {
        return new DecimalLiteral(value);
    }

    public static Literal createDecimalLiteral(BigDecimal value) {
        return new DecimalLiteral(value);
    }

    public static Literal createBigIntegerLiteral(long value) {
        return new BigIntegerLiteral(value);
    }

    public static Literal createBigIntegerLiteral(BigInteger value) {
        return new BigIntegerLiteral(value);
    }

    public static Literal createDateLiteral(Calendar value) {
        return new DateLiteral(value);
    }

    public static Literal createStringLiteral(String value) {
        return new StringLiteral(value);
    }

    public static Literal createLongLiteral(long value) {
        return new LongLiteral(value);
    }

}
