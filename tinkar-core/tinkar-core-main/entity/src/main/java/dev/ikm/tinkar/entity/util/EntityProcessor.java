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
package dev.ikm.tinkar.entity.util;

import dev.ikm.tinkar.common.util.time.Stopwatch;
import dev.ikm.tinkar.component.FieldDataType;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ObjIntConsumer;

public abstract class EntityProcessor implements ObjIntConsumer<byte[]> {

    AtomicInteger totalCount = new AtomicInteger();
    AtomicInteger conceptCount = new AtomicInteger();
    AtomicInteger semanticCount = new AtomicInteger();
    AtomicInteger patternCount = new AtomicInteger();
    AtomicInteger stampCount = new AtomicInteger();
    AtomicInteger other = new AtomicInteger();
    Stopwatch stopwatch = new Stopwatch();

    @Override
    public void accept(byte[] bytes, int value) {
        // bytes starts with number of arrays (int = 4 bytes), then size of first array (int = 4 bytes), then entity format version then type token, -1 since index starts at 0...
        FieldDataType componentType = FieldDataType.fromToken(bytes[9]);
        switch (componentType) {
            case PATTERN_CHRONOLOGY:
                patternCount.incrementAndGet();
                totalCount.incrementAndGet();
                break;
            case CONCEPT_CHRONOLOGY:
                conceptCount.incrementAndGet();
                totalCount.incrementAndGet();
                break;
            case SEMANTIC_CHRONOLOGY:
                semanticCount.incrementAndGet();
                totalCount.incrementAndGet();
                break;
            case STAMP:
                stampCount.incrementAndGet();
                totalCount.incrementAndGet();
                break;
            default:
                other.incrementAndGet();
                totalCount.incrementAndGet();
        }
        processBytesForType(componentType, bytes);
    }

    public abstract void processBytesForType(FieldDataType componentType, byte[] bytes);

    public void finish() {
        this.stopwatch.end();
    }

    public String report() {
        this.stopwatch.end();
        StringBuilder sb = new StringBuilder();
        sb.append("Finished: ").append(this.getClass().getSimpleName());
        sb.append("\nDuration: ").append(stopwatch.durationString());
        sb.append("\nAverage realization time: ").append(stopwatch.averageDurationForElementString(totalCount.get()));
        if (conceptCount.get() > 0) {
            sb.append("\nConcepts: ").append(conceptCount);
        }
        if (semanticCount.get() > 0) {
            sb.append("\nSemantics: ").append(semanticCount);
        }
        if (patternCount.get() > 0) {
            sb.append("\nType pattern: ").append(patternCount);
        }
        if (stampCount.get() > 0) {
            sb.append("\nStamps: ").append(stampCount);
        }
        if (other.get() > 0) {
            sb.append("\nOthers: ").append(other);
        }
        sb.append("\n");
        return sb.toString();
    }
}
