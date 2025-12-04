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
package dev.ikm.tinkar.reasoner.hybrid;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.SnomedIds;
import dev.ikm.elk.snomed.interval.Interval;
import dev.ikm.elk.snomed.model.Concept;
import dev.ikm.elk.snomed.model.Definition;
import dev.ikm.elk.snomed.model.DefinitionType;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalAxiom;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalAxiom.Atom.TypedAtom.IntervalRole;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpression;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpressionBuilder;
import dev.ikm.tinkar.ext.lang.owl.OwlElToLogicalExpression;
import dev.ikm.tinkar.reasoner.elksnomed.ElkSnomedData;
import dev.ikm.tinkar.reasoner.elksnomed.ElkSnomedUtil;
import dev.ikm.tinkar.reasoner.service.ReasonerService;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.TinkarTerm;

public abstract class HybridReasonerIntervalTestBase extends HybridReasonerTestBase {

	private static final Logger LOG = LoggerFactory.getLogger(HybridReasonerIntervalTestBase.class);

	// 395507008 |Premature infant (finding)|
	private static final long premature_infant_sctid = 395507008l;

	private void updatePremature() throws Exception {
		ViewCalculator vc = PrimitiveDataTestUtil.getViewCalculator();
		// 103335007 |Duration (attribute)|
		int duration_role_nid = ElkSnomedData.getNid(103335007);
		{
			LogicalExpressionBuilder builder = new LogicalExpressionBuilder();
			int attr_nid = ElkSnomedData.getNid(SnomedIds.concept_model_data_attribute);
			builder.IntervalPropertySet(builder.And(builder.ConceptAxiom(attr_nid)));
			LogicalExpression le = builder.build();
//			LOG.info("IntervalPropertySet:\n" + le);
			ElkSnomedUtil.updateStatedSemantic(vc, duration_role_nid, le);
//			SemanticEntityVersion sev = ElkSnomedUtil.getStatedSemantic(vc, duration_role_nid);
//			LOG.info("SEV:\n" + sev);
		}
		int pi_nid = ElkSnomedData.getNid(premature_infant_sctid);
		Concept pi_con = ElkSnomedUtil.getConcept(vc, pi_nid);
		Path intervals_file = Paths.get("src/test/resources",
				"intervals-" + getEditionDir() + "-" + getVersion() + ".txt");
//		LOG.info("Intervals file: " + intervals_file);
		for (String line : Files.readAllLines(intervals_file)) {
			String[] fields = line.split("\t");
			long sctid = Long.parseLong(fields[0]);
			int nid = ElkSnomedData.getNid(sctid);
			Interval interval = Interval.fromString(fields[1]);
			int units_nid = ElkSnomedData.getNid(interval.getUnitOfMeasure().getId());
			interval.setUnitOfMeasure(new Concept(units_nid));
//			LOG.info("Interval: " + interval + " " + sctid + " " + PrimitiveData.text(nid));
//			SemanticEntityVersion sev = ElkSnomedUtil.getStatedSemantic(vc, nid);
//			LOG.info("SEV:\n" + sev);
			Concept con = ElkSnomedUtil.getConcept(vc, nid);
			Definition def = con.getDefinitions().getFirst();
			def.setDefinitionType(DefinitionType.EquivalentConcept);
			def.getSuperConcepts().clear();
			def.addSuperConcept(pi_con);
			def.getRoleGroups().clear();
			def.getUngroupedRoles().clear();
			def.getUngroupedConcreteRoles().clear();
			LogicalExpression le = new OwlElToLogicalExpression().build(def);
			LogicalExpressionBuilder builder = new LogicalExpressionBuilder(le);
			IntervalRole interval_role = builder.IntervalRole(ConceptFacade.make(duration_role_nid),
					interval.getLowerBound(), interval.isLowerOpen(), interval.getUpperBound(), interval.isUpperOpen(),
					ConceptFacade.make(units_nid));
			builder.addToFirstAnd(0, interval_role);
			le = builder.build();
//			LOG.info("ROLE:\n" + le);
			ElkSnomedUtil.updateStatedSemantic(vc, nid, le);
//			LOG.info("SEV:\n" + ElkSnomedUtil.getStatedSemantic(vc, nid));
		}
	}

	public ReasonerService initReasonerService() {
		ReasonerService rs = new IntervalReasonerService();
		rs.init(getViewCalculator(), TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN,
				TinkarTerm.EL_PLUS_PLUS_INFERRED_AXIOMS_PATTERN);
		rs.setProgressUpdater(null);
		return rs;
	}

	private int getNid(String line, int field) {
		String con = line.split("\t")[field];
		String id = con.substring(con.indexOf("[") + 1, con.indexOf(" "));
		long sctid = Long.parseLong(id);
		return ElkSnomedData.getNid(sctid);
	}

	@Test
	public void premature() throws Exception {
		updatePremature();
		ReasonerService rs = initReasonerService();
		rs.extractData();
		rs.loadData();
		rs.computeInferences();
		rs.buildNecessaryNormalForm();
		rs.writeInferredResults();
		int pi_nid = ElkSnomedData.getNid(premature_infant_sctid);
		LOG.info("-".repeat(20));
		print(rs, pi_nid, 0);
		{
			String file_name = "intervals-sups-" + getEditionDir() + "-" + getVersion() + ".txt";
			List<String> expect_lines = Files.lines(Paths.get("src/test/resources", file_name)).toList();
			HashMap<Integer, Set<Integer>> expect = new HashMap<>();
			for (String line : expect_lines) {
				int con = getNid(line, 0);
				int sup = getNid(line, 1);
				expect.putIfAbsent(con, new HashSet<>());
				expect.get(con).add(sup);
			}
			for (int con : expect.keySet()) {
				HashSet<Integer> sups = new HashSet<>();
				rs.getParents(con).forEach(sups::add);
				assertEquals(expect.get(con), sups);
				LogicalExpression nnf = rs.getNecessaryNormalForm(con);
				ImmutableList<IntervalRole> ir = nnf.nodesOfType(LogicalAxiom.Atom.TypedAtom.IntervalRole.class);
				LOG.info(PrimitiveData.text(con) + "\n" + nnf + "\n" + ir.getFirst());
				assertEquals(1, ir.size());
			}
			assertEquals(22, expect.keySet().size());
		}
	}

	private void print(ReasonerService rs, int nid, int i) {
		LOG.info("\t".repeat(i) + PrimitiveData.text(nid));
		rs.getChildren(nid).forEach(x -> print(rs, x, i + 1));
	}

}
