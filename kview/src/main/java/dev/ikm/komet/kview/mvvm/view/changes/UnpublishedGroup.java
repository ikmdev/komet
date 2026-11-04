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

import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.SemanticEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A component heading in the unpublished-changes tree, with the changes grouped under it: the
 * changes to the semantics on the component, and on those semantics in turn. The component is
 * a concept, a pattern, or a semantic whose own component is absent. The component's own
 * change, when it has one, is shown on the heading itself (its kind tag), not as a row under it.
 *
 * @param nid       the component's nid
 * @param component the component as read when the group was built; null when it is absent
 * @param type      what the component is
 * @param kind      what the component's own unpublished version does to it; null when the
 *                  component itself is published and only semantics on it changed
 * @param title     the component's description text on the journal's view; for a component
 *                  without one, what it is instead
 * @param untitled  true when the component has no text of its own and the title says what it is
 *                  instead ({@link UnpublishedChange#untitled()}); the heading shows it muted
 * @param changes   every change the heading stands for, flat: the component's own first, then
 *                  the rows under it in the tree's order; the group actions act on all of them
 * @param rows      the rows directly under the heading, by title, each with the changes nested
 *                  under it ({@link UnpublishedChange#children()}): a concept's descriptions,
 *                  each with its dialects
 */
public record UnpublishedGroup(long nid, Entity<?> component, UnpublishedChange.Type type,
                               UnpublishedChange.Kind kind, String title, boolean untitled,
                               List<UnpublishedChange> changes, List<UnpublishedChange> rows)
        implements UnpublishedRow {

    /**
     * The tag on the heading: what happened to the component. Its own change's kind when it has
     * one; otherwise {@link UnpublishedChange.Kind#EDITED}, since a published component whose
     * semantics changed was edited, even though its own version is untouched.
     */
    public UnpublishedChange.Kind headingKind() {
        return kind == null ? UnpublishedChange.Kind.EDITED : kind;
    }

    /** The changes to the semantics on the component, at every depth: all but the component's own. */
    public List<UnpublishedChange> semanticChanges() {
        return changes.stream().filter(change -> change.nid() != nid).toList();
    }

    /**
     * Groups changes under the component each is on, groups by title and a group's changes with
     * the component's own change first, then by title. A change whose component is a listed
     * change too nests under that change's row rather than heading a row of its own: a dialect
     * under its description, a description under its concept. A change whose component is not
     * listed sits directly under its group's heading, the component at the top of its chain: a
     * dialect changed on a published description sits under the description's concept, saying
     * which description it is on. Every change is in exactly one group, once.
     *
     * @param changes    the changes, in any order
     * @param calculator the view the group titles are read on
     * @return the groups
     */
    public static List<UnpublishedGroup> groupsOf(List<UnpublishedChange> changes, ViewCalculator calculator) {
        Map<Long, UnpublishedChange> byNid = new LinkedHashMap<>();
        for (UnpublishedChange change : changes) {
            byNid.put(change.nid(), change);
        }
        Map<Long, List<UnpublishedChange>> childrenOf = new HashMap<>();
        Map<Long, List<UnpublishedChange>> topRowsOf = new LinkedHashMap<>();
        for (UnpublishedChange change : byNid.values()) {
            long parent = parentChangeNid(change, byNid);
            if (parent != change.nid() && byNid.containsKey(parent)) {
                childrenOf.computeIfAbsent(parent, _ -> new ArrayList<>()).add(change);
            } else {
                topRowsOf.computeIfAbsent(parent, _ -> new ArrayList<>()).add(change);
            }
        }
        List<UnpublishedGroup> groups = new ArrayList<>();
        topRowsOf.forEach((rootNid, members) -> groups.add(of(rootNid, members, childrenOf, calculator)));
        groups.sort(Comparator.comparing(UnpublishedGroup::title, String.CASE_INSENSITIVE_ORDER));
        return groups;
    }

    /**
     * Where a change belongs: its own nid for a concept, a pattern or a semantic on nothing; the
     * nid of the nearest listed change up its chain of components, which it nests under; else
     * the nid of the component at the top of the chain, which heads its group. The chain climbs
     * through semantics on semantics until a concept, a pattern, or a component the store does
     * not have.
     */
    private static long parentChangeNid(UnpublishedChange change, Map<Long, UnpublishedChange> byNid) {
        Set<Long> visited = new HashSet<>();
        long component = change.rootNid();
        while (component != change.nid() && !byNid.containsKey(component) && visited.add(component)) {
            if (EntityHandle.get(component).orNull() instanceof SemanticEntity<?> semantic
                    && EntityHandle.get(semantic.referencedComponentNid()).orNull() != null) {
                component = semantic.referencedComponentNid();
            } else {
                break;
            }
        }
        return component;
    }

    private static UnpublishedGroup of(long rootNid, List<UnpublishedChange> members,
                                       Map<Long, List<UnpublishedChange>> childrenOf, ViewCalculator calculator) {
        Entity<?> component = EntityHandle.get(rootNid).orNull();
        UnpublishedChange.Type type = UnpublishedChange.Type.of(component);
        UnpublishedChange own = members.stream().filter(change -> change.nid() == rootNid).findFirst().orElse(null);
        // Under the heading: the changes whose component is not listed, and the changes on the
        // heading's own change, when it has one.
        List<UnpublishedChange> under = new ArrayList<>(members.stream().filter(change -> change.nid() != rootNid).toList());
        under.addAll(childrenOf.getOrDefault(rootNid, List.of()));
        List<UnpublishedChange> rows = nested(under, childrenOf);
        List<UnpublishedChange> flat = new ArrayList<>();
        if (own != null) {
            flat.add(own);
        }
        flatten(rows, flat);
        String text = own != null ? own.title() : UnpublishedChange.text(rootNid, null, calculator);
        boolean untitled = own != null ? own.untitled() : text == null;
        String title = text == null ? type.label() : text;
        return new UnpublishedGroup(rootNid, component, type, own == null ? null : own.kind(), title, untitled,
                List.copyOf(flat), rows);
    }

    /** The changes by title, each with its children nested the same way, to any depth. */
    private static List<UnpublishedChange> nested(List<UnpublishedChange> changes,
                                                  Map<Long, List<UnpublishedChange>> childrenOf) {
        List<UnpublishedChange> ordered = new ArrayList<>();
        for (UnpublishedChange change : changes) {
            List<UnpublishedChange> children = childrenOf.getOrDefault(change.nid(), List.of()).stream()
                    .map(UnpublishedChange::nestedUnderItsComponent).toList();
            ordered.add(children.isEmpty() ? change : change.withChildren(nested(children, childrenOf)));
        }
        ordered.sort(Comparator.comparing(UnpublishedChange::title, String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(ordered);
    }

    /** The rows in the tree's order, each before the changes nested under it. */
    private static void flatten(List<UnpublishedChange> rows, List<UnpublishedChange> into) {
        for (UnpublishedChange row : rows) {
            into.add(row);
            flatten(row.children(), into);
        }
    }
}
