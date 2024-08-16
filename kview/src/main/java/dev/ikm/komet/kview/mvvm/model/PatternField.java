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
package dev.ikm.komet.kview.mvvm.model;

import dev.ikm.tinkar.terms.EntityFacade;

/**
 * object to capture data for a Pattern>Field
 * @param fieldOrder order in 1..n
 * @param displayName display name
 * @param dataType tinkar data type
 * @param purpose purpose concept
 * @param meaning meaning concept
 * @param comments user comments
 */
public record PatternField(int fieldOrder, String displayName, EntityFacade dataType,
                          EntityFacade purpose, EntityFacade meaning, String comments) {

}
