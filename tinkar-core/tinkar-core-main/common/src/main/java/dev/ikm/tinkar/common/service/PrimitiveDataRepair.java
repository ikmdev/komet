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
package dev.ikm.tinkar.common.service;

/**
 * Methods to handle data repair tasks intended for fixing issues related to
 * legacy imports, or data received from 3rd parties that might have to be repaired
 * in the underlying data store. <b>These methods are not intended for routine use
 * by application users which could invalidate user actions outside the normal
 * process of inactivating mistakes or "redoing" actions.</b>
 */
public interface PrimitiveDataRepair {
    /**
     * Erase all references to components associated with this native
     * identifier, <b>bypassing the change set journaling process</b>.
     * @param nid native identifier for the component to erase.
     */
    void erase(int nid);

    /**
     * Bypasses the normal merge process of merging versions.
     * @param nid identifier to associate these bytes to with.
     * @param bytesToOverwrite the bytes to overwrite any existing bytes with.
     */
    void put(int nid, byte[] bytesToOverwrite);

    /**
     * Copy versions associated with the component identified by nidToErase
     * into the chronology identified by nidToMergeInto, then erase the chronology
     * identified by nidToErase.
     * @param nidToErase native identifier for the component to erase.
     * @param nidToMergeInto native identifier for the component to accept versions
     *                       from the component being erased.
     */
    void mergeThenErase(int nidToErase, int nidToMergeInto);

}
