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
package dev.ikm.tinkar.coordinate.stamp;


//~--- JDK imports ------------------------------------------------------------


import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAccessor;

//~--- interfaces -------------------------------------------------------------

/**
 * Analog: A structural derivative that often differs by a single element.
 * 
 * @param <T> The type of object to make a time-based analog from.
 */
public interface TimeBasedAnalogMaker<T> {
   /**
    * Analog: A structural derivative that often differs by a single element.
    * @param dateTimeText the text to parse such as "2007-12-03T10:15:30", which is specified by the ISO-8601 extended offset date-time format.
    *  @return a new {@code <T>}  with the specified stamp position time.
    */
   default T makeAnalog(CharSequence dateTimeText) {
      return withStampPositionTime(LocalDateTime.parse(dateTimeText)
                                     .toEpochSecond(ZoneOffset.UTC) * 1000);
   }

   /**
    * Make analog.
    *
    * @param stampPositionTime the time of the stamp position for the analog
    * @return a new {@code <T>}  with the specified stamp position time.
    */
   T withStampPositionTime(long stampPositionTime);

   /**
    * Analog: A structural derivative that often differs by a single element.
    * @param temporalSpecification temporal - the temporal object to specify the time for the resulting {@code <T>}
    * @return a new {@code <T>}  with the specified stamp position time.
    */
   default T makeAnalog(TemporalAccessor temporalSpecification) {
      return withStampPositionTime(LocalDateTime.from(temporalSpecification)
                                     .toEpochSecond(ZoneOffset.UTC) * 1000);
   }

   /**
    * Analog: A structural derivative that often differs by a single element.
    * @param year the year to represent, from MIN_YEAR to MAX_YEAR
    * @param month the month-of-year to represent, from 1 (January) to 12 (December)
    * @param dayOfMonth the day-of-month to represent, from 1 to 31
    * @param hour the hour-of-day to represent, from 0 to 23
    * @param minute the minute-of-hour to represent, from 0 to 59
    * @param second the second-of-minute to represent, from 0 to 59
    * @return a new {@code <T>}  with the specified stamp position time.
    */
   default T makeAnalog(int year, int month, int dayOfMonth, int hour, int minute, int second) {
      return withStampPositionTime(LocalDateTime.of(year, month, dayOfMonth, hour, minute, second)
                                     .toEpochSecond(ZoneOffset.UTC) * 1000);
   }
}

