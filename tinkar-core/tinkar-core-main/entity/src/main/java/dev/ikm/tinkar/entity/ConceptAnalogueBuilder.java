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

public record ConceptAnalogueBuilder(ConceptRecord analogue,
                                     RecordListBuilder<ConceptVersionRecord> analogVersions) {
    /**
     * If there is a version with the same stamp as versionToAdd, it will be removed prior to adding the
     * new version so you don't get duplicate versions with the same stamp.
     *
     * @param versionToAdd
     * @return
     */
    public ConceptAnalogueBuilder with(ConceptVersionRecord versionToAdd) {
        return add(versionToAdd);
    }

    /**
     * If there is a version with the same stamp as versionToAdd, it will be removed prior to adding the
     * new version, so you don't get duplicate versions with the same stamp.
     *
     * @param versionToAdd
     * @return
     */
    public ConceptAnalogueBuilder add(ConceptVersionRecord versionToAdd) {
        remove(versionToAdd);
        analogVersions.add(new ConceptVersionRecord(analogue, versionToAdd.stampNid()));
        return this;
    }

    /**
     * Removal is based on equivalence of stampNid, not based on a deep equals.
     *
     * @param versionToRemove
     * @return
     */
    public ConceptAnalogueBuilder remove(ConceptEntityVersion versionToRemove) {
        analogVersions.removeIf(conceptVersionRecord -> conceptVersionRecord.stampNid() == versionToRemove.stampNid());
        return this;
    }

    /**
     * Removal is based on equivalence of stampNid, not based on a deep equals.
     *
     * @param versionToRemove
     * @return
     */
    public ConceptAnalogueBuilder without(ConceptVersionRecord versionToRemove) {
        return remove(versionToRemove);
    }

    public ConceptRecord build() {
        analogVersions.build();
        return analogue;
    }
}
