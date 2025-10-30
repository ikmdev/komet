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
package dev.ikm.komet.framework.controls;

import dev.ikm.tinkar.common.util.time.DateTimeUtil;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static dev.ikm.tinkar.common.service.PrimitiveData.PREMUNDANE_TIME;
import static dev.ikm.tinkar.common.util.time.DateTimeUtil.PREMUNDANE;

/**
 * @deprecated Use {@link DateTimeUtil} instead
 */
@Deprecated
public class TimeUtils {

    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MMM-dd HH:mm:ss")
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault());

    /**
     * utility method to give a readable phrase to a last edited timestamp
     * @param pastTime
     * @param zone
     * @return the formatted recent timestamp in "X duration ago"
     * @deprecated Use {@link DateTimeUtil} instead
     */
    @Deprecated
    public static String calculateTimeAgoWithPeriodAndDuration(LocalDateTime pastTime, ZoneId zone) {
        Period period = Period.between(pastTime.toLocalDate(), getCurrentTimeByTimeZone(zone).toLocalDate());
        Duration duration = Duration.between(pastTime, getCurrentTimeByTimeZone(zone));
        if (period.getYears() != 0) {
            return "Edited several years ago";
        } else if (period.getMonths() != 0) {
            return "Edited " + duration.toDays() + " days ago";
        } else if (period.getDays() != 0) {
            return "Edited " + duration.toDays() + " days ago";
        } else if (duration.toHours() != 0) {
            return "Edited " + duration.toMinutes() + " minutes ago";
        } else if (duration.toMinutes() != 0) {
            return "Edited " + duration.toMinutes() + " minutes ago";
        } else if (duration.getSeconds() != 0) {
            return "Edited several seconds ago";
        } else {
            return "Edited " + duration.toMinutes() + " minutes ago";
        }
    }

    /**
     *
     * @param zone
     * @return
     * @deprecated Use {@link DateTimeUtil} instead
     */
    @Deprecated
    private static OffsetDateTime getCurrentTimeByTimeZone(ZoneId zone) {
        OffsetDateTime offsetdatetime = OffsetDateTime.now();
        return offsetdatetime;
    }

    /**
     * Converts a date represented using a long to a human readable String.
     *
     * @param stampTime the time represented as long
     * @return a human readable String
     * @deprecated Use {@link DateTimeUtil} instead
     */
    @Deprecated
    public static String toDateString(long stampTime) {
        if (!(stampTime == PREMUNDANE_TIME)) {
            Instant stampInstance = Instant.ofEpochSecond(stampTime / 1000);
            return DATE_TIME_FORMATTER.format(stampInstance);
        } else {
            return PREMUNDANE;
        }
    }
}
