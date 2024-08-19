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
package dev.ikm.komet.kview.mvvm.viewmodel;

import dev.ikm.tinkar.terms.EntityFacade;

import java.util.Collections;
import java.util.Set;

import static dev.ikm.tinkar.terms.TinkarTerm.*;

public class DataViewModelHelper {

    public static final Set<EntityFacade> DATA_TYPE_OPTIONS = Collections.unmodifiableSet(Set.of(
            STRING,
            COMPONENT_FIELD,
            COMPONENT_ID_SET_FIELD,
            COMPONENT_ID_LIST_FIELD,
            DITREE_FIELD,
            DIGRAPH_FIELD,
            CONCEPT_FIELD,
            SEMANTIC_FIELD_TYPE,
            INTEGER_FIELD,
            FLOAT_FIELD,
            BOOLEAN_FIELD,
            BYTE_ARRAY_FIELD,
            ARRAY_FIELD,
            INSTANT_LITERAL,
            LONG,
            UUID_DATA_TYPE,
            VERTEX_FIELD
    ));
}
