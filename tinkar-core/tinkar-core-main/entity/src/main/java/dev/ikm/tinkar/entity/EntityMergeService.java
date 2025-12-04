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

import java.util.concurrent.Future;

/**
 *
 */
public interface EntityMergeService {

    /**
     * Adjudication means that some actor (human or software) will evaluate the version fields, and only include
     * all or part of these fields to make the result semantically correct.
     * This merge assumes that the entities have the same public identifier.
     * TODO: Update model to support adding new UUIDs to public ID over time. Use existing stamps in versions or make a new version for each addition. The binary merge routine will need modification.
     *
     * @param entityToMergeInto
     * @param entitiesToMergeFrom
     * @return
     */
    Future<Entity> adjudicatedMerge(Entity entityToMergeInto, Entity... entitiesToMergeFrom);

}
