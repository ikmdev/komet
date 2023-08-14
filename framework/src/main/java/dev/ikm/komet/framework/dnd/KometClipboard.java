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

import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import dev.ikm.tinkar.component.Component;
import dev.ikm.tinkar.component.Version;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;

import java.util.*;
import java.util.function.Function;

//~--- classes ----------------------------------------------------------------

/**
 * 
 */
public class KometClipboard
        extends ClipboardContent {

    public static final DataFormat KOMET_PROXY_LIST = new DataFormat("application/komet-proxy-list");
    public static final DataFormat KOMET_CONCEPT_PROXY = new DataFormat("application/komet-concept-proxy");
    public static final DataFormat KOMET_PATTERN_PROXY = new DataFormat("application/komet-pattern-proxy");
    public static final DataFormat KOMET_SEMANTIC_PROXY = new DataFormat("application/komet-semantic-proxy");
    public static final DataFormat KOMET_CONCEPT_VERSION_PROXY = new DataFormat("application/komet-concept-version-proxy");
    public static final DataFormat KOMET_PATTERN_VERSION_PROXY = new DataFormat("application/komet-pattern-version-proxy");
    public static final DataFormat KOMET_SEMANTIC_VERSION_PROXY = new DataFormat("application/komet-semantic-version-proxy");

    public static final Set<DataFormat> CONCEPT_TYPES = new HashSet<>(Arrays.asList(KOMET_CONCEPT_VERSION_PROXY, KOMET_CONCEPT_PROXY));
    public static final Set<DataFormat> PATTERN_TYPES = new HashSet<>(Arrays.asList(KOMET_PATTERN_VERSION_PROXY, KOMET_PATTERN_PROXY));
    public static final Set<DataFormat> SEMANTIC_TYPES = new HashSet<>(Arrays.asList(KOMET_SEMANTIC_VERSION_PROXY, KOMET_SEMANTIC_PROXY));
    private static final HashMap<DataFormat, Function<? super Component, ? extends Object>> GENERATOR_MAP
            = new HashMap<>();

    //~--- static initializers -------------------------------------------------
    static {
        GENERATOR_MAP.put(
                DataFormat.HTML,
                (t) -> {
                    throw new UnsupportedOperationException();
                });
        GENERATOR_MAP.put(
                DataFormat.PLAIN_TEXT,
                (t) -> t.publicId().toString());
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

    public static boolean containsAny(Collection<?> c1,
                                      Collection<?> c2) {
        return !Collections.disjoint(c1, c2);
    }

    private void addEntity(EntityFacade entityFacade) {
        if (entityFacade instanceof ConceptEntity conceptEntity) {
            this.put(KOMET_CONCEPT_PROXY, conceptEntity.toXmlFragment());
        } else if (entityFacade instanceof PatternEntity patternEntity) {
            this.put(KOMET_PATTERN_PROXY, patternEntity.toXmlFragment());
        } else if (entityFacade instanceof SemanticEntity semanticEntity) {
            this.put(KOMET_SEMANTIC_PROXY, semanticEntity.toXmlFragment());
        } else {
            Entity<?> entity = Entity.getFast(entityFacade);
            addEntity(entity);
        }
    }

    //~--- methods -------------------------------------------------------------
    private void addExtra(DataFormat format, Component component) {
        put(format, GENERATOR_MAP.get(format)
                .apply(component));
    }

    @Override
    public Object get(Object key) {
        return super.get(key);
    }
}
