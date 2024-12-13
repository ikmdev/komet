package dev.ikm.komet.layout.component.version.field;

import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpression;

/**
 * Represents a field that holds a logical expression as its value.
 *
 * This interface extends KlField parameterized with a LogicalExpression type.
 *
 * Logical expression is an adaptor class on top of an underlying Directed Tree.
 */
public interface KlLogicalExpressionField extends KlField<LogicalExpression> {
}
