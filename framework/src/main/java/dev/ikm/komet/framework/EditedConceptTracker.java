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
package dev.ikm.komet.framework;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.entity.ConceptEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.tinkar.common.util.broadcast.Subscriber;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.common.sets.ConcurrentHashSet;

public class EditedConceptTracker {

	private static final Logger LOG = LoggerFactory.getLogger(EditedConceptTracker.class);

	// Use ConcurrentHashMap to store edits keyed by referencedComponentNid
	private static final ConcurrentHashMap<Integer, SemanticEntityVersion> edits = new ConcurrentHashMap<>();
	private static final ConcurrentHashSet<Integer> changedEntityNids = new ConcurrentHashSet<>();
	private static final AtomicBoolean subscribed = new AtomicBoolean(false);
	
	// Store subscriber as a strong reference to prevent GC - must be after changedEntityNids
	private static final Subscriber<Integer> subscriber = nid -> {
		if (nid == Integer.MIN_VALUE) {
			return; // Sentinel from endLoadPhase — not a real entity
		}
		changedEntityNids.add(nid);
		if (LOG.isDebugEnabled()) {
			Entity entity = Entity.getFast(nid);
			if (entity != null) {
				LOG.debug("Entity changed: nid={}, type={}", nid, entity.getClass().getSimpleName());
			} else {
				LOG.debug("Entity changed: nid={}, type=unknown (entity not found)", nid);
			}
		}
	};

	// Static initializer to subscribe as soon as class loads
	static {
		LOG.info("EditedConceptTracker initializing");
		ensureSubscribed();
	}

	/**
	 * Call this method early in application startup to ensure the tracker
	 * is subscribed before any entity changes occur.
	 */
	public static void initialize() {
		// Just accessing this class triggers the static initializer
		LOG.info("EditedConceptTracker initialized, subscription active: {}", subscribed.get());
	}

	public static Collection<SemanticEntityVersion> getEdits() {
		return edits.values();
	}

	public static void ensureSubscribed() {
		if (subscribed.compareAndSet(false, true)) {
			// Use the stored subscriber instance to prevent GC
			Entity.provider().addSubscriberWithWeakReference(subscriber);
			LOG.info("=== SUBSCRIBED to entity changes at {} ===", System.currentTimeMillis());
		} else {
			LOG.debug("Already subscribed to entity changes");
		}
	}

	public static void addEditsFromChanges(ViewCalculator viewCalculator) {
		int statedPatternNid = viewCalculator.logicCoordinateRecord().statedAxiomsPatternNid();
		int changeCount = changedEntityNids.size();
		
		LOG.info("=== PROCESSING CHANGES ===");
		LOG.info("Subscription active: {}", subscribed.get());
		LOG.info("Total changed entities to process: {}", changeCount);
		LOG.info("Total edits already tracked: {}", edits.size());
		LOG.info("Stated axioms pattern NID: {} ({})", statedPatternNid, PrimitiveData.text(statedPatternNid));
		
		if (changeCount == 0) {
			LOG.warn("*** NO CHANGES CAPTURED - either no edits were made, or subscription happened after changes ***");
		}
		
		int processedSemanticCount = 0;
		int processedConceptCount = 0;
		int skippedCount = 0;
		
		for (Integer nid : changedEntityNids.toArray(new Integer[0])) {
			try {
				Entity entity = Entity.getFast(nid);
				if (entity == null) {
					LOG.info("  -> Skipped nid={}: entity not found in data store", nid);
					skippedCount++;
					changedEntityNids.remove(nid);
					continue;
				}
				String entityType = entity.getClass().getSimpleName();

				LOG.info("Processing entity: nid={}, type={}, text='{}'", nid, entityType, PrimitiveData.text(nid));

				// Case 1: The NID is directly a semantic with the stated pattern
				if (entity instanceof SemanticEntity<?> semantic && semantic.patternNid() == statedPatternNid) {
					LOG.info("  -> Found SEMANTIC with stated pattern: nid={}, referencedComponentNid={} ({})",
							nid, semantic.referencedComponentNid(), PrimitiveData.text(semantic.referencedComponentNid()));

					Latest<SemanticEntityVersion> latestSemantic = viewCalculator.latest(semantic.nid());
					if (latestSemantic.isPresent()) {
						latestSemantic.ifPresent(version -> {
							addEdit(version);
							LOG.info("     Added edit for semantic version");
						});
						processedSemanticCount++;
					} else {
						LOG.warn("     No latest version found for semantic nid={}", nid);
					}
				}
				// Case 2: The NID is a concept - find its stated axiom semantics
				else if (entity instanceof ConceptEntity<?>) {
					LOG.info("  -> Found CONCEPT: nid={}, searching for stated axiom semantics", nid);

					int[] semanticNids = PrimitiveData.get().semanticNidsForComponentOfPattern(nid, statedPatternNid);
					LOG.info("     Found {} semantic(s) with stated pattern for concept", semanticNids.length);

					for (int semanticNid : semanticNids) {
						Entity semanticEntity = Entity.getFast(semanticNid);
						if (semanticEntity instanceof SemanticEntity<?> semantic) {
							Latest<SemanticEntityVersion> latestSemantic = viewCalculator.latest(semantic.nid());
							if (latestSemantic.isPresent()) {
								latestSemantic.ifPresent(version -> {
									addEdit(version);
									LOG.info("     Added edit for concept's semantic nid={}", semanticNid);
								});
								processedConceptCount++;
							} else {
								LOG.warn("     No latest version for semantic nid={}", semanticNid);
							}
						}
					}
				} else {
					LOG.debug("  -> Skipped entity (not concept or stated semantic): type={}", entityType);
					skippedCount++;
				}
			} catch (Exception e) {
				LOG.error("Error processing changed entity nid={}: {}", nid, e.getMessage(), e);
				skippedCount++;
			}

			changedEntityNids.remove(nid);
		}
		
		LOG.info("=== PROCESSING COMPLETE ===");
		LOG.info("Processed {} direct semantics, {} concept semantics, skipped {} others", 
				processedSemanticCount, processedConceptCount, skippedCount);
		LOG.info("Total edits in tracker: {}", edits.size());
		LOG.info("===========================");
	}

	public static void addEdit(SemanticEntityVersion edit) {
		// ConcurrentHashMap automatically handles replacement
		Integer key = edit.referencedComponentNid();
		SemanticEntityVersion previous = edits.put(key, edit);
		if (previous != null) {
			LOG.debug("Replaced existing edit for referencedComponentNid={} ({})", 
					key, PrimitiveData.text(key));
		} else {
			LOG.info("Added NEW edit for referencedComponentNid={} ({})", 
					key, PrimitiveData.text(key));
		}
	}

	public static void removeEdits() {
		int editCount = edits.size();
		int pendingCount = changedEntityNids.size();
		edits.clear();
		changedEntityNids.clear();
		LOG.info("Cleared {} edits and {} pending changes", editCount, pendingCount);
	}

}
