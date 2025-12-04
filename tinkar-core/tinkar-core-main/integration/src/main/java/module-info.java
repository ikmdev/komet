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
import dev.ikm.tinkar.common.service.DataServiceController;
import dev.ikm.tinkar.common.service.DefaultDescriptionForNidService;
import dev.ikm.tinkar.common.service.PublicIdService;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.StampService;

open module dev.ikm.tinkar.integration {
    exports dev.ikm.tinkar.integration;
    requires dev.ikm.tinkar.common;
    requires dev.ikm.tinkar.entity;
    requires dev.ikm.tinkar.provider.entity;
    requires dev.ikm.tinkar.terms;
    requires dev.ikm.jpms.protobuf;
    requires org.slf4j;
    requires org.eclipse.collections.api;
    requires org.eclipse.collections.impl;
    requires dev.ikm.tinkar.provider.search;
    requires org.junit.jupiter.api;
    requires dev.ikm.tinkar.provider.ephemeral;
    requires dev.ikm.tinkar.provider.spinedarray;
    requires com.github.benmanes.caffeine;

    uses DataServiceController;
    uses DefaultDescriptionForNidService;
    uses EntityService;
    uses PublicIdService;
    uses StampService;
}
