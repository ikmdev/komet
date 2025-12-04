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
package dev.ikm.tinkar.provider.mvstore;

import dev.ikm.tinkar.common.service.DataUriOption;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;

import java.io.IOException;

import static dev.ikm.tinkar.provider.mvstore.constants.MvStoreControllerNames.OPEN_CONTROLLER_NAME;

public class MvStoreOpenController extends MvStoreController {

    @Override
    public void setDataUriOption(DataUriOption option) {
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT, option.toFile());
    }

    @Override
    public String controllerName() {
        return OPEN_CONTROLLER_NAME;
    }

    @Override
    public void start() {
        if (MVStoreProvider.singleton == null) {
            try {
                new MVStoreProvider();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
