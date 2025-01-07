package dev.ikm.komet.layout.component.version.field;

import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpression;

/**
 * Represents a field that holds a logical expression as its value.
 *
 * This interface extends KlField parameterized with a LogicalExpression type.
 *
 * Logical expression is an adaptor class on top of an underlying Directed Tree.
 */
@RegularName( "Logical Expression Field")
@ParentConcept(KlDirectedTreeField.class)
public interface KlLogicalExpressionField extends KlField<LogicalExpression> {
}
