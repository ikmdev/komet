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

import dev.ikm.komet.amplify.details.DetailsNodeFactory;
import dev.ikm.komet.amplify.properties.PropertiesNodeFactory;
import dev.ikm.komet.framework.KometNodeFactory;

module dev.ikm.komet.amplify {
    requires transitive dev.ikm.komet.framework;
    requires dev.ikm.komet.search;
    requires dev.ikm.komet.navigator;
    requires dev.ikm.komet.classification;

    opens dev.ikm.komet.amplify.details to javafx.fxml;
    exports dev.ikm.komet.amplify.details;

    opens dev.ikm.komet.amplify.properties to javafx.fxml;
    exports dev.ikm.komet.amplify.properties;

    opens dev.ikm.komet.amplify.timeline to javafx.fxml;
    exports dev.ikm.komet.amplify.timeline;

    opens dev.ikm.komet.amplify.journal to javafx.fxml;
    exports dev.ikm.komet.amplify.journal;

    opens dev.ikm.komet.amplify.landingpage to javafx.fxml;
    exports dev.ikm.komet.amplify.landingpage;

    opens dev.ikm.komet.amplify.window to javafx.fxml;
    exports dev.ikm.komet.amplify.window;

    exports dev.ikm.komet.amplify.om;
    exports dev.ikm.komet.amplify.commons;

    opens dev.ikm.komet.amplify.om to javafx.fxml;
    exports dev.ikm.komet.amplify.events;

    opens dev.ikm.komet.amplify.mvvm to javafx.fxml;
    opens dev.ikm.komet.amplify.mvvm.validator to javafx.fxml;
    exports dev.ikm.komet.amplify.mvvm;
    exports dev.ikm.komet.amplify.mvvm.validator;
    exports dev.ikm.komet.amplify.viewmodels;
    opens dev.ikm.komet.amplify.viewmodels to javafx.fxml;
    exports dev.ikm.komet.amplify.mvvm.loader;
    opens dev.ikm.komet.amplify.mvvm.loader to javafx.fxml;
    exports dev.ikm.komet.amplify.stamp;
    opens dev.ikm.komet.amplify.stamp to javafx.fxml;


    opens dev.ikm.komet.amplify.lidr.viewmodels to javafx.fxml;
    exports dev.ikm.komet.amplify.lidr.viewmodels;

    opens dev.ikm.komet.amplify.lidr.details to javafx.fxml;
    exports dev.ikm.komet.amplify.lidr.details;
    opens dev.ikm.komet.amplify.lidr.properties to javafx.fxml;
    exports dev.ikm.komet.amplify.lidr.properties;
    opens dev.ikm.komet.amplify.lidr.device to javafx.fxml;
    exports dev.ikm.komet.amplify.lidr.device;
    opens dev.ikm.komet.amplify.lidr.analyte to javafx.fxml;
    exports dev.ikm.komet.amplify.lidr.analyte;
    opens dev.ikm.komet.amplify.lidr.results to javafx.fxml;
    exports dev.ikm.komet.amplify.lidr.results;

    provides KometNodeFactory with DetailsNodeFactory, PropertiesNodeFactory;

    uses dev.ikm.komet.framework.events.EvtBus;
}
