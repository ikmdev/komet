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
package dev.ikm.tinkar.coordinate.navigation;

import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.coordinate.stamp.StateSet;

public interface NavigationCoordinateDelegate extends NavigationCoordinate {
    NavigationCoordinate navigationCoordinate();

    @Override
    default IntIdSet navigationPatternNids() {
        return navigationCoordinate().navigationPatternNids();
    }

    @Override
    default StateSet vertexStates() {
        return navigationCoordinate().vertexStates();
    }

    @Override
    default boolean sortVertices() {
        return navigationCoordinate().sortVertices();
    }

    @Override
    default IntIdList verticesSortPatternNidList() {
        return navigationCoordinate().verticesSortPatternNidList();
    }

    @Override
    default NavigationCoordinateRecord toNavigationCoordinateRecord() {
        return navigationCoordinate().toNavigationCoordinateRecord();
    }
}
