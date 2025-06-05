package dev.ikm.komet.kview.controls;

import java.time.LocalDate;

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
}
