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
package dev.ikm.komet.kview.mvvm.view.pattern;

import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import org.eclipse.collections.api.list.ImmutableList;

import java.time.Instant;

import static dev.ikm.tinkar.terms.KernelPattern.*;

/**
 * The one-line text a pattern's instance (a semantic) is listed under in an Instances list.
 * <p>
 * A semantic rarely has a description of its own, so for the well-known patterns the text is
 * built from the semantic's fields and its referenced component ("Stated definition for: …",
 * "US dialect Preferred: …"); any other entity is listed under its preferred description.
 * Shared by the classic Pattern window's Instances bumpout and the KL Pattern window's
 * INSTANCES tab.
 */
public final class PatternInstanceDescriptionHelper {

    private PatternInstanceDescriptionHelper() {
    }

    /**
     * @param calculator the view the descriptions are read through
     * @param entity     the instance to describe
     */
    public static String titleFor(ViewCalculator calculator, Entity<?> entity) {
        if (!(entity instanceof SemanticEntity<?> semanticEntity)) {
            return calculator.getPreferredDescriptionTextWithFallbackOrNid(entity.nid());
        }
        long patternNid = semanticEntity.patternNid();
        if (patternNid == IDENTIFIER_PATTERN.nid()) {
            //TODO Move better string descriptions to language calculator
            ImmutableList<Object> fields = latestFields(calculator, semanticEntity);
            return calculator.getPreferredDescriptionTextWithFallbackOrNid((EntityFacade) fields.get(0))
                    + ": " + fields.get(1);
        }
        if (patternNid == Entity.nidForPattern(EL_PLUS_PLUS_INFERRED_AXIOMS_PATTERN.publicId())) {
            return "Inferred definition for: " + referencedComponentText(calculator, semanticEntity);
        }
        if (patternNid == Entity.nidForPattern(INFERRED_NAVIGATION_PATTERN.publicId())) {
            return "Inferred is-a relationships for: " + referencedComponentText(calculator, semanticEntity);
        }
        if (patternNid == Entity.nidForPattern(PATHS_PATTERN.publicId())) {
            return referencedComponentText(calculator, semanticEntity);
        }
        if (patternNid == Entity.nidForPattern(EL_PLUS_PLUS_STATED_AXIOMS_PATTERN.publicId())) {
            return "Stated definition for: " + referencedComponentText(calculator, semanticEntity);
        }
        if (patternNid == Entity.nidForPattern(STATED_NAVIGATION_PATTERN.publicId())) {
            return "Stated is-a relationships for: " + referencedComponentText(calculator, semanticEntity);
        }
        if (patternNid == Entity.nidForPattern(GB_DIALECT_PATTERN.publicId())) {
            ImmutableList<Object> fields = latestFields(calculator, semanticEntity);
            return "UK dialect " + calculator.getPreferredDescriptionTextWithFallbackOrNid((EntityFacade) fields.get(0))
                    + ": " + referencedComponentText(calculator, semanticEntity);
        }
        if (patternNid == Entity.nidForPattern(US_DIALECT_PATTERN.publicId())) {
            ImmutableList<Object> fields = latestFields(calculator, semanticEntity);
            return "US dialect " + calculator.getPreferredDescriptionTextWithFallbackOrNid((EntityFacade) fields.get(0))
                    + ": " + referencedComponentText(calculator, semanticEntity);
        }
        if (patternNid == Entity.nidForPattern(PATH_ORIGINS_PATTERN.publicId())) {
            ImmutableList<Object> fields = latestFields(calculator, semanticEntity);
            return referencedComponentText(calculator, semanticEntity)
                    + " origin: " + DateTimeUtil.format((Instant) fields.get(1))
                    + " on " + calculator.getPreferredDescriptionTextWithFallbackOrNid((EntityFacade) fields.get(0));
        }
        return calculator.getPreferredDescriptionTextWithFallbackOrNid(entity.nid());
    }

    private static String referencedComponentText(ViewCalculator calculator, SemanticEntity<?> semanticEntity) {
        return calculator.getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid());
    }

    @SuppressWarnings("unchecked")
    private static ImmutableList<Object> latestFields(ViewCalculator calculator, SemanticEntity<?> semanticEntity) {
        Latest<? extends SemanticEntityVersion> latest = calculator.latest((SemanticEntity<SemanticEntityVersion>) semanticEntity);
        return (ImmutableList<Object>) latest.get().fieldValues();
    }
}
