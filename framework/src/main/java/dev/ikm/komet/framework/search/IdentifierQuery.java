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
package dev.ikm.komet.framework.search;

import dev.ikm.komet.framework.ComponentLookup;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.uuid.UuidUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

/**
 * A search query that names components by identifier instead of by text
 * ({@code IKE-Network/ike-issues#1174}). Three forms are read:
 * <ul>
 *   <li>a nid, written as a negative integer: {@code -2147483000};</li>
 *   <li>a UUID: {@code 02018e5a-46ba-5297-92f1-6931b9f98a12};</li>
 *   <li>a bracketed list of nids, UUIDs, or both:
 *       {@code [-2147483000, 02018e5a-46ba-5297-92f1-6931b9f98a12]}.</li>
 * </ul>
 * Any other text is a text query. That includes a bracketed list with an element that is
 * neither an integer nor a UUID, so a text query may begin and end with a bracket.
 *
 * <p>A nid is local to one knowledge base. It is read as typed, to look a component up in the
 * knowledge base that is open, and it is never kept: {@link #storedForm()} writes every
 * component by UUID, which is what a search node saves in preferences.
 */
public final class IdentifierQuery {

    /** One identifier as it was written in the query. */
    private sealed interface Identifier {
    }

    /** A nid, as typed. */
    private record Nid(int nid) implements Identifier {
    }

    /** A UUID, as typed. */
    private record Uuid(UUID uuid) implements Identifier {
    }

    private final List<Identifier> identifiers;

    private IdentifierQuery(List<Identifier> identifiers) {
        this.identifiers = List.copyOf(identifiers);
    }

    /**
     * Reads query text as an identifier query.
     *
     * @param queryText the text of the search field; may be null
     * @return the identifier query, or empty when the text is not one of the identifier forms
     *         and is therefore a text query
     */
    public static Optional<IdentifierQuery> parse(String queryText) {
        if (queryText == null) {
            return Optional.empty();
        }
        String text = queryText.strip();
        if (text.startsWith("[") && text.endsWith("]")) {
            return parseList(text.substring(1, text.length() - 1));
        }
        if (text.startsWith("-")) {
            OptionalInt nid = parseInt(text);
            return nid.isPresent()
                    ? Optional.of(new IdentifierQuery(List.of(new Nid(nid.getAsInt()))))
                    : Optional.empty();
        }
        return UuidUtil.getUUID(text).map(uuid -> new IdentifierQuery(List.of(new Uuid(uuid))));
    }

    /**
     * The form of query text that a search node stores in preferences. A text query is stored
     * as it is. An identifier query is stored by UUID ({@link #storedForm()}), so that no nid
     * is kept.
     *
     * @param queryText the text of the search field; may be null
     * @return the text to store; null only when {@code queryText} is null
     */
    public static String storedForm(String queryText) {
        Optional<IdentifierQuery> identifierQuery = parse(queryText);
        return identifierQuery.isPresent() ? identifierQuery.get().storedForm() : queryText;
    }

    /**
     * The nids, in the open knowledge base, of the components this query names, in the order
     * they were written. A nid is taken as typed. A UUID is looked up, and one the knowledge
     * base does not hold is left out and is assigned no nid.
     *
     * @return the nids; empty when the query names nothing the knowledge base holds
     */
    public int[] nids() {
        int[] nids = new int[identifiers.size()];
        int count = 0;
        for (Identifier identifier : identifiers) {
            switch (identifier) {
                case Nid typed -> nids[count++] = typed.nid();
                case Uuid typed -> {
                    OptionalInt nid = ComponentLookup.nid(typed.uuid());
                    if (nid.isPresent()) {
                        nids[count++] = nid.getAsInt();
                    }
                }
            }
        }
        return Arrays.copyOf(nids, count);
    }

    /**
     * This query written with UUIDs only: a single UUID, or a bracketed list of them. A nid is
     * replaced by the least UUID of the component it names in the open knowledge base. A nid
     * that names nothing there is left out.
     *
     * <p>The result is itself an identifier query, so it can be run again from preferences,
     * against the same knowledge base or another.
     *
     * @return the query by UUID; the empty string when nothing is left
     */
    public String storedForm() {
        List<String> uuids = new ArrayList<>(identifiers.size());
        for (Identifier identifier : identifiers) {
            switch (identifier) {
                case Uuid typed -> uuids.add(typed.uuid().toString());
                case Nid typed -> leastUuid(typed.nid()).ifPresent(uuid -> uuids.add(uuid.toString()));
            }
        }
        if (uuids.isEmpty()) {
            return "";
        }
        if (uuids.size() == 1) {
            return uuids.get(0);
        }
        return "[" + String.join(", ", uuids) + "]";
    }

    /**
     * Reads the inside of a bracketed list. Every element must be an integer or a UUID;
     * otherwise the text is not an identifier query.
     */
    private static Optional<IdentifierQuery> parseList(String inside) {
        List<Identifier> identifiers = new ArrayList<>();
        for (String part : inside.split(",", -1)) {
            String element = part.strip();
            OptionalInt nid = parseInt(element);
            if (nid.isPresent()) {
                identifiers.add(new Nid(nid.getAsInt()));
                continue;
            }
            Optional<UUID> uuid = UuidUtil.getUUID(element);
            if (uuid.isEmpty()) {
                return Optional.empty();
            }
            identifiers.add(new Uuid(uuid.get()));
        }
        return Optional.of(new IdentifierQuery(identifiers));
    }

    private static OptionalInt parseInt(String possibleInt) {
        try {
            return OptionalInt.of(Integer.parseInt(possibleInt));
        } catch (NumberFormatException notAnInt) {
            return OptionalInt.empty();
        }
    }

    /**
     * The UUID that stands for the component a nid names in the open knowledge base, if it names
     * one: the least of its UUIDs ({@link PublicId#leastUuid()}). Any of them names the component
     * in another knowledge base; the least is chosen so the stored form does not depend on the
     * order this one lists them in.
     */
    private static Optional<UUID> leastUuid(int nid) {
        try {
            PublicId publicId = PrimitiveData.publicId(nid);
            if (publicId == null || publicId.uuidCount() == 0) {
                return Optional.empty();
            }
            return Optional.of(publicId.leastUuid());
        } catch (RuntimeException namesNothing) {
            return Optional.empty();
        }
    }
}
