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
package dev.ikm.komet.kview.mvvm.model;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.component.Stamp;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityProxy;

public class DescrName {

    // the public ID of the parent concept of this description semantic
    private PublicId parentConcept;

    private String nameText;

    private Stamp stamp;

    private ConceptFacade nameType;

    private ConceptEntity caseSignificance;

    private ConceptEntity status;

    private ConceptEntity module;

    private ConceptEntity language;



    // the public ID of the description semantic that this class represents
    private PublicId semanticPublicId;


    public DescrName(PublicId parentConcept, String nameText, ConceptFacade nameType, ConceptEntity caseSignificance,
                     ConceptEntity status, ConceptEntity module, ConceptEntity language, PublicId semanticPublicId) {
        this.parentConcept = parentConcept;
        this.nameText = nameText;
        this.nameType = nameType;
        this.caseSignificance = caseSignificance;
        this.status = status;
        this.module = module;
        this.language = language;
        this.semanticPublicId = semanticPublicId;
    }

    public DescrName(PublicId parentConcept, String nameText, ConceptFacade nameType, ConceptEntity caseSignificance,
                     ConceptEntity status, ConceptEntity module, ConceptEntity language, PublicId semanticPublicId, Stamp stamp) {
        this.parentConcept = parentConcept;
        this.nameText = nameText;
        this.nameType = nameType;
        this.caseSignificance = caseSignificance;
        this.status = status;
        this.module = module;
        this.language = language;
        this.semanticPublicId = semanticPublicId;
        this.stamp = stamp;
    }

    public PublicId getParentConcept() {
        return parentConcept;
    }

    public void setParentConcept(PublicId parentConcept) {
        this.parentConcept = parentConcept;
    }

    public String getNameText() {
        return nameText;
    }

    public void setNameText(String nameText) {
        this.nameText = nameText;
    }

    public ConceptFacade getNameType() {
        return nameType;
    }

    public void setNameType(ConceptFacade nameType) {
        this.nameType = nameType;
    }

    public ConceptEntity getCaseSignificance() {
        return caseSignificance;
    }

    public void setCaseSignificance(ConceptEntity caseSignificance) {
        this.caseSignificance = caseSignificance;
    }

    public ConceptEntity getStatus() {
        return status;
    }

    public void setStatus(ConceptEntity status) {
        this.status = status;
    }

    public ConceptEntity getModule() {
        return module;
    }

    public void setModule(ConceptEntity module) {
        this.module = module;
    }

    public ConceptEntity getLanguage() {
        return language;
    }

    public void setLanguage(ConceptEntity language) {
        this.language = language;
    }

    public PublicId getSemanticPublicId() {
        return semanticPublicId;
    }

    public void setSemanticPublicId(PublicId semanticPublicId) {
        this.semanticPublicId = semanticPublicId;
    }

    public Stamp getStamp() {
        return stamp;
    }
}
