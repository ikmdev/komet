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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A component heading in the unpublished-changes tree, with the changes grouped under it: the
 * changes to the semantics on the component. The component's own change, when it has one, is
 * shown on the heading itself (its kind tag), not as a row under it.
 *
 * @param nid       the component's nid
 * @param component the component as read when the group was built; null when it is absent
 * @param type      what the component is
 * @param kind      what the component's own unpublished version does to it; null when the
 *                  component itself is published and only semantics on it changed
 * @param title     the component's description text on the journal's view
 * @param changes   every change the heading stands for, the component's own first, then the
 *                  semantics' by title; the group actions act on all of them
 */
public record UnpublishedGroup(long nid, Entity<?> component, UnpublishedChange.Type type,
                               UnpublishedChange.Kind kind, String title,
                               List<UnpublishedChange> changes) implements UnpublishedRow {

    /**
     * The tag on the heading: what happened to the component. Its own change's kind when it has
     * one; otherwise {@link UnpublishedChange.Kind#EDITED}, since a published component whose
     * semantics changed was edited, even though its own version is untouched.
     */
    public UnpublishedChange.Kind headingKind() {
        return kind == null ? UnpublishedChange.Kind.EDITED : kind;
    }

    /** The changes to the semantics on the component: the rows under the heading. */
    public List<UnpublishedChange> semanticChanges() {
        return changes.stream().filter(change -> change.nid() != nid).toList();
    }

    /**
     * Groups changes under the component each is on, groups by title and a group's changes with
     * the component's own change first, then by title.
     *
     * @param changes    the changes, in any order
     * @param calculator the view the group titles are read on
     * @return the groups
     */
    public static List<UnpublishedGroup> groupsOf(List<UnpublishedChange> changes, ViewCalculator calculator) {
        Map<Long, List<UnpublishedChange>> byRoot = new LinkedHashMap<>();
        for (UnpublishedChange change : changes) {
            byRoot.computeIfAbsent(change.rootNid(), _ -> new ArrayList<>()).add(change);
        }
        List<UnpublishedGroup> groups = new ArrayList<>();
        byRoot.forEach((rootNid, members) -> groups.add(of(rootNid, members, calculator)));
        groups.sort(Comparator.comparing(UnpublishedGroup::title, String.CASE_INSENSITIVE_ORDER));
        return groups;
    }

    private static UnpublishedGroup of(long rootNid, List<UnpublishedChange> members, ViewCalculator calculator) {
        Entity<?> component = EntityHandle.get(rootNid).orNull();
        UnpublishedChange.Type type = UnpublishedChange.Type.of(component);
        UnpublishedChange own = members.stream().filter(change -> change.nid() == rootNid).findFirst().orElse(null);
        List<UnpublishedChange> ordered = new ArrayList<>(members);
        ordered.sort(Comparator
                .comparing((UnpublishedChange change) -> change.nid() != rootNid)
                .thenComparing(UnpublishedChange::title, String.CASE_INSENSITIVE_ORDER));
        String title = own != null
                ? own.title()
                : UnpublishedChange.text(rootNid, "Unnamed " + type.label().toLowerCase(), calculator);
        return new UnpublishedGroup(rootNid, component, type, own == null ? null : own.kind(), title, List.copyOf(ordered));
    }
}
