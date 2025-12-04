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
package dev.ikm.tinkar.coordinate;

import dev.ikm.tinkar.coordinate.internal.PathServiceFinder;
import dev.ikm.tinkar.coordinate.stamp.StampBranchRecord;
import dev.ikm.tinkar.coordinate.stamp.StampPathImmutable;
import dev.ikm.tinkar.coordinate.stamp.StampPositionRecord;
import org.eclipse.collections.api.set.ImmutableSet;

public interface PathService {

    static PathService get() {
        return PathServiceFinder.INSTANCE.get();
    }

    ImmutableSet<StampBranchRecord> getPathBranches(int pathNid);

    ImmutableSet<StampPathImmutable> getPaths();

    ImmutableSet<StampPositionRecord> getPathOrigins(int pathNid);
}
