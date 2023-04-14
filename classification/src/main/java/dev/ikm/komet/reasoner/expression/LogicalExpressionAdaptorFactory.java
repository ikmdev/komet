package dev.ikm.komet.reasoner.expression;

import dev.ikm.tinkar.component.graph.Graph;
import dev.ikm.tinkar.component.graph.GraphAdaptorFactory;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;

public class LogicalExpressionAdaptorFactory implements GraphAdaptorFactory<LogicalExpression> {

    @Override
    public LogicalExpression adapt(Graph graph) {
        return new LogicalExpression((DiTreeEntity) graph);
    }


}
