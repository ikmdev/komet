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
package dev.ikm.komet.layout.check;

import java.util.Objects;

/**
 * The immutable result of a single check run: a {@link Status} plus a short human-readable
 * detail line explaining the verdict (for example the reason a check failed, or the text of a
 * rule consequence that fired).
 *
 * <p>Instances are created with the static factory methods {@link #pass(String)},
 * {@link #fail(String)}, {@link #error(String)} and {@link #unknown(String)}.
 *
 * @param status the verdict; never {@code null}
 * @param detail a short, single-line explanation suitable for display; never {@code null}
 *               (an empty string is used when no detail is supplied)
 */
public record CheckResult(Status status, String detail) {

    /**
     * Canonical constructor that null-checks the status and normalizes a {@code null} detail to
     * the empty string.
     *
     * @param status the verdict; must not be {@code null}
     * @param detail a short explanation, or {@code null} for none
     */
    public CheckResult {
        Objects.requireNonNull(status, "status");
        detail = (detail == null) ? "" : detail;
    }

    /**
     * Creates a passing result.
     *
     * @param detail a short explanation of why the item passed, or {@code null} for none
     * @return a {@link CheckResult} with status {@link Status#PASS}
     */
    public static CheckResult pass(String detail) {
        return new CheckResult(Status.PASS, detail);
    }

    /**
     * Creates a failing result.
     *
     * @param detail a short explanation of why the item failed, or {@code null} for none
     * @return a {@link CheckResult} with status {@link Status#FAIL}
     */
    public static CheckResult fail(String detail) {
        return new CheckResult(Status.FAIL, detail);
    }

    /**
     * Creates an error result, signalling that the check itself could not be completed.
     *
     * @param detail a short explanation of the error, or {@code null} for none
     * @return a {@link CheckResult} with status {@link Status#ERROR}
     */
    public static CheckResult error(String detail) {
        return new CheckResult(Status.ERROR, detail);
    }

    /**
     * Creates an unknown (no-verdict) result.
     *
     * @param detail a short explanation, or {@code null} for none
     * @return a {@link CheckResult} with status {@link Status#UNKNOWN}
     */
    public static CheckResult unknown(String detail) {
        return new CheckResult(Status.UNKNOWN, detail);
    }
}
