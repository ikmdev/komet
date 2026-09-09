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
package dev.ikm.komet.kview.mvvm.view.genpurpose;

import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpressionBuilder;
import dev.ikm.tinkar.terms.TinkarTerm;

/**
 * The definitions a new stated-axiom semantic is seeded with — the classic concept window's
 * "Add Necessary Set" / "Add Sufficient Set" actions ({@code ConceptViewModel.createConcept}).
 * Pure: builds the tree and nothing else.
 */
public final class StatedDefinitionSeeds {

    private StatedDefinitionSeeds() {
    }

    /**
     * A definition holding one necessary or sufficient set whose only member is an is-a to
     * "Anonymous concept" — the placeholder chip the user then replaces in the inline axiom tree.
     *
     * @param necessary a necessary set when true, a sufficient set otherwise
     */
    public static DiTreeEntity seedDefinition(boolean necessary) {
        LogicalExpressionBuilder builder = new LogicalExpressionBuilder();
        if (necessary) {
            builder.NecessarySet(builder.And(builder.ConceptAxiom(TinkarTerm.ANONYMOUS_CONCEPT.nid())));
        } else {
            builder.SufficientSet(builder.And(builder.ConceptAxiom(TinkarTerm.ANONYMOUS_CONCEPT.nid())));
        }
        return switch (builder.build().sourceGraph()) {
            case DiTreeEntity tree -> tree;
            case DiTreeEntity.Builder treeBuilder -> treeBuilder.build();
            default -> throw new IllegalStateException("Unexpected source graph type");
        };
    }
}
