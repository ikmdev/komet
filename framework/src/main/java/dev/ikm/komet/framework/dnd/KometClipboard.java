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
            return conceptNidFromProxyXml((String) dragboard.getContent(KOMET_CONCEPT_PROXY));
        }
        return OptionalInt.empty();
    }

    /**
     * Decodes a concept nid from a {@link #KOMET_CONCEPT_PROXY} XML fragment — the testable
     * seam behind {@link #conceptNid(Dragboard)}. Returns empty for a {@code null}, blank,
     * or unparseable fragment rather than throwing.
     *
     * @param proxyXmlFragment the serialized concept proxy
     * @return the concept nid, or {@link OptionalInt#empty()}
     */
    static OptionalInt conceptNidFromProxyXml(String proxyXmlFragment) {
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
