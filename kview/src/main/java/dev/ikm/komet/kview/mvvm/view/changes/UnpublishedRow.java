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
package dev.ikm.komet.kview.mvvm.view.changes;

/**
 * A row of the unpublished-changes tree: a {@link UnpublishedGroup component heading} or one
 * {@link UnpublishedChange change} under it.
 */
public sealed interface UnpublishedRow permits UnpublishedGroup, UnpublishedChange {

    /** The nid of the component the row stands for. */
    long nid();

    /** The row's main text. */
    String title();
}
