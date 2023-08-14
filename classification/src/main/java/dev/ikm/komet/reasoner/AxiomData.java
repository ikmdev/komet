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
package dev.ikm.komet.reasoner;

import au.csiro.ontology.model.Axiom;
import au.csiro.ontology.model.Concept;
import au.csiro.ontology.model.Feature;
import au.csiro.ontology.model.Role;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import dev.ikm.tinkar.collection.SpinedIntObjectMap;
import dev.ikm.tinkar.common.sets.ConcurrentHashSet;
import org.roaringbitmap.buffer.ImmutableRoaringBitmap;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AxiomData<A> {
    public final SpinedIntObjectMap<ImmutableList<A>> nidAxiomsMap = new SpinedIntObjectMap<>();
    public final ConcurrentHashSet<A> axiomsSet = new ConcurrentHashSet<>();
    public final ConcurrentHashMap<Integer, Concept> nidConceptMap = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<Integer, Feature> nidFeatureMap = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<Integer, Role> nidRoleMap = new ConcurrentHashMap<>();
    public final AtomicInteger processedSemantics = new AtomicInteger();

    public ImmutableIntList classificationConceptSet = null;
}
