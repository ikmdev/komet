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
package dev.ikm.tinkar.provider.spinedarray;

import dev.ikm.tinkar.common.service.DataServiceProperty;
import dev.ikm.tinkar.common.service.DataUriOption;
import dev.ikm.tinkar.common.service.LoadDataFromFileController;
import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.common.validation.ValidationRecord;
import dev.ikm.tinkar.common.validation.ValidationSeverity;
import dev.ikm.tinkar.entity.EntityCountSummary;
import dev.ikm.tinkar.provider.spinedarray.SpinedArrayProvider.Lifecycle;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static dev.ikm.tinkar.provider.spinedarray.constants.SpinedArrayControllerNames.NEW_CONTROLLER_NAME;

public class SpinedArrayNewController extends SpinedArrayController {
    private static final Logger LOG = LoggerFactory.getLogger(SpinedArrayNewController.class);
    String importDataFileString;
    DataServiceProperty newFolderProperty = new DataServiceProperty("New folder name", false, true);
    MutableMap<DataServiceProperty, String> providerProperties = Maps.mutable.empty();

    {
        providerProperties.put(newFolderProperty, null);
    }

    @Override
    public ImmutableMap<DataServiceProperty, String> providerProperties() {
        return providerProperties.toImmutable();
    }

    @Override
    public void setDataServiceProperty(DataServiceProperty key, String value) {
        providerProperties.put(key, value);
    }

    @Override
    public ValidationRecord[] validate(DataServiceProperty dataServiceProperty, Object value, Object target) {
        if (newFolderProperty.equals(dataServiceProperty)) {
            File rootFolder = new File(System.getProperty("user.home"), "Solor");
            if (value instanceof String fileName) {
                if (fileName.isBlank()) {
                    return new ValidationRecord[]{new ValidationRecord(ValidationSeverity.ERROR,
                            "Directory name cannot be blank", target)};
                } else {
                    File possibleFile = new File(rootFolder, fileName);
                    if (possibleFile.exists()) {
                        return new ValidationRecord[]{new ValidationRecord(ValidationSeverity.ERROR,
                                "Directory already exists", target)};
                    }
                }
            }
        }
        return new ValidationRecord[]{};
    }

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
    public void setDataUriOption(DataUriOption option) {
        try {
            importDataFileString = option.uri().toURL().getFile();
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String controllerName() {
        return NEW_CONTROLLER_NAME;
    }

    @Override
    public void start() {
        if (SpinedArrayProvider.lifecycle.compareAndSet(Lifecycle.UNINITIALIZED, Lifecycle.STARTING)) {
         try {
                File rootFolder = new File(System.getProperty("user.home"), "Solor");
                File dataDirectory = new File(rootFolder, providerProperties.get(newFolderProperty));
                ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT, dataDirectory);
                SpinedArrayProvider.get();

                ServiceLoader<LoadDataFromFileController> controllerFinder = PluggableService.load(LoadDataFromFileController.class);
                LoadDataFromFileController loader = controllerFinder.findFirst().get();
                Future<EntityCountSummary> loadFuture = (Future<EntityCountSummary>) loader.load(new File(importDataFileString));
                EntityCountSummary count = loadFuture.get();
                LOG.info("Spined array loaded: " + count.toString() + "");
                SpinedArrayProvider.get().save();
                SpinedArrayProvider.lifecycle.set(Lifecycle.RUNNING);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean loading() {
        return SpinedArrayProvider.lifecycle.get() == Lifecycle.STARTING;
    }
}
