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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A minimal recursive-descent JSON value parser (objects, arrays, strings, numbers, booleans,
 * {@code null}) — hand-rolled so {@link NexusSearchClient} doesn't need to pull in a JSON
 * library. {@code jackson-databind} is declared in {@code komet-bom}'s dependency management
 * but has zero real consumers anywhere in this reactor; adding a use for it here just to parse
 * one narrow, well-known response shape would repeat this session's earlier Maven Resolver /
 * JPMS dependency surprise rather than avoid it.
 */
final class MinimalJsonParser {

    private final String json;
    private int position;

    private MinimalJsonParser(String json) {
        this.json = json;
    }

    /**
     * Parses a complete JSON document into plain Java values: {@code Map<String, Object>} for
     * objects, {@code List<Object>} for arrays, {@code String}, {@code Double}, {@code Boolean},
     * or {@code null}.
     *
     * @param json the JSON text to parse
     * @return the parsed value
     * @throws IOException if {@code json} is not well-formed
     */
    static Object parse(String json) throws IOException {
        MinimalJsonParser parser = new MinimalJsonParser(json);
        parser.skipWhitespace();
        Object value = parser.parseValue();
        parser.skipWhitespace();
        if (parser.position != json.length()) {
            throw new IOException("Unexpected trailing content at position " + parser.position);
        }
        return value;
    }

    private Object parseValue() throws IOException {
        char c = peek();
        return switch (c) {
            case '{' -> parseObject();
            case '[' -> parseArray();
            case '"' -> parseString();
            case 't', 'f' -> parseBoolean();
            case 'n' -> parseNull();
            default -> parseNumber();
        };
    }

    private Map<String, Object> parseObject() throws IOException {
        expect('{');
        Map<String, Object> result = new LinkedHashMap<>();
        skipWhitespace();
        if (peek() == '}') {
            position++;
            return result;
        }
        while (true) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            expect(':');
            skipWhitespace();
            result.put(key, parseValue());
            skipWhitespace();
            if (expectOneOf(',', '}') == '}') {
                return result;
            }
        }
    }

    private List<Object> parseArray() throws IOException {
        expect('[');
        List<Object> result = new ArrayList<>();
        skipWhitespace();
        if (peek() == ']') {
            position++;
            return result;
        }
        while (true) {
            skipWhitespace();
            result.add(parseValue());
            skipWhitespace();
            if (expectOneOf(',', ']') == ']') {
                return result;
            }
        }
    }

    private String parseString() throws IOException {
        expect('"');
        StringBuilder builder = new StringBuilder();
        while (true) {
            if (position >= json.length()) {
                throw new IOException("Unterminated string");
            }
            char c = json.charAt(position++);
            if (c == '"') {
                return builder.toString();
            }
            if (c != '\\') {
                builder.append(c);
                continue;
            }
            if (position >= json.length()) {
                throw new IOException("Unterminated escape sequence");
            }
            char escaped = json.charAt(position++);
            switch (escaped) {
                case '"' -> builder.append('"');
                case '\\' -> builder.append('\\');
                case '/' -> builder.append('/');
                case 'b' -> builder.append('\b');
                case 'f' -> builder.append('\f');
                case 'n' -> builder.append('\n');
                case 'r' -> builder.append('\r');
                case 't' -> builder.append('\t');
                case 'u' -> {
                    if (position + 4 > json.length()) {
                        throw new IOException("Truncated unicode escape");
                    }
                    builder.append((char) Integer.parseInt(json.substring(position, position + 4), 16));
                    position += 4;
                }
                default -> throw new IOException("Unknown escape sequence: \\" + escaped);
            }
        }
    }

    private Boolean parseBoolean() throws IOException {
        if (json.startsWith("true", position)) {
            position += 4;
            return Boolean.TRUE;
        }
        if (json.startsWith("false", position)) {
            position += 5;
            return Boolean.FALSE;
        }
        throw new IOException("Invalid literal at position " + position);
    }

    private Object parseNull() throws IOException {
        if (json.startsWith("null", position)) {
            position += 4;
            return null;
        }
        throw new IOException("Invalid literal at position " + position);
    }

    private Double parseNumber() throws IOException {
        int start = position;
        if (peek() == '-') {
            position++;
        }
        while (position < json.length() && isNumberChar(json.charAt(position))) {
            position++;
        }
        if (position == start) {
            throw new IOException("Invalid value at position " + position);
        }
        return Double.parseDouble(json.substring(start, position));
    }

    private static boolean isNumberChar(char c) {
        return Character.isDigit(c) || c == '.' || c == 'e' || c == 'E' || c == '+' || c == '-';
    }

    private void skipWhitespace() {
        while (position < json.length() && Character.isWhitespace(json.charAt(position))) {
            position++;
        }
    }

    private char peek() throws IOException {
        if (position >= json.length()) {
            throw new IOException("Unexpected end of JSON input");
        }
        return json.charAt(position);
    }

    private void expect(char expected) throws IOException {
        if (peek() != expected) {
            throw new IOException("Expected '" + expected + "' at position " + position);
        }
        position++;
    }

    private char expectOneOf(char a, char b) throws IOException {
        char c = peek();
        if (c != a && c != b) {
            throw new IOException("Expected '" + a + "' or '" + b + "' at position " + position);
        }
        position++;
        return c;
    }
}
