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

import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.State;

public class ObservableCompoundVersion {
    ObservableStamp stamp;

    public void setField(PatternFacade pattern, ConceptFacade fieldMeaning, Object fieldValue) {
        setField(pattern.nid(), fieldMeaning.nid(), fieldValue);
    }

    public void setField(int patternNid, int fieldMeaningNid, Object fieldValue) {
        throw new UnsupportedOperationException();
    }

    public void setStamp(ObservableStamp stamp) {
        this.stamp = stamp;
    }

    public void setStatus(State state) {
        // Need to handle stamp & transactions properly.
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setAuthorNid(int authorSequence) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setModuleNid(int moduleSequence) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setPathNid(int pathSequence) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setTime(long time) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean committed() {
        return !uncommitted();
    }

    public boolean uncommitted() {
        StampEntity stampEntity = (StampEntity) stamp.entity();
        if (stampEntity.time() == Long.MAX_VALUE) {
            return true;
        }
        if (Transaction.forStamp(stamp).isPresent()) {
            // Participating in an active transaction...
            return true;
        }
        return false;
    }
}
