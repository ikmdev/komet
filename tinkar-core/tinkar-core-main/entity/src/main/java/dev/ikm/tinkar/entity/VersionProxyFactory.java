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
package dev.ikm.tinkar.entity;

import dev.ikm.tinkar.common.util.text.EscapeUtil;
import dev.ikm.tinkar.common.util.uuid.UuidUtil;
import dev.ikm.tinkar.component.Version;
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

import static dev.ikm.tinkar.terms.ProxyFactory.CONCEPT_ELEMENT;
import static dev.ikm.tinkar.terms.ProxyFactory.DESCRIPTION_ATTRIBUTE;
import static dev.ikm.tinkar.terms.ProxyFactory.ENTITY_ELEMENT;
import static dev.ikm.tinkar.terms.ProxyFactory.PATTERN_ELEMENT;
import static dev.ikm.tinkar.terms.ProxyFactory.SEMANTIC_ELEMENT;
import static dev.ikm.tinkar.terms.ProxyFactory.UUIDS_ATTRIBUTE;

public class VersionProxyFactory {
    public static final String STAMP_UUIDS_ATTRIBUTE = "stamp";

    private static ThreadLocal<DocumentBuilder> documentBuilder = ThreadLocal.withInitial(() -> {
        try {
            return DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    });

    public static <T extends VersionProxy> T fromXmlFragment(String xmlString) {
        try {
            Document doc = documentBuilder.get().parse(new InputSource(new StringReader(xmlString)));
            Element element = doc.getDocumentElement();
            Attr uuids = element.getAttributeNode(UUIDS_ATTRIBUTE);
            Attr desc = element.getAttributeNode(DESCRIPTION_ATTRIBUTE);
            Attr stamp = element.getAttributeNode(STAMP_UUIDS_ATTRIBUTE);

            return (T) switch (element.getTagName()) {
                case ENTITY_ELEMENT -> VersionProxy.make(desc.getValue(), UuidUtil.fromString(uuids.getValue()), UuidUtil.fromString(stamp.getValue()));
                case PATTERN_ELEMENT -> VersionProxy.Pattern.make(desc.getName(), UuidUtil.fromString(uuids.getValue()), UuidUtil.fromString(stamp.getValue()));
                case SEMANTIC_ELEMENT -> VersionProxy.Semantic.make(desc.getName(), UuidUtil.fromString(uuids.getValue()), UuidUtil.fromString(stamp.getValue()));
                case CONCEPT_ELEMENT -> VersionProxy.Concept.make(desc.getName(), UuidUtil.fromString(uuids.getValue()), UuidUtil.fromString(stamp.getValue()));
                default -> throw new IllegalStateException("Unexpected value: " + element.getTagName());
            };
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    static String toXmlFragment(Version version) {
        StringBuilder sb = new StringBuilder("<");
        if (version instanceof dev.ikm.tinkar.component.Concept) {
            sb.append(CONCEPT_ELEMENT).append(" " +
                    DESCRIPTION_ATTRIBUTE + "=\"");
        } else if (version instanceof dev.ikm.tinkar.component.Semantic) {
            sb.append(SEMANTIC_ELEMENT).append(" " +
                    DESCRIPTION_ATTRIBUTE + "=\"");
        } else if (version instanceof dev.ikm.tinkar.component.Pattern) {
            sb.append(PATTERN_ELEMENT).append(" " +
                    DESCRIPTION_ATTRIBUTE + "=\"");
        } else {
            sb.append(ENTITY_ELEMENT).append(" " +
                    DESCRIPTION_ATTRIBUTE + "=\"");
        }
        if (version instanceof EntityVersion entityVersion) {
            sb.append(EscapeUtil.forXML(entityVersion.chronology().description()));
        } else {
            sb.append("No description");
        }
        sb.append("\" " +
                UUIDS_ATTRIBUTE + "=\"");
        sb.append(UuidUtil.toString(version.publicId()));

        sb.append(" ");
        sb.append(STAMP_UUIDS_ATTRIBUTE).append("=\"");
        sb.append(UuidUtil.toString(version.stamp().publicId()));
        sb.append("\"/>");
        return sb.toString();
    }
}
