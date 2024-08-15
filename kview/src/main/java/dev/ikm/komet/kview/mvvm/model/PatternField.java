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

import dev.ikm.tinkar.terms.EntityFacade;

public class PatternField {

    private int fieldOrder;
    private String displayName;

    private EntityFacade dataType;

    private EntityFacade purpose;

    private EntityFacade meaning;

    private String comments;

    public PatternField(int fieldOrder, String displayName, EntityFacade dataType,
                        EntityFacade purpose, EntityFacade meaning, String comments) {
        this.fieldOrder = fieldOrder;
        this.displayName = displayName;
        this.dataType = dataType;
        this.purpose = purpose;
        this.meaning = meaning;
        this.comments = comments;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public int getFieldOrder() {
        return fieldOrder;
    }

    public void setFieldOrder(int fieldOrder) {
        this.fieldOrder = fieldOrder;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public EntityFacade getDataType() {
        return dataType;
    }

    public void setDataType(EntityFacade dataType) {
        this.dataType = dataType;
    }

    public EntityFacade getPurpose() {
        return purpose;
    }

    public void setPurpose(EntityFacade purpose) {
        this.purpose = purpose;
    }

    public EntityFacade getMeaning() {
        return meaning;
    }

    public void setMeaning(EntityFacade meaning) {
        this.meaning = meaning;
    }
}
