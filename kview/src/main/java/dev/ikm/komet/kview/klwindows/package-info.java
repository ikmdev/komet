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
 * Provides interfaces and implementations for a comprehensive window management framework
 * within the Komet application.
 * <p>
 * This package enables the creation, persistence, and management of specialized windows
 * that display and edit various types of knowledge entities (concepts, patterns, etc.)
 * within a unified workspace environment. The framework is built around a type-based
 * system that allows for consistent handling of different window categories while
 * supporting specialized behavior for each entity type.
 * <p>
 * Key components include:
 * <ul>
 *   <li><b>Window Type System</b> - Interfaces and implementations for classifying windows
 *       by their purpose and content type ({@link dev.ikm.komet.kview.klwindows.EntityKlWindowType},
 *       {@link dev.ikm.komet.kview.klwindows.EntityKlWindowTypes})</li>
 *   <li><b>State Management</b> - Classes for capturing, persisting, and restoring window
 *       state, including position, size, and entity references
 *       ({@link dev.ikm.komet.kview.klwindows.EntityKlWindowState})</li>
 *   <li><b>Window Interfaces</b> - Core interfaces defining window behavior and lifecycle
 *       management ({@link dev.ikm.komet.kview.klwindows.ChapterKlWindow})</li>
 *   <li><b>Factory System</b> - Factory interfaces and registry for creating appropriate
 *       window implementations based on entity types
 *       ({@link dev.ikm.komet.kview.klwindows.EntityKlWindowFactory})</li>
 *   <li><b>Base Implementations</b> - Abstract classes providing common functionality
 *       for concrete window implementations
 *       ({@link dev.ikm.komet.kview.klwindows.AbstractChapterKlWindow},
 *       {@link dev.ikm.komet.kview.klwindows.AbstractEntityChapterKlWindow})</li>
 * </ul>
 * <p>
 * The framework supports four primary window types, each with specialized factories:
 * <ul>
 *   <li>Concept Windows - For viewing and editing conceptual entities</li>
 *   <li>Pattern Windows - For viewing and editing pattern entity structures</li>
 *   <li>LIDR Windows - For viewing and editing LIDR interfaces</li>
 *   <li>General Editing Windows - For pattern-semantic editing functionality</li>
 * </ul>
 * <p>
 * <h2>Extensibility Architecture</h2>
 * The framework is designed for maximum extensibility, making it
 * straightforward to add new window types as the application evolves. This flexibility is
 * achieved through several architectural patterns:
 * <p>
 * <ul>
 *   <li><b>Pluggable Type System</b> - New window types can be created by either:
 *     <ul>
 *       <li>Adding constants to the existing {@code EntityKlWindowTypes} enum</li>
 *       <li>Creating entirely new enum implementations of {@code EntityKlWindowType}</li>
 *       <li>Implementing {@code EntityKlWindowType} directly for non-enum types</li>
 *     </ul>
 *   </li>
 *   <li><b>Dynamic Type Registration</b> - The {@code EntityKlWindowType.Registry} provides
 *       methods to register both enum classes and individual instances at runtime, allowing
 *       for modular extensions without modifying core code</li>
 *   <li><b>Factory Pattern + Service Loader</b> - New window implementations can be added by:
 *     <ul>
 *       <li>Implementing the {@code EntityKlWindowFactory} interface</li>
 *       <li>Creating a concrete window implementation extending {@code AbstractEntityChapterKlWindow}</li>
 *       <li>Registering the factory through Java's ServiceLoader mechanism</li>
 *     </ul>
 *   </li>
 *   <li><b>State Inheritance</b> - The {@code EntityKlWindowState} class supports arbitrary
 *       additional properties, enabling new window types to store type-specific state
 *       without requiring changes to the core persistence framework</li>
 *   <li><b>Abstract Base Classes</b> - Extending {@code AbstractEntityChapterKlWindow} provides
 *       immediate access to core functionality while allowing specialization through template
 *       method patterns</li>
 * </ul>
 * <p>
 * <h3>Adding a New Window Type</h3>
 * The process to add a new window type requires minimal code and follows these steps:
 * <p>
 * <ol>
 *   <li>Define a new window type identifier:
 *     <pre>{@code
 *     // Add to existing enum
 *     enum EntityKlWindowTypes implements EntityKlWindowType {
 *         // Existing types...
 *         NEW_TYPE("new_type_")
 *     }
 *     // OR create custom implementation
 *     public class CustomWindowType implements EntityKlWindowType {
 *         public static final CustomWindowType INSTANCE = new CustomWindowType();
 *         @Override public String getPrefix() { return "custom_"; }
 *         // Register in static block
 *         static { EntityKlWindowType.Registry.registerInstance(INSTANCE); }
 *     }
 *     }</pre>
 *   </li>
 *   <li>Create a specialized window implementation:
 *     <pre>{@code
 *     public class CustomEntityChapterKlWindow extends AbstractEntityChapterKlWindow {
 *         // Implement specialized behavior...
 *     }
 *     }</pre>
 *   </li>
 *   <li>Create and register a factory:
 *     <pre>{@code
 *     public class CustomKlWindowFactory implements EntityKlWindowFactory {
 *         @Override
 *         public AbstractEntityChapterKlWindow create(UUID journalTopic,
 *                                              EntityFacade entityFacade,
 *                                              ViewProperties viewProperties,
 *                                              KometPreferences preferences) {
 *             return new CustomEntityChapterKlWindow(journalTopic,
 *                                                    entityFacade,
 *                                                    viewProperties,
 *                                                    preferences);
 *         }
 *
 *         @Override
 *         public EntityKlWindowType getWindowType() {
 *             return CustomWindowType.INSTANCE; // or EntityKlWindowTypes.NEW_TYPE
 *         }
 *     }
 *     }</pre>
 *   </li>
 *   <li>Register the factory via module-info.java:
 *     <pre>{@code
 *     provides EntityKlWindowFactory with
 *             // Existing factories...
 *             my.package.CustomKlWindowFactory;
 *     }</pre>
 *   </li>
 * </ol>
 * <p>
 * This extensible architecture ensures that the Komet application can evolve to support
 * new entity types and specialized visualization/editing requirements without requiring
 * modifications to the core window management framework. The combination of interfaces,
 * abstract base classes, and registry patterns creates a system where "closed for modification,
 * open for extension" principles are fully realized.
 * <p>
 * The window system integrates with the Komet preferences framework to enable persistence
 * of window positions, sizes, and content across application sessions. It also supports
 * event-based communication between related windows through journal topics.
 * <p>
 * Example usage:
 * <pre>{@code
 * // Get a factory for a specific window type
 * EntityKlWindowFactory factory = EntityKlWindowFactory.Registry.getFactory(EntityKlWindowTypes.CONCEPT);
 *
 * // Create a window for an entity
 * CompletableFuture<ChapterKlWindow<Pane>> windowFuture = EntityKlWindowFactory.Registry.createWindowAsync(
 *     EntityKlWindowTypes.CONCEPT,
 *     journalUuid,
 *     entityFacade,
 *     viewProperties,
 *     preferences
 * );
 *
 * // Handle the created window
 * windowFuture.thenAccept(window -> {
 *     // Add the window to the workspace
 *     workspace.addWindow(window);
 *
 *     // Set up window close handling
 *     window.setOnClose(() -> workspace.removeWindow(window));
 * });
 * }</pre>
 *
 * @see dev.ikm.komet.kview.klwindows.EntityKlWindowType
 * @see dev.ikm.komet.kview.klwindows.EntityKlWindowTypes
 * @see dev.ikm.komet.kview.klwindows.EntityKlWindowState
 * @see dev.ikm.komet.kview.klwindows.ChapterKlWindow
 * @see dev.ikm.komet.kview.klwindows.EntityKlWindowFactory
 * @see dev.ikm.komet.kview.klwindows.AbstractChapterKlWindow
 * @see dev.ikm.komet.kview.klwindows.AbstractEntityChapterKlWindow
 */
package dev.ikm.komet.kview.klwindows;