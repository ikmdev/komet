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

public enum RuleServiceFinder {
    INSTANCE;
    RuleService service;

    RuleServiceFinder() {
        System.setProperty("org.evrete.runtime.compiler.Options",
                "--release|19|--enable-preview|--add-modules|dev.ikm.komet.framework,dev.ikm.komet.rules");

        // TODO Developer documentation and cookbook need to describe why this inclusion of jars is done
        // TODO Find alternatives, including pre-compilation of rules.
        if (System.getProperty("jdk.module.path") == null) {
            Path javaHome = FileSystems.getDefault().getPath(System.getProperty("java.home"));
            Path thisModules = javaHome.resolve("module-jars");
            System.setProperty("jdk.module.path", thisModules.toAbsolutePath().toString());
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
