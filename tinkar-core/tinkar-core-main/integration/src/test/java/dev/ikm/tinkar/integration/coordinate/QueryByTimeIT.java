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
package dev.ikm.tinkar.integration.coordinate;

import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecordBuilder;
import dev.ikm.tinkar.coordinate.stamp.StampPositionRecord;
import dev.ikm.tinkar.coordinate.stamp.StampPositionRecordBuilder;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampEntityVersion;
import dev.ikm.tinkar.integration.TestConstants;
import dev.ikm.tinkar.integration.helper.DataStore;
import dev.ikm.tinkar.integration.helper.TestHelper;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class QueryByTimeIT {

    private static final Logger LOG = LoggerFactory.getLogger(QueryByTimeIT.class);
    private static final File DATASTORE_ROOT = TestConstants.createFilePathInTargetFromClassName.apply(
            QueryByTimeIT.class);

    @BeforeAll
    public void beforeAll() {
        TestHelper.startDataBase(DataStore.SPINED_ARRAY_STORE, DATASTORE_ROOT);
        TestHelper.loadDataFile(TestConstants.PB_STARTER_DATA_REASONED);
    }

    @Test
    @Order(1)
    @DisplayName("Test to retrieve data by multi path")
    public void retrieveDataByTimeMultiPathTest() {
        String time = "2020-10-22T12:31:04";
        LocalDateTime localDateTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        long timestamp = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        int patternNid = EntityService.get().nidForPublicId(TinkarTerm.DESCRIPTION_PATTERN);

        Set<Integer> pathNids = new HashSet<>();
        EntityService.get().forEachSemanticOfPattern(patternNid,patternEntity1 ->
            patternEntity1.stampNids().forEach(stampNid -> {
                StampEntity<? extends StampEntityVersion> stamp = EntityService.get().getStampFast(stampNid);
                pathNids.add(stamp.pathNid());
            })
        );
        pathNids.forEach(pathNid -> {
            LOG.info("PATH NID: " + EntityService.get().getEntityFast(pathNid));
            Stream<Latest<SemanticEntityVersion>> filteredVersionsStream =
                    findAllPatternsForPathByTime(patternNid, pathNid, timestamp);
            filteredVersionsStream.forEach(latestVersion -> {
                if (latestVersion.isPresent()) {
                    SemanticEntityVersion semanticVersion = latestVersion.get();
                    LOG.info("Latest version for SemanticEntity with NID " +
                            semanticVersion.nid() + " at time " + time + ": " + semanticVersion);
                }
            });
        });
    }

    Stream<Latest<SemanticEntityVersion>> findAllPatternsForPathByTime(int patternNid, int pathNid, long targetTimestamp) {
        StampPositionRecord stampPositionRecord = StampPositionRecordBuilder.builder().time(Long.MAX_VALUE).pathForPositionNid(pathNid).build();
        StampCoordinateRecord stampCoordinateRecord = StampCoordinateRecordBuilder.builder()
                .allowedStates(StateSet.ACTIVE_AND_INACTIVE)
                .stampPosition(stampPositionRecord)
                .moduleNids(IntIds.set.empty())
                .build().withStampPositionTime(Long.MAX_VALUE);
        StampCalculator stampCalculatorWithCache = stampCoordinateRecord.stampCalculator();
        return stampCalculatorWithCache.streamLatestVersionForPattern(patternNid)
                .filter(latestVersion -> {
                    if (latestVersion.isPresent()) {
                        SemanticEntityVersion semanticVersion = latestVersion.get();
                        return semanticVersion.stamp().time() > targetTimestamp;
                    }
                    return false;
                });
    }

    @Test
    @Order(2)
    @DisplayName("Test to retrieve data by time")
    public void retrieveDataByTimeTest() {
        String time = "2020-10-22T12:31:04";
        LocalDateTime localDateTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        long timestamp = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        int patternNid = EntityService.get().nidForPublicId(TinkarTerm.DESCRIPTION_PATTERN);
        Stream<Latest<SemanticEntityVersion>> filteredVersionsStream =
                findAllPatternsByTime(patternNid, timestamp);
        filteredVersionsStream.forEach(latestVersion -> {
            if (latestVersion.isPresent()) {
                SemanticEntityVersion semanticVersion = latestVersion.get();
                LOG.info("Latest version for SemanticEntity with NID " +
                        semanticVersion.nid() + " at time " + time + ": " + semanticVersion);
            }
        });
    }

    Stream<Latest<SemanticEntityVersion>> findAllPatternsByTime(int patternNid, long targetTimestamp) {
        StampCoordinateRecord stampCoordinateRecord = StampCoordinateRecord.make(StateSet.ACTIVE_AND_INACTIVE, Coordinates.Position.LatestOnDevelopment(),
                IntIds.set.empty());
        StampCalculator stampCalculatorWithCache = stampCoordinateRecord.stampCalculator();
        return stampCalculatorWithCache.streamLatestVersionForPattern(patternNid)
                .filter(latestVersion -> {
                    if (latestVersion.isPresent()) {
                        SemanticEntityVersion semanticVersion = latestVersion.get();
                        return semanticVersion.stamp().time() > targetTimestamp;
                    }
                    return false;
                });
    }

}
