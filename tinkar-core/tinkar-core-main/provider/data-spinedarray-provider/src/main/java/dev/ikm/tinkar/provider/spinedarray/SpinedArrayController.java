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

import dev.ikm.tinkar.common.service.DataServiceController;
import dev.ikm.tinkar.common.service.PrimitiveDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public abstract class SpinedArrayController implements DataServiceController<PrimitiveDataService> {
    private static final Logger LOG = LoggerFactory.getLogger(SpinedArrayController.class);
    private static final long STARTUP_TIMEOUT_SECONDS = 30;

    @Override
    public Class<? extends PrimitiveDataService> serviceClass() {
        return PrimitiveDataService.class;
    }

    @Override
    public boolean running() {
        return SpinedArrayProvider.lifecycle.get() == SpinedArrayProvider.Lifecycle.RUNNING;
    }


    @Override
    public void start() {
        LOG.info("SpinedArrayController.start() called on thread: {}", Thread.currentThread().getName());

        // Simply call get() - it handles all synchronization via StableValue.orElseSet()
        // Multiple threads can safely call this; orElseSet() ensures only one initialization
        try {
            SpinedArrayProvider provider = SpinedArrayProvider.get();
            LOG.info("SpinedArrayProvider.get() returned successfully, lifecycle: {}",
                    SpinedArrayProvider.lifecycle.get());

            // Double-check that initialization completed properly
            if (SpinedArrayProvider.lifecycle.get() != SpinedArrayProvider.Lifecycle.RUNNING) {
                throw new IllegalStateException(
                    "SpinedArrayProvider initialized but not in RUNNING state: " +
                    SpinedArrayProvider.lifecycle.get()
                );
            }
        } catch (Exception e) {
            LOG.error("Failed to start SpinedArrayProvider", e);
            throw new RuntimeException("Failed to start SpinedArrayProvider", e);
        }
    }

    @Override
    public void stop() {
        SpinedArrayProvider.get().close();
    }

    @Override
    public void save() {
        SpinedArrayProvider.get().save();
    }

    @Override
    public void reload() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PrimitiveDataService provider() {
        if (SpinedArrayProvider.lifecycle.get() == SpinedArrayProvider.Lifecycle.UNINITIALIZED) {
            start();
        }
        return SpinedArrayProvider.get();
    }

    @Override
    public String toString() {
        return controllerName();
    }
}
