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
package dev.ikm.tinkar.ext.binding;

/**
 * Text Block Templates used for Java Interpolation and Binding
 */
public class JavaTemplates {
    public static final String JAVA_BINDING_TEMPLATE =
            """
            ${package}
                            
            ${imports}
                            
            /**
             * Tinkar Bindings class to enable programmatic access to tinkar data elements known to be stored in an unarbitrary database.
             * @author  ${author}
             */
            public class ${className} {
            
                /**
                 * Namespace used in the UUID creation process for tinkar components (e.g., Concept, Pattern, Semantic, and STAMP)
                 */
                public static final UUID NAMESPACE = UUID.fromString("${namespaceUUID}");
               
                ${concepts}
                
                ${patterns}
                
                ${semantics}
                
            }
            """;

    public static final String JAVA_CONCEPT_COMMENT_TEMPLATE =
            """
                 
                /**
                 * Java binding for the concept described as ${conceptCommentDescription} and identified by the following UUID(s):
                 * <ul>
                 ${uuids}
                 * </ul>
                 */
                public static final Concept ${conceptVariable} = Concept.make("${conceptDescription}", PublicIds.of(${conceptPublicId}));
            """;

    public static final String JAVA_COMMENT_BLOCK_UUID_TEMPLATE =
            """
                 * <li>${entityUUID}
            """;

    public static final String JAVA_PATTERN_COMMENT_TEMPLATE =
            """
                 
                /**
                 * Java binding for the pattern described as ${patternCommentDescription} identified by the following as UUID(s):
                 * <ul>
                 ${uuids}
                 * </ul>
                 * <p>
                 * Pattern contains the following fields:
                 * <ul>
                 ${patternFieldDefinitions}
                 * <ul>
                 */
                public static final Pattern ${patternVariable} = Pattern.make("${patternDescription}", PublicIds.of(${patternPublicId}));
            """;

    public static final String JAVA_PATTERN_FIELD_DEFINITION_TEMPLATE =
            """
                 * <li>Field ${fieldIndex} is a ${fieldDataType} that represents ${fieldMeaning}.
            """;

}
