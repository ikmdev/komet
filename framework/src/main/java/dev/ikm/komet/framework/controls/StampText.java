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
import network.ike.docs.konceptcore.KonceptKind;

import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.StampEntityVersion;

/**
 * The compact one-line text shown beside the STAMP {@link KonceptKind#STAMP} pentagon on an inline
 * badge — {@code status · date-time · author}. It is the inline form of the standard stamp display
 * that {@code dev.ikm.komet.details.concept.StampControl} builds (the {@code S}/{@code T}/{@code A}
 * lines): same {@link DateTimeUtil#FORMATTER} (date + time, no milliseconds) and the same author
 * resolution via {@link ViewCalculator#getPreferredDescriptionTextWithFallbackOrNid(int)}, with the
 * module and path dropped so the badge stays compact (they remain in the expanded stamp view).
 */
public final class StampText {

    private StampText() {
    }

    /**
     * A compact one-line description of the latest version of the stamp with the given nid.
     *
     * @param stampNid   the stamp nid
     * @param calculator the view used to read the latest stamp version and resolve the author name;
     *                   {@code null} yields {@code "Stamp"}
     * @return {@code status · date-time · author}, or {@code "Stamp"} when the stamp cannot be read
     */
    public static String compact(int stampNid, ViewCalculator calculator) {
        if (calculator == null) {
            return "Stamp";
        }
        Latest<StampEntityVersion> latest = calculator.latest(stampNid);
        if (latest.isAbsent()) {
            return "Stamp";
        }
        StampEntityVersion version = latest.get();
        String time = DateTimeUtil.format(version.time());
        String author = calculator.getPreferredDescriptionTextWithFallbackOrNid(version.authorNid());
        return version.state() + " · " + time + " · " + author;
    }
}
