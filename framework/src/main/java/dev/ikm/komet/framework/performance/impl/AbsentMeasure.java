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
package dev.ikm.komet.framework.performance.impl;

import dev.ikm.komet.framework.performance.Measure;
import dev.ikm.tinkar.terms.ConceptFacade;

import java.util.Optional;

public class AbsentMeasure implements Measure {
    @Override
    public float getLowerBound() {
        return 0;
    }

    @Override
    public boolean includeLowerBound() {
        return true;
    }

    @Override
    public float getUpperBound() {
        return 0;
    }

    @Override
    public boolean includeUpperBound() {
        return true;
    }

    @Override
    public Optional<Float> getResolution() {
        return Optional.empty();
    }

    @Override
    public Optional<ConceptFacade> getMeasureSemantic() {
        return Optional.empty();
    }
}
