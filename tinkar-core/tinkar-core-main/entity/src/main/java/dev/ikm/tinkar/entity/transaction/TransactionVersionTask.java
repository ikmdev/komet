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

import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.entity.ConceptVersionRecord;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternRecord;
import dev.ikm.tinkar.entity.PatternVersionRecord;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampRecord;
import dev.ikm.tinkar.entity.StampVersionRecord;
import dev.ikm.tinkar.terms.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parent class of CancelVersionTask and CommitVersionTask. Used for operating on a single version,
 * not for a batch of edits.
 */
public abstract class TransactionVersionTask extends TrackingCallable<Void> {
    protected static final Logger LOG = LoggerFactory.getLogger(TransactionVersionTask.class);
    final EntityVersion version;

    public TransactionVersionTask(ConceptVersionRecord version) {
        this((EntityVersion) version);
    }

    private TransactionVersionTask(EntityVersion version) {
        this.version = version;
        updateTitle(getTitleString() + version.getClass().getSimpleName());
        addToTotalWork(1);
    }

    protected abstract String getTitleString();

    public TransactionVersionTask(PatternVersionRecord version) {
        this((EntityVersion) version);
    }

    public TransactionVersionTask(SemanticVersionRecord version) {
        this((EntityVersion) version);
    }

    public TransactionVersionTask(StampVersionRecord version) {
        this((EntityVersion) version);
    }

    @Override
    public final Void compute() throws Exception {
        try {
            Transaction.forVersion(version).ifPresentOrElse(transaction -> {
                if (transaction.componentsInTransaction.size() == 1) {
                    performTransactionAction(transaction);
                } else {
                    // remove from transaction, add to new transaction.
                    Transaction transactionForVersion = Transaction.make();
                    StampEntity oldStamp = version.stamp();
                    StampEntity newStamp = transactionForVersion.getStamp(getStateForVersion(version), oldStamp.time(),
                            oldStamp.authorNid(), oldStamp.moduleNid(), oldStamp.pathNid());

                    transaction.removeComponent(version.entity());
                    transactionForVersion.addComponent(version.entity());

                    switch (version) {
//                        case ConceptVersionRecord conceptVersionRecord -> {
//                            ConceptRecord analogue = conceptVersionRecord.chronology()
//                                    .without(conceptVersionRecord)
//                                    .with(conceptVersionRecord.withStampNid(newStamp.nid()))
//                                    .build();
//                            Entity.provider().putEntity(analogue);
//                        }
                        case SemanticVersionRecord semanticVersionRecord -> {
                            SemanticRecord analogue = semanticVersionRecord.chronology()
                                    .without(semanticVersionRecord)
                                    .with(semanticVersionRecord.withStampNid(newStamp.nid()))
                                    .build();
                            Entity.provider().putEntity(analogue);
                        }
                        case PatternVersionRecord patternVersionRecord -> {
                            PatternRecord analogue = patternVersionRecord.chronology()
                                    .without(patternVersionRecord)
                                    .with(patternVersionRecord.withStampNid(newStamp.nid()))
                                    .build();
                            Entity.provider().putEntity(analogue);
                        }
                        default -> throw new IllegalStateException("Unexpected value: " + version);
                    }
                    transactionForVersion.commit();
                    Entity.provider().notifyRefreshRequired(transaction);
                }
            }, () -> {
                AlertStreams.getRoot().dispatch(AlertObject.makeError("No transaction found. ", "Canceling Stamp",
                        new IllegalStateException("No transaction for version: " + version)));
                StampRecord stampRecord = (StampRecord) version.stamp();
                StampRecord canceledStamp = stampRecord.withAndBuild(stampRecord.lastVersion().with().stateNid(State.CANCELED.nid()).time(Long.MIN_VALUE).build());
                Entity.provider().putEntity(canceledStamp);
            });

            return null;
        } catch (Throwable t) {
            LOG.error(t.getLocalizedMessage(), t);
            throw t;
        }
    }

    protected abstract void performTransactionAction(Transaction transactionForAction);

    protected abstract State getStateForVersion(EntityVersion version);
}
