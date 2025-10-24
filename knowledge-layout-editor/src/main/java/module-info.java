import dev.ikm.komet.kview.klwindows.EntityKlWindowFactory;

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
module dev.ikm.komet.kleditorapp {
    requires dev.ikm.komet.kview;

    // JPro related modules
    requires jpro.webapi;
    requires one.jpro.platform.auth.core;
    requires one.jpro.platform.file;

    requires transitive dev.ikm.komet.layout;
    requires jdk.jfr;
    requires org.eclipse.jgit;

    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.base;

    requires org.slf4j;
    requires dev.ikm.tinkar.common;
    requires dev.ikm.tinkar.entity;
    requires dev.ikm.tinkar.terms;
    requires dev.ikm.tinkar.events;

    exports dev.ikm.komet.kleditorapp.view;
    opens dev.ikm.komet.kleditorapp.view to javafx.fxml, dev.ikm.komet.application;

//    uses EvtBus;
    uses EntityKlWindowFactory;
}
