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


import dev.ikm.tinkar.common.binary.Decoder;
import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.common.binary.Encoder;
import dev.ikm.tinkar.common.binary.EncoderOutput;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.ImmutableCoordinate;
import dev.ikm.tinkar.coordinate.logic.LogicCoordinate;
import dev.ikm.tinkar.coordinate.logic.PremiseType;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.terms.TinkarTerm;
import io.soabase.recordbuilder.core.RecordBuilder;

import java.util.Objects;

@RecordBuilder
public record NavigationCoordinateRecord(IntIdSet navigationPatternNids,
                                         StateSet vertexStates,
                                         boolean sortVertices,
                                         IntIdList verticesSortPatternNidList)
        implements NavigationCoordinate, ImmutableCoordinate, NavigationCoordinateRecordBuilder.With {

    public static NavigationCoordinateRecord make(IntIdSet navigationPatternNids) {
        return new NavigationCoordinateRecord(navigationPatternNids, StateSet.ACTIVE, true, IntIds.list.empty());
    }

    public static NavigationCoordinateRecord make(IntIdSet navigationPatternNids,
                                                  StateSet vertexStates,
                                                  boolean sortVertices,
                                                  IntIdList verticesSortPatternNidList) {
        return new NavigationCoordinateRecord(navigationPatternNids, vertexStates,
                sortVertices, verticesSortPatternNidList);
    }

    public static NavigationCoordinateRecord makeInferred() {
        return new NavigationCoordinateRecord(
                IntIds.set.of(TinkarTerm.INFERRED_NAVIGATION_PATTERN.nid()),
                StateSet.ACTIVE_AND_INACTIVE, true, IntIds.list.empty());
    }

    public static NavigationCoordinateRecord makeStated() {
        return new NavigationCoordinateRecord(
                IntIds.set.of(TinkarTerm.STATED_NAVIGATION_PATTERN.nid()),
                StateSet.ACTIVE_AND_INACTIVE, true, IntIds.list.empty());
    }

    public static NavigationCoordinateRecord make(PremiseType premiseType) {
        if (premiseType == PremiseType.INFERRED) {
            return makeInferred(Coordinates.Logic.ElPlusPlus());
        }
        return makeStated(Coordinates.Logic.ElPlusPlus());
    }

    public static NavigationCoordinateRecord makeInferred(LogicCoordinate logicCoordinate) {
        return new NavigationCoordinateRecord(
                IntIds.set.of(logicCoordinate.inferredAxiomsPatternNid()),
                StateSet.ACTIVE_AND_INACTIVE, true, IntIds.list.empty());
    }

    public static NavigationCoordinateRecord makeStated(LogicCoordinate logicCoordinate) {
        return new NavigationCoordinateRecord(
                IntIds.set.of(logicCoordinate.statedAxiomsPatternNid()),
                StateSet.ACTIVE_AND_INACTIVE, true, IntIds.list.empty());
    }

    @Decoder
    public static NavigationCoordinateRecord decode(DecoderInput in) {
        switch (Encodable.checkVersion(in)) {
            default:
                return new NavigationCoordinateRecord(IntIds.set.of(in.readNidArray()),
                        StateSet.decode(in),
                        in.readBoolean(), IntIds.list.of(in.readNidArray()));
        }
    }

    @Override
    public IntIdList verticesSortPatternNidList() {
        return this.verticesSortPatternNidList;
    }

    @Override
    public NavigationCoordinateRecord toNavigationCoordinateRecord() {
        return this;
    }

    @Override
    public boolean sortVertices() {
        return this.sortVertices;
    }

    @Override
    @Encoder
    public void encode(EncoderOutput out) {
        out.writeNidArray(this.navigationPatternNids.toArray());
        vertexStates.encode(out);
        out.writeBoolean(this.sortVertices);
        out.writeNidArray(this.verticesSortPatternNidList.toArray());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NavigationCoordinateRecord that)) return false;
        return navigationPatternNids().equals(that.navigationPatternNids());
    }

    @Override
    public int hashCode() {
        return Objects.hash(navigationPatternNids());
    }

    @Override
    public String toString() {
        /*
        IntIdSet navigationPatternNids,
                                         StateSet vertexStates,
                                         boolean sortVertices,
                                         IntIdList verticesSortPatternNidList
         */
        StringBuilder sb = new StringBuilder("NavigationCoordinateRecord{");

        sb.append("navigationConcepts=[");
        for (int nid : navigationPatternNids.toArray()) {
            sb.append(PrimitiveData.text(nid));
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length() - 1);
        sb.append("], ");
        sb.append("vertexStates=").append(vertexStates);
        sb.append(", sortVertices=").append(sortVertices);
        sb.append(", verticesSortPatternList=[");
        for (int nid : verticesSortPatternNidList.toArray()) {
            sb.append(PrimitiveData.text(nid));
            sb.append(", ");
        }
        if (verticesSortPatternNidList.notEmpty()) {
            sb.delete(sb.length() - 2, sb.length() - 1);
        }
        sb.append("]}");

        return sb.toString();
    }
}
