package dev.ikm.tinkar.ext.lang.owl;

import dev.ikm.elk.snomed.SnomedIds;
import dev.ikm.elk.snomed.SnomedOntology;
import dev.ikm.elk.snomed.model.Concept;
import dev.ikm.elk.snomed.model.ConcreteRoleType;
import dev.ikm.elk.snomed.model.RoleType;
import dev.ikm.elk.snomed.owlel.OwlElObjectFactory;
import dev.ikm.elk.snomed.owlel.model.OwlElObject;
import dev.ikm.elk.snomed.owlel.parser.SnomedOfsParser;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpression;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpressionBuilder;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OwlElExpressionToLogicalExpression extends OwlElToLogicalExpression {

	private static final Logger LOG = LoggerFactory.getLogger(OwlElExpressionToLogicalExpression.class);

	private List<String> owlExpressions;

	private int conceptNid;

	private SnomedOntology ontology;

	public OwlElExpressionToLogicalExpression(List<String> owlExpressions, int conceptNid) {
		super();
		this.owlExpressions = owlExpressions;
		this.conceptNid = conceptNid;
	}

	private static String uuidMatchToNid(MatchResult uuidMatch, boolean sub_object_property) {
		String str = uuidMatch.group();
		// remove []
		str = str.substring(1, str.length() - 1);
		UUID uuid = UUID.fromString(str);
		// The OwlEl transform depends on this value
		// TODO make this a param in the owl el transform
		if (!sub_object_property && TinkarTerm.ROLE_GROUP.contains(uuid))
			return String.valueOf(SnomedIds.role_group);
		int nid = PrimitiveData.nid(uuid);
		return String.valueOf(nid);
	}

	public static String uuidToNid(String expr) {
		// TODO make this a param in the owl el transform
		boolean sub_object_property = expr.startsWith("SubObjectProperty");
		// uuids are enclosed in []
		Pattern pattern = Pattern.compile("\\[.+?\\]");
		Matcher matcher = pattern.matcher(expr);
		return matcher.replaceAll(uuid -> uuidMatchToNid(uuid, sub_object_property));
	}

	public void testParse() {
		LOG.info("Exprs (" + owlExpressions.size() + ") " + PrimitiveData.text(conceptNid));
		owlExpressions.forEach(expr -> LOG.info("     : " + expr));
		for (String owlExpression : owlExpressions) {
			OwlElObjectFactory factory = new OwlElObjectFactory();
			SnomedOfsParser parser = new SnomedOfsParser(factory);
			OwlElObject obj = parser.buildExpression(owlExpression);
			LOG.info("Res: " + obj);
			if (parser.getSyntaxError() != null)
				LOG.error(owlExpression);
		}
	}

	private void load() throws IOException {
		List<String> nid_exprs = owlExpressions.stream().map(OwlElExpressionToLogicalExpression::uuidToNid).toList();
		ontology = SnomedOntology.load(nid_exprs);
	}

	public LogicalExpression build() throws Exception {
		builder = new LogicalExpressionBuilder();
		if (!owlExpressions.isEmpty()) {
			load();
			Concept con = ontology.getConcept(conceptNid);
			if (con != null)
				process(con);
			// TODO role & concrete role
			RoleType role_type = ontology.getRoleType(conceptNid);
			if (role_type != null)
				process(role_type);
			ConcreteRoleType concrete_role_type = ontology.getConcreteRoleType(conceptNid);
			if (concrete_role_type != null)
				process(concrete_role_type);
		}
		return builder.build();
	}

}
