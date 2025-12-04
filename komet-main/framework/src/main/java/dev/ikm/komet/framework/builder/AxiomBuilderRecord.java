/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.framework.builder;

import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.terms.EntityFacade;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import dev.ikm.tinkar.common.id.VertexId;
import dev.ikm.tinkar.common.id.VertexIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.component.Concept;
import dev.ikm.tinkar.component.graph.Vertex;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.TinkarTerm;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public record AxiomBuilderRecord(ConceptFacade axiomMeaning, MutableList<AxiomPropertyRecord> properties,
                                 MutableList<AxiomBuilderRecord> children, UUID vertexUuid, int vertexIndex,
                                 AtomicInteger nextAxiomIndex)
        implements AxiomPart, Vertex {
    public AxiomBuilderRecord(AtomicInteger nextAxiomIndex) {
        this(TinkarTerm.DEFINITION_ROOT, Lists.mutable.empty(), Lists.mutable.empty(), UUID.randomUUID(),
                nextAxiomIndex.getAndIncrement(), nextAxiomIndex);
    }

    @Override
    public VertexId vertexId() {
        return VertexIds.of(vertexUuid);
    }

    @Override
    public int vertexIndex() {
        return vertexIndex;
    }

    @Override
    public Concept meaning() {
        return axiomMeaning;
    }

    @Override
    public <T> Optional<T> property(Concept propertyConcept) {
        for (AxiomPropertyRecord propertyRecord : properties) {
            if (propertyRecord.propertyMeaning().nid() == PrimitiveData.nid(propertyConcept.publicId())) {
                return Optional.of((T) propertyRecord.propertyValue());
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Concept> propertyAsConcept(Concept propertyConcept) {
        Optional<?> optionalPropertyValue = property(propertyConcept);

        if (optionalPropertyValue.isEmpty()) {
            return Optional.empty();
        }
        Optional<Entity> optionalEntityValue = switch (optionalPropertyValue.get()) {
            case Integer nid -> EntityService.get().getEntity(nid);
            case EntityFacade facade -> EntityService.get().getEntity(facade);
            case null -> throw new IllegalStateException("optionalPropertyValue is null");
            default -> throw new IllegalStateException("optionalPropertyValue is not an identifier or facade: " + optionalPropertyValue.get());
        };
        if (optionalEntityValue.isEmpty()) {
            throw new IllegalStateException("Entity specified by property is not in database:: " + optionalPropertyValue.get());
        }
        if (optionalEntityValue.get() instanceof Concept conceptFacade) {
            return Optional.of(conceptFacade);
        }
        throw new IllegalStateException("Cannot convert property to concept. Property: " + optionalPropertyValue.get());
    }


    @Override
    public <T> T propertyFast(Concept propertyConcept) {
        for (AxiomPropertyRecord propertyRecord : properties) {
            if (propertyRecord.propertyMeaning().nid() == PrimitiveData.nid(propertyConcept.publicId())) {
                return (T) propertyRecord.propertyValue();
            }
        }
        return null;
    }

    @Override
    public <C extends Concept> RichIterable<C> propertyKeys() {
        MutableList<C> keys = Lists.mutable.empty();
        for (AxiomPropertyRecord propertyRecord : properties) {
            keys.add((C) propertyRecord.propertyMeaning());
        }
        return keys;
    }

    public AxiomBuilderRecord withAnd(AxiomBuilderRecord... andChildren) {
        MutableList<AxiomBuilderRecord> andChildrenList = Lists.mutable.of(andChildren);
        AxiomBuilderRecord and = new AxiomBuilderRecord(TinkarTerm.AND, Lists.mutable.empty(), andChildrenList,
                UUID.randomUUID(), nextAxiomIndex.getAndIncrement(), nextAxiomIndex);
        this.children.add(and);
        return and;
    }

    public AxiomBuilderRecord makeSome(ConceptFacade roleType, ConceptFacade roleRestriction) {
        AxiomPropertyRecord roleTypeRecord = new AxiomPropertyRecord(TinkarTerm.ROLE_TYPE, roleType);
        AxiomPropertyRecord roleOperatorRecord = new AxiomPropertyRecord(TinkarTerm.ROLE_OPERATOR, TinkarTerm.EXISTENTIAL_RESTRICTION);
        MutableList<AxiomPropertyRecord> properties = Lists.mutable.of(roleTypeRecord, roleOperatorRecord);
        AxiomBuilderRecord some = new AxiomBuilderRecord(TinkarTerm.ROLE, properties, Lists.mutable.empty(),
                UUID.randomUUID(), nextAxiomIndex.getAndIncrement(), nextAxiomIndex);
        some.children.add(makeConceptReference(roleRestriction));
        return some;
    }

    public AxiomBuilderRecord makeConceptReference(ConceptFacade referencedConcept) {
        AxiomBuilderRecord conceptReference = AxiomBuilderRecord.make(TinkarTerm.CONCEPT_REFERENCE, nextAxiomIndex,
                new AxiomPropertyRecord(TinkarTerm.CONCEPT_REFERENCE, referencedConcept));
        return conceptReference;
    }

    public static AxiomBuilderRecord make(ConceptFacade axiomMeaning, AtomicInteger nextAxiomIndex, AxiomPart... axiomParts) {
        MutableList<AxiomBuilderRecord> children = Lists.mutable.empty();
        MutableList<AxiomPropertyRecord> properties = Lists.mutable.empty();

        for (AxiomPart part : axiomParts) {
            switch (part) {
                case AxiomBuilderRecord axiomBuilderRecord -> children.add(axiomBuilderRecord);
                case AxiomPropertyRecord axiomPropertyRecord -> properties.add(axiomPropertyRecord);
            }
        }
        return new AxiomBuilderRecord(axiomMeaning, properties, children, UUID.randomUUID(),
                nextAxiomIndex.getAndIncrement(), nextAxiomIndex);
    }

    public void withNecessarySet(AxiomBuilderRecord... setElements) {
        withSet(TinkarTerm.NECESSARY_SET, setElements);
    }

    public void withSet(ConceptFacade setType, AxiomBuilderRecord... setElements) {
        AxiomBuilderRecord logicalSet = make(setType);
        children.add(logicalSet);

        MutableList<AxiomBuilderRecord> andChildrenList = Lists.mutable.of(setElements);
        AxiomBuilderRecord and = new AxiomBuilderRecord(TinkarTerm.AND, Lists.mutable.empty(), andChildrenList,
                UUID.randomUUID(), nextAxiomIndex.getAndIncrement(), nextAxiomIndex);
        logicalSet.children.add(and);
    }

    public AxiomBuilderRecord make(ConceptFacade axiomMeaning) {
        MutableList<AxiomBuilderRecord> children = Lists.mutable.empty();
        MutableList<AxiomPropertyRecord> properties = Lists.mutable.empty();
        return new AxiomBuilderRecord(axiomMeaning, properties, children, UUID.randomUUID(),
                nextAxiomIndex.getAndIncrement(), nextAxiomIndex);
    }

    public void withSufficientSet(AxiomBuilderRecord... setElements) {
        withSet(TinkarTerm.SUFFICIENT_SET, setElements);
    }

    public AxiomBuilderRecord makeRoleGroup(AxiomBuilderRecord... groupElements) {
        AxiomPropertyRecord roleTypeRecord = new AxiomPropertyRecord(TinkarTerm.ROLE_TYPE, TinkarTerm.ROLE_GROUP);
        AxiomPropertyRecord roleOperatorRecord = new AxiomPropertyRecord(TinkarTerm.ROLE_OPERATOR, TinkarTerm.EXISTENTIAL_RESTRICTION);
        MutableList<AxiomPropertyRecord> properties = Lists.mutable.of(roleTypeRecord, roleOperatorRecord);
        AxiomBuilderRecord group = new AxiomBuilderRecord(TinkarTerm.ROLE, properties, Lists.mutable.empty(),
                UUID.randomUUID(), nextAxiomIndex.getAndIncrement(), nextAxiomIndex);

        MutableList<AxiomBuilderRecord> andChildrenList = Lists.mutable.of(groupElements);
        AxiomBuilderRecord and = new AxiomBuilderRecord(TinkarTerm.AND, Lists.mutable.empty(), andChildrenList,
                UUID.randomUUID(), nextAxiomIndex.getAndIncrement(), nextAxiomIndex);
        group.children.add(and);
        return group;
    }
}
