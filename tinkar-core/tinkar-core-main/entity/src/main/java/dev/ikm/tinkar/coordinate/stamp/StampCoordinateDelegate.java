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
package dev.ikm.tinkar.coordinate.stamp;

import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIdSet;

public interface StampCoordinateDelegate extends StampCoordinate {

    StampCoordinate getStampFilter();

    @Override
    default int pathNidForFilter() {
        return getStampFilter().pathNidForFilter();
    }

    @Override
    default StateSet allowedStates() {
        return getStampFilter().allowedStates();
    }

    @Override
    default IntIdSet moduleNids() {
        return getStampFilter().moduleNids();
    }

    @Override
    default IntIdList modulePriorityNidList() {
        return getStampFilter().modulePriorityNidList();
    }

    @Override
    default StampPosition stampPosition() {
        return getStampFilter().stampPosition();
    }

    @Override
    default StampCoordinate withAllowedStates(StateSet stateSet) {
        return getStampFilter().withAllowedStates(stateSet);
    }

    @Override
    default StampCoordinate withStampPositionTime(long stampPositionTime) {
        return getStampFilter().withStampPositionTime(stampPositionTime);
    }

    @Override
    default IntIdSet excludedModuleNids() {
        return getStampFilter().excludedModuleNids();
    }

    @Override
    default StampCoordinate withStampPosition(StampPositionRecord stampPosition) {
        throw new UnsupportedOperationException();
    }

    @Override
    default StampCoordinate withModuleNids(IntIdSet moduleNids) {
        throw new UnsupportedOperationException();
    }

    @Override
    default StampCoordinate withExcludedModuleNids(IntIdSet excludedModuleNids) {
        throw new UnsupportedOperationException();
    }

    @Override
    default StampCoordinate withModulePriorityNidList(IntIdList modulePriorityNidList) {
        throw new UnsupportedOperationException();
    }
}
