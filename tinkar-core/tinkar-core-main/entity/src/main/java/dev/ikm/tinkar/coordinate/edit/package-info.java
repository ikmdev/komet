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
 * <h2>Edit Coordinate System</h2>
 *
 * <p>Provides coordinates that specify metadata for creating, modifying, and organizing knowledge
 * content within the Tinkar framework. Edit coordinates ensure proper provenance tracking and
 * organizational structure for all changes made to the knowledge graph.</p>
 *
 * <h3>Core Responsibilities</h3>
 *
 * <p>Edit coordinates manage five critical aspects of content authoring and modification:</p>
 *
 * <dl>
 * <dt><strong>Author Attribution</strong></dt>
 * <dd>Specifies which user or system is responsible for changes. The author NID is recorded in
 * the STAMP metadata of every version created or modified.</dd>
 *
 * <dt><strong>Default Module</strong></dt>
 * <dd>Defines the module where new content is created. When developing new concepts, semantics,
 * or patterns, they are assigned to this module. Modifications to existing content retain their
 * original module assignment.</dd>
 *
 * <dt><strong>Destination Module</strong></dt>
 * <dd>Specifies the target module when performing modularization operations. Existing content can
 * be moved to this module during module reorganization activities.</dd>
 *
 * <dt><strong>Default Path</strong></dt>
 * <dd>Identifies the development path where new content is created. This path forms part of the
 * STAMP coordinate and determines the branching context for new versions.</dd>
 *
 * <dt><strong>Promotion Path</strong></dt>
 * <dd>Defines the target path when promoting content from one development branch to another
 * (e.g., from development to staging, or from staging to production).</dd>
 * </dl>
 *
 * <h3>Development Workflow Support</h3>
 *
 * <p>Edit coordinates support three primary content management workflows:</p>
 *
 * <h4>1. Developing</h4>
 * <p>Creating new content or modifying existing content on the current development path:</p>
 * <ul>
 * <li>New content is assigned to the <strong>default module</strong></li>
 * <li>New content is created on the <strong>default path</strong></li>
 * <li>Changes are attributed to the specified <strong>author</strong></li>
 * <li>Existing content retains its original module assignment</li>
 * </ul>
 *
 * <h4>2. Modularizing</h4>
 * <p>Reorganizing content across module boundaries:</p>
 * <ul>
 * <li>Content is moved to the <strong>destination module</strong></li>
 * <li>Changes are written to the current <strong>default path</strong></li>
 * <li>Preserves version history while changing module organization</li>
 * </ul>
 *
 * <h4>3. Promoting</h4>
 * <p>Moving content from one development branch to another:</p>
 * <ul>
 * <li>Content retains its current <strong>module assignment</strong></li>
 * <li>A copy of the content is written to the <strong>promotion path</strong></li>
 * <li>Supports staged release workflows (dev → test → production)</li>
 * </ul>
 *
 * <h3>Core Interface</h3>
 *
 * <p>The {@link dev.ikm.tinkar.coordinate.edit.EditCoordinate} interface defines the contract
 * for all edit coordinate implementations. It provides:</p>
 *
 * <ul>
 * <li>Accessor methods for all five coordinate components (author, modules, paths)</li>
 * <li>Content-based UUID generation for coordinate identity and caching</li>
 * <li>Conversion to {@link dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord} for immutability</li>
 * <li>User-friendly string representation for debugging and display</li>
 * </ul>
 *
 * <h3>Implementation Patterns</h3>
 *
 * <p>The package provides two primary implementation patterns:</p>
 *
 * <h4>EditCoordinateRecord</h4>
 * <p>Immutable record implementation suitable for most use cases. Created via factory methods
 * or the {@code Coordinates.Edit} class:</p>
 * <pre>{@code
 * EditCoordinateRecord editCoord = Coordinates.Edit.Default();
 * EditCoordinateRecord customCoord = EditCoordinateRecord.make(
 *     authorNid,
 *     defaultModuleNid,
 *     destinationModuleNid,
 *     defaultPathNid,
 *     promotionPathNid
 * );
 * }</pre>
 *
 * <h4>EditCoordinateDelegate</h4>
 * <p>Delegation pattern allowing classes to implement EditCoordinate by delegating to an
 * underlying coordinate instance. Useful for view coordinates and other composite structures:</p>
 * <pre>{@code
 * public class MyViewCoordinate implements EditCoordinateDelegate {
 *     private final EditCoordinate editCoordinate;
 *
 *     @Override
 *     public EditCoordinate getEditCoordinate() {
 *         return editCoordinate;
 *     }
 * }
 * }</pre>
 *
 * <h3>Activity Enumeration</h3>
 *
 * <p>The {@link dev.ikm.tinkar.coordinate.edit.Activity} enum identifies which workflow
 * is currently active:</p>
 * <ul>
 * <li>{@code DEVELOPING} - Creating or modifying content in place</li>
 * <li>{@code MODULARIZING} - Reorganizing content across modules</li>
 * <li>{@code PROMOTING} - Moving content across development paths</li>
 * </ul>
 *
 * <h3>Integration with STAMP</h3>
 *
 * <p>Edit coordinates work in conjunction with STAMP coordinates to provide complete version
 * control:</p>
 *
 * <ul>
 * <li><strong>Author</strong> from edit coordinate → Author field in STAMP</li>
 * <li><strong>Module</strong> from edit coordinate → Module field in STAMP</li>
 * <li><strong>Path</strong> from edit coordinate → Path field in STAMP</li>
 * <li><strong>Time</strong> automatically recorded → Time field in STAMP</li>
 * <li><strong>State</strong> specified during edit → Status field in STAMP</li>
 * </ul>
 *
 * <h3>Usage Example</h3>
 *
 * <pre>{@code
 * // Create an edit coordinate for development work
 * EditCoordinateRecord editCoord = EditCoordinateRecord.make(
 *     TinkarTerm.USER.nid(),                    // author
 *     TinkarTerm.SOLOR_OVERLAY_MODULE.nid(),    // default module for new content
 *     TinkarTerm.SOLOR_OVERLAY_MODULE.nid(),    // destination module (when modularizing)
 *     TinkarTerm.DEVELOPMENT_PATH.nid(),        // default path for new content
 *     TinkarTerm.MASTER_PATH.nid()              // promotion path (when promoting)
 * );
 *
 * // Use in content creation
 * ConceptBuilder builder = ConceptBuilder.builder()
 *     .author(editCoord.getAuthorForChanges())
 *     .module(editCoord.getDefaultModule())
 *     .path(editCoord.getDefaultPath())
 *     .build();
 * }</pre>
 *
 * <h3>Thread Safety and Immutability</h3>
 *
 * <p>EditCoordinateRecord instances are immutable and thread-safe, making them suitable for:</p>
 * <ul>
 * <li>Sharing across multiple threads</li>
 * <li>Use as map keys or in sets</li>
 * <li>Caching and reuse</li>
 * <li>Functional programming patterns</li>
 * </ul>
 *
 * @see dev.ikm.tinkar.coordinate.edit.EditCoordinate
 * @see dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord
 * @see dev.ikm.tinkar.coordinate.edit.EditCoordinateDelegate
 * @see dev.ikm.tinkar.coordinate.edit.Activity
 * @see dev.ikm.tinkar.coordinate.stamp.StampCoordinate
 * @see dev.ikm.tinkar.coordinate.Coordinates.Edit
 */
package dev.ikm.tinkar.coordinate.edit;
