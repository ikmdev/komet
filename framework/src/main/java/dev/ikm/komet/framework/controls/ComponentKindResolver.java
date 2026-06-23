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
package dev.ikm.komet.framework.controls;

import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.coordinate.language.LanguageCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.terms.EntityFacade;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <em>Positively</em> determines the {@link ComponentKind} a component carries — the honest-typing
 * guardrail for the component badge (ike-issues#638): the kind is read from the store, never assumed,
 * so an unresolvable id becomes {@link ComponentKind#UNKNOWN} rather than silently a concept.
 *
 * <p>The four atoms map directly from the entity type ({@link ConceptEntity}, {@link PatternEntity},
 * {@link StampEntity}); a {@link SemanticEntity} is further split into {@link ComponentKind#DESCRIPTION}
 * when its pattern is one of the view <em>coordinate's</em> description patterns — asked of the
 * {@link ViewCalculator}, never a hardcoded {@code TinkarTerm.DESCRIPTION_PATTERN}, so configured
 * dialects and description types are respected — and {@link ComponentKind#SEMANTIC} otherwise.
 */
public final class ComponentKindResolver {

    private static final Logger LOG = LoggerFactory.getLogger(ComponentKindResolver.class);

    private ComponentKindResolver() {
    }

    /**
     * Determines the kind of the component referenced by {@code facade}.
     *
     * @param facade     the component to classify; {@code null} yields {@link ComponentKind#UNKNOWN}
     * @param calculator the view used to resolve description patterns; {@code null} treats every
     *                   semantic as a plain {@link ComponentKind#SEMANTIC}
     * @return the component's kind, never {@code null}
     */
    public static ComponentKind resolve(EntityFacade facade, ViewCalculator calculator) {
        return facade == null ? ComponentKind.UNKNOWN : resolve(facade.nid(), calculator);
    }

    /**
     * Determines the kind of the component with the given nid.
     *
     * @param nid        the component nid
     * @param calculator the view used to resolve description patterns; {@code null} treats every
     *                   semantic as a plain {@link ComponentKind#SEMANTIC}
     * @return the component's kind, never {@code null}; {@link ComponentKind#UNKNOWN} when the nid
     *         does not resolve to a known component
     */
    public static ComponentKind resolve(int nid, ViewCalculator calculator) {
        Entity<?> entity;
        try {
            entity = EntityHandle.get(nid).entity().orElse(null);
        } catch (RuntimeException e) {
            LOG.warn("Could not resolve a component for nid {}", nid, e);
            return ComponentKind.UNKNOWN;
        }
        if (entity == null) {
            return ComponentKind.UNKNOWN;
        }
        return switch (entity) {
            case ConceptEntity<?> _ -> ComponentKind.CONCEPT;
            case PatternEntity<?> _ -> ComponentKind.PATTERN;
            case StampEntity<?> _ -> ComponentKind.STAMP;
            case SemanticEntity<?> semantic -> isDescription(semantic.patternNid(), calculator)
                    ? ComponentKind.DESCRIPTION
                    : ComponentKind.SEMANTIC;
            default -> ComponentKind.UNKNOWN;
        };
    }

    /**
     * Whether {@code patternNid} is one of the view coordinate's description patterns.
     *
     * @param patternNid the semantic's pattern nid
     * @param calculator the view whose language coordinates list the description patterns
     * @return {@code true} if the pattern is a description pattern in any of the view's language
     *         coordinates
     */
    private static boolean isDescription(int patternNid, ViewCalculator calculator) {
        if (calculator == null) {
            return false;
        }
        ImmutableList<LanguageCoordinateRecord> coordinates =
                calculator.languageCalculator().languageCoordinateList();
        for (LanguageCoordinateRecord coordinate : coordinates) {
            IntIdList descriptionPatterns = coordinate.descriptionPatternPreferenceNidList();
            for (int i = 0; i < descriptionPatterns.size(); i++) {
                if (descriptionPatterns.get(i) == patternNid) {
                    return true;
                }
            }
        }
        return false;
    }
}
