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

public class Stopwatch {
    private Instant startTime;
    private Instant lastUpdate;
    private Instant endTime;
    private Duration updateInterval = Duration.ofMillis(100);

    public Stopwatch() {
        this.startTime = Instant.now();
        this.lastUpdate = startTime;
    }

    public Stopwatch(Duration updateInterval) {
        this.startTime = Instant.now();
        this.lastUpdate = startTime;
        this.updateInterval = updateInterval;
    }

    public void end() {
        this.endTime = Instant.now();
    }

    public void reset() {
        this.startTime = Instant.now();
    }

    public void stop() {
        this.endTime = Instant.now();
    }

    public boolean updateIntervalElapsed() {
        Instant now = Instant.now();
        if (this.updateInterval.compareTo(Duration.between(this.lastUpdate, now)) < 0) {
            this.lastUpdate = now;
            return true;
        }
        return false;
    }

    public String durationString() {
        return DurationUtil.format(duration());
    }

    public Duration duration() {
        Instant endForDuration = endTime;
        if (endForDuration == null) {
            endForDuration = Instant.now();
        }
        return Duration.between(startTime, endForDuration);
    }

    public String averageDurationForElementString(int count) {
        return DurationUtil.format(averageDurationForElement(count));
    }

    public Duration averageDurationForElement(int count) {
        Instant endForDuration = endTime;
        if (endForDuration == null) {
            endForDuration = Instant.now();
        }
        Duration entireDuration = Duration.between(this.startTime, endForDuration);
        Duration average = entireDuration;
        if (count > 0) {
            average = entireDuration.dividedBy(count);
        }
        return average;
    }
}
