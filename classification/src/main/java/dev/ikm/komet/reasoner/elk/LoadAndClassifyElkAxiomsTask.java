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
package dev.ikm.komet.reasoner.elk;

import dev.ikm.komet.reasoner.AxiomData;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.service.TrackingCallable;
import org.semanticweb.elk.exceptions.ElkException;
import org.semanticweb.elk.loading.AxiomLoader;
import org.semanticweb.elk.loading.ElkLoadingException;
import org.semanticweb.elk.owl.interfaces.ElkAxiom;
import org.semanticweb.elk.owl.interfaces.ElkClass;
import org.semanticweb.elk.owl.interfaces.ElkEntity;
import org.semanticweb.elk.owl.visitors.ElkAxiomProcessor;
import org.semanticweb.elk.reasoner.Reasoner;
import org.semanticweb.elk.reasoner.ReasonerFactory;
import org.semanticweb.elk.reasoner.completeness.IncompleteResult;
import org.semanticweb.elk.reasoner.completeness.Incompleteness;
import org.semanticweb.elk.reasoner.completeness.IncompletenessMonitor;
import org.semanticweb.elk.reasoner.config.ReasonerConfiguration;
import org.semanticweb.elk.reasoner.taxonomy.model.Taxonomy;
import org.semanticweb.elk.util.concurrent.computation.InterruptMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LoadAndClassifyElkAxiomsTask extends TrackingCallable<Taxonomy<ElkClass>> implements AxiomLoader.Factory {
    private static final Logger LOG = LoggerFactory.getLogger(LoadAndClassifyElkAxiomsTask.class);
    final AxiomData<ElkAxiom> axiomData;

    public LoadAndClassifyElkAxiomsTask(AxiomData<ElkAxiom> axiomData) {
        super(true, true);
        this.axiomData = axiomData;
        updateTitle("Loading axioms into ELK reasoner. ");
    }

    @Override
    protected Taxonomy<ElkClass> compute() throws Exception {
        try {
            int axiomCount = this.axiomData.processedSemantics.get();
            updateProgress(0, axiomCount);

            // create reasoner
            ReasonerFactory reasoningFactory = new ReasonerFactory();

            ReasonerConfiguration configuration = ReasonerConfiguration.getConfiguration();
            Reasoner reasoner = reasoningFactory.createReasoner(this, configuration);

            reasoner.ensureLoading();

            boolean inconsistent = Incompleteness.getValue(reasoner.isInconsistent());
            Taxonomy<ElkClass> taxonomy = Incompleteness.getValue(reasoner.getTaxonomyQuietly());

            LOG.info("getTaxonomyQuietly complete: " + taxonomy.getTopNode());
            updateMessage("Load in " + durationString());
            return taxonomy;
        } catch (ElkException e) {
            AlertStreams.dispatchToRoot(e);
            throw e;
        }
    }

    @Override
    public AxiomLoader getAxiomLoader(InterruptMonitor interrupter) {
        return new ElkLoader();
    }

    private class ElkLoader implements AxiomLoader {
        private boolean started = false;
        private volatile boolean finished = false;
        /**
         * the exception created if something goes wrong
         */
        protected volatile Exception exception;

        @Override
        public void load(ElkAxiomProcessor axiomInserter, ElkAxiomProcessor axiomDeleter) throws ElkLoadingException {
            if (finished)
                return;

            if (!started) {
                started = true;
            }
            for (ElkAxiom elkAxiom : axiomData.axiomsSet) {
                if (isInterrupted()) {
                    break;
                }
                axiomInserter.visit(elkAxiom);
            }

            if (exception != null) {
                throw new ElkLoadingException(exception);
            }
        }

        @Override
        public boolean isLoadingFinished() {
            return finished;
        }

        @Override
        public void dispose() {

        }

        @Override
        public boolean isInterrupted() {
            return false;
        }
    }
}
