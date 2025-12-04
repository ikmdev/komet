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

import java.util.ServiceLoader;

/**
 * The PluginServiceLoader interface defines a method for obtaining a ServiceLoader
 * for a specific service class, including all dynamic plugin ModuleLayers.
 */
public interface PluginServiceLoader {

    /**
     * Returns a ServiceLoader for the given pluggable service class.
     * <p>
     * Note that the caller must prepare to abandon all references to the
     * loader and service if the deployment allows for dynamic removal of plugin layers,
     * as would be notified by a PluginLifecycleListener.pluginLayerBeingRemoved
     * event. If the deployment allows for dynamic removal of plugin layers,
     * then the caller must either: 1) register for the events, and act accordingly, or
     * 2) only use the services on a transient and dynamic manner on demand. Otherwise,
     * a memory leak may occur, and if an unloaded plugin layers is reloaded without
     * removing all references to the unloaded layer's classes, undefined behaviour may result.
     *
     * @param service the pluggable service class
     * @param <S>     the type of the service
     * @return a ServiceLoader object for the given service class
     */
    <S> ServiceLoader<S> loader(Class<S> service);

    /**
     * Use the plugin class loader provided by the plugable service loader to
     * @param className
     * @return
     * @throws ClassNotFoundException
     */
    Class<?> forName(String className) throws ClassNotFoundException;
}
