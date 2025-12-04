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

import dev.ikm.tinkar.component.FieldDataType;
import dev.ikm.tinkar.component.PatternChronology;
import dev.ikm.tinkar.terms.PatternFacade;

public interface PatternEntity<T extends PatternEntityVersion>
        extends Entity<T>,
                PatternChronology<T>,
                PatternFacade {

    @Override
    default FieldDataType entityDataType() {
        return FieldDataType.PATTERN_CHRONOLOGY;
    }

    @Override
    default FieldDataType versionDataType() {
        return FieldDataType.PATTERN_VERSION;
    }


    /**
     * Last version by time, for use in select circumstances when a stamp calculator is not attainable.
     * Use of the stamp calculator is strongly preferred.
     * @return
     */
    default PatternEntityVersion lastVersion() {
        if (versions().size() == 1) {
            return versions().get(0);
        }
        PatternEntityVersion latest = null;
        for (PatternEntityVersion version : versions()) {
            if (version.time() == Long.MIN_VALUE) {
                // if canceled (Long.MIN_VALUE), latest is canceled.
                return version;
            } else if (latest == null) {
                latest = version;
            } else if (latest.time() == Long.MAX_VALUE) {
                latest = version;
            } else if (version.time() == Long.MAX_VALUE) {
                // ignore uncommitted version;
            } else if (latest.time() < version.time()) {
                latest = version;
            }
        }
        return latest;
    }

}
