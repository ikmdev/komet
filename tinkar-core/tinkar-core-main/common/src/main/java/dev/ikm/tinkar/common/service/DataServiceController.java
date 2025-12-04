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
package dev.ikm.tinkar.common.service;


import dev.ikm.tinkar.common.validation.ValidationRecord;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.ImmutableMap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface DataServiceController<T> {

    /** Properties that are necessary to configure the service. */
    default ImmutableMap<DataServiceProperty, String> providerProperties() {
        return Maps.immutable.empty();
    }

    default Optional<String> getDataServiceProperty(DataServiceProperty propertyKey) {
        return Optional.ofNullable(providerProperties().get(propertyKey));
    }

    default void setDataServiceProperty(DataServiceProperty key, String value) {
        throw new UnsupportedOperationException();
    }

    default ValidationRecord[] validate(DataServiceProperty dataServiceProperty, Object value, Object target) {
        return new ValidationRecord[] {};
    }

    default List<DataUriOption> providerOptions() {
        List<DataUriOption> dataUriOptions = new ArrayList<>();
        File rootFolder = new File(System.getProperty("user.home"), "Solor");
        if (!rootFolder.exists()) {
            rootFolder.mkdirs();
        }
        for (File f : rootFolder.listFiles()) {
            if (f.isDirectory()) {
                String[] children = f.list((dir, name) -> isValidDataLocation(name));
                if (children.length != 0) {
                    dataUriOptions.add(new DataUriOption(f.getName(), f.toURI()));
                }
            }
        }
        return dataUriOptions;
    }

    boolean isValidDataLocation(String name);

    void setDataUriOption(DataUriOption option);
    String controllerName();
    Class<? extends T> serviceClass();
    boolean running();
    void start();
    void stop();
    void save();
    void reload();
    T provider();

    /**
     *
     * @return true if loading new database from file.
     */
    default boolean loading() {
        return false;
    }

}
