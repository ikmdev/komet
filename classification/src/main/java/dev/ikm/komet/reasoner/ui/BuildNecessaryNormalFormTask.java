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
package dev.ikm.komet.reasoner.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.tinkar.reasoner.service.ReasonerService;
import dev.ikm.tinkar.common.service.TrackingCallable;

public class BuildNecessaryNormalFormTask extends TrackingCallable<ReasonerService> {

    private static final Logger LOG = LoggerFactory.getLogger(BuildNecessaryNormalFormTask.class);

    private final ReasonerService reasonerService;

    public BuildNecessaryNormalFormTask(ReasonerService reasonerService) {
        super(false, true);
        this.reasonerService = reasonerService;
        updateTitle("Building necessary normal form");
    }

    @Override
    protected ReasonerService compute() throws Exception {
        reasonerService.buildNecessaryNormalForm();
        String msg = "Build necessary normal form in "+ durationString();
        updateMessage(msg);
        LOG.info(msg);
        return reasonerService;
    }

}