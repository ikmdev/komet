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
import dev.ikm.tinkar.common.service.ExecutorController;

module dev.ikm.komet.amplify.test {
    requires transitive dev.ikm.komet.framework;
    requires transitive dev.ikm.komet.amplify;
//    requires dev.ikm.tinkar.common;
//    requires dev.ikm.tinkar.entity;
//    requires dev.ikm.tinkar.provider.entity;
//    requires dev.ikm.tinkar.terms;
//    requires dev.ikm.tinkar.coordinate;
//    requires com.google.protobuf;
    requires dev.ikm.komet.executor;
    requires org.junit.jupiter.api;
    requires org.slf4j;
//    requires org.eclipse.collections.api;
//    provides ExecutorController with KometExecutorController;
    uses dev.ikm.komet.framework.events.EvtBus;
    uses ExecutorController;
    uses CachingService;
//    uses DataServiceController;
//    uses DefaultDescriptionForNidService;
//    uses EntityService;
//    uses PublicIdService;
//    uses StampService;
    exports dev.ikm.komet.amplify.mvvm.test;
}
