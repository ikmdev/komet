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
package dev.ikm.komet.framework.performance;

import dev.ikm.tinkar.terms.ConceptFacade;

import java.util.Optional;

public interface Measure {

    default boolean isPresent() {
        if (getLowerBound() == 0 & !includeLowerBound()) {
            return true;
        }
        return getLowerBound() > 0;
    }

    /**
     * @return the lower bound for this measurement
     */
    float getLowerBound();

    /**
     * @return true if the lower bound is part of the interval.
     */
    boolean includeLowerBound();

    default boolean mightBePresent() {
        return getUpperBound() > 0;
    }

    /**
     * @return the upper bound for this measurement
     */
    float getUpperBound();

    default boolean mightBeAbsent() {
        if (isAbsent()) {
            return true;
        }
        return getLowerBound() <= 0 && getUpperBound() >= 0;
    }

    default boolean isAbsent() {
        return getUpperBound() == 0f && includeUpperBound() && getLowerBound() == 0f && includeLowerBound();
    }

    /**
     * @return true if the upper bound is part of the interval.
     */
    boolean includeUpperBound();

    /**
     * @return the resolution of this measurement.
     */
    Optional<Float> getResolution();

    /**
     * In most cases, the semantics of the measurement are the units of measure.
     *
     * @return the semantics for this measurement.
     */
    Optional<ConceptFacade> getMeasureSemantic();

    default boolean withinRange(Float rangeBottom, Float rangeTop) {
        if (rangeTop < rangeBottom) {
            throw new IllegalStateException("rangeTop of " + rangeTop +
                    " is not greater than or equal to range bottom of " + rangeBottom +
                    ".");
        }
        return withinRange(rangeBottom) && withinRange(rangeTop);
    }

    default boolean withinRange(Float numberToTest) {
        if (numberToTest >= getLowerBound() && numberToTest <= getUpperBound()) {
            if (numberToTest == getLowerBound() & !includeLowerBound()) {
                return false;
            }
            if (numberToTest == getUpperBound() & !includeUpperBound()) {
                return false;
            }
            return true;
        }
        return false;
    }
}
