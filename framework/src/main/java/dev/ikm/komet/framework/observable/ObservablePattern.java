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
package dev.ikm.komet.framework.observable;

import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;

public final class ObservablePattern
        extends ObservableEntity<ObservablePatternVersion>
        implements PatternEntity<ObservablePatternVersion> {
    ObservablePattern(PatternEntity<PatternVersionRecord> patternEntity) {
        super(patternEntity);
    }

    @Override
    protected ObservablePatternVersion wrap(EntityVersion version) {
        return new ObservablePatternVersion(this, (PatternVersionRecord) version);
    }

    @Override
    public ObservablePatternSnapshot getSnapshot(ViewCalculator calculator) {
        return new ObservablePatternSnapshot(calculator, this);
    }

    @Override
    protected void addAdditionalChronologyFeatures(MutableList<Feature> features) {
        // Nothing to add.
    }
}
