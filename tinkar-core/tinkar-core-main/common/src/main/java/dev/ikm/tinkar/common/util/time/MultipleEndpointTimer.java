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
package dev.ikm.tinkar.common.util.time;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

/**
 * Supports timing on concurrent threads with statistics for multiple endpoints.
 * @param <T>
 */
public class MultipleEndpointTimer<T extends Enum<T>> {
    final Class<T> endPointsEnumClass;
    final SumInfo[] sumInfoArray;
    final SumInfo globalInfo = new SumInfo();

    final T[] enums;

    public MultipleEndpointTimer(Class<T> endPointsEnumClass) {
        this.endPointsEnumClass = endPointsEnumClass;
        enums = endPointsEnumClass.getEnumConstants();
        sumInfoArray = new SumInfo[enums.length];
        for (T enumValue: enums) {
            sumInfoArray[enumValue.ordinal()] = new SumInfo();
        }
    }

    public Stopwatch startNew() {
        return new Stopwatch();
    }
    public String progress() {
        StringBuilder sb = new StringBuilder();
        sb.append("Processed ");
        appendSumInfo(sb, globalInfo);
        sb.append("\n");
        return sb.toString();
    }

    public String summary() {
        StringBuilder sb = new StringBuilder();
        for (T enumValue: enums) {
            sb.append(enumValue.toString()).append(": ");
            appendSumInfo(sb, sumInfoArray[enumValue.ordinal()]);
            sb.append("\n");
        }

        sb.append("Overall: ");
        appendSumInfo(sb, globalInfo);
        sb.append("\n");
        return sb.toString();
    }

    private void appendSumInfo(StringBuilder sb, SumInfo sumInfoForEndpoint) {
        Duration minDuration = Duration.ofNanos(sumInfoForEndpoint.min.get());
        Duration maxDuration = Duration.ofNanos(sumInfoForEndpoint.max.get());
        long count = sumInfoForEndpoint.count.sum();
        Duration averageDuration = Duration.ofNanos((long)(sumInfoForEndpoint.sum.sum() / count));
        sb.append(String.format("count: %,d", count));
        sb.append(" min: ").append(DurationUtil.format(minDuration));
        sb.append(" mean: ").append(DurationUtil.format(averageDuration));
        sb.append(" max: ").append(DurationUtil.format(maxDuration));
    }

    private record SumInfo(LongAdder count, DoubleAdder sum, AtomicLong min, AtomicLong max) {
        public SumInfo() {
            this(new LongAdder(), new DoubleAdder(), new AtomicLong(Long.MAX_VALUE), new AtomicLong(Long.MIN_VALUE));
        }
        public void accept(long nanoseconds) {
            count.increment();
            sum.add(nanoseconds);
            long currentMax = max.get();
            while (nanoseconds > currentMax) {
                max.compareAndSet(currentMax, nanoseconds);
                currentMax = max.get();
            }
            long currentMin = min.get();
            while (nanoseconds < currentMin) {
                min.compareAndSet(currentMin, nanoseconds);
                currentMin = min.get();
            }
        }
    }

    public class Stopwatch {
        private final Instant start = Instant.now();
        private Instant end;

        public Duration end(T endPoint) {
            this.end = Instant.now();
            SumInfo sumInfoForEndpoint = sumInfoArray[endPoint.ordinal()];
            Duration duration = Duration.between(start, end);
            long nanoseconds = duration.toNanos();
            sumInfoForEndpoint.accept(nanoseconds);
            globalInfo.accept(nanoseconds);
            return duration;
        }
        public Duration end() {
            this.end = Instant.now();
             Duration duration = Duration.between(start, end);
            globalInfo.accept(duration.toNanos());
            return duration;
        }
    }


}
