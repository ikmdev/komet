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
import dev.ikm.komet.framework.dnd.DragRegistry;
import dev.ikm.komet.framework.events.DefaultEvtBus;
import dev.ikm.komet.framework.rulebase.RuleService;
import dev.ikm.komet.preferences.PreferencesService;
import dev.ikm.tinkar.common.service.CachingService;

open module dev.ikm.komet.framework {
    exports dev.ikm.komet.framework.activity;
    exports dev.ikm.komet.framework.alerts;
    exports dev.ikm.komet.framework.annotations;
    exports dev.ikm.komet.framework.builder;
    exports dev.ikm.komet.framework.concurrent;
    exports dev.ikm.komet.framework.context;
    exports dev.ikm.komet.framework.controls;
    exports dev.ikm.komet.framework.dnd;
    exports dev.ikm.komet.framework.docbook;
    exports dev.ikm.komet.framework.graphics;
    exports dev.ikm.komet.framework.observable;
    exports dev.ikm.komet.framework.panel;
    exports dev.ikm.komet.framework.performance.impl;
    exports dev.ikm.komet.framework.performance;
    exports dev.ikm.komet.framework.preferences;
    exports dev.ikm.komet.framework.progress;
    exports dev.ikm.komet.framework.propsheet.editor to org.controlsfx.controls, dev.ikm.komet.list, dev.ikm.komet.application;
    exports dev.ikm.komet.framework.propsheet;
    exports dev.ikm.komet.framework.rulebase;
    exports dev.ikm.komet.framework.search;
    exports dev.ikm.komet.framework.temp;
    exports dev.ikm.komet.framework.uncertain;
    exports dev.ikm.komet.framework.view;
    exports dev.ikm.komet.framework;
    exports dev.ikm.komet.framework.window;
    exports dev.ikm.komet.framework.tabs;
    exports dev.ikm.komet.framework.panel.axiom;
    exports dev.ikm.komet.framework.events;
    exports dev.ikm.komet.framework.events.appevents;

    provides CachingService with DragRegistry.CacheProvider;
    requires io.github.classgraph;
    requires dev.ikm.tinkar.collection;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.ikonli.fontawesome;
    requires org.kordamp.ikonli.foundation;
    requires org.kordamp.ikonli.icomoon;
    requires org.kordamp.ikonli.ionicons4;
    requires org.kordamp.ikonli.mapicons;
    requires org.kordamp.ikonli.material2;
    requires org.kordamp.ikonli.materialdesign2;
    requires org.kordamp.ikonli.materialdesign;
    requires org.kordamp.ikonli.octicons;
    requires org.kordamp.ikonli.runestroicons;
    requires org.kordamp.ikonli.unicons;
    requires org.carlfx.cognitive;
    requires static java.compiler;
    requires static io.soabase.recordbuilder.core;
    requires transitive javafx.base;
    requires transitive javafx.graphics;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.swing;
    requires transitive org.controlsfx.controls;
    requires transitive dev.ikm.komet.preferences;
    requires transitive dev.ikm.komet.terms;
    requires transitive dev.ikm.tinkar.common;
    requires transitive dev.ikm.tinkar.coordinate;
    requires transitive org.eclipse.collections;
    requires transitive org.eclipse.collections.api;
    requires transitive dev.ikm.tinkar.entity;
    requires transitive dev.ikm.tinkar.fhir.transformers;
    requires transitive dev.ikm.tinkar.terms;
    requires transitive org.kordamp.ikonli.javafx;
    requires transitive org.slf4j;
    requires transitive dev.ikm.tinkar.ext.lang.owl; // Owl expression builder
    uses TaskListsService;
    uses PreferencesService;
    uses KometNodeFactory;
    uses dev.ikm.tinkar.common.alert.AlertReportingService;
    uses RuleService;


    provides dev.ikm.komet.framework.events.EvtBus
            with DefaultEvtBus;

    uses dev.ikm.komet.framework.events.EvtBus;
}
