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
package dev.ikm.tinkar.entity.transaction;

import dev.ikm.tinkar.entity.ConceptVersionRecord;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternVersionRecord;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.entity.StampVersionRecord;
import dev.ikm.tinkar.terms.State;

/**
 * For canceling a single version. If the version is part of a shared transaction, then
 * it will be removed from that transaction, and added to a new single version transaction.
 */
public class CommitVersionTask extends TransactionVersionTask {
    public CommitVersionTask(ConceptVersionRecord version) {
        super(version);
    }

    public CommitVersionTask(PatternVersionRecord version) {
        super(version);
    }

    public CommitVersionTask(SemanticVersionRecord version) {
        super(version);
    }

    public CommitVersionTask(StampVersionRecord version) {
        super(version);
    }

    protected String getTitleString() {
        return "Committing version for: ";
    }

    @Override
    protected void performTransactionAction(Transaction transactionForAction) {
        transactionForAction.commit();
    }

    @Override
    protected State getStateForVersion(EntityVersion version) {
        return version.state();
    }
}
