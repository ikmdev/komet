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

import dev.ikm.tinkar.common.service.TrackingCallable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CancelTransactionTask extends TrackingCallable<Void> {
    private static final Logger LOG = LoggerFactory.getLogger(CancelTransactionTask.class);
    final Transaction transaction;

    public CancelTransactionTask(Transaction transaction) {
        updateTitle(getTitleString() + transaction.transactionUuid());
        this.transaction = transaction;
        addToTotalWork(transaction.stampsInTransactionCount());
    }

    protected String getTitleString() {
        return "Canceling transaction: ";
    }

    @Override
    public Void compute() throws Exception {
        try {
            int count = transaction.cancel();
            for (int i = 0; i < count; i++) {
                completedUnitOfWork();
            }
            return null;
        } catch (Throwable t) {
            LOG.error(t.getLocalizedMessage(), t);
            throw t;
        }
    }

    protected long getTime() {
        return Long.MIN_VALUE;
    }
}
