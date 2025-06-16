package dev.ikm.komet.kview.controls;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static dev.ikm.komet.kview.controls.RangeCalendarControl.DATE_FORMATTER;
import static dev.ikm.komet.kview.controls.RangeCalendarControl.DEFAULT_DATE_PATTERN;

public record DateRange(int index, LocalDate startDate, LocalDate endDate, boolean exclude) {

    public boolean inRange(LocalDate date) {
        return startDate.isBefore(date) && date.isBefore(endDate);
    }

    public boolean isValid() {
        return startDate != null && endDate != null;
    }

    public boolean contains(LocalDate date) {
        return !(date.isBefore(startDate) || date.isAfter(endDate));
    }

    public boolean contains(DateRange dateRange) {
        return contains(dateRange.startDate) && contains(dateRange.endDate);
    }

    public static Optional<DateRange> of(int index, String date, boolean exclude) {
        if (date == null || !date.contains(":")) {
            return Optional.empty();
        }
        String[] split = date.split(":");
        if (split.length != 2) {
            return Optional.empty();
        }
        return Optional.of(new DateRange(index,
                LocalDate.parse(split[0], DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN)),
                LocalDate.parse(split[1], DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN)), exclude));
    }

    @Override
    public String toString() {
        if (startDate == null || endDate == null) {
            return "";
        }
        return DATE_FORMATTER.format(startDate) + ":" + DATE_FORMATTER.format(endDate);
    }
}
