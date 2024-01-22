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
package dev.ikm.komet.amplify.details;

import dev.ikm.komet.preferences.NidTextEnum;
import javafx.scene.layout.Pane;

public class ConceptPreference {
    private Pane conceptPane;

    NidTextEnum nidType;
    Integer nid;

    public Pane getConceptPane() {
        return conceptPane;
    }

    public void setConceptPane(Pane conceptPane) {
        this.conceptPane = conceptPane;
    }

    public ConceptPreference(NidTextEnum nidType, Integer nid, Pane conceptPane) {
        this.nidType = nidType;
        this.nid = nid;
        this.conceptPane = conceptPane;
    }

    public Integer getNid() {
        return nid;
    }

    public void setNid(Integer nid) {
        this.nid = nid;
    }

    public NidTextEnum getNidType() {
        return nidType;
    }

    public void setNidType(NidTextEnum nidType) {
        this.nidType = nidType;
    }
}

