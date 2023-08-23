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
package dev.ikm.komet.rules.evrete;

import com.google.auto.service.AutoService;
import dev.ikm.komet.framework.panel.axiom.AxiomSubjectRecord;
import dev.ikm.komet.framework.performance.Request;
import dev.ikm.komet.framework.performance.impl.ObservationRecord;
import dev.ikm.komet.framework.performance.impl.RequestRecord;
import dev.ikm.komet.framework.rulebase.RuleService;
import dev.ikm.komet.framework.performance.Statement;
import dev.ikm.komet.framework.performance.Topic;
import dev.ikm.komet.framework.rulebase.Consequence;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.rules.annotated.AxiomFocusedRules;
import dev.ikm.komet.rules.annotated.ComponentFocusRules;
import dev.ikm.komet.rules.annotated.NewConceptRules;
import dev.ikm.tinkar.common.sets.ConcurrentHashSet;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.entity.ConceptEntityVersion;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.evrete.Configuration;
import org.evrete.KnowledgeService;
import org.evrete.api.ActivationMode;
import org.evrete.api.FactHandle;
import org.evrete.api.Knowledge;
import org.evrete.api.StatelessSession;
import org.evrete.dsl.AbstractDSLProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Map;

@AutoService(RuleService.class)
public class EvreteRulesService implements RuleService {

    private static final Logger LOG = LoggerFactory.getLogger(EvreteRulesService.class);

    private Configuration conf = new Configuration();
    private KnowledgeService service;

    private Knowledge knowledge;

    public EvreteRulesService() throws IOException {
        conf.addImport(Topic.class);
        conf.addImport(ConceptFacade.class);
        conf.addImport(ConceptEntityVersion.class);
        conf.addImport(EntityVersion.class);
        conf.addImport(EntityVertex.class);
        conf.addImport(TinkarTerm.class);
        conf.addImport(Request.class);
        conf.addImport(RequestRecord.class);
        conf.addImport(ObservationRecord.class);
        conf.addImport(Statement.class);
        conf.addImport(AxiomSubjectRecord.class);

        for (Map.Entry<Object, Object> confEntry: conf.entrySet()) {
            LOG.info(confEntry.toString());
        }

        this.service = new KnowledgeService(this.conf, MethodHandles.lookup());
        this.knowledge = service.newKnowledge(AbstractDSLProvider.PROVIDER_JAVA_C,
                ComponentFocusRules.class, NewConceptRules.class, AxiomFocusedRules.class);
        LOG.info("Constructed EvreteRulesService");
    }

    @Override
    public ImmutableList<Consequence<?>> execute(String knowledgeBaseName,
                                                 ImmutableList<Statement> statements,
                                                 ViewProperties viewProperties, EditCoordinate editCoordinate) {

        StatelessSession session = this.knowledge.newStatelessSession(ActivationMode.CONTINUOUS);
        ConcurrentHashSet<Consequence<?>> globalActionSet = new ConcurrentHashSet<>();
        FactHandle globalActionListHandle = session.insert0(globalActionSet, false);

        session.insert(statements.castToList());
        session.insert(viewProperties);
        session.insert(editCoordinate);

        session.fire((handle, object) -> {
            // Inspect memory objects
            LOG.atDebug().log("handle: " + handle + " object: " + object);
            if (object instanceof ConcurrentHashSet<?> set) {
                LOG.atDebug().log("Set items: " + set.stream().toList());
            }
        });
        MutableList<Consequence<?>> globalActionList = Lists.mutable.ofAll(globalActionSet);
        globalActionList.sort((o1, o2) -> o1.compareTo(o2));
        return globalActionList.toImmutableList();
    }
}
