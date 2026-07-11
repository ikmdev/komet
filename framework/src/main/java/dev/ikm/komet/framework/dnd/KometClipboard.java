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
package dev.ikm.komet.framework.dnd;

//~--- JDK imports ------------------------------------------------------------

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import dev.ikm.tinkar.component.Component;
import dev.ikm.tinkar.component.Version;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.ProxyFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

//~--- classes ----------------------------------------------------------------

/**
 * 
 */
public class KometClipboard extends ClipboardContent {
    public static final DataFormat MULTI_PARENT_GRAPH_DRAG_FORMAT = new DataFormat("application/multi-parent-graph-format");
    public static final DataFormat KOMET_PROXY_LIST = new DataFormat("application/komet-proxy-list");
    /** A drag of several concepts at once: each concept's {@code PublicId} as a {@code UUID[]}, in order. */
    public static final DataFormat KOMET_CONCEPT_LIST = new DataFormat("application/komet-concept-list");
    public static final DataFormat KOMET_CONCEPT_PROXY = new DataFormat("application/komet-concept-proxy");
    public static final DataFormat KOMET_PATTERN_PROXY = new DataFormat("application/komet-pattern-proxy");
    public static final DataFormat KOMET_SEMANTIC_PROXY = new DataFormat("application/komet-semantic-proxy");
    public static final DataFormat KOMET_CONCEPT_VERSION_PROXY = new DataFormat("application/komet-concept-version-proxy");
    public static final DataFormat KOMET_PATTERN_VERSION_PROXY = new DataFormat("application/komet-pattern-version-proxy");
    public static final DataFormat KOMET_SEMANTIC_VERSION_PROXY = new DataFormat("application/komet-semantic-version-proxy");
    public static final DataFormat KOMET_STAMP_PROXY = new DataFormat("application/komet-stamp-proxy");
    public static final DataFormat KOMET_STAMP_VERSION_PROXY = new DataFormat("application/komet-stamp-version-proxy");

    /**
     * Drag format used when dragging a Component. Can be used anywhere a Component is in the clipboard and a Public Id is stored
     * in there.
     */
    public static final DataFormat COMPONENT_DRAG_FORMAT = new DataFormat("application/x-komet-component-drag-format");

    public static final Set<DataFormat> CONCEPT_TYPES = new HashSet<>(Arrays.asList(KOMET_CONCEPT_VERSION_PROXY, KOMET_CONCEPT_PROXY));
    public static final Set<DataFormat> PATTERN_TYPES = new HashSet<>(Arrays.asList(KOMET_PATTERN_VERSION_PROXY, KOMET_PATTERN_PROXY));
    public static final Set<DataFormat> SEMANTIC_TYPES = new HashSet<>(Arrays.asList(KOMET_SEMANTIC_VERSION_PROXY, KOMET_SEMANTIC_PROXY));
    public static final Set<DataFormat> STAMP_TYPES = new HashSet<>(Arrays.asList(KOMET_STAMP_VERSION_PROXY, KOMET_STAMP_PROXY));
    private static final HashMap<DataFormat, Function<? super Component, ? extends Object>> GENERATOR_MAP
            = new HashMap<>();

    /**
     * Encodes a {@link PublicId} as a comma-separated string of UUID values,
     * for example, suitable for use as {@link #COMPONENT_DRAG_FORMAT} clipboard content.
     *
     * @param publicId the PublicId to encode
     * @return a comma-separated UUID string
     */
    public static String encodePublicId(PublicId publicId) {
        return Arrays.stream(publicId.asUuidArray())
                .map(UUID::toString)
                .collect(Collectors.joining(","));
    }

    /**
     * Decodes a comma-separated UUID string (as produced by {@link #encodePublicId})
     * back into a {@link PublicId}.
     *
     * @param encoded the comma-separated UUID string
     * @return the decoded PublicId
     * @throws IllegalArgumentException if any token is not a valid UUID
     */
    public static PublicId decodePublicId(String encoded) {
        return PublicIds.of(encoded.split(","));
    }

    /**
     * Decodes a comma-separated UUID string (as produced by {@link #encodePublicId})
     * into a {@code UUID[]}.
     *
     * @param encoded the comma-separated UUID string
     * @return array of UUIDs
     * @throws IllegalArgumentException if any token is not a valid UUID
     */
    public static UUID[] decodeUuids(String encoded) {
        return Arrays.stream(encoded.split(","))
                .map(UUID::fromString)
                .toArray(UUID[]::new);
    }

