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
import dev.ikm.komet.framework.KometNodeFactory;
import dev.ikm.komet.reasoner.ReasonerResultsNodeFactory;
import dev.ikm.tinkar.reasoner.service.ReasonerService;

module dev.ikm.komet.classification {
	requires dev.ikm.jpms.eclipse.collections;
	requires dev.ikm.jpms.eclipse.collections.api;
	requires dev.ikm.jpms.roaringbitmap;
	requires org.jgrapht.core;
	requires org.slf4j;
	
	requires dev.ikm.tinkar.collection;
	requires dev.ikm.tinkar.coordinate;
	requires dev.ikm.tinkar.entity;

	requires transitive dev.ikm.komet.framework;

	requires dev.ikm.tinkar.reasoner.service;

	uses ReasonerService;

	requires dev.ikm.tinkar.reasoner.elkowl;
	requires dev.ikm.tinkar.reasoner.elksnomed;
	requires dev.ikm.tinkar.reasoner.hybrid;

	exports dev.ikm.komet.reasoner;

	opens dev.ikm.komet.reasoner;

	provides KometNodeFactory with ReasonerResultsNodeFactory;

}
