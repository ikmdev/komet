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
import dev.ikm.tinkar.common.service.ExecutorController;
import dev.ikm.tinkar.provider.executor.AlertLogSubscriber;
import dev.ikm.tinkar.provider.executor.ExecutorProviderController;

@SuppressWarnings("module")
        // 7 in HL7 is not a version reference
module dev.ikm.tinkar.provider.executor {
    requires org.slf4j;
    
    provides AlertReportingService with AlertLogSubscriber;
    provides CachingService with ExecutorProviderController.CacheProvider;
    provides ExecutorController with ExecutorProviderController;

    requires java.base;
    requires dev.ikm.tinkar.common;
    
    exports dev.ikm.tinkar.provider.executor;
}