    //~--- static initializers -------------------------------------------------
    static {
        GENERATOR_MAP.put(
                DataFormat.HTML,
                (t) -> {
                    throw new UnsupportedOperationException();
                });
        GENERATOR_MAP.put(
                DataFormat.PLAIN_TEXT,
                (t) -> t != null ? t.publicId().toString() : "");
    }

    //~--- constructors --------------------------------------------------------
    public KometClipboard(Component component) {
        if (component instanceof Version version) {
            if (version instanceof ConceptEntityVersion conceptVersion) {
                this.put(KOMET_CONCEPT_VERSION_PROXY, conceptVersion.toXmlFragment());
            } else if (version instanceof SemanticEntityVersion semanticVersion) {
                this.put(KOMET_SEMANTIC_VERSION_PROXY, semanticVersion.toXmlFragment());
            } else if (version instanceof PatternEntityVersion patternVersion) {
                this.put(KOMET_PATTERN_VERSION_PROXY, patternVersion.toXmlFragment());
            } else if (version instanceof StampEntityVersion stampVersion) {
                this.put(KOMET_STAMP_VERSION_PROXY, stampVersion.toXmlFragment());
            }
        } else if (component instanceof EntityFacade entityFacade) {
            addEntity(entityFacade);
        }
        addExtra(DataFormat.PLAIN_TEXT, component);

    }

    public KometClipboard(List<EntityProxy> entityProxyList) {
        this.put(KOMET_PROXY_LIST, entityProxyList);
        this.put(DataFormat.PLAIN_TEXT, entityProxyList.toString());
    }

    private KometClipboard() {
        super();
    }

    /**
     * Builds clipboard content for a single component proxy, putting its atom format
     * <em>unconditionally</em> from the proxy's serialized form — there is no
     * {@link EntityHandle} presence gate, so the payload is present even when the component
     * is not yet loaded (the gate that made drops silently fail). The format is chosen by
     * the proxy's concrete component type (concept / pattern / semantic / stamp).
     *
     * @param proxy the component proxy to place on the clipboard; must not be {@code null}
     * @return clipboard content carrying {@code proxy}
     */
    public static KometClipboard forProxy(EntityProxy proxy) {
        KometClipboard content = new KometClipboard();
        content.put(formatFor(proxy), proxy.toXmlFragment());
        content.put(DataFormat.PLAIN_TEXT, proxy.publicId().toString());
        return content;
    }

    /**
     * Builds clipboard content for the concept with the given nid — a convenience over
     * {@link #forProxy(EntityProxy)} for the common concept drag. Resolves the concept's
     * description from the store (see {@link #forConcept(int, String)} for why a description
     * is required).
     *
     * @param nid the concept nid
     * @return clipboard content carrying the concept proxy
     */
    public static KometClipboard forConcept(int nid) {
        return forConcept(nid, null);
    }

    /**
     * Builds clipboard content for a concept nid with a known description (e.g. a badge's
     * resolved label). A concept proxy MUST carry a non-null description: {@code toXmlFragment()}
     * escapes the description for XML, and a {@code null} one throws there. The supplied
     * description is used when present; otherwise the store's default text is resolved; the nid
     * string is the final fallback so the payload is always well-formed.
     *
     * @param nid         the concept nid
     * @param description the concept's display label, or {@code null} to resolve from the store
     * @return clipboard content carrying the concept proxy
     */
    public static KometClipboard forConcept(int nid, String description) {
        String resolved = (description != null && !description.isBlank())
                ? description
                : PrimitiveData.text(nid);
        if (resolved == null || resolved.isBlank()) {
            resolved = Integer.toString(nid);
        }
        return forProxy(EntityProxy.Concept.make(resolved, PrimitiveData.publicId(nid)));
    }

    /**
     * Builds the eager clipboard map for the component with the given nid — the centralized source-side
     * builder for a Koncept drag <em>or</em> copy (ike-issues#638). JavaFX's {@link ClipboardContent} is
     * an eager map, so every representation is populated up front and the drop side simply picks the
     * format it wants — there is no per-target conversion. The map carries the component's <em>actual</em>
     * base type (concept, description or other semantic, pattern, or stamp) and, when that is not already
     * a concept, the <em>resolved</em> {@link #KOMET_CONCEPT_PROXY} (a description's referenced concept),
     * so a concept drop target always finds a concept. The same instance serves a {@link Dragboard} or
     * the system {@code Clipboard}.
     *
     * @param nid the component nid — a concept, a description/semantic, a pattern, or a stamp
     * @return the eager clipboard content for {@code nid}
     */
    public static KometClipboard forComponent(int nid) {
        KometClipboard content = new KometClipboard();
        Entity<?> entity = loadedEntity(nid);
        if (entity != null) {
            content.addEntity(entity);
        }
        // A concept drop target always wants a concept: resolve a semantic to its referenced concept;
        // for an unloaded nid fall back to treating it as a concept (the prior unconditional-payload
        // guarantee). A loaded pattern or stamp is genuinely not a concept, so carries no concept proxy.
        if (!content.containsKey(KOMET_CONCEPT_PROXY)) {
            OptionalInt conceptNid = (entity == null) ? OptionalInt.of(nid) : resolvedConceptNid(nid);
            conceptNid.ifPresent(cn -> content.put(KOMET_CONCEPT_PROXY, conceptProxyXml(cn)));
        }
        content.put(DataFormat.PLAIN_TEXT, PrimitiveData.publicId(nid).toString());
        return content;
    }

