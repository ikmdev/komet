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
package dev.ikm.komet.kview.mvvm.view.changes;

import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.KernelPattern;
import dev.ikm.tinkar.terms.State;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import java.util.Optional;

/**
 * One change in the unpublished-changes tray: a component with a version saved in an open
 * transaction, described for display, and the component it is grouped under.
 *
 * @param nid      the component's nid
 * @param entity   the component as read when the row was built
 * @param rootNid  the component the change is grouped under: the component itself for a concept
 *                 or a pattern, else the concept or pattern its semantic is ultimately on
 * @param type     what the component is
 * @param kind     what the unpublished version does to it
 * @param title    the component's description text on the journal's view
 * @param subtitle what it is, and for a semantic, which pattern it is an instance of
 */
public record UnpublishedChange(long nid, Entity<?> entity, long rootNid, Type type, Kind kind,
                                String title, String subtitle) implements UnpublishedRow {

    /** What the changed component is. */
    public enum Type {
        CONCEPT("Concept", "C"),
        PATTERN("Pattern", "P"),
        SEMANTIC("Semantic", "S");

        private final String label;
        private final String glyph;

        Type(String label, String glyph) {
            this.label = label;
            this.glyph = glyph;
        }

        public String label() {
            return label;
        }

        /** The one-letter mark shown in the row's type badge. */
        public String glyph() {
            return glyph;
        }

        /** The type of a component, by what it is; a missing component counts as a concept. */
        public static Type of(Entity<?> entity) {
            return switch (entity) {
                case PatternEntity<?> _ -> PATTERN;
                case SemanticEntity<?> _ -> SEMANTIC;
                case ConceptEntity<?> _ -> CONCEPT;
                case null, default -> CONCEPT;
            };
        }
    }

    /** What the unpublished version does to the component. */
    public enum Kind {
        /** The component has no published version yet. */
        NEW("New"),
        /** A published component with an active unpublished version. */
        EDITED("Edited"),
        /** A published component whose unpublished version is inactive. */
        INACTIVATED("Inactivated");

        private final String label;

        Kind(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    /**
     * Describes a component with an unpublished version.
     *
     * @param entity     a concept, pattern or semantic with at least one uncommitted version
     * @param calculator the view the description texts are read on
     * @return the row
     * @throws IllegalArgumentException if the entity is neither a concept, a pattern nor a semantic
     */
    public static UnpublishedChange of(Entity<?> entity, ViewCalculator calculator) {
        Type type = switch (entity) {
            case ConceptEntity<?> _ -> Type.CONCEPT;
            case PatternEntity<?> _ -> Type.PATTERN;
            case SemanticEntity<?> _ -> Type.SEMANTIC;
            default -> throw new IllegalArgumentException("Not a concept, pattern or semantic: " + entity);
        };
        String title = text(entity.nid(), "Unnamed " + type.label().toLowerCase(), calculator);
        String subtitle = switch (entity) {
            case SemanticEntity<?> semantic -> text(semantic.patternNid(), "Semantic", calculator);
            default -> type.label();
        };
        return new UnpublishedChange(entity.nid(), entity, rootComponentNid(entity), type, kindOf(entity), title, subtitle);
    }

    /**
     * The component a change is grouped under: a concept or pattern itself; for a semantic, the
     * component it is on, followed through semantics on semantics up to a concept or pattern.
     * A chain whose next component is absent, or longer than any real nesting, stops where it is.
     */
    static long rootComponentNid(Entity<?> entity) {
        Entity<?> component = entity;
        for (int hop = 0; component instanceof SemanticEntity<?> semantic && hop < 32; hop++) {
            Entity<?> referenced = EntityHandle.get(semantic.referencedComponentNid()).orNull();
            if (referenced == null) {
                break;
            }
            component = referenced;
        }
        return component.nid();
    }

    /**
     * A component's description text on the view, or the fallback while it has none: a concept
     * just created in a window has no description until its first name is submitted.
     * <p>
     * The descriptions are read from the store each time rather than through the calculator's
     * cache: that cache remembers a component's descriptions by the component's nid and is
     * invalidated only by writes to that nid, so a description added to a concept after the
     * concept was first looked up (the usual order in a new-concept window) never reaches it. A
     * description whose only version was discarded stays in the store with no versions and is
     * passed over, since the calculator refuses it ({@code No versions for: SemanticRecord}).
     */
    static String text(long nid, String fallback, ViewCalculator calculator) {
        MutableList<SemanticEntity> descriptions = Lists.mutable.empty();
        Entity.provider().forEachSemanticForComponentOfPattern(nid, KernelPattern.DESCRIPTION_PATTERN.nid(),
                description -> {
                    if (!description.versions().isEmpty()) {
                        descriptions.add(description);
                    }
                });
        if (descriptions.notEmpty()) {
            Latest<SemanticEntityVersion> preferred = calculator.getDescription(descriptions.toImmutable());
            if (preferred.isPresent()) {
                Optional<String> text = calculator.getTextFromSemanticVersion(preferred.get());
                if (text.isPresent() && !text.get().isBlank()) {
                    return text.get();
                }
            }
            // No description the view prefers (a draft may have no dialect yet): any description's text.
            for (SemanticEntity description : descriptions) {
                Latest<SemanticEntityVersion> latest = calculator.latest(description.nid());
                if (latest.isPresent()) {
                    Optional<String> text = calculator.getTextFromSemanticVersion(latest.get());
                    if (text.isPresent() && !text.get().isBlank()) {
                        return text.get();
                    }
                }
            }
        }
        // A semantic describes itself by its text; a component without a description, by the fallback.
        String text;
        try {
            text = calculator.getDescriptionTextOrNid(nid);
        } catch (IllegalStateException noVersions) {
            return fallback;
        }
        return text == null || text.isBlank() || text.equals(Long.toString(nid)) ? fallback : text;
    }

    /**
     * What the entity's uncommitted versions do to it: it is new when no version of it is
     * committed, inactivated when its last uncommitted version is inactive, edited otherwise.
     * Canceled versions are neither committed nor uncommitted and do not count.
     */
    static Kind kindOf(Entity<?> entity) {
        boolean anyCommitted = false;
        EntityVersion lastUncommitted = null;
        for (EntityVersion version : entity.versions()) {
            if (version.uncommitted()) {
                lastUncommitted = version;
            } else if (!version.canceled()) {
                anyCommitted = true;
            }
        }
        if (!anyCommitted) {
            return Kind.NEW;
        }
        if (lastUncommitted != null && lastUncommitted.state() == State.INACTIVE) {
            return Kind.INACTIVATED;
        }
        return Kind.EDITED;
    }
}
