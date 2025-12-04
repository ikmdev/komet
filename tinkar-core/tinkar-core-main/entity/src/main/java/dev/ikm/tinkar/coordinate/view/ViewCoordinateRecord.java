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
package dev.ikm.tinkar.coordinate.view;

import dev.ikm.tinkar.common.binary.Decoder;
import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.common.binary.Encoder;
import dev.ikm.tinkar.common.binary.EncoderOutput;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;
import dev.ikm.tinkar.coordinate.language.LanguageCoordinate;
import dev.ikm.tinkar.coordinate.language.LanguageCoordinateRecord;
import dev.ikm.tinkar.coordinate.logic.LogicCoordinate;
import dev.ikm.tinkar.coordinate.logic.LogicCoordinateRecord;
import dev.ikm.tinkar.coordinate.navigation.NavigationCoordinate;
import dev.ikm.tinkar.coordinate.navigation.NavigationCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import io.soabase.recordbuilder.core.RecordBuilder;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.util.List;

@RecordBuilder
public record ViewCoordinateRecord(StampCoordinateRecord stampCoordinate,
                                   ImmutableList<LanguageCoordinateRecord> languageCoordinateList,
                                   LogicCoordinateRecord logicCoordinate,
                                   NavigationCoordinateRecord navigationCoordinate,
                                   EditCoordinateRecord editCoordinate)
        implements ViewCoordinate, ViewCoordinateImmutable, ViewCoordinateRecordBuilder.With {

    public static ViewCoordinateRecord make(StampCoordinateRecord viewStampFilter,
                                            LanguageCoordinate languageCoordinate,
                                            LogicCoordinate logicCoordinate,
                                            NavigationCoordinate navigationCoordinate,
                                            EditCoordinate editCoordinate) {

        return new ViewCoordinateRecord(viewStampFilter,
                Lists.immutable.of(languageCoordinate.toLanguageCoordinateRecord()),
                logicCoordinate.toLogicCoordinateRecord(),
                navigationCoordinate.toNavigationCoordinateRecord(),
                editCoordinate.toEditCoordinateRecord());
    }

    public static ViewCoordinateRecord make(StampCoordinateRecord viewStampFilter,
                                            List<? extends LanguageCoordinate> languageCoordinates,
                                            LogicCoordinate logicCoordinate,
                                            NavigationCoordinate navigationCoordinate,
                                            EditCoordinate editCoordinate) {
        MutableList<LanguageCoordinateRecord> languageCoordinateRecords = Lists.mutable.empty();
        languageCoordinates.forEach(languageCoordinate -> languageCoordinateRecords.add(languageCoordinate.toLanguageCoordinateRecord()));
        return new ViewCoordinateRecord(viewStampFilter,
                languageCoordinateRecords.toImmutable(),
                logicCoordinate.toLogicCoordinateRecord(),
                navigationCoordinate.toNavigationCoordinateRecord(),
                editCoordinate.toEditCoordinateRecord());
    }

    @Decoder
    public static ViewCoordinateRecord decode(DecoderInput in) {
        switch (Encodable.checkVersion(in)) {
            default:
                StampCoordinateRecord stampCoordinateRecord = StampCoordinateRecord.decode(in);
                int languageCoordinateCount = in.readInt();
                MutableList<LanguageCoordinateRecord> languageCoordinateRecords = Lists.mutable.ofInitialCapacity(languageCoordinateCount);
                for (int i = 0; i < languageCoordinateCount; i++) {
                    languageCoordinateRecords.add(LanguageCoordinateRecord.decode(in));
                }
                LogicCoordinateRecord logicCoordinateRecord = LogicCoordinateRecord.decode(in);
                NavigationCoordinateRecord navigationCoordinateRecord = NavigationCoordinateRecord.decode(in);
                if (in.encodingFormatVersion() > FIRST_VERSION) {
                    EditCoordinateRecord editCoordinateRecord = EditCoordinateRecord.decode(in);
                    return new ViewCoordinateRecord(stampCoordinateRecord,
                            languageCoordinateRecords.toImmutable(),
                            logicCoordinateRecord,
                            navigationCoordinateRecord,
                            editCoordinateRecord);
                }
                return new ViewCoordinateRecord(stampCoordinateRecord,
                        languageCoordinateRecords.toImmutable(),
                        logicCoordinateRecord,
                        navigationCoordinateRecord,
                        Coordinates.Edit.Default());
        }
    }

    @Override
    public ViewCoordinateRecord toViewCoordinateRecord() {
        return this;
    }

    @Override
    public Iterable<LanguageCoordinateRecord> languageCoordinateIterable() {
        return languageCoordinateList();
    }

    @Override
    @Encoder
    public void encode(EncoderOutput out) {
        stampCoordinate.encode(out);
        out.writeInt(languageCoordinateList.size());
        for (LanguageCoordinateRecord languageCoordinateRecord : languageCoordinateList) {
            languageCoordinateRecord.encode(out);
        }
        logicCoordinate.encode(out);
        navigationCoordinate.encode(out);
        editCoordinate.encode(out);
    }
}
