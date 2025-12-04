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
package dev.ikm.tinkar.terms;

import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.util.text.EscapeUtil;
import dev.ikm.tinkar.common.util.uuid.UuidUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

public class ProxyFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ProxyFactory.class);
    public static final String CONCEPT_ELEMENT = "concept";
    public static final String SEMANTIC_ELEMENT = "semantic";
    public static final String PATTERN_ELEMENT = "pattern";
    public static final String STAMP_ELEMENT = "stamp";
    public static final String ENTITY_ELEMENT = "entity";
    public static final String UUIDS_ATTRIBUTE = "uuids";
    public static final String DESCRIPTION_ATTRIBUTE = "desc";
    private static ThreadLocal<DocumentBuilder> documentBuilder = ThreadLocal.withInitial(() -> {
        try {
            return DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    });

    public static <T extends EntityProxy> Optional<T> fromXmlFragmentOptional(String xmlString) {
        if (xmlString.contains("<") && xmlString.contains(">")) {
            try {
                Document doc = documentBuilder.get().parse(new InputSource(new StringReader(xmlString)));
                Element element = doc.getDocumentElement();
                Attr uuids = element.getAttributeNode(UUIDS_ATTRIBUTE);
                Attr desc = element.getAttributeNode(DESCRIPTION_ATTRIBUTE);
                EntityProxy proxy = switch (element.getTagName()) {
                    case ENTITY_ELEMENT -> EntityProxy.make(desc.getValue(), UuidUtil.fromString(uuids.getValue()));
                    case PATTERN_ELEMENT -> EntityProxy.Pattern.make(desc.getValue(), UuidUtil.fromString(uuids.getValue()));
                    case SEMANTIC_ELEMENT -> EntityProxy.Semantic.make(desc.getValue(), UuidUtil.fromString(uuids.getValue()));
                    case CONCEPT_ELEMENT -> EntityProxy.Concept.make(desc.getValue(), UuidUtil.fromString(uuids.getValue()));
                    default -> {
                        IllegalStateException ex = new IllegalStateException("Unexpected value: " + element.getTagName());
                        AlertStreams.dispatchToRoot(ex);
                        yield null;
                    }
                };
                return (Optional<T>) Optional.ofNullable(proxy);
            } catch (SAXException | IOException e) {
                AlertStreams.dispatchToRoot(new Exception("Input string: " + xmlString, e));
            }
        }
        return Optional.empty();
    }

    public static <T extends EntityProxy> T fromFacade(EntityFacade facade) {
        return switch (facade) {
            case EntityProxy proxy -> (T) proxy;
            case ConceptFacade concept -> (T) EntityProxy.Concept.make(concept);
            case PatternFacade pattern -> (T) EntityProxy.Pattern.make(pattern.nid());
            case SemanticFacade semantic -> (T) EntityProxy.Semantic.make(semantic.nid());
            case StampFacade stamp -> (T) EntityProxy.Stamp.make(stamp.nid());
            default -> {
                IllegalStateException ex = new IllegalStateException("Unexpected value: " + facade);
                AlertStreams.dispatchToRoot(ex);
                throw new UnsupportedOperationException("Can't handle: " + facade);
            }
        };
    }

    public static <T extends EntityProxy> T fromXmlFragment(String xmlString) {
        try {
            Document doc = documentBuilder.get().parse(new InputSource(new StringReader(xmlString)));
            Element element = doc.getDocumentElement();
            Attr uuids = element.getAttributeNode(UUIDS_ATTRIBUTE);
            Attr desc = element.getAttributeNode(DESCRIPTION_ATTRIBUTE);
            return (T) switch (element.getTagName()) {
                case ENTITY_ELEMENT -> EntityProxy.make(desc.getValue(), UuidUtil.fromString(uuids.getValue()));
                case PATTERN_ELEMENT -> EntityProxy.Pattern.make(desc.getValue(), UuidUtil.fromString(uuids.getValue()));
                case SEMANTIC_ELEMENT -> EntityProxy.Semantic.make(desc.getValue(), UuidUtil.fromString(uuids.getValue()));
                case CONCEPT_ELEMENT -> EntityProxy.Concept.make(desc.getValue(), UuidUtil.fromString(uuids.getValue()));
                case STAMP_ELEMENT -> EntityProxy.Stamp.make(desc.getValue(), UuidUtil.fromString(uuids.getValue()));
                default -> throw new IllegalStateException("Unexpected value: " + element.getTagName());
            };
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toXmlFragment(EntityFacade facade) {
        StringBuilder sb = new StringBuilder("<");
        switch (facade) {
            case ConceptFacade concept -> sb.append(CONCEPT_ELEMENT).append(" " +
                    DESCRIPTION_ATTRIBUTE + "=\"");
            case PatternFacade pattern -> sb.append(PATTERN_ELEMENT).append(" " +
                    DESCRIPTION_ATTRIBUTE + "=\"");
            case SemanticFacade semantic -> sb.append(SEMANTIC_ELEMENT).append(" " +
                    DESCRIPTION_ATTRIBUTE + "=\"");
            case StampFacade stamp -> sb.append(STAMP_ELEMENT).append(" " +
                    DESCRIPTION_ATTRIBUTE + "=\"");
            case EntityProxy proxy -> sb.append(ENTITY_ELEMENT).append(" " +
                    DESCRIPTION_ATTRIBUTE + "=\"");
            default -> {
                sb.append(ENTITY_ELEMENT).append(" " +
                        DESCRIPTION_ATTRIBUTE + "=\"");
            }
        }
        sb.append(EscapeUtil.forXML(facade.description())).append("\" " +
                UUIDS_ATTRIBUTE + "=\"");
        sb.append(UuidUtil.toString(facade.publicId()));
        sb.append("\"/>");
        return sb.toString();
    }
}
