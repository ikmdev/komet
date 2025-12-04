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

import dev.ikm.tinkar.common.service.PrimitiveData;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 *
 */
public class DateTimeUtil {
    public static final long MS_IN_YEAR =   1000L * 60 * 60 * 24 * 365;
    public static final long MS_IN_MONTH =  1000L * 60 * 60 * 24 * 30;
    public static final long MS_IN_DAY =    1000L * 60 * 60 * 24;
    public static final long MS_IN_HOUR =   1000L * 60 * 60;
    public static final long MS_IN_MINUTE = 1000L * 60;
    public static final long MS_IN_SEC =    1000L;

    public static final DateTimeFormatter EASY_TO_READ_DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    public static final DateTimeFormatter EASY_TO_READ_TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a zzz");
    public static final DateTimeFormatter EASY_TO_READ_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a zzz MMM dd, yyyy");
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static final DateTimeFormatter SEC_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter MIN_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static final DateTimeFormatter SHORT_MIN_FORMATTER = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm");
    public static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00");
    public static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");
    public static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy");
    public static final DateTimeFormatter ZONE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    public static final DateTimeFormatter TEXT_FORMAT_WITH_ZONE = DateTimeFormatter.ofPattern("MMM dd, yyyy; hh:mm:ss a zzz");
    public static final DateTimeFormatter TIME_SIMPLE = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter COMPRESSED_DATE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssz");
    public static final DateTimeFormatter COMPRESSED_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static final String LATEST = "Latest";
    public static final String CANCELED = "Canceled";
    public static final String PREMUNDANE = "Premundane";

    public static Instant epochMsToInstant(long epochMilliSecond) {
        if (epochMilliSecond == Long.MAX_VALUE) {
            return Instant.MAX;
        }
        if (epochMilliSecond == Long.MIN_VALUE) {
            return Instant.MIN;
        }
        return Instant.ofEpochMilli(epochMilliSecond);
    }

    public static long instantToEpochMs(Instant instant) {
        if (instant.equals(Instant.MIN)) {
            return Long.MIN_VALUE;
        }
        if (instant.equals(Instant.MAX)) {
            return Long.MAX_VALUE;
        }
        return instant.toEpochMilli();
    }

    public static ZonedDateTime epochToZonedDateTime(long epochMilliSecond) {
        return Instant.ofEpochMilli(epochMilliSecond).atZone(ZoneOffset.UTC);
    }
    public static Instant epochToInstant(long epochMilliSecond) {
        return Instant.ofEpochMilli(epochMilliSecond);
    }