    /** The loaded entity for a nid, or {@code null} when it is not currently loadable. */
    private static Entity<?> loadedEntity(int nid) {
        try {
            return EntityHandle.get(nid).entity().orElse(null);
        } catch (RuntimeException notLoadable) {
            return null;
        }
    }

    /**
     * The concept nid a koncept drag should also carry: a concept resolves to itself; a description — or
     * any semantic — resolves to the concept it references (following {@code referencedComponentNid} until
     * a concept is reached); a pattern, stamp, or unresolvable component yields empty (it is not a concept).
     *
     * @param nid the dragged component nid
     * @return the referenced concept nid, or {@link OptionalInt#empty()} when none
     */
    private static OptionalInt resolvedConceptNid(int nid) {
        int current = nid;
        for (int hop = 0; hop < 8; hop++) {
            Entity<?> entity = loadedEntity(current);
            if (entity instanceof ConceptEntity<?>) {
                return OptionalInt.of(current);
            }
            if (entity instanceof SemanticEntity<?> semantic) {
                current = semantic.referencedComponentNid();
            } else {
                return OptionalInt.empty();
            }
        }
        return OptionalInt.empty();
    }

    /** A well-formed {@link #KOMET_CONCEPT_PROXY} XML fragment for a concept nid (description required). */
    private static String conceptProxyXml(int conceptNid) {
        String description = PrimitiveData.text(conceptNid);
        if (description == null || description.isBlank()) {
            description = Integer.toString(conceptNid);
        }
        return EntityProxy.Concept.make(description, PrimitiveData.publicId(conceptNid)).toXmlFragment();
    }

    /**
     * The atom clipboard format for a proxy's concrete component type, falling back to
     * {@link #COMPONENT_DRAG_FORMAT} for a bare (untyped) {@link EntityProxy}.
     *
     * @param proxy the component proxy
     * @return the matching {@link DataFormat}
     */
    static DataFormat formatFor(EntityProxy proxy) {
        if (proxy instanceof EntityProxy.Concept) {
            return KOMET_CONCEPT_PROXY;
        }
        if (proxy instanceof EntityProxy.Pattern) {
            return KOMET_PATTERN_PROXY;
        }
        if (proxy instanceof EntityProxy.Semantic) {
            return KOMET_SEMANTIC_PROXY;
        }
        if (proxy instanceof EntityProxy.Stamp) {
            return KOMET_STAMP_PROXY;
        }
        return COMPONENT_DRAG_FORMAT;
    }

    public static boolean containsAny(Collection<?> c1,
                                      Collection<?> c2) {
        return !Collections.disjoint(c1, c2);
    }

    private void addEntity(EntityFacade entityFacade) {
        EntityHandle.get(entityFacade)
                .ifConcept(concept -> this.put(KOMET_CONCEPT_PROXY, concept.toXmlFragment()))
                .ifPattern(pattern -> this.put(KOMET_PATTERN_PROXY, pattern.toXmlFragment()))
                .ifSemantic(semantic -> this.put(KOMET_SEMANTIC_PROXY, semantic.toXmlFragment()))
                .ifStamp(stamp -> this.put(KOMET_STAMP_PROXY, stamp.toXmlFragment()));
    }

    //~--- methods -------------------------------------------------------------
    private void addExtra(DataFormat format, Component component) {
        put(format, GENERATOR_MAP.get(format)
                .apply(component));
    }

    /**
     * The concept nid carried by a Komet concept dragboard, or empty when the board has no
     * {@link #KOMET_CONCEPT_PROXY} content or the payload is malformed. The inverse of
     * {@link #forConcept(int)} on the drop side.
     *
     * @param dragboard the drag-and-drop content; may be {@code null}
     * @return the dropped concept nid, or {@link OptionalInt#empty()}
     */
    public static OptionalInt conceptNid(Dragboard dragboard) {
        if (dragboard != null && dragboard.hasContent(KOMET_CONCEPT_PROXY)) {
            return nidFromProxyXml((String) dragboard.getContent(KOMET_CONCEPT_PROXY));
        }
        return OptionalInt.empty();
    }

