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
package dev.ikm.tinkar.entity;

import dev.ikm.tinkar.component.FieldDataType;
import dev.ikm.tinkar.component.SemanticChronology;
import dev.ikm.tinkar.terms.SemanticFacade;
import org.eclipse.collections.api.list.ImmutableList;

public interface SemanticEntity<T extends SemanticEntityVersion> extends Entity<T>,
        SemanticFacade, SemanticChronology<T> {

    @Override
    ImmutableList<T> versions();

    @Override
    default FieldDataType entityDataType() {
        return FieldDataType.SEMANTIC_CHRONOLOGY;
    }

    @Override
    default FieldDataType versionDataType() {
        return FieldDataType.SEMANTIC_VERSION;
    }

    @Override
    default Entity referencedComponent() {
        return EntityHandle.getEntityOrThrow(referencedComponentNid());
    }

    int referencedComponentNid();

    @Override
    default PatternEntity pattern() {
        return EntityHandle.getPatternOrThrow(patternNid());
    }

    int patternNid();

    default int topEnclosingComponentNid() {
        return topEnclosingComponent().nid();
    }

    default Entity<? extends EntityVersion> topEnclosingComponent() {
        Entity<? extends EntityVersion> referencedComponent = EntityHandle.getEntityOrThrow(referencedComponentNid());
        while (referencedComponent instanceof SemanticEntity parentSemantic) {
            referencedComponent = EntityHandle.getEntityOrThrow(parentSemantic.referencedComponentNid());
        }
        return referencedComponent;
    }
}
