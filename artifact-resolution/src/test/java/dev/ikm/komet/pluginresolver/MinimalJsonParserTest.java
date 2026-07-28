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
package dev.ikm.komet.pluginresolver;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinimalJsonParserTest {

    @Test
    void parsesFlatObject() throws IOException {
        Object parsed = MinimalJsonParser.parse("{\"group\":\"dev.ikm.komet\",\"name\":\"artifact-resolution\",\"assets\":42,\"latest\":true,\"extra\":null}");
        assertTrue(parsed instanceof Map);
        Map<?, ?> map = (Map<?, ?>) parsed;
        assertEquals("dev.ikm.komet", map.get("group"));
        assertEquals("artifact-resolution", map.get("name"));
        assertEquals(42.0, map.get("assets"));
        assertEquals(Boolean.TRUE, map.get("latest"));
        assertNull(map.get("extra"));
    }

    @Test
    void parsesNestedArraysAndObjects() throws IOException {
        Object parsed = MinimalJsonParser.parse("""
                {"items":[{"group":"g1","name":"a1","assets":[{"path":"x/y.zip"}]},{"group":"g2","name":"a2","assets":[]}],"continuationToken":null}
                """);
        Map<?, ?> root = (Map<?, ?>) parsed;
        List<?> items = (List<?>) root.get("items");
        assertEquals(2, items.size());
        Map<?, ?> first = (Map<?, ?>) items.get(0);
        assertEquals("g1", first.get("group"));
        List<?> assets = (List<?>) first.get("assets");
        assertEquals(1, assets.size());
        assertEquals("x/y.zip", ((Map<?, ?>) assets.get(0)).get("path"));
    }

    @Test
    void unescapesStringContent() throws IOException {
        Object parsed = MinimalJsonParser.parse("\"line1\\nline2\\t\\\"quoted\\\"\\u0041\"");
        assertEquals("line1\nline2\t\"quoted\"A", parsed);
    }

    @Test
    void parsesTopLevelScalars() throws IOException {
        assertEquals(3.5, MinimalJsonParser.parse("3.5"));
        assertEquals(Boolean.FALSE, MinimalJsonParser.parse("false"));
        assertNull(MinimalJsonParser.parse("null"));
        assertEquals("bare", MinimalJsonParser.parse("\"bare\""));
    }

    @Test
    void rejectsTrailingContent() {
        assertThrows(IOException.class, () -> MinimalJsonParser.parse("{}garbage"));
    }

    @Test
    void rejectsMalformedInput() {
        assertThrows(IOException.class, () -> MinimalJsonParser.parse("{\"unterminated\": "));
        assertThrows(IOException.class, () -> MinimalJsonParser.parse("[1, 2,]"));
    }
}
