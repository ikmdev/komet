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
package dev.ikm.tinkar.coordinate.stamp.change;

import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.service.NonExistentValue;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.entity.graph.isomorphic.IsomorphicResultsLeafHash;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.function.Function;

import static dev.ikm.tinkar.common.util.Symbols.HEAVY_TRIANGLE_HEADED_RIGHTWARDS_ARROW;

/**
 *
 * @param nid
 * @param changeRecords
 */
public record ChangeChronology(int nid, ImmutableList<VersionChangeRecord> changeRecords) {

    public String toString(ViewCalculator viewCalculator, boolean showPriorValue) {
        StringBuilder sb = new StringBuilder("Changes for ");
        Entity referencedEntity = EntityService.get().getEntityFast(nid);
        switch (referencedEntity) {
            case ConceptEntity conceptFacade -> sb.append("concept ");
            case PatternEntity patternFacade -> sb.append("pattern ");
            case SemanticEntity semantic -> sb.append(viewCalculator.getDescriptionTextOrNid(semantic.patternNid())).append(" semantic: ");
            case StampEntity stamp -> sb.append("stamp ");
            case Object object -> sb.append(object.getClass().getSimpleName());
        }
        sb.append(viewCalculator.getDescriptionTextOrNid(nid)).append("\n\n");
        for (VersionChangeRecord changeRecord: changeRecords) {
            StampEntity stampForChange = Entity.getStamp(changeRecord.stampNid());
            sb.append(DateTimeUtil.format(stampForChange.time())).append(" change by: ");
            sb.append(viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(stampForChange.authorNid()));
            for (FieldChangeRecord fieldChange: changeRecord.changes()) {
                sb.append("\n   ");
                Function<Object, String> formatFunction = value -> value.toString();
                if (fieldChange.currentValue().fieldDefinition(viewCalculator).meaningNid() == TinkarTerm.TIME_FOR_VERSION.nid()) {
                    formatFunction = value -> switch (value) {
                        case Long epochMs -> DateTimeUtil.format(epochMs);
                        case NonExistentValue nonExistentValue -> nonExistentValue.toString();
                        default -> value.toString();
                    };
                } else if (fieldChange.currentValue().fieldDefinition(viewCalculator).dataTypeNid() == TinkarTerm.CONCEPT_FIELD.nid() ||
                        fieldChange.currentValue().fieldDefinition(viewCalculator).dataTypeNid() == TinkarTerm.COMPONENT_FIELD.nid()) {
                    formatFunction = value -> switch (value) {
                        case ConceptFacade conceptFacade -> viewCalculator.getPreferredDescriptionTextOrNid(conceptFacade);
                        default -> value.toString();
                    };
                }
                sb.append(viewCalculator.getPreferredDescriptionStringOrNid(fieldChange.currentValue().fieldDefinition(viewCalculator).meaningNid())).append(": ");
                if (showPriorValue) {
                    sb.append(formatFunction.apply(fieldChange.priorValue().value())).append(" ").append(HEAVY_TRIANGLE_HEADED_RIGHTWARDS_ARROW).append(" ");
                }
                sb.append(formatFunction.apply(fieldChange.currentValue().value()));
                if (fieldChange.currentValue().value() instanceof DiTreeEntity currentTree &&
                    fieldChange.priorValue().value() instanceof DiTreeEntity priorTree) {
                    SemanticEntity theSemantic = (SemanticEntity) referencedEntity;
                    IsomorphicResultsLeafHash<?> isomorphicResult = new IsomorphicResultsLeafHash(currentTree, priorTree, theSemantic.referencedComponentNid());
                    try {
                        isomorphicResult.call();
                        sb.append("\n");
                        if (!isomorphicResult.getAdditionalVertexRoots().isEmpty())  {
                            sb.append("\nAdditions: \n\n");
                            isomorphicResult.getAdditionalVertexRoots().forEach((EntityVertex additionRoot) -> {
                                sb.append("  ").append(isomorphicResult.getReferenceTree().fragmentToString(additionRoot));
                                sb.append("\n");
                            });
                        }
                        if (!isomorphicResult.getDeletedVertexRoots().isEmpty()) {
                            sb.append("\nDeletions: \n\n");
                            isomorphicResult.getDeletedVertexRoots().forEach((EntityVertex deletionRoot) -> {
                                sb.append("  ").append(isomorphicResult.getComparisonTree().fragmentToString(deletionRoot));
                                sb.append("\n");
                            });
                        }
                    } catch (Exception e) {
                        AlertStreams.dispatchToRoot(e);
                    }
                }
            }
            sb.append("\n\n");
        }

        return sb.toString();
    }
}
