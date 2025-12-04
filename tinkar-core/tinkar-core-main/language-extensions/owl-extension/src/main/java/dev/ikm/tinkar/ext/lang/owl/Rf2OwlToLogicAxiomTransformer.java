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


import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.common.util.time.Stopwatch;
import dev.ikm.tinkar.coordinate.logic.PremiseType;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class Rf2OwlToLogicAxiomTransformer extends TrackingCallable<Void> {
    private static final Logger LOG = LoggerFactory.getLogger(Rf2OwlToLogicAxiomTransformer.class);
    final int transformSize = 10240;
    private final Transaction transaction;

    private final PatternFacade rf2OwlPattern;
    private final PatternFacade logicalAxiomPattern;
    private int authorNid = TinkarTerm.USER.nid();
    private int moduleNid = Integer.MAX_VALUE;
    private int pathNid = Integer.MAX_VALUE;

    public Rf2OwlToLogicAxiomTransformer(Transaction transaction,
                                         PatternFacade rf2OwlPattern,
                                         PatternFacade logicalAxiomPattern) {
        super(false, true);
        this.transaction = transaction;
        this.rf2OwlPattern = rf2OwlPattern;
        this.logicalAxiomPattern = logicalAxiomPattern;
        updateTitle("Converting RF2 OWL to expressions");
    }

    public Rf2OwlToLogicAxiomTransformer(Transaction transaction,
                                         PatternFacade rf2OwlPattern,
                                         PatternFacade logicalAxiomPattern,
                                         int authorNid, int moduleNid, int pathNid) {
        this(transaction, rf2OwlPattern, logicalAxiomPattern);
        this.authorNid = authorNid;
        this.moduleNid = moduleNid;
        this.pathNid = pathNid;
    }

    @Override
    protected Void compute() throws Exception {
        Stopwatch stopwatch = new Stopwatch();
        updateMessage("Computing stated OWL expressions...");
        LOG.info("Computing stated OWL expressions...");
        addToTotalWork(4);
        completedUnitOfWork();
        updateMessage("Transforming stated OWL RF2 expressions...");
        List<TransformationGroup> statedTransformList = new ArrayList<>();
        AtomicInteger count = new AtomicInteger();
        int rf2OwlPatternNid = rf2OwlPattern.nid();

        EntityService.get().beginLoadPhase();
        try {
            List<Callable<Void>> tasks = new ArrayList<>();

            PrimitiveData.get().forEachConceptNid(conceptNid -> {
                int[] semanticNids = EntityService.get().semanticNidsForComponentOfPattern(conceptNid, rf2OwlPatternNid);
                if (semanticNids != null) {
                    if (semanticNids.length > 0) {
                        TransformationGroup tg = new TransformationGroup(conceptNid, semanticNids, PremiseType.STATED);
                        statedTransformList.add(tg);
                        count.incrementAndGet();
                    }
                }
                if (statedTransformList.size() == transformSize) {
                    List<TransformationGroup> listForTask = List.copyOf(statedTransformList);
                    OwlToLogicAxiomTransformerAndWriter transformer = new OwlToLogicAxiomTransformerAndWriter(
                            transaction, listForTask, logicalAxiomPattern.nid(), authorNid, moduleNid, pathNid);
                    tasks.add(transformer);
                    statedTransformList.clear();
                }
            });

            // pickup any items remaining in the list.
            if (!statedTransformList.isEmpty()) {
                OwlToLogicAxiomTransformerAndWriter remainingStatedtransformer = new OwlToLogicAxiomTransformerAndWriter(
                        transaction, statedTransformList, logicalAxiomPattern.nid(), authorNid, moduleNid, pathNid);
                tasks.add(remainingStatedtransformer);
            }

            LOG.info("Started processing {} batches", tasks.size());

            TinkExecutor.threadPool().invokeAll(tasks);

            completedUnitOfWork();

            transaction.commit();
            completedUnitOfWork();
            updateMessage("Completed transformation");
        } finally {
            EntityService.get().endLoadPhase();
            LOG.info("Completed processing of {} stated OWL expressions in {}", count.get(), stopwatch.durationString());
        }
        return null;
    }
}
