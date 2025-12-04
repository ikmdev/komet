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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import dev.ikm.tinkar.common.alert.AlertReportingService;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.DataServiceController;
import dev.ikm.tinkar.common.service.DefaultDescriptionForNidService;
import dev.ikm.tinkar.common.service.ExecutorController;
import dev.ikm.tinkar.common.service.PluginServiceLoader;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.PrimitiveDataService;
import dev.ikm.tinkar.common.service.PublicIdService;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.common.service.TinkExecutor;

@SuppressWarnings("module")
        // 7 in HL7 is not a version reference
module dev.ikm.tinkar.common {
    requires transitive java.prefs;
    requires dev.ikm.jpms.activej.bytebuf;
    requires dev.ikm.jpms.activej.common;
    requires org.eclipse.collections.api;
    requires org.eclipse.collections.impl;
    requires org.slf4j;

    requires roaringbitmap;

    exports dev.ikm.tinkar.common.alert;
    exports dev.ikm.tinkar.common.binary;
    exports dev.ikm.tinkar.common.bind;
    exports dev.ikm.tinkar.common.bind.annotations;
    exports dev.ikm.tinkar.common.bind.annotations.axioms;
    exports dev.ikm.tinkar.common.bind.annotations.names;
    exports dev.ikm.tinkar.common.bind.annotations.publicid;
    exports dev.ikm.tinkar.common.id;
    exports dev.ikm.tinkar.common.service;
    exports dev.ikm.tinkar.common.util.functional;
    exports dev.ikm.tinkar.common.util.ints2long;
    exports dev.ikm.tinkar.common.util.io;
    exports dev.ikm.tinkar.common.util.text;
    exports dev.ikm.tinkar.common.util.thread;
    exports dev.ikm.tinkar.common.util.time;
    exports dev.ikm.tinkar.common.util.uuid;
    exports dev.ikm.tinkar.common.validation;
    exports dev.ikm.tinkar.common.sets;
    exports dev.ikm.tinkar.common.flow;
    exports dev.ikm.tinkar.common.id.impl;
    exports dev.ikm.tinkar.common.util.broadcast;
    exports dev.ikm.tinkar.common.util;

    provides CachingService with
            TinkExecutor.CacheProvider,
            ServiceProperties.CacheProvider,
            PrimitiveData.CacheProvider,
            PrimitiveDataService.CacheProvider;

    uses AlertReportingService;
    uses CachingService;
    uses DataServiceController;
    uses DefaultDescriptionForNidService;
    uses ExecutorController;
    uses PluginServiceLoader;
    uses PublicIdService;
}
