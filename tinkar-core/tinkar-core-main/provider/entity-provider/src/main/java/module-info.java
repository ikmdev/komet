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

import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.DefaultDescriptionForNidService;
import dev.ikm.tinkar.common.service.PrimitiveDataService;
import dev.ikm.tinkar.common.service.PublicIdService;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.StampService;
import dev.ikm.tinkar.provider.entity.DefaultDescriptionForNidServiceFactory;
import dev.ikm.tinkar.provider.entity.EntityProvider;
import dev.ikm.tinkar.provider.entity.EntityServiceFactory;
import dev.ikm.tinkar.provider.entity.PublicIdServiceFactory;
import dev.ikm.tinkar.provider.entity.StampProvider;

@SuppressWarnings("module")
// 7 in HL7 is not a version reference
module dev.ikm.tinkar.provider.entity {
    exports dev.ikm.tinkar.provider.entity;
    requires com.github.benmanes.caffeine;
    requires org.eclipse.collections.api;
    requires org.eclipse.collections.impl;
    requires dev.ikm.tinkar.common;
    requires dev.ikm.tinkar.provider.search;
    requires org.slf4j;
    requires dev.ikm.tinkar.terms;

    provides CachingService with EntityProvider.CacheProvider;
    provides DefaultDescriptionForNidService with DefaultDescriptionForNidServiceFactory;
    provides EntityService with EntityServiceFactory;
    provides PublicIdService with PublicIdServiceFactory;
    provides StampService with StampProvider;

	uses PrimitiveDataService;
}
