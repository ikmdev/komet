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

import java.util.concurrent.TimeUnit;

/**
 *
 * 
 */
public class DurationUtil {
    /**
     * Seconds per minute.
     */
    static final int SECONDS_PER_MINUTE = 60;

    /**
     * Minutes per hour.
     */
    static final int MINUTES_PER_HOUR = 60;

    /**
     * Seconds per hour.
     */
    static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;

    public static String format(java.time.Duration d) {
        final StringBuilder builder = new StringBuilder();
        final long          seconds = d.getSeconds();

        if (seconds > 0) {
            final long hours   = seconds / SECONDS_PER_HOUR;
            final int  minutes = (int) ((seconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE);
            final int  secs    = (int) (seconds % SECONDS_PER_MINUTE);

            if (hours != 0) {
                builder.append(hours)
                        .append(" h ");
            }

            if (minutes != 0) {
                builder.append(minutes)
                        .append(" m ");
            }

            builder.append(secs)
                    .append(" s");
            return builder.toString();
        }

        final int  nanos = d.getNano();
        final long milis = TimeUnit.MILLISECONDS.convert(nanos, TimeUnit.NANOSECONDS);

        if (milis > 0) {
            return builder.append(milis)
                    .append(" ms")
                    .toString();
        }

        final long micro = TimeUnit.MICROSECONDS.convert(nanos, TimeUnit.NANOSECONDS);

        if (micro > 0) {
            return builder.append(micro)
                    .append(" μs")
                    .toString();
        }

        return builder.append(nanos)
                .append(" ns")
                .toString();
    }

    public static String msTo8601(long milliseconds) {
        StringBuilder builder = new StringBuilder();
        builder.append("P");

        boolean addT = true;

        if (milliseconds >= DateTimeUtil.MS_IN_YEAR) { // years
            builder.append(milliseconds/DateTimeUtil.MS_IN_YEAR);
            builder.append("Y");
            milliseconds = milliseconds % DateTimeUtil.MS_IN_YEAR;
        }
        if (milliseconds >= DateTimeUtil.MS_IN_MONTH) { // months
            builder.append(milliseconds/DateTimeUtil.MS_IN_MONTH);
            builder.append("M");
            milliseconds = milliseconds % DateTimeUtil.MS_IN_MONTH;
        }
        if (milliseconds >= DateTimeUtil.MS_IN_DAY) { // days
            builder.append(milliseconds/DateTimeUtil.MS_IN_DAY);
            builder.append("D");
            milliseconds = milliseconds % DateTimeUtil.MS_IN_DAY;
        }
        if (milliseconds >= DateTimeUtil.MS_IN_HOUR) { // hours
            addT = false;
            builder.append("T");
            builder.append(milliseconds/DateTimeUtil.MS_IN_HOUR);
            builder.append("H");
            milliseconds = milliseconds % DateTimeUtil.MS_IN_HOUR;
        }
        if (milliseconds >= DateTimeUtil.MS_IN_MINUTE) { // minutes
            if (addT) {
                builder.append("T");
                addT = false;
            }
            builder.append(milliseconds/DateTimeUtil.MS_IN_MINUTE);
            builder.append("M");
            milliseconds = milliseconds % DateTimeUtil.MS_IN_MINUTE;
        }
        if (milliseconds >= DateTimeUtil.MS_IN_SEC) { // seconds
            long microseconds = milliseconds % DateTimeUtil.MS_IN_SEC;
            if (addT) {
                builder.append("T");
                addT = false;
            }
            builder.append(milliseconds/DateTimeUtil.MS_IN_SEC);
            if (microseconds >= 100) {
                builder.append(".").append(microseconds);
            } else if (microseconds >= 10) {
                builder.append(".0").append(microseconds);
            } else if (microseconds > 0) {
                builder.append("0.00").append(microseconds);
            }
            builder.append("S");
        } else if (milliseconds > 0) {
            if (addT) {
                builder.append("T");
                addT = false;
            }
            if (milliseconds >= 100) {
                builder.append("0.").append(milliseconds);
            } else if (milliseconds >= 10) {
                builder.append("0.0").append(milliseconds);
            } else {
                builder.append("0.00").append(milliseconds);
            }

            builder.append("S");

        }

        return builder.toString();
    }
}