    /**
     * The semantic nid carried by a Komet dragboard (e.g. a dragged description), or empty when the
     * board has no {@link #KOMET_SEMANTIC_PROXY}. The drop-side request for the semantic itself,
     * alongside {@link #conceptNid(Dragboard)} for the concept a description describes.
     *
     * @param dragboard the drag-and-drop content; may be {@code null}
     * @return the dropped semantic nid, or {@link OptionalInt#empty()}
     */
    public static OptionalInt semanticNidFrom(Dragboard dragboard) {
        if (dragboard != null && dragboard.hasContent(KOMET_SEMANTIC_PROXY)) {
            return nidFromProxyXml((String) dragboard.getContent(KOMET_SEMANTIC_PROXY));
        }
        return OptionalInt.empty();
    }

    /** The base-type proxy formats {@link #entityNidFrom(Dragboard)} reads, in precedence order. */
    private static final List<DataFormat> ATOM_PROXY_FORMATS =
            List.of(KOMET_CONCEPT_PROXY, KOMET_SEMANTIC_PROXY, KOMET_PATTERN_PROXY, KOMET_STAMP_PROXY);

    /**
     * The nid of whatever base-type proxy the dragboard carries — concept, semantic, pattern, or stamp,
     * in that precedence — the drop-side request for "the component as dragged", with no conversion.
     *
     * @param dragboard the drag-and-drop content; may be {@code null}
     * @return the dropped component nid, or {@link OptionalInt#empty()}
     */
    public static OptionalInt entityNidFrom(Dragboard dragboard) {
        if (dragboard != null) {
            for (DataFormat format : ATOM_PROXY_FORMATS) {
                if (dragboard.hasContent(format)) {
                    return nidFromProxyXml((String) dragboard.getContent(format));
                }
            }
        }
        return OptionalInt.empty();
    }

    /**
     * Advertises several concepts on {@code content} as the {@link #KOMET_CONCEPT_LIST} format, in
     * order — the drag-source side of a multi-concept drag, read back by {@link #conceptNidsFrom}.
     *
     * @param content the clipboard content being assembled
     * @param nids    the concept nids to carry, in the order the drop should see them
     */
    public static void putConcepts(ClipboardContent content, int[] nids) {
        List<UUID[]> ids = new ArrayList<>(nids.length);
        for (int nid : nids) {
            ids.add(PrimitiveData.publicId(nid).asUuidArray());
        }
        content.put(KOMET_CONCEPT_LIST, ids);
    }

    /**
     * The concept nids a multi-concept drag carries, in order, or an empty array when the dragboard
     * has no {@link #KOMET_CONCEPT_LIST} content — the drop-side reader for a multi-concept drag.
     * A malformed entry is skipped rather than throwing.
     *
     * @param dragboard the drag-and-drop content; may be {@code null}
     * @return the dropped concept nids in order, possibly empty
     */
    public static int[] conceptNidsFrom(Dragboard dragboard) {
        if (dragboard == null || !dragboard.hasContent(KOMET_CONCEPT_LIST)) {
            return new int[0];
        }
        @SuppressWarnings("unchecked")
        List<UUID[]> ids = (List<UUID[]>) dragboard.getContent(KOMET_CONCEPT_LIST);
        return ids.stream()
                .filter(uuids -> uuids != null && uuids.length > 0)
                .mapToInt(uuids -> PrimitiveData.nid(PublicIds.of(uuids)))
                .toArray();
    }

    /**
     * Decodes a component nid from any base-type proxy XML fragment (concept, semantic, pattern, or
     * stamp) — the testable seam behind {@link #conceptNid(Dragboard)} and the other typed readers.
     * Returns empty for a {@code null}, blank, or unparseable fragment rather than throwing.
     *
     * @param proxyXmlFragment the serialized component proxy
     * @return the component nid, or {@link OptionalInt#empty()}
     */
    static OptionalInt nidFromProxyXml(String proxyXmlFragment) {
        if (proxyXmlFragment == null || proxyXmlFragment.isBlank()) {
            return OptionalInt.empty();
        }
        try {
            return OptionalInt.of(ProxyFactory.fromXmlFragment(proxyXmlFragment).nid());
        } catch (RuntimeException malformed) {
            return OptionalInt.empty();
        }
    }

    @Override
    public Object get(Object key) {
        return super.get(key);
    }
}
