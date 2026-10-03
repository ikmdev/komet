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
package dev.ikm.komet.framework.observable.read;

import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.terms.TinkarTerm;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Membership reads under a view calculator: which patterns are membership patterns
 * (semantic purpose {@link TinkarTerm#MEMBERSHIP_SEMANTIC}, no fields), and whether a
 * concept is currently a member of one.
 */
public final class MembershipReads {

    private MembershipReads() {
    }

    /** The latest version, under the calculator, of every pattern whose purpose is membership. */
    public static List<PatternEntityVersion> membershipPatterns(ViewCalculator viewCalculator) {
        Objects.requireNonNull(viewCalculator, "View calculator cannot be null");
        List<PatternEntityVersion> membershipPatterns = new ArrayList<>();
        PrimitiveData.get().forEachPatternNid(patternNid ->
                viewCalculator.stampCalculator().<PatternEntityVersion>latest(patternNid)
                        .ifPresent(patternVersion -> {
                            if (patternVersion.semanticPurposeNid() == TinkarTerm.MEMBERSHIP_SEMANTIC.nid()) {
                                membershipPatterns.add(patternVersion);
                            }
                        }));
        return membershipPatterns;
    }

    /** Whether the concept's membership semantic for the pattern exists and is active under the calculator. */
    public static boolean isMember(ViewCalculator viewCalculator, int conceptNid, int patternNid) {
        Objects.requireNonNull(viewCalculator, "View calculator cannot be null");
        int[] semanticNids = PrimitiveData.get().semanticNidsForComponentOfPattern(conceptNid, patternNid);
        return semanticNids.length > 0 && viewCalculator.stampCalculator().isLatestActive(semanticNids[0]);
    }
}