    public static String format(long epochMilliSecond) {
        return format(epochMilliSecond, FORMATTER);
    }
    public static String format(long epochMilliSecond, DateTimeFormatter formatter) {
        if (epochMilliSecond == Long.MAX_VALUE) {
            return LATEST;
        }
        if (epochMilliSecond == Long.MIN_VALUE) {
            return CANCELED;
        }
        if (epochMilliSecond == PrimitiveData.PREMUNDANE_TIME) {
            return PREMUNDANE;
        }
        ZonedDateTime positionTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMilliSecond), ZoneOffset.UTC);
        ZonedDateTime inLocalZone = positionTime.withZoneSameInstant(ZoneId.systemDefault());
        return inLocalZone.format(formatter);
    }
    public static String timeNowSimple() {
        return TIME_SIMPLE.format(ZonedDateTime.now());
    }

    public static String nowInISO() {
        return DateTimeFormatter.ISO_DATE_TIME.format(ZonedDateTime.now());
    }
    public static String nowWithZoneCompact() {
        return COMPRESSED_DATE_TIME.format(ZonedDateTime.now());
    }
    public static String nowWithZone() {
        return textFormatWithZone(ZonedDateTime.now());
    }
    public static String textFormatWithZone(ZonedDateTime zonedDateTime) {
        return TEXT_FORMAT_WITH_ZONE.format(zonedDateTime);
    }

    /**
     *
     * @param dateTime such as '2011-12-03T10:15:30', '2011-12-03T10:15:30+01:00' or '2011-12-03T10:15:30+01:00[Europe/Paris]
     * @return Epoch millisecond of the date time...
     */
    public static long parseWithZone(String dateTime) {
        if (dateTime.equalsIgnoreCase(LATEST)) {
            return Long.MAX_VALUE;
        }
        if (dateTime.equalsIgnoreCase(CANCELED)) {
            return Long.MIN_VALUE;
        }
        if (dateTime.equalsIgnoreCase(PREMUNDANE)) {
            return PrimitiveData.PREMUNDANE_TIME;
        }
        return ZonedDateTime.parse(dateTime, ZONE_FORMATTER).toInstant().toEpochMilli();
    }

    /**
     *
     * @param dateTime yyyyMMdd'T'HHmmssz
     * @return Epoch millisecond of the date time...
     */
    public static long compressedParse(String dateTime) {
        if (dateTime.equalsIgnoreCase(LATEST)) {
            return Long.MAX_VALUE;
        }
        if (dateTime.equalsIgnoreCase(CANCELED)) {
            return Long.MIN_VALUE;
        }
        if (dateTime.equalsIgnoreCase(PREMUNDANE)) {
            return PrimitiveData.PREMUNDANE_TIME;
        }
        return LocalDateTime.parse(dateTime, COMPRESSED_DATE_TIME).atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
    }

    /**
     *
     * @param date yyyyMMdd
     * @return Epoch millisecond of the date time...
     */
    public static long compressedDateParse(String date) {
        if (date.equalsIgnoreCase(LATEST)) {
            return Long.MAX_VALUE;
        }
        if (date.equalsIgnoreCase(CANCELED)) {
            return Long.MIN_VALUE;
        }
        if (date.equalsIgnoreCase(PREMUNDANE)) {
            return PrimitiveData.PREMUNDANE_TIME;
        }
        return LocalDate.parse(date, COMPRESSED_DATE).atStartOfDay().atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
    }

    /**
     *
     * @param dateTime yyyy-MM-dd HH:mm
     * @return Epoch millisecond of the date time...
     */
    public static long parse(String dateTime) {
        if (dateTime.equalsIgnoreCase(LATEST)) {
            return Long.MAX_VALUE;
        }
        if (dateTime.equalsIgnoreCase(CANCELED)) {
            return Long.MIN_VALUE;
        }
        if (dateTime.equalsIgnoreCase(PREMUNDANE)) {
            return PrimitiveData.PREMUNDANE_TIME;
        }
        return LocalDateTime.parse(dateTime, FORMATTER).atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
    }
    public static String format(Instant instant, DateTimeFormatter formatter) {
        if (instant.equals(Instant.MAX)) {
            return LATEST;
        }
        if (instant.equals(Instant.MIN)) {
            return CANCELED;
        }
        if (instant.equals(PrimitiveData.PREMUNDANE_INSTANT)) {
            return PREMUNDANE;
        }
        return formatter.format(instant.atOffset(ZoneOffset.UTC));
    }
    public static String format(Instant instant) {
        return format(instant, FORMATTER);
    }
    public static String format(ZonedDateTime zonedDateTime) {
        return zonedDateTime.format(ZONE_FORMATTER);
    }
    public static String format(Instant instant, Double resolution) {
        if (instant.equals(Instant.MAX)) {
            return LATEST;
        }
        if (instant.equals(Instant.MIN)) {
            return CANCELED;
        }
        if (instant.equals(PrimitiveData.PREMUNDANE_INSTANT)) {
            return PREMUNDANE;
        }
        if (resolution < MS_IN_SEC) {
            return ZONE_FORMATTER.format(instant);
        }
        if (resolution < MS_IN_MINUTE) {
            return SEC_FORMATTER.format(instant);
        }
        if (resolution < MS_IN_HOUR) {
            return MIN_FORMATTER.format(instant);
        }
        if (resolution < MS_IN_DAY) {
            return HOUR_FORMATTER.format(instant);
        }
        if (resolution < MS_IN_MONTH) {
            return DAY_FORMATTER.format(instant);
        }
        if (resolution < MS_IN_YEAR) {
            return MONTH_FORMATTER.format(instant);
        }
        return YEAR_FORMATTER.format(instant);
    }
    public static String format(ZonedDateTime zonedDateTime, Double resolution) {
        if (resolution < MS_IN_SEC) {
            return zonedDateTime.format(ZONE_FORMATTER);
        }
        if (resolution < MS_IN_MINUTE) {
            return zonedDateTime.format(SEC_FORMATTER);
        }
        if (resolution < MS_IN_HOUR) {
            return zonedDateTime.format(MIN_FORMATTER);
        }
        if (resolution < MS_IN_DAY) {
            return zonedDateTime.format(HOUR_FORMATTER);
        }
        if (resolution < MS_IN_MONTH) {
            return zonedDateTime.format(DAY_FORMATTER);
        }
        if (resolution < MS_IN_YEAR) {
            return zonedDateTime.format(MONTH_FORMATTER);
        }
        return zonedDateTime.format(YEAR_FORMATTER);
    }

    public static long toEpochMilliseconds(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
    public static String getDayOfMonthSuffix(final int n) {
        if (n < 1 || n > 31) {
            throw new IllegalArgumentException("illegal day of month: " + n);
        }
        if (n >= 11 && n <= 13) {
            return "th";
        }
        switch (n % 10) {
            case 1:  return "st";
            case 2:  return "nd";
            case 3:  return "rd";
            default: return "th";
        }
    }
}
