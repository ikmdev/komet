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
package dev.ikm.komet.reasoner.elksnomed;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.eclipse.collections.api.list.primitive.ImmutableIntList;

import dev.ikm.elk.snomed.model.Concept;
import dev.ikm.elk.snomed.model.RoleType;
import dev.ikm.tinkar.common.service.PrimitiveData;

public class ElkSnomedData {

	public final ConcurrentHashMap<Integer, Concept> nidConceptMap = new ConcurrentHashMap<>();

	public final ConcurrentHashMap<Integer, RoleType> nidRoleMap = new ConcurrentHashMap<>();

	public final AtomicInteger processedSemantics = new AtomicInteger();

	public final AtomicInteger activeConceptCount = new AtomicInteger();

	public final AtomicInteger inactiveConceptCount = new AtomicInteger();

	public ImmutableIntList classificationConceptSet = null;

	public RoleType getRole(int roleNid) {
		return this.nidRoleMap.computeIfAbsent(roleNid, RoleType::new);
	}

	public Concept getConcept(int conceptNid) {
		return this.nidConceptMap.computeIfAbsent(conceptNid, Concept::new);
	}

	public void writeConcepts(Path path) throws Exception {
		Files.write(path, nidConceptMap.keySet().stream() //
				.map(key -> PrimitiveData.publicId(key).asUuidArray()[0] + "\t" + PrimitiveData.text(key)) //
				.sorted() //
				.collect(Collectors.toList()));
	}

	public void writeRoles(Path path) throws Exception {
		Files.write(path, nidRoleMap.keySet().stream() //
				.map(key -> PrimitiveData.publicId(key).asUuidArray()[0] + "\t" + PrimitiveData.text(key)) //
				.sorted() //
				.collect(Collectors.toList()));
	}

}
