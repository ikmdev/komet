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
package dev.ikm.tinkar.reasoner.elkowl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import dev.ikm.tinkar.collection.SpinedIntObjectMap;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.sets.ConcurrentHashSet;

/**
 * @deprecated
 * No longer maintained.
 * 
 * Use dev.ikm.tinkar.reasoner.elksnomed
 */
@Deprecated
public class ElkOwlData {

	public final SpinedIntObjectMap<ImmutableList<OWLAxiom>> nidAxiomsMap = new SpinedIntObjectMap<>();

	public final ConcurrentHashSet<OWLAxiom> axiomsSet = new ConcurrentHashSet<>();

	public final ConcurrentHashMap<Integer, OWLClass> nidConceptMap = new ConcurrentHashMap<>();

	public final ConcurrentHashMap<Integer, OWLObjectProperty> nidRoleMap = new ConcurrentHashMap<>();

	public final AtomicInteger processedSemantics = new AtomicInteger();

	private final AtomicInteger activeConceptCount = new AtomicInteger();

	private final AtomicInteger inactiveConceptCount = new AtomicInteger();

	public ImmutableIntList classificationConceptSet = null;

	private OWLDataFactory dataFactory;

	public int getActiveConceptCount() {
		return activeConceptCount.get();
	}

	public int incrementActiveConceptCount() {
		return activeConceptCount.incrementAndGet();
	}

	public int getInactiveConceptCount() {
		return inactiveConceptCount.get();
	}

	public int incrementInactiveConceptCount() {
		return inactiveConceptCount.incrementAndGet();
	}

	public ElkOwlData(OWLDataFactory dataFactory) {
		this.dataFactory = dataFactory;
	}

	private OWLObjectProperty getOwlObjectProperty(int id) {
		return dataFactory.getOWLObjectProperty(":" + id, ElkOwlPrefixManager.getPrefixManager());
	}

	private OWLClass getOwlClass(int id) {
		return dataFactory.getOWLClass(":" + id, ElkOwlPrefixManager.getPrefixManager());
	}

	public OWLObjectProperty getRole(int roleNid) {
		return this.nidRoleMap.computeIfAbsent(roleNid, this::getOwlObjectProperty);
	}

	public OWLClass getConcept(int conceptNid) {
		return this.nidConceptMap.computeIfAbsent(conceptNid, this::getOwlClass);
	}

	public void writeConcepts(Path path) throws Exception {
		Files.write(path, nidConceptMap.keySet().stream() //
				.map(key -> PrimitiveData.publicId(key).asUuidArray()[0] + "\t" + PrimitiveData.text(key)) //
				.sorted() //
				.toList());
	}

	public void writeRoles(Path path) throws Exception {
		Files.write(path, nidRoleMap.keySet().stream() //
				.map(key -> PrimitiveData.publicId(key).asUuidArray()[0] + "\t" + PrimitiveData.text(key)) //
				.sorted() //
				.toList());
	}

	public void writeAxioms(Path path) throws Exception {
		Files.write(path, axiomsSet.stream() //
				.map(ElkOwlPrefixManager::removePrefix) //
				.sorted() //
				.toList());
	}

}
