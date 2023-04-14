package dev.ikm.komet.reasoner.expression;

import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.TinkarTerm;

/**
 * Provide a standard means to connect implementation classes and terminology.
 */
public enum LogicalAxiomSemantic {
    AND(TinkarTerm.AND, LogicalAxiom.Atom.Connective.And.class),
    CONCEPT(TinkarTerm.CONCEPT_REFERENCE, LogicalAxiom.Atom.ConceptAxiom.class),
    DEFINITION_ROOT(TinkarTerm.DEFINITION_ROOT, LogicalAxiom.Atom.DefinitionRoot.class),
    NECESSARY_SET(TinkarTerm.NECESSARY_SET, LogicalAxiom.Atom.LogicalSet.NecessarySet.class),
    OR(TinkarTerm.OR, LogicalAxiom.Atom.Connective.Or.class),
    PROPERTY_PATTERN_IMPLICATION(TinkarTerm.PROPERTY_PATTERN_IMPLICATION, LogicalAxiom.Atom.PropertyPatternImplication.class),
    PROPERTY_SET(TinkarTerm.PROPERTY_SET, LogicalAxiom.Atom.LogicalSet.PropertySet.class),
    ROLE(TinkarTerm.ROLE_TYPE, LogicalAxiom.Atom.TypedAtom.Role.class),
    SUFFICIENT_SET(TinkarTerm.SUFFICIENT_SET, LogicalAxiom.Atom.LogicalSet.SufficientSet.class),
    DISJOINT_WITH(TinkarTerm.DISJOINT_WITH, LogicalAxiom.Atom.DisjointWithAxiom.class),
    FEATURE(TinkarTerm.FEATURE, LogicalAxiom.Atom.TypedAtom.Feature.class),
    LITERAL_BOOLEAN(TinkarTerm.BOOLEAN_LITERAL, LogicalAxiom.Atom.Literal.LiteralBoolean.class),
    LITERAL_FLOAT(TinkarTerm.FLOAT_LITERAL, LogicalAxiom.Atom.Literal.LiteralFloat.class),
    LITERAL_INSTANT(TinkarTerm.INSTANT_LITERAL, LogicalAxiom.Atom.Literal.LiteralInteger.class),
    LITERAL_INTEGER(TinkarTerm.INTEGER_LITERAL, LogicalAxiom.Atom.Literal.LiteralInteger.class),
    LITERAL_STRING(TinkarTerm.STRING_LITERAL, LogicalAxiom.Atom.Literal.LiteralString.class);

    public final int nid;
    public final Class<? extends LogicalAxiom> axiomClass;

    LogicalAxiomSemantic(ConceptFacade meaningFacade, Class<? extends LogicalAxiom> axiom) {
        this.nid = meaningFacade.nid();
        this.axiomClass = axiom;
    }

    public static LogicalAxiomSemantic get(ConceptFacade meaningFacade) {
        return get(meaningFacade.nid());
    }

    public static LogicalAxiomSemantic get(int meaningNid) {
        for (LogicalAxiomSemantic meaning : LogicalAxiomSemantic.values()) {
            if (meaning.nid == meaningNid) {
                return meaning;
            }
        }
        throw new IllegalStateException("No meaning for nid: " + meaningNid + " " + PrimitiveData.text(meaningNid));
    }

}
