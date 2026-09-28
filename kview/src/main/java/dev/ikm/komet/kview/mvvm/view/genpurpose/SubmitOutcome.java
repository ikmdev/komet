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
package dev.ikm.komet.kview.mvvm.view.genpurpose;

/**
 * What became of a semantic submitted from the properties panel's edit form (see
 * {@link GenPurposeDetailsController#onSemanticSubmitted}). The form picks its toast from it.
 */
public enum SubmitOutcome {
    /**
     * Saved as an uncommitted version in the window's open transaction, where it stays until the
     * toolbar's Publish button commits it — the window uses the Publish flow.
     */
    UNCOMMITTED_UNTIL_PUBLISHED,
    /**
     * Saved as an uncommitted version, though the window commits on submit: it is in create mode
     * and a required pattern still has no semantic, so the component cannot be created yet. The
     * version commits together with the component on the submit that meets the last requirement.
     */
    UNCOMMITTED_UNTIL_REQUIREMENTS_MET,
    /** Committed. */
    COMMITTED,
    /** Committed, and the commit also created the window's component: the window is now in edit mode. */
    COMPONENT_CREATED
}
