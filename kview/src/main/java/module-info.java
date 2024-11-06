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

import dev.ikm.komet.kview.mvvm.view.details.DetailsNodeFactory;
import dev.ikm.komet.kview.mvvm.view.properties.PropertiesNodeFactory;
import dev.ikm.komet.framework.KometNodeFactory;

module dev.ikm.komet.kview {
    requires transitive dev.ikm.komet.framework;
    requires dev.ikm.komet.search;
    requires dev.ikm.tinkar.provider.search;
    requires dev.ikm.komet.navigator;
    requires dev.ikm.komet.classification;
    requires dev.ikm.komet.progress;
    requires org.carlfx.cognitive;
    requires org.carlfx.axonic;
    requires dev.ikm.tinkar.composer;
    requires one.jpro.platform.auth.core;

    exports dev.ikm.komet.kview.state;
    exports dev.ikm.komet.kview.state.pattern;

    opens dev.ikm.komet.kview.mvvm.view.details to javafx.fxml, org.carlfx.cognitive;
    exports dev.ikm.komet.kview.mvvm.view.details;

    opens dev.ikm.komet.kview.mvvm.view.properties to javafx.fxml, org.carlfx.cognitive;
    exports dev.ikm.komet.kview.mvvm.view.properties;

    opens dev.ikm.komet.kview.mvvm.view.timeline to javafx.fxml, org.carlfx.cognitive;
    exports dev.ikm.komet.kview.mvvm.view.timeline;

    opens dev.ikm.komet.kview.mvvm.view.journal to javafx.fxml, org.carlfx.cognitive;
    exports dev.ikm.komet.kview.mvvm.view.journal;

    opens dev.ikm.komet.kview.mvvm.view.landingpage to javafx.fxml, org.carlfx.cognitive;
    exports dev.ikm.komet.kview.mvvm.view.landingpage;

    opens dev.ikm.komet.kview.mvvm.view.search to javafx.fxml, org.carlfx.cognitive;
    exports dev.ikm.komet.kview.mvvm.view.search;

    opens dev.ikm.komet.kview.mvvm.view.reasoner to javafx.fxml, org.carlfx.cognitive;
    exports dev.ikm.komet.kview.mvvm.view.reasoner;

    opens dev.ikm.komet.kview.fxutils.window to javafx.fxml, org.carlfx.cognitive;
    exports dev.ikm.komet.kview.fxutils.window;

    opens dev.ikm.komet.kview.mvvm.view.common to javafx.fxml, org.carlfx.cognitive;
    exports dev.ikm.komet.kview.mvvm.view.common;

    exports dev.ikm.komet.kview.mvvm.model;
    exports dev.ikm.komet.kview.fxutils;

    opens dev.ikm.komet.kview.mvvm.model to javafx.fxml, org.carlfx.cognitive;
    exports dev.ikm.komet.kview.events;

    exports dev.ikm.komet.kview.mvvm.view.stamp;
    opens dev.ikm.komet.kview.mvvm.view.stamp to javafx.fxml, org.carlfx.cognitive;

    opens dev.ikm.komet.kview.mvvm.viewmodel to javafx.fxml, org.carlfx.cognitive;
    exports dev.ikm.komet.kview.mvvm.viewmodel;

    opens dev.ikm.komet.kview.lidr.mvvm.viewmodel to javafx.fxml, org.carlfx.cognitive;
    exports dev.ikm.komet.kview.lidr.mvvm.viewmodel;

    opens dev.ikm.komet.kview.lidr.mvvm.model;
    exports dev.ikm.komet.kview.lidr.mvvm.model;
    opens dev.ikm.komet.kview.data.schema;
    exports dev.ikm.komet.kview.data.schema;
    opens dev.ikm.komet.kview.data.persistence;
    exports dev.ikm.komet.kview.data.persistence;

    opens dev.ikm.komet.kview.lidr.mvvm.view.details to javafx.fxml, org.carlfx.cognitive;
    exports dev.ikm.komet.kview.lidr.mvvm.view.details;
    opens dev.ikm.komet.kview.lidr.mvvm.view.properties to javafx.fxml, org.carlfx.cognitive;
    exports dev.ikm.komet.kview.lidr.mvvm.view.properties;
    opens dev.ikm.komet.kview.lidr.mvvm.view.device to javafx.fxml, org.carlfx.cognitive;
    exports dev.ikm.komet.kview.lidr.mvvm.view.device;
    opens dev.ikm.komet.kview.lidr.mvvm.view.analyte to javafx.fxml, org.carlfx.cognitive;
    exports dev.ikm.komet.kview.lidr.mvvm.view.analyte;
    opens dev.ikm.komet.kview.lidr.mvvm.view.results to javafx.fxml, org.carlfx.cognitive;
    exports dev.ikm.komet.kview.lidr.mvvm.view.results;
    opens dev.ikm.komet.kview.mvvm.view.progress to javafx.fxml, org.carlfx.cognitive;
    exports dev.ikm.komet.kview.mvvm.view.progress;


    // TODO a temporary export screen for next gen ui.
    opens dev.ikm.komet.kview.mvvm.view.export;
    exports dev.ikm.komet.kview.mvvm.view.export;
    exports dev.ikm.komet.kview.mvvm.view;

    opens dev.ikm.komet.kview.mvvm.view.login;
    exports dev.ikm.komet.kview.mvvm.view.login;

    exports dev.ikm.komet.kview.mvvm.view.descriptionname;
    opens dev.ikm.komet.kview.mvvm.view.descriptionname to javafx.fxml, org.carlfx.cognitive;

    exports dev.ikm.komet.kview.mvvm.view.pattern;
    opens dev.ikm.komet.kview.mvvm.view.pattern to javafx.fxml, org.carlfx.cognitive;
    exports dev.ikm.komet.kview.events.pattern;

    exports dev.ikm.komet.kview.mvvm.view.navigation;
    opens dev.ikm.komet.kview.mvvm.view.navigation to javafx.fxml, org.carlfx.cognitive;

    provides KometNodeFactory with DetailsNodeFactory, PropertiesNodeFactory;

    uses dev.ikm.komet.framework.events.EvtBus;
}
