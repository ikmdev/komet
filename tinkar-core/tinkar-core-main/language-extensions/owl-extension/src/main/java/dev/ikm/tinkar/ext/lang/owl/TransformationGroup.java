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
package dev.ikm.tinkar.ext.lang.owl;

import dev.ikm.tinkar.coordinate.logic.PremiseType;

import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author kec
 */
public class TransformationGroup {
    final int conceptNid;
    final int[] semanticNids;
    final PremiseType premiseType;

    public TransformationGroup(int conceptNid, int[] semanticNids, PremiseType premiseType) {
        this.conceptNid = conceptNid;
        this.semanticNids = semanticNids;
        this.premiseType = premiseType;
    }

    public int getConceptNid() {
        return conceptNid;
    }

    public int[] getSemanticNids() {
        return semanticNids;
    }

    public PremiseType getPremiseType() {
        return premiseType;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("TransformationGroup{");
        builder.append(conceptNid);
        builder.append(", ");
        builder.append(Arrays.toString(semanticNids));
        builder.append(" ");
        builder.append(premiseType);
        builder.append('}');
        return builder.toString();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + this.conceptNid;
        hash = 23 * hash + Objects.hashCode(this.premiseType);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TransformationGroup other = (TransformationGroup) obj;
        if (this.conceptNid != other.conceptNid) {
            return false;
        }
        if (!Arrays.equals(this.semanticNids, other.semanticNids)) {
            return false;
        }
        return this.premiseType.nid() == other.premiseType.nid();
    }

}