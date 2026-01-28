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

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import dev.ikm.tinkar.common.util.broadcast.Subscriber;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.common.sets.ConcurrentHashSet;

public class EditedConceptTracker {

	private static ArrayList<SemanticEntityVersion> edits = new ArrayList<>();
	private static final ConcurrentHashSet<Integer> changedEntityNids = new ConcurrentHashSet<>();
	private static final AtomicBoolean subscribed = new AtomicBoolean(false);

	public static ArrayList<SemanticEntityVersion> getEdits() {
		return edits;
	}

	public static void ensureSubscribed() {
		if (subscribed.compareAndSet(false, true)) {
			Subscriber<Integer> subscriber = changedEntityNids::add;
			Entity.provider().addSubscriberWithWeakReference(subscriber);
		}
	}

	public static void addEditsFromChanges(ViewCalculator viewCalculator) {
		int statedPatternNid = viewCalculator.logicCoordinateRecord().statedAxiomsPatternNid();
		for (Integer nid : changedEntityNids.toArray(new Integer[0])) {
			Entity entity = Entity.getFast(nid);
			if (entity instanceof SemanticEntity<?> semantic && semantic.patternNid() == statedPatternNid) {
				Latest<SemanticEntityVersion> latestSemantic = viewCalculator.latest(semantic.nid());
				latestSemantic.ifPresent(EditedConceptTracker::addEdit);
			}
			changedEntityNids.remove(nid);
		}
	}

	public static void addEdit(SemanticEntityVersion edit) {
		edits.removeIf(ex_edit -> ex_edit.referencedComponentNid() == edit.referencedComponentNid());
		edits.add(edit);
	}

	public static void removeEdits() {
		edits = new ArrayList<>();
	}

}
