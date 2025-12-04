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

/**
 * Parent interface for all <code>ImmutableEntity</code> classes.
 *
 * TODO: consider removing the Entity interface and working directly with the records.
 * Except the ObservableEntity implements the interface, so need to consider how we best approach.
 * Maybe this current approach is optimal.
 */
public sealed interface ImmutableEntity<T extends EntityVersion> extends Entity<T>
    permits ConceptRecord, SemanticRecord, StampRecord, PatternRecord {
}
