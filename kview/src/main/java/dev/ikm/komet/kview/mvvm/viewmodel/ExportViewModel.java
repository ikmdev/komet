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
package dev.ikm.komet.kview.mvvm.viewmodel;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.terms.EntityFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static dev.ikm.tinkar.terms.TinkarTerm.PRIMORDIAL_PATH;
import static dev.ikm.tinkar.terms.TinkarTerm.SANDBOX_PATH;

/**
 * The {@code ExportViewModel} class is responsible for handling the export functionality
 * within the application. It extends {@link FormViewModel} to leverage form-based interactions
 * and validation mechanisms.
 */
public class ExportViewModel extends FormViewModel {

    private static final Logger LOG = LoggerFactory.getLogger(ExportViewModel.class);

    /**
     * Constructs a new {@code ExportViewModel} instance.
     * Initializes the ViewModel by invoking the superclass constructor
     * and adding a null view property to the properties list.
     */
    public ExportViewModel() {
        super();
        addProperty(VIEW_PROPERTIES, (ViewProperties) null);
    }

    /**
     * Retrieves the set of paths that are relevant for the export operation.
     * This includes both the primordial path and the sandbox path.
     *
     * @return an unmodifiable {@code Set} containing the {@code EntityFacade}
     *         instances for the primordial and sandbox paths.
     */
    public Set<EntityFacade> getPaths() {
        return Set.of(PRIMORDIAL_PATH, SANDBOX_PATH);
    }
}
