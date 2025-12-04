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
package dev.ikm.komet.details.concept;

import javafx.scene.Node;
import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import dev.ikm.komet.framework.observable.ObservableEntitySnapshot;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.terms.ConceptFacade;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ComponentPaneModel extends BadgedVersionPaneModel {
    public ComponentPaneModel(ViewProperties viewProperties,
                              ObservableEntitySnapshot observableEntitySnapshot,
                              List<ConceptFacade> semanticOrderForChronology,
                              MutableIntIntMap stampOrderHashMap,
                              HashMap<String, AtomicBoolean> disclosureStateMap) {
        super();
        throw new UnsupportedOperationException();
    }

    @Override
    public Node getBadgedPane() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void doExpandAllAction(ExpandAction newValue) {
        throw new UnsupportedOperationException();
    }
}
