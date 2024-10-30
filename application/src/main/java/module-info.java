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
import dev.ikm.komet.framework.concurrent.TaskListsService;
import dev.ikm.komet.framework.events.DefaultEvtBus;
import dev.ikm.tinkar.common.service.DataServiceController;
import dev.ikm.tinkar.common.service.DefaultDescriptionForNidService;
import dev.ikm.tinkar.common.service.PublicIdService;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.StampService;

module dev.ikm.komet.application {

    exports dev.ikm.komet.app;
    exports dev.ikm.komet.app.util;
    opens dev.ikm.komet.app to javafx.fxml;

    // TODO Not happy that I have to specify these here... Can't dynamically add modules?
    requires dev.ikm.tinkar.provider.spinedarray;
    requires dev.ikm.tinkar.provider.mvstore;
    requires dev.ikm.tinkar.provider.ephemeral;
    // End not happy...

    // JPro related modules
    requires jpro.webapi;
    requires one.jpro.platform.auth.core;
    requires one.jpro.platform.internal.util;
    opens jpro.html;

    requires javafx.controls;
    requires javafx.fxml;
    requires nsmenufx;
    requires fr.brouillard.oss.cssfx;
    requires org.carlfx.cognitive;
    requires org.controlsfx.controls;
    requires dev.ikm.komet.classification;
    requires dev.ikm.komet.details;
    requires dev.ikm.komet.builder;
    requires dev.ikm.komet.kview;
    requires dev.ikm.komet.artifact;
    requires dev.ikm.komet.executor;
    requires dev.ikm.komet.list;
    requires dev.ikm.komet.navigator;
    requires dev.ikm.komet.preferences;
    requires dev.ikm.komet.progress;
    requires dev.ikm.komet.search;
    requires dev.ikm.tinkar.common;
    requires dev.ikm.tinkar.entity;
    requires dev.ikm.tinkar.provider.entity;
    requires dev.ikm.tinkar.terms;
    requires org.kordamp.ikonli.javafx;
    requires jdk.jdwp.agent;
    requires transitive dev.ikm.komet.rules;

    uses DataServiceController;
    uses DefaultDescriptionForNidService;
    uses EntityService;
    uses KometNodeFactory;
    uses PublicIdService;
    uses StampService;
    uses TaskListsService;
    uses DefaultEvtBus;

    // For ScenicView...
    //requires org.scenicview.scenicview;
}