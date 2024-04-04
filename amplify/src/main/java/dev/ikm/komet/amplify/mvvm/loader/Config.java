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
package dev.ikm.komet.amplify.mvvm.loader;

import java.net.URL;

/**
 * A Config represents an FXML URL, controller class, controller object, zero to many NamedVm objects.
 * This convenience object help define configurations before calling the make() methond on the FXMLMvvmLoader.
 */
public class Config {
    private URL fxml;
    private Class controllerClass;
    private Object controller;

    private NamedVm[] namedViewModels;

    public URL fxml() {
        return fxml;
    }

    public Config() {

    }
    public Config(URL fxml, NamedVm ...namedViewModels) {
        this.fxml = fxml;
        if (namedViewModels != null && namedViewModels.length > 0) {
            for(NamedVm namedVm:namedViewModels) {
                addNamedViewModel(namedVm);
            }
        }
    }
    public Config(URL fxml, Class controllerClass) {
        this.fxml = fxml;
        this.controllerClass = controllerClass;
    }
    public Config(URL fxml, Object controllerClass) {
        this.fxml = fxml;
        this.controller = controllerClass;
    }

    public Config fxml(URL fxml) {
        this.fxml = fxml;
        return this;
    }

    public Class controllerClass() {
        return controllerClass;
    }

    public Config controllerClass(Class controllerClass) {
        this.controllerClass = controllerClass;
        return this;
    }

    public Object controller() {
        return controller;
    }

    public Config controller(Object controllerObject) {
        this.controller = controllerObject;
        return this;
    }

    public NamedVm[] namedViewModels() {
        if (namedViewModels == null) {
            namedViewModels = new NamedVm[0];
        }
        return namedViewModels;
    }

    public Config namedViewModels(NamedVm ...namedViewModels) {
        this.namedViewModels = namedViewModels;
        return this;
    }
    public Config addNamedViewModel(NamedVm namedVm) {
        int len = 0;
        if (this.namedViewModels != null && this.namedViewModels.length > 0) {
            len = this.namedViewModels.length + 1;
            NamedVm[] viewModels2 = new NamedVm[len];
            System.arraycopy(namedViewModels, 0, this.namedViewModels, 0, len);
            viewModels2[len -1] = namedVm;
            this.namedViewModels = viewModels2;
        } else {
            this.namedViewModels = new NamedVm[1];
            this.namedViewModels[0] = namedVm;
        }
        return this;
    }
}
