package dev.ikm.komet.reasoner.expression;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.list.MutableList;
import dev.ikm.tinkar.component.graph.DiTree;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;

public class LogicalExpression {
    final DiTree<EntityVertex> sourceGraph;

    final ListIterable<LogicalAxiomAdaptor> adaptors;

    public LogicalExpression(DiTreeEntity sourceGraph, MutableList<LogicalAxiomAdaptor> adaptors) {
        this.sourceGraph = sourceGraph;
        this.adaptors = adaptors.clone();
    }

    public LogicalExpression(DiTreeEntity sourceGraph, ImmutableList<LogicalAxiomAdaptor> adaptors) {
        this.sourceGraph = sourceGraph;
        this.adaptors = adaptors;
    }

    public LogicalExpression(DiTree<EntityVertex> sourceGraph) {
        this.sourceGraph = sourceGraph;
        int vertexCount = sourceGraph.vertexMap().size();
        MutableList<LogicalAxiomAdaptor> mutableAdaptorList = Lists.mutable.ofInitialCapacity(vertexCount);
        this.adaptors = mutableAdaptorList;

        for (int i = 0; i < vertexCount; i++) {
            EntityVertex vertex = sourceGraph.vertex(i);
            if (vertex != null) {
                switch (LogicalAxiomSemantic.get(vertex.getMeaningNid())) {
                    case AND -> new LogicalAxiomAdaptor.AndAdaptor(this, i);
                    case CONCEPT -> new LogicalAxiomAdaptor.ConceptAxiomAdaptor(this, i);
                    case DEFINITION_ROOT -> new LogicalAxiomAdaptor.DefinitionRootAdaptor(this, i);
                    case DISJOINT_WITH -> new LogicalAxiomAdaptor.DisjointWithAxiomAdaptor(this, i);
                    case FEATURE -> new LogicalAxiomAdaptor.FeatureAxiomAdaptor(this, i);
                    case LITERAL_BOOLEAN -> new LogicalAxiomAdaptor.LiteralBooleanAdaptor(this, i);
                    case LITERAL_FLOAT -> new LogicalAxiomAdaptor.LiteralFloatAdaptor(this, i);
                    case LITERAL_INSTANT -> new LogicalAxiomAdaptor.LiteralInstantAdaptor(this, i);
                    case LITERAL_INTEGER -> new LogicalAxiomAdaptor.LiteralIntegerAdaptor(this, i);
                    case LITERAL_STRING -> new LogicalAxiomAdaptor.LiteralStringAdaptor(this, i);
                    case NECESSARY_SET -> new LogicalAxiomAdaptor.NecessarySetAdaptor(this, i);
                    case OR -> new LogicalAxiomAdaptor.OrAdaptor(this, i);
                    case PROPERTY_PATTERN_IMPLICATION -> new LogicalAxiomAdaptor.PropertyPatternImplicationAdaptor(this, i);
                    case PROPERTY_SET -> new LogicalAxiomAdaptor.PropertySetAdaptor(this, i);
                    case ROLE -> new LogicalAxiomAdaptor.RoleAxiomAdaptor(this, i);
                    case SUFFICIENT_SET -> new LogicalAxiomAdaptor.SufficientSetAdaptor(this, i);
                }
            } else {
                mutableAdaptorList.add(null);
            }
        }
    }

    public LogicalExpression build() {
        if (sourceGraph instanceof DiTreeEntity.Builder diTreeBuilder) {
            return new LogicalExpression(diTreeBuilder.build(), this.adaptors.toImmutable());
        }
        throw new IllegalStateException("sourceGraph not instanceof DiTreeEntity.Builder");
    }

    public LogicalAxiom.DefinitionRoot definitionRoot() {
        return (LogicalAxiom.DefinitionRoot) adaptors.get(sourceGraph.root().vertexIndex());
    }

    public boolean contains(LogicalAxiomSemantic axiomType) {
        for (LogicalAxiomAdaptor adaptor : adaptors) {
            if (axiomType.axiomClass.isAssignableFrom(adaptor.getClass())) {
                return true;
            }
        }
        return false;
    }

    public ImmutableList<LogicalAxiom> nodesOfType(LogicalAxiomSemantic axiomType) {
        MutableList<LogicalAxiom> axiomsOfType = Lists.mutable.ofInitialCapacity(8);
        for (LogicalAxiomAdaptor axiomAdaptor : adaptors) {
            if (axiomType.axiomClass.isAssignableFrom(axiomAdaptor.getClass())) {
                axiomsOfType.add(axiomAdaptor);
            }
        }
        return axiomsOfType.toImmutable();
    }

    public DiTree<EntityVertex> sourceGraph() {
        return sourceGraph;
    }
    
    @Override
    public String toString() {
        return sourceGraph.toString();
    }
}
