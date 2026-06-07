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

/**
 * The outcome of a check performed by a supplemental "check" area against a focused item.
 *
 * <p>A check area (for example a Claude-backed or rules-engine-backed area) evaluates the
 * item currently in focus and reports one of these four states, which the area renders as a
 * green / red / amber / grey status indicator.
 *
 * <p>This is a compiler-visible enum rather than a string or boolean so that callers must
 * handle every state explicitly and the "not yet evaluated / nothing to evaluate" case is
 * distinct from a genuine failure.
 *
 * @see CheckResult
 */
public enum Status {

    /** The item satisfied the check (rendered green). */
    PASS,

    /** The item did not satisfy the check (rendered red). */
    FAIL,

    /**
     * The check could not be completed because of an error (rendered amber). This signals a
     * problem with running the check itself — a network failure, a missing rule service, an
     * exception — as opposed to a clean negative verdict, which is {@link #FAIL}.
     */
    ERROR,

    /**
     * No verdict is available (rendered grey). Used before the first run and when there is no
     * item in focus to evaluate.
     */
    UNKNOWN
}
