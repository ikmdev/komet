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
package dev.ikm.tinkar.coordinate.stamp.calculator;


import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.PathService;
import dev.ikm.tinkar.coordinate.stamp.StampBranchRecord;
import dev.ikm.tinkar.coordinate.stamp.StampPathImmutable;
import dev.ikm.tinkar.coordinate.stamp.StampPositionRecord;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;

import java.time.Instant;

public class PathProvider implements PathService {
    public ImmutableSet<StampBranchRecord> getPathBranches(int pathNid) {
        MutableSet<StampBranchRecord> branchSet = Sets.mutable.empty();
        EntityService.get().forEachSemanticOfPattern(TinkarTerm.PATH_ORIGINS_PATTERN.nid(), semanticEntity -> {
            // Referenced component = path for which this is an origin
            // Field 0 = path from which the origin is derived
            // Field 1 = instant of the origin
            // Get versions, get fieldValues.
            // TODO assumption 1... Only one version.
            if (semanticEntity.versions().size() == 1) {
                SemanticEntityVersion originVersion = semanticEntity.versions().get(0);
                ImmutableList<Object> fields = originVersion.fieldValues();
                ConceptFacade pathFromWhichOriginDerived = EntityProxy.Concept.make(((EntityFacade) fields.get(0)).nid());
                if (pathFromWhichOriginDerived.nid() == pathNid) {
                    Instant originTime = (Instant) fields.get(1);
                    StampBranchRecord stampBranchRecord = new StampBranchRecord(semanticEntity.referencedComponentNid(),
                            DateTimeUtil.instantToEpochMs(originTime));
                    branchSet.add(stampBranchRecord);
                }
            } else {
                throw new UnsupportedOperationException("Can't handle more than one version yet...");
            }
        });
        return branchSet.toImmutable();
    }

    @Override
    public ImmutableSet<StampPathImmutable> getPaths() {
        int[] pathsPatternSemanticNids = EntityService.get().semanticNidsOfPattern(TinkarTerm.PATHS_PATTERN.nid());
        MutableSet<StampPathImmutable> pathSet = Sets.mutable.ofInitialCapacity(pathsPatternSemanticNids.length);
        for (int pathsPatternSemanticNid : pathsPatternSemanticNids) {
            SemanticEntity semanticEntity = Entity.getFast(pathsPatternSemanticNid);
            int pathNid = semanticEntity.referencedComponentNid();
            pathSet.add(StampPathImmutable.make(pathNid, getPathOrigins(pathNid)));
        }
        return pathSet.toImmutable();
    }

    public ImmutableSet<StampPositionRecord> getPathOrigins(int pathNid) {
        MutableSet<StampPositionRecord> originSet = Sets.mutable.empty();
        EntityService.get().forEachSemanticForComponentOfPattern(pathNid, TinkarTerm.PATH_ORIGINS_PATTERN.nid(), semanticEntity -> {
            // Get versions, get fieldValues.
            // TODO assumption 1... Only one version.
            if (semanticEntity.versions().size() == 1) {
                SemanticEntityVersion originVersion = semanticEntity.versions().get(0);
                ImmutableList<Object> fields = originVersion.fieldValues();
                ConceptFacade pathConcept;
                if (fields.get(0) instanceof ConceptFacade conceptFacade) {
                    pathConcept = conceptFacade;
                } else if (fields.get(0) instanceof EntityFacade entityFacade) {
                    pathConcept = EntityProxy.Concept.make(entityFacade.nid());
                } else {
                    throw new IllegalStateException("Can't construct ConceptFacade from: " + fields.get(0));
                }
                originSet.add(StampPositionRecord.make((Instant) fields.get(1), pathConcept));
            } else {
                throw new UnsupportedOperationException("Can't handle more than one version yet...");
            }
        });

        if (originSet.isEmpty() && pathNid != TinkarTerm.PRIMORDIAL_PATH.nid()) {
            // A boot strap issue, only the primordial path should have no origins.
            // If terminology not completely loaded, content may not yet be ready.
            if (pathNid != TinkarTerm.SANDBOX_PATH.nid() && pathNid != TinkarTerm.MASTER_PATH.nid() && pathNid != TinkarTerm.DEVELOPMENT_PATH.nid()) {
                throw new IllegalStateException("Path with no origin: " + EntityService.get().getEntityFast(pathNid));
            }
            if (pathNid == TinkarTerm.DEVELOPMENT_PATH.nid()) {
                return Sets.immutable.with(StampPositionRecord.make(Long.MAX_VALUE, TinkarTerm.SANDBOX_PATH.nid()));
            }
            return Sets.immutable.with(StampPositionRecord.make(Long.MAX_VALUE, TinkarTerm.PRIMORDIAL_PATH.nid()));
        }
        return originSet.toImmutable();
    }

}
