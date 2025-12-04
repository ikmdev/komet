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

import dev.ikm.tinkar.common.service.DataServiceController;
import dev.ikm.tinkar.common.service.PrimitiveDataService;

public abstract class MvStoreController implements DataServiceController<PrimitiveDataService> {
    @Override
    public boolean isValidDataLocation(String name) {
        return name.equals("mvstore.dat");
    }

    @Override
    public Class<? extends PrimitiveDataService> serviceClass() {
        return PrimitiveDataService.class;
    }

    @Override
    public void stop() {
        MVStoreProvider.singleton.close();
        MVStoreProvider.singleton = null;
    }

    @Override
    public void save() {
        MVStoreProvider.singleton.save();
    }

    @Override
    public boolean running() {
        if (MVStoreProvider.singleton != null) {
            return true;
        }
        return false;
    }

    @Override
    public void reload() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PrimitiveDataService provider() {
        if (MVStoreProvider.singleton == null) {
            start();
        }
        return MVStoreProvider.singleton;
    }

    @Override
    public String toString() {
        return controllerName();
    }
}
