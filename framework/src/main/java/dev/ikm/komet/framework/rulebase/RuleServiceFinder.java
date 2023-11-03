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
package dev.ikm.komet.framework.rulebase;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

public enum RuleServiceFinder {
    INSTANCE;
    RuleService service;

    RuleServiceFinder() {
        System.setProperty("org.evrete.runtime.compiler.Options",
                "--release|21|--add-modules|dev.ikm.komet.framework,dev.ikm.komet.rules");

        if (System.getProperty("jdk.module.path") == null) {

            Path javaHome = FileSystems.getDefault().getPath(System.getProperty("java.home"));
            Path thisModules = javaHome.resolve("lib").resolve("modules");

            System.setProperty("jdk.module.path",
                    thisModules.toAbsolutePath().toString());
            System.setProperty("jlink.binary", Boolean.TRUE.toString());
        }

        ServiceLoader<RuleService> serviceLoader = ServiceLoader.load(RuleService.class);
        Optional<RuleService> optionalService = serviceLoader.findFirst();
        if (optionalService.isPresent()) {
            this.service = optionalService.get();
        } else {
            throw new NoSuchElementException("No " + RuleService.class.getName() +
                    " found by ServiceLoader...");
        }

    }

    public RuleService get() {
        return service;
    }
}
