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
import dev.ikm.komet.reasoner.elkowl.ElkOwlReasonerService;
import dev.ikm.komet.reasoner.service.ReasonerService;

module dev.ikm.komet.reasoner.elkowl {
	requires org.eclipse.collections;
	requires org.eclipse.collections.api;
	requires org.slf4j;

	requires dev.ikm.tinkar.collection;
	requires dev.ikm.tinkar.coordinate;
	requires dev.ikm.tinkar.entity;

	requires org.semanticweb.owlapi;
	requires org.semanticweb.owlapi.apibinding;
	requires org.semanticweb.owlapi.impl;
	requires org.semanticweb.owlapi.parsers;

	requires org.semanticweb.elk.owlapi;

	requires dev.ikm.elk.snomed;
	requires dev.ikm.elk.snomed.owl;

	requires dev.ikm.komet.reasoner.service;

	exports dev.ikm.komet.reasoner.elkowl;

	provides ReasonerService with ElkOwlReasonerService;

}
