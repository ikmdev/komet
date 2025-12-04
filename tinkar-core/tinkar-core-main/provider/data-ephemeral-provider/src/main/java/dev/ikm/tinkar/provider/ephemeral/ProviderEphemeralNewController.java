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
package dev.ikm.tinkar.provider.ephemeral;

import dev.ikm.tinkar.common.service.DataServiceController;
import dev.ikm.tinkar.common.service.DataUriOption;
import dev.ikm.tinkar.common.service.LoadDataFromFileController;
import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.common.service.PrimitiveDataService;
import dev.ikm.tinkar.entity.EntityCountSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static dev.ikm.tinkar.provider.ephemeral.constants.EphemeralStoreControllerName.NEW_CONTROLLER_NAME;

public class ProviderEphemeralNewController implements DataServiceController<PrimitiveDataService> {

    private static final Logger LOG = LoggerFactory.getLogger(ProviderEphemeralNewController.class);
    private DataUriOption dataUriOption;

    public List<DataUriOption> providerOptions() {
        List<DataUriOption> dataUriOptions = new ArrayList<>();
        File rootFolder = new File(System.getProperty("user.home"), "Solor");
        if (!rootFolder.exists()) {
            rootFolder.mkdirs();
        }
        for (File f : rootFolder.listFiles()) {
            if (isValidDataLocation(f.getName())) {
                dataUriOptions.add(new DataUriOption(f.getName(), f.toURI()));
            }
        }
        return dataUriOptions;
    }

    @Override
    public boolean isValidDataLocation(String name) {
        return name.toLowerCase().endsWith("pb.zip") ||
                (name.toLowerCase().endsWith(".zip") && name.toLowerCase().contains("tink"));
    }

    @Override
    public void setDataUriOption(DataUriOption dataUriOption) {
        this.dataUriOption = dataUriOption;
    }

    @Override
    public String controllerName() {
        return NEW_CONTROLLER_NAME;
    }

    @Override
    public Class<? extends PrimitiveDataService> serviceClass() {
        return PrimitiveDataService.class;
    }

    public boolean running() {
        return ProviderEphemeral.singleton != null;
    }

    @Override
    public void start() {
        try {
            ProviderEphemeral.provider();
            if (this.dataUriOption != null) {
                File file = new File(this.dataUriOption.uri());
                ServiceLoader<LoadDataFromFileController> controllerFinder = PluggableService.load(LoadDataFromFileController.class);
                LoadDataFromFileController loader = controllerFinder.findFirst().get();
                Future<EntityCountSummary> loadFuture = (Future<EntityCountSummary>) loader.load(file);
                EntityCountSummary entityCountSummary = loadFuture.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        ProviderEphemeral.provider().close();
    }

    @Override
    public void save() {
        // nothing to save.
    }

    @Override
    public void reload() {
        throw new UnsupportedOperationException("Can't reload yet.");
    }

    @Override
    public PrimitiveDataService provider() {
        return ProviderEphemeral.provider();
    }

    @Override
    public String toString() {
        return NEW_CONTROLLER_NAME;
    }
}
