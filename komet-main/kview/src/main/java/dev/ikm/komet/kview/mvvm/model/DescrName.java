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
import dev.ikm.tinkar.terms.EntityFacade;

public class DescrName {

    // the public ID of the parent concept of this description semantic
    private PublicId parentConcept;

    private String nameText;

    private Stamp stamp;

    private EntityFacade nameType;

    private EntityFacade caseSignificance;

    private EntityFacade status;

    private EntityFacade module;

    private EntityFacade language;



    // the public ID of the description semantic that this class represents
    private PublicId semanticPublicId;


    public DescrName(PublicId parentConcept, String nameText, EntityFacade nameType, EntityFacade caseSignificance,
                     EntityFacade status, EntityFacade module, EntityFacade language, PublicId semanticPublicId) {
        this.parentConcept = parentConcept;
        this.nameText = nameText;
        this.nameType = nameType;
        this.caseSignificance = caseSignificance;
        this.status = status;
        this.module = module;
        this.language = language;
        this.semanticPublicId = semanticPublicId;
    }

    public DescrName(PublicId parentConcept, String nameText, EntityFacade nameType, EntityFacade caseSignificance,
                     EntityFacade status, EntityFacade module, EntityFacade language, PublicId semanticPublicId, Stamp stamp) {
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

    public EntityFacade getNameType() {
        return nameType;
    }

    public void setNameType(EntityFacade nameType) {
        this.nameType = nameType;
    }

    public EntityFacade getCaseSignificance() {
        return caseSignificance;
    }

    public void setCaseSignificance(EntityFacade caseSignificance) {
        this.caseSignificance = caseSignificance;
    }

    public EntityFacade getStatus() {
        return status;
    }

    public void setStatus(EntityFacade status) {
        this.status = status;
    }

    public EntityFacade getModule() {
        return module;
    }

    public void setModule(EntityFacade module) {
        this.module = module;
    }

    public EntityFacade getLanguage() {
        return language;
    }

    public void setLanguage(EntityFacade language) {
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
