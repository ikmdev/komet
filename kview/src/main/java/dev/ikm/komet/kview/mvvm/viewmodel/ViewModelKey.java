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
package dev.ikm.komet.kview.mvvm.viewmodel;

/**
 * Typed property keys for the cognitive {@code ViewModel} framework. Replaces
 * the per-class {@code public static String} constants previously declared on
 * {@code FormViewModel}, {@code GenEditingViewModel}, and
 * {@code GenPurposeViewModel} — those duplicated keys across viewmodels and
 * lost compile-time safety on every {@code addProperty}/{@code setPropertyValue}
 * call site.
 *
 * <p>Cognitive's {@code addProperty(Enum, ...)}, {@code setPropertyValue(Enum, ...)},
 * and {@code getPropertyValue(Enum)} overloads delegate internally to the
 * String overloads via {@link Enum#name()}, so using these constants is
 * functionally equivalent to using the enum's name as a String key — except
 * the compiler now catches typos, autocompletes the choices, and lets us
 * rename keys safely.
 *
 * <p>The underlying property-table key (what cognitive stores) is the enum
 * constant's {@link #name()} string ({@code "FIELD_INDEX"} rather than the
 * legacy {@code "fieldIndex"} camelCase). The change is invisible to callers
 * because the table is in-memory and not persisted; the migration is atomic
 * (the old String constants are deleted) so reader/writer agreement is
 * compiler-enforced.
 */
public enum ViewModelKey {
    /** Topic UUID for events scoped to the current journal window. */
    CURRENT_JOURNAL_WINDOW_TOPIC,

    /** Topic UUID for events scoped to the concept window. */
    CONCEPT_TOPIC,

    /** {@code ViewProperties} for the current view. */
    VIEW_PROPERTIES,

    /** Form mode: {@code CREATE}, {@code EDIT}, or {@code VIEW} (see
     *  {@link FormViewModel.FormMode}). The property value is currently a
     *  String — see {@code FormViewModel.CREATE/EDIT/VIEW}. */
    MODE,

    /** Position of the field being acted on within the entity's
     *  {@code fieldValues()} list. {@code -1} means "no specific field —
     *  the user is acting on the whole semantic." Distinct from
     *  {@code IndexerSchema.INDEXED_FIELD_ORDINAL} (a Lucene field name
     *  in {@code search-provider}) — they share neither identity nor type. */
    FIELD_INDEX,

    /** {@code StampViewModel} associated with the current edit. */
    STAMP_VIEW_MODEL,

    /** Topic UUID for events scoped to the current editing window. */
    WINDOW_TOPIC,

    /** Ordered collection of {@code FieldRecord} entries for the current entity. */
    FIELDS_COLLECTION,

    /** Reference to the {@code SemanticEntity} being edited. */
    SEMANTIC,

    /** {@code EntityFacade} of the referenced component for the current semantic. */
    REF_COMPONENT,

    /** {@code PatternEntity} that defines the current semantic's field shape. */
    PATTERN,

    /** A single field record (used in single-field editing flows). */
    FIELD,

    /** Hash of the default field values; used to detect whether the form has
     *  been modified relative to the pattern's defaults. */
    DEFAULT_FIELDS_HASH,

    /** {@code ObservableComposer} for the general-purpose editing flow. */
    COMPOSER
}
