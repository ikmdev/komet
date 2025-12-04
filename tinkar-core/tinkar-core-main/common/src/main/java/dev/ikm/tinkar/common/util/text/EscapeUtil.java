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
package dev.ikm.tinkar.common.util.text;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class EscapeUtil {
    /**
     * Escape characters for text appearing as XML data, between tags.
     *
     * <p>The following characters are replaced with corresponding character entities:</p>
     * <table border='1' cellpadding='3' cellspacing='0'>
     * <tr><th> Character </th><th> Encoding </th></tr>
     * <tr><td> &lt; </td><td> &amp;lt; </td></tr>
     * <tr><td> &gt; </td><td> &amp;gt; </td></tr>
     * <tr><td> &amp; </td><td> &amp;amp; </td></tr>
     * <tr><td> &quot; </td><td> &amp;quot;</td></tr>
     * <tr><td> &#039; </td><td> &amp;#039;</td></tr>
     * </table>
     */
    public static String forXML(String aText){
        final StringBuilder result = new StringBuilder();
        final StringCharacterIterator iterator = new StringCharacterIterator(aText);
        char character =  iterator.current();
        while (character != CharacterIterator.DONE ){
            if (character == '<') {
                result.append("&lt;");
            }
            else if (character == '>') {
                result.append("&gt;");
            }
            else if (character == '\"') {
                result.append("&quot;");
            }
            else if (character == '\'') {
                result.append("&#039;");
            }
            else if (character == '&') {
                result.append("&amp;");
            }
            else {
                //the char is not a special one
                //add it to the result as is
                result.append(character);
            }
            character = iterator.next();
        }
        return result.toString();
    }

    public static String fromXML(String aText) {
        aText = aText.replace("&lt;", "<");
        aText = aText.replace("&quot;", ">");
        aText = aText.replace("&lt;", "\"");
        return aText.replace("&amp;", "&");
    }
}