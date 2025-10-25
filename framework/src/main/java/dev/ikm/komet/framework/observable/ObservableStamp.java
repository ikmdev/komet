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
package dev.ikm.komet.framework.observable;

import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;

public final class ObservableStamp
        extends ObservableEntity<ObservableStampVersion> {
    ObservableStamp(StampEntity<StampVersionRecord> stampEntity) {
        super(stampEntity);
    }

    @Override
    protected ObservableStampVersion wrap(EntityVersion version) {
        return new ObservableStampVersion((StampVersionRecord) version);
    }

    @Override
    public ObservableEntitySnapshot getSnapshot(ViewCalculator calculator) {
        throw new UnsupportedOperationException();
    }

    /**
     * Retrieves the most recent version of the observable stamp based on the timestamp.
     * If there is only one version, it returns that version.
     * In the case of multiple versions, it evaluates each version to determine the latest one.
     * Versions with a timestamp of {@code Long.MIN_VALUE} (cancelled) take precedence and are immediately returned.
     * Uncommitted versions marked with {@code Long.MAX_VALUE} are ignored in the evaluation of the latest version.
     *
     * @return The latest {@link ObservableStampVersion}, or the canceled version if one exists.
     */
    public ObservableStampVersion lastVersion() {
        if (versions().size() == 1) {
            return versions().get(0);
        }
        ObservableStampVersion latest = null;
        for (ObservableStampVersion version : versions()) {
            if (version.time() == Long.MIN_VALUE) {
                // if canceled (Long.MIN_VALUE), the latest is canceled.
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

    @Override
    protected void addAdditionalChronologyFeatures(MutableList<Feature> features) {
        // Nothing to add...
    }
}
