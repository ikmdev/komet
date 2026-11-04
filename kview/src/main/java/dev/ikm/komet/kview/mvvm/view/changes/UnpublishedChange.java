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

import dev.ikm.komet.framework.controls.KonceptKindResolver;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.Field;
import dev.ikm.tinkar.entity.FieldDefinitionForEntity;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.ComponentWithNid;
import dev.ikm.tinkar.terms.KernelConcept;
import dev.ikm.tinkar.terms.KernelPattern;
import dev.ikm.tinkar.terms.State;
import network.ike.docs.konceptcore.KonceptKind;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * One change in the unpublished-changes tray: a component with a version saved in an open
 * transaction, described for display, and the component it is grouped under.
 *
 * @param nid      the component's nid
 * @param entity   the component as read when the row was built
 * @param rootNid  the component the change is on: the component itself for a concept or a
 *                 pattern, else the component its semantic is on, which may be a semantic in
 *                 turn. When that component is a listed change too, this change is nested under
 *                 it ({@link UnpublishedGroup#groupsOf}): a description under its concept, a
 *                 dialect under its description
 * @param type     what the component is
 * @param kind     what the unpublished version does to it
 * @param title    the component's description text on the journal's view; for a dialect, its
 *                 acceptability and dialect; for a component without text, what it is instead
 *                 (see {@code subtitle})
 * @param subtitle what it is: for a description, its type and language; for a dialect, the
 *                 description it is on, with that description's type; for another semantic,
 *                 which pattern it is an instance of. Null when the title already says what the component is, because it has no
 *                 text of its own. A membership semantic never has text; a concept just created
 *                 in a window has none until its first name is submitted. Such a row shows the
 *                 one line, muted, rather than a placeholder name that draws the eye without
 *                 telling anything
 * @param children the listed changes on this component, nested under its row in the tray; the
 *                 dialects of a description. Empty until {@link UnpublishedGroup#groupsOf} nests
 *                 them
 * @param namesItsComponent whether the subtitle only says which component this change is on, as
 *                 a dialect's does
 * @param underItsComponent whether this row is nested under the row of the component it is on
 */
public record UnpublishedChange(long nid, Entity<?> entity, long rootNid, Type type, Kind kind,
                                String title, String subtitle, List<UnpublishedChange> children,
                                boolean namesItsComponent, boolean underItsComponent)
        implements UnpublishedRow {

    public UnpublishedChange {
        children = List.copyOf(children);
    }

    /** True when the component has no text of its own and the title says what it is instead. */
    public boolean untitled() {
        return subtitle == null;
    }

    /**
     * Whether the row shows its subtitle. A row nested under the component its subtitle names has
     * that component right above it, so the line would only repeat it; the review window, which
     * lists changes flat, still shows it.
     */
    public boolean showsSubtitle() {
        return subtitle != null && !(namesItsComponent && underItsComponent);
    }

    /** This change with the given changes nested under it. */
    UnpublishedChange withChildren(List<UnpublishedChange> children) {
        return new UnpublishedChange(nid, entity, rootNid, type, kind, title, subtitle, children,
                namesItsComponent, underItsComponent);
    }

    /** This change as a row under the row of the component it is on. */
    UnpublishedChange nestedUnderItsComponent() {
        return new UnpublishedChange(nid, entity, rootNid, type, kind, title, subtitle, children, namesItsComponent, true);
    }

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
        Lines lines = switch (entity) {
            case SemanticEntity<?> semantic -> describe(semantic, calculator);
            default -> {
                String text = text(entity.nid(), null, calculator);
                yield text == null ? new Lines(type.label(), null) : new Lines(text, type.label());
            }
        };
        return new UnpublishedChange(entity.nid(), entity, rootComponentNid(entity), type, kindOf(entity),
                lines.title(), lines.subtitle(), List.of(), lines.namesItsComponent(), false);
    }

    /** A row's title over its subtitle; no subtitle when the title only says what the component is. */
    private record Lines(String title, String subtitle, boolean namesItsComponent) {

        Lines(String title, String subtitle) {
            this(title, subtitle, false);
        }
    }

    /**
     * Describes a semantic by what its pattern makes it. A dialect, a semantic with a field whose
     * purpose is description acceptability: its acceptability and dialect over the description it
     * is on, so a dialect listed without its description still says which text it extends. A
     * description, by the view's description patterns: its text over its type and language. Any
     * other semantic: its own text over its pattern's name, or the pattern's name alone when it
     * has no text.
     */
    private static Lines describe(SemanticEntity<?> semantic, ViewCalculator calculator) {
        long patternNid = semantic.patternNid();
        String patternText = text(patternNid, "Semantic", calculator);
        Latest<SemanticEntityVersion> version = versionOf(semantic, calculator);
        if (version.isPresent() && calculator.latest(patternNid).isPresent()) {
            Latest<Field<Object>> acceptability = calculator.getFieldForSemanticWithPurpose(
                    version, KernelConcept.DESCRIPTION_ACCEPTABILITY);
            if (acceptability.isPresent()) {
                return dialectLines(semantic, acceptability.get(), patternText, calculator);
            }
            DescriptionFields description = descriptionFields(semantic, calculator);
            if (description != null && description.text() != null) {
                String what = joined(description.type(), description.language());
                return new Lines(description.text(), what == null ? patternText : what);
            }
        }
        String text = text(semantic.nid(), null, calculator);
        return text == null ? new Lines(patternText, null) : new Lines(text, patternText);
    }

    /**
     * A dialect's two lines. The dialect's name is the meaning of the acceptability field, which
     * says which dialect the pattern records.
     */
    private static Lines dialectLines(SemanticEntity<?> semantic, Field<Object> acceptability, String patternText,
                                      ViewCalculator calculator) {
        String dialect = acceptability.fieldDefinition(calculator) instanceof FieldDefinitionForEntity definition
                ? name(definition.meaningNid(), calculator)
                : null;
        String title = joined(conceptName(acceptability.value(), calculator), dialect);
        String described = describedText(semantic.referencedComponentNid(), calculator);
        return title == null
                ? new Lines(patternText, described, described != null)
                : new Lines(title, described == null ? patternText : described, described != null);
    }

    /** The fields of a description a row shows; any of them null when the semantic has no value. */
    private record DescriptionFields(String text, String type, String language) {
    }

    /** The description's fields, or null when the semantic is not a description on the view. */
    private static DescriptionFields descriptionFields(SemanticEntity<?> semantic, ViewCalculator calculator) {
        if (KonceptKindResolver.resolve(semantic.nid(), calculator) != KonceptKind.DESCRIPTION) {
            return null;
        }
        Latest<SemanticEntityVersion> version = versionOf(semantic, calculator);
        return new DescriptionFields(
                fieldValue(version, KernelConcept.TEXT_FOR_DESCRIPTION, calculator) instanceof String text
                        && !text.isBlank() ? text : null,
                conceptName(fieldValue(version, KernelConcept.DESCRIPTION_TYPE, calculator), calculator),
                conceptName(fieldValue(version, KernelConcept.LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION, calculator), calculator));
    }

    /**
     * The description a dialect is on, quoted, with its type: {@code of “Heart” · Regular name};
     * null when the component is not a description with text.
     */
    private static String describedText(long nid, ViewCalculator calculator) {
        if (!(EntityHandle.get(nid).orNull() instanceof SemanticEntity<?> semantic)) {
            return null;
        }
        DescriptionFields description = descriptionFields(semantic, calculator);
        if (description == null || description.text() == null) {
            return null;
        }
        return joined("of “" + description.text() + "”", description.type());
    }

    /**
     * The semantic's version on the view, or its last version while the view has none: an
     * unpublished version may be on a module or path the view does not include.
     */
    private static Latest<SemanticEntityVersion> versionOf(SemanticEntity<?> semantic, ViewCalculator calculator) {
        if (semantic.versions().isEmpty()) {
            return Latest.empty();
        }
        Latest<SemanticEntityVersion> latest = calculator.latest(semantic.nid());
        return latest.isPresent() ? latest : Latest.of(semantic.versions().getLast());
    }

    /** The value of the field with the meaning, or null when the pattern has no such field. */
    private static Object fieldValue(Latest<SemanticEntityVersion> version, KernelConcept meaning, ViewCalculator calculator) {
        Latest<Field<Object>> field = calculator.getFieldForSemanticWithMeaning(version, meaning);
        return field.isPresent() ? field.get().value() : null;
    }

    /** The name of a concept-valued field's concept, or null when the value is not a component. */
    private static String conceptName(Object value, ViewCalculator calculator) {
        return value instanceof ComponentWithNid component ? name(component.nid(), calculator) : null;
    }

    /** A component's regular name on the view, falling back to its other names; null with none. */
    private static String name(long nid, ViewCalculator calculator) {
        try {
            String name = calculator.getPreferredDescriptionTextWithFallbackOrNid(nid);
            return name.equals(Long.toString(nid)) ? null : name;
        } catch (IllegalStateException noVersions) {
            return null;
        }
    }

    /** The parts that are present, separated as a row separates them; null with none. */
    private static String joined(String... parts) {
        String joined = Arrays.stream(parts).filter(Objects::nonNull).collect(Collectors.joining(" · "));
        return joined.isEmpty() ? null : joined;
    }

    /**
     * The component a change is listed under: a concept or pattern itself; for a semantic, the
     * component it is on, whether a concept, a pattern or another semantic. A semantic whose
     * component is absent from the store stands on its own.
     */
    static long rootComponentNid(Entity<?> entity) {
        if (entity instanceof SemanticEntity<?> semantic
                && EntityHandle.get(semantic.referencedComponentNid()).orNull() != null) {
            return semantic.referencedComponentNid();
        }
        return entity.nid();
    }

    /**
     * A component's description text on the view, or the fallback (which may be null) while it
     * has none: a concept just created in a window has no description until its first name is
     * submitted.
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
