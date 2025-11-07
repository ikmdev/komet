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

/**
 * <h1>Observable Framework for JavaFX-Native Tinkar Entity Management</h1>
 *
 * <p>The Observable package provides a comprehensive JavaFX-native framework for working with Tinkar
 * entities, offering real-time UI binding, automatic change notifications, and transactional editing
 * capabilities. This framework bridges Tinkar's immutable entity model with JavaFX's reactive property
 * system, enabling developers to build responsive, data-driven user interfaces with minimal boilerplate.
 *
 * <h2>Core Architecture</h2>
 *
 * <p>The Observable framework follows a layered architecture with clear separation of concerns:
 *
 * <pre>
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    Application Layer                            │
 * │              (JavaFX UI Controls & Controllers)                 │
 * └─────────────────────────────────────────────────────────────────┘
 *                            ↕ Binding
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                   Observable Framework                          │
 * │  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────┐   │
 * │  │ ObservableEntity │  │ ObservableVersion│  │ ObservableXxx│   │
 * │  │                  │  │                  │  │   .Editable  │   │
 * │  │ - ObservableCon- │  │ - Concept        │  │              │   │
 * │  │   cept           │  │ - Pattern        │  │ Cached field │   │
 * │  │ - ObservableSeм- │  │ - Semantic       │  │ editing with │   │
 * │  │   antic          │  │ - Stamp          │  │ save/commit  │   │
 * │  │ - ObservablePat- │  │                  │  │              │   │
 * │  │   tern           │  │ Observable ver-  │  │              │   │
 * │  │                  │  │ sions with       │  │              │   │
 * │  │ Canonical cached │  │ JavaFX props     │  │              │   │
 * │  │ entities         │  │                  │  │              │   │
 * │  └──────────────────┘  └──────────────────┘  └──────────────┘   │
 * └─────────────────────────────────────────────────────────────────┘
 *                            ↕
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    Tinkar Entity Layer                          │
 * │        (Immutable Entities, Versions, Transactions)             │
 * └─────────────────────────────────────────────────────────────────┘
 * </pre>
 *
 * <h2>Key Concepts</h2>
 *
 * <h3>1. Observable Entities</h3>
 * <p>Observable entities wrap Tinkar's immutable entities and provide JavaFX observable properties:
 * <ul>
 *   <li>{@link dev.ikm.komet.framework.observable.ObservableEntity} - Base class for all observable entities</li>
 *   <li>{@link dev.ikm.komet.framework.observable.ObservableConcept} - Observable concept entities</li>
 *   <li>{@link dev.ikm.komet.framework.observable.ObservableSemantic} - Observable semantic entities</li>
 *   <li>{@link dev.ikm.komet.framework.observable.ObservablePattern} - Observable pattern entities</li>
 *   <li>{@link dev.ikm.komet.framework.observable.ObservableStamp} - Observable stamps with lifecycle tracking</li>
 * </ul>
 *
 * <p><b>Canonical Instance Guarantee:</b> Each Tinkar entity NID maps to exactly one Observable instance,
 * maintained through a weak-value cache. This ensures UI consistency and prevents memory leaks.
 *
 * <h3>2. Observable Versions</h3>
 * <p>Versions represent specific states of entities at particular points in time (STAMPs):
 * <ul>
 *   <li>{@link dev.ikm.komet.framework.observable.ObservableVersion} - Base class for observable versions</li>
 *   <li>{@link dev.ikm.komet.framework.observable.ObservableConceptVersion}</li>
 *   <li>{@link dev.ikm.komet.framework.observable.ObservableSemanticVersion}</li>
 *   <li>{@link dev.ikm.komet.framework.observable.ObservablePatternVersion}</li>
 *   <li>{@link dev.ikm.komet.framework.observable.ObservableStampVersion}</li>
 * </ul>
 *
 * <p>Each version provides JavaFX properties for all its fields, enabling direct UI binding without
 * manual synchronization.
 *
 * <h3>3. Editable Versions (Inner Classes)</h3>
 * <p>Editable versions cache changes for transactional editing with save/commit/rollback semantics:
 * <ul>
 *   <li>{@link dev.ikm.komet.framework.observable.ObservableVersion.Editable} - Base editable version</li>
 *   <li>{@link dev.ikm.komet.framework.observable.ObservableConceptVersion.Editable}</li>
 *   <li>{@link dev.ikm.komet.framework.observable.ObservableSemanticVersion.Editable}</li>
 *   <li>{@link dev.ikm.komet.framework.observable.ObservablePatternVersion.Editable}</li>
 *   <li>{@link dev.ikm.komet.framework.observable.ObservableStampVersion.Editable}</li>
 * </ul>
 *
 * <p><b>Design Rationale:</b> Editable versions are inner classes to clearly express their relationship
 * with their read-only counterparts, reduce namespace pollution, and enable simpler, more intuitive names.
 *
 * <h3>4. Observable Features and Fields</h3>
 * <p>Features represent individual data points within versions:
 * <ul>
 *   <li>{@link dev.ikm.komet.framework.observable.ObservableFeature} - Base class for features</li>
 *   <li>{@link dev.ikm.komet.framework.observable.ObservableField} - Semantic field values</li>
 *   <li>{@link dev.ikm.komet.framework.observable.ObservableFeatureDefinition} - Pattern field definitions</li>
 * </ul>
 *
 * <p>Corresponding editable features (also as inner classes):
 * <ul>
 *   <li>{@link dev.ikm.komet.framework.observable.ObservableFeature.Editable}</li>
 *   <li>{@link dev.ikm.komet.framework.observable.ObservableField.Editable}</li>
 *   <li>{@link dev.ikm.komet.framework.observable.ObservableFeatureDefinition.Editable}</li>
 * </ul>
 *
 * <h2>Usage Patterns</h2>
 *
 * <h3>Pattern 1: Reading Entities with UI Binding</h3>
 * <pre>{@code
 * // Get a canonical observable entity by NID
 * ObservableConcept concept = ObservableEntityHandle.getConceptOrThrow(conceptNid);
 *
 * // Access the latest version
 * ObservableConceptVersion version = concept.lastVersion();
 *
 * // Bind UI controls to observable properties
 * stateLabel.textProperty().bind(version.stateProperty().asString());
 * authorLabel.textProperty().bind(
 *     version.authorProperty().asString()
 * );
 *
 * // Changes to the entity in the database automatically update the UI
 * // through the event bus and property notifications
 * }</pre>
 *
 * <h3>Pattern 2: Creating New Entities with ObservableComposer</h3>
 * <pre>{@code
 * // Create a composer with STAMP coordinates
 * ObservableComposer composer = ObservableComposer.builder()
 *     .stampCalculator(stampCalculator)
 *     .author(TinkarTerm.USER)
 *     .module(TinkarTerm.PRIMORDIAL_MODULE)
 *     .path(TinkarTerm.DEVELOPMENT_PATH)
 *     .defaultState(State.ACTIVE)
 *     .build();
 *
 * // Use unified API - automatically handles create vs edit
 * PublicId myConceptId = PublicIds.newRandom();
 * EntityComposer<ObservableConceptVersion.Editable, ObservableConcept> conceptComposer =
 *     composer.composeConcept(myConceptId);
 *
 * // Get the editable version
 * ObservableConceptVersion.Editable editable = conceptComposer.getEditableVersion();
 *
 * // Bind to UI or modify programmatically
 * stateComboBox.valueProperty().bindBidirectional(
 *     editable.editableStateProperty()
 * );
 *
 * // Save as uncommitted
 * conceptComposer.save();
 *
 * // Commit the transaction
 * composer.commit();
 * }</pre>
 *
 * <h3>Pattern 3: Editing Semantics with Fields</h3>
 * <pre>{@code
 * ObservableComposer composer = ObservableComposer.builder()
 *     .stampCalculator(stampCalculator)
 *     .author(TinkarTerm.USER)
 *     .module(TinkarTerm.PRIMORDIAL_MODULE)
 *     .path(TinkarTerm.DEVELOPMENT_PATH)
 *     .build();
 *
 * // Get existing semantic
 * ObservableSemantic semantic = ObservableEntityHandle.getSemanticOrThrow(semanticNid);
 *
 * // Create editor
 * PublicId semanticId = semantic.publicId();
 * EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> semanticComposer =
 *     composer.composeSemantic(semanticId, null, null); // nulls OK for editing existing
 *
 * // Get editable version and fields
 * ObservableSemanticVersion.Editable editableVersion = semanticComposer.getEditableVersion();
 * ObservableList<ObservableField.Editable<?>> fields = editableVersion.getEditableFields();
 *
 * // Bind individual fields to UI
 * TextField descriptionField = new TextField();
 * ObservableField.Editable<String> textField =
 *     (ObservableField.Editable<String>) fields.get(0);
 *
 * descriptionField.textProperty().bindBidirectional(
 *     (Property<String>) textField.editableValueProperty()
 * );
 *
 * // Check for changes
 * saveButton.disableProperty().bind(
 *     Bindings.createBooleanBinding(
 *         () -> !semanticComposer.isDirty(),
 *         textField.editableValueProperty()
 *     )
 * );
 *
 * // Save and commit
 * if (semanticComposer.isDirty()) {
 *     semanticComposer.save();
 * }
 * composer.commit();
 * }</pre>
 *
 * <h3>Pattern 4: Observing Entity Changes</h3>
 * <pre>{@code
 * // Subscribe to entity changes via event bus
 * EvtBus.subscribe(conceptNid, EvtType.ENTITY_UPDATE, evt -> {
 *     Platform.runLater(() -> {
 *         // Observable entity automatically updated
 *         // UI properties reflect new values
 *         statusLabel.setText("Entity updated");
 *     });
 * });
 *
 * // Alternative: Listen to specific properties
 * ObservableConceptVersion version = concept.lastVersion();
 * version.stateProperty().addListener((obs, oldValue, newValue) -> {
 *     System.out.println("State changed: " + oldValue + " -> " + newValue);
 * });
 * }</pre>
 *
 * <h2>Threading Model</h2>
 *
 * <p><b>⚠️ CRITICAL:</b> All Observable framework operations MUST occur on the JavaFX Application Thread.
 * This includes:
 * <ul>
 *   <li>Creating, accessing, or modifying Observable entities</li>
 *   <li>Reading or writing Observable properties</li>
 *   <li>Using {@link dev.ikm.komet.framework.observable.ObservableComposer}</li>
 *   <li>Working with editable versions</li>
 * </ul>
 *
 * <p>The framework enforces this requirement and will throw exceptions if violated. Use
 * {@code Platform.runLater()} to marshal operations from background threads.
 *
 * <h2>Memory Management</h2>
 *
 * <p>The Observable framework uses weak-value caches to prevent memory leaks:
 * <ul>
 *   <li><b>Entity Cache:</b> Canonical observable entities are weakly cached by NID</li>
 *   <li><b>Editable Version Cache:</b> Editable versions are weakly cached by (NID, stampNid)</li>
 *   <li><b>Automatic Cleanup:</b> Unused instances are garbage collected when no longer referenced</li>
 * </ul>
 *
 * <p>This design ensures memory efficiency while maintaining the canonical instance guarantee for
 * actively-used entities.
 *
 * <h2>Transaction Management</h2>
 *
 * <p>The framework integrates with Tinkar's transaction system:
 * <ol>
 *   <li><b>Uncommitted State:</b> Changes saved but transaction not committed (stamps have time = Long.MAX_VALUE)</li>
 *   <li><b>Committed State:</b> Transaction committed, stamps updated with actual timestamp</li>
 *   <li><b>Observable Transition:</b> {@link dev.ikm.komet.framework.observable.ObservableStamp}
 *       properties automatically reflect the state transition</li>
 * </ol>
 *
 * <p>{@link dev.ikm.komet.framework.observable.ObservableComposer} provides high-level transaction
 * management with observable state properties that UI components can bind to.
 *
 * <h2>Class Structure Overview</h2>
 *
 * <pre>
 * ObservableEntity (abstract sealed)
 * ├── ObservableConcept
 * ├── ObservableSemantic
 * ├── ObservablePattern
 * └── ObservableStamp
 *
 * ObservableVersion (abstract sealed)
 * ├── ObservableVersion.Editable (abstract sealed inner class)
 * ├── ObservableConceptVersion
 * │   └── ObservableConceptVersion.Editable (inner class)
 * ├── ObservableSemanticVersion
 * │   └── ObservableSemanticVersion.Editable (inner class)
 * ├── ObservablePatternVersion
 * │   └── ObservablePatternVersion.Editable (inner class)
 * └── ObservableStampVersion
 *     └── ObservableStampVersion.Editable (inner class)
 *
 * ObservableFeature (sealed)
 * ├── ObservableFeature.Editable (abstract sealed inner class)
 * └── ObservableField
 *     └── ObservableField.Editable (inner class)
 *
 * ObservableFeatureDefinition (final)
 * └── ObservableFeatureDefinition.Editable (inner class)
 * </pre>
 *
 * <h2>Key Design Principles</h2>
 *
 * <ol>
 *   <li><b>Canonical Instances:</b> One observable instance per entity NID ensures UI consistency</li>
 *   <li><b>Inner Class Pattern:</b> Editable versions as inner classes express relationships clearly</li>
 *   <li><b>JavaFX Native:</b> All properties are JavaFX observables for seamless UI integration</li>
 *   <li><b>Weak Caching:</b> Automatic memory management without manual lifecycle tracking</li>
 *   <li><b>Thread Confinement:</b> JavaFX thread-only model simplifies reasoning about state</li>
 *   <li><b>Event Bus Integration:</b> Changes propagate through {@link dev.ikm.tinkar.common.service.EvtBus}</li>
 * </ol>
 *
 * <h2>Best Practices</h2>
 *
 * <ul>
 *   <li><b>Use {@link dev.ikm.komet.framework.observable.ObservableEntityHandle}:</b>
 *       For canonical entity access by NID or PublicId</li>
 *   <li><b>Use {@link dev.ikm.komet.framework.observable.ObservableComposer}:</b>
 *       For transactional entity creation and editing with unified API</li>
 *   <li><b>Bind, Don't Poll:</b> Use JavaFX property binding instead of manual synchronization</li>
 *   <li><b>Check isDirty():</b> Before saving to avoid unnecessary database writes</li>
 *   <li><b>Commit Explicitly:</b> Call {@code composer.commit()} to finalize transactions</li>
 *   <li><b>Handle Uncommitted State:</b> UI should show visual indicators for uncommitted changes</li>
 * </ul>
 *
 * <h2>Performance Considerations</h2>
 *
 * <ul>
 *   <li><b>Lazy Loading:</b> Observable entities load versions on demand</li>
 *   <li><b>Property Efficiency:</b> JavaFX properties only notify listeners when values change</li>
 *   <li><b>Cache Efficiency:</b> Weak caching prevents memory bloat without sacrificing performance</li>
 *   <li><b>Event Bus Throttling:</b> Consider throttling UI updates for high-frequency changes</li>
 * </ul>
 *
 * <h2>Related Packages</h2>
 *
 * <ul>
 *   <li>{@code dev.ikm.tinkar.entity} - Underlying immutable entity system</li>
 *   <li>{@code dev.ikm.tinkar.entity.transaction} - Transaction management</li>
 *   <li>{@code dev.ikm.tinkar.common.service} - Event bus and service layer</li>
 *   <li>{@code dev.ikm.komet.framework.propsheet} - Property sheet UI components</li>
 * </ul>
 *
 * @see dev.ikm.komet.framework.observable.ObservableEntity
 * @see dev.ikm.komet.framework.observable.ObservableVersion
 * @see dev.ikm.komet.framework.observable.ObservableComposer
 * @see dev.ikm.komet.framework.observable.ObservableEntityHandle
 * @since 1.0
 */
package dev.ikm.komet.framework.observable;
