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

import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticVersionRecord;

public class ObservableSemantic
        extends ObservableEntity<ObservableSemanticVersion, SemanticVersionRecord>
        implements SemanticEntity<ObservableSemanticVersion> {
    ObservableSemantic(SemanticEntity<SemanticVersionRecord> semanticEntity) {
        super(semanticEntity);
    }

    @Override
    protected ObservableSemanticVersion wrap(SemanticVersionRecord version) {
        return new ObservableSemanticVersion(version);
    }

    @Override
    public ObservableSemanticSnapshot getSnapshot(ViewCalculator calculator) {
        return new ObservableSemanticSnapshot(calculator, this);
    }

    @Override
    public int referencedComponentNid() {
        return ((SemanticEntity) entity()).referencedComponentNid();
    }

    @Override
    public int patternNid() {
        return ((SemanticEntity) entity()).patternNid();
    }

}
