package dev.ikm.komet.framework.performance;

public interface Observation extends Statement {
    default boolean isPresent() {
        return value().isPresent();
    }

    Measure value();

    default boolean isAbsent() {
        return value().isAbsent();
    }

    default boolean mightBePresent() {
        return value().mightBePresent();
    }

    default boolean mightBeAbsent() {
        return value().mightBeAbsent();
    }

    default boolean withinRange(Float rangeBottom, Float rangeTop) {
        return value().withinRange(rangeBottom, rangeTop);
    }

    default boolean withinRange(Float numberToTest) {
        return value().withinRange(numberToTest);
    }

}
