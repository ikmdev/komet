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

import java.time.*;

public class TimeAgoCalculatorUtil {

    /**
     * utility method to give a readable phrase to a last edited timestamp
     * @param pastTime
     * @param zone
     * @return the formatted recent timestamp in "X duration ago"
     */
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

    private static OffsetDateTime getCurrentTimeByTimeZone(ZoneId zone) {
        OffsetDateTime offsetdatetime = OffsetDateTime.now();
        return offsetdatetime;
    }
}
