package dev.ikm.tinkar.reasoner.elksnomed.test;

import java.util.HashMap;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.SnomedOntology;
import dev.ikm.elk.snomed.model.Concept;
import dev.ikm.elk.snomed.model.ConcreteRole;
import dev.ikm.elk.snomed.model.ConcreteRoleType;
import dev.ikm.elk.snomed.model.Definition;
import dev.ikm.elk.snomed.model.Role;
import dev.ikm.elk.snomed.model.RoleGroup;
import dev.ikm.elk.snomed.model.RoleType;
import dev.ikm.tinkar.reasoner.elksnomed.ElkSnomedData;

public class NidToSctid {

	private static final Logger LOG = LoggerFactory.getLogger(NidToSctid.class);

	private ElkSnomedData data;

	private SnomedOntology snomedOntology;

	private HashMap<Long, Long> nid_to_sctid = new HashMap<>();

	private HashMap<Long, RoleType> new_roles = new HashMap<>();

	private HashMap<Long, ConcreteRoleType> new_concrete_roles = new HashMap<>();

	private HashMap<Long, Concept> new_concepts = new HashMap<>();

	public NidToSctid(ElkSnomedData data, SnomedOntology snomedOntology) {
		super();
		this.data = data;
		this.snomedOntology = snomedOntology;
	}

	public SnomedOntology build() throws Exception {
		HashSet<Long> not_in_snomed = new HashSet<>();
		for (RoleType role : snomedOntology.getRoleTypes()) {
			long nid = ElkSnomedData.getNid(role.getId());
			nid_to_sctid.put(nid, role.getId());
		}
		for (ConcreteRoleType role : snomedOntology.getConcreteRoleTypes()) {
			long nid = ElkSnomedData.getNid(role.getId());
			nid_to_sctid.put(nid, role.getId());
		}
		for (Concept con : snomedOntology.getConcepts()) {
			long nid = ElkSnomedData.getNid(con.getId());
			nid_to_sctid.put(nid, con.getId());
		}
		for (RoleType role : data.getRoleTypes()) {
			long nid = role.getId();
			RoleType new_role = new RoleType(nid_to_sctid.get(nid));
			new_roles.put(nid, new_role);
		}
		for (ConcreteRoleType role : data.getConcreteRoleTypes()) {
			long nid = role.getId();
			ConcreteRoleType new_role = new ConcreteRoleType(nid_to_sctid.get(nid));
			new_concrete_roles.put(nid, new_role);
		}
		HashSet<Integer> primordial_nids = PrimitiveDataTestUtil.getPrimordialNids();
		for (Concept concept : data.getConcepts()) {
			long nid = concept.getId();
			if (nid_to_sctid.get(nid) == null) {
				not_in_snomed.add(nid);
				if (!primordial_nids.contains((int) nid))
					LOG.error("None for: " + nid);
				continue;
			}
			Concept new_concept = new Concept(nid_to_sctid.get(nid));
			new_concepts.put(nid, new_concept);
		}
		for (RoleType role : data.getRoleTypes()) {
			RoleType new_role = new_roles.get(role.getId());
			if (role.getChained() != null)
				new_role.setChained(new_roles.get(role.getChained().getId()));
			new_role.setChained(role.getChained());
			new_role.setReflexive(role.isReflexive());
			new_role.setTransitive(role.isTransitive());
			role.getSuperRoleTypes().forEach(sup -> new_role.addSuperRoleType(new_roles.get(sup.getId())));
		}
		for (ConcreteRoleType role : data.getConcreteRoleTypes()) {
			ConcreteRoleType new_role = new_concrete_roles.get(role.getId());
			role.getSuperConcreteRoleTypes()
					.forEach(sup -> new_role.addSuperConcreteRoleType(new_concrete_roles.get(sup.getId())));
		}
		for (Concept concept : data.getConcepts()) {
			if (not_in_snomed.contains(concept.getId()))
				continue;
			Concept new_concept = new_concepts.get(concept.getId());
			for (Definition def : concept.getDefinitions()) {
				new_concept.addDefinition(makeNewDefinition(def));
			}
			for (Definition def : concept.getGciDefinitions()) {
				new_concept.addGciDefinition(makeNewDefinition(def));
			}
		}
		return new SnomedOntology(new_concepts.values(), new_roles.values(), new_concrete_roles.values());
	}

	public Definition makeNewDefinition(Definition def) {
		Definition new_def = new Definition();
		new_def.setDefinitionType(def.getDefinitionType());
		def.getSuperConcepts().forEach(sup -> new_def.addSuperConcept(new_concepts.get(sup.getId())));
		def.getUngroupedRoles().forEach(role -> new_def.addUngroupedRole(makeNewRole(role)));
		def.getUngroupedConcreteRoles().forEach(role -> new_def.addUngroupedConcreteRole(makeNewConcreteRole(role)));
		def.getRoleGroups().forEach(rg -> new_def.addRoleGroup(makeRoleGroup(rg)));
		return new_def;
	}

	private Role makeNewRole(Role role) {
		Role new_role = new Role(new_roles.get(role.getRoleType().getId()),
				new_concepts.get(role.getConcept().getId()));
		return new_role;
	}

	private ConcreteRole makeNewConcreteRole(ConcreteRole role) {
		ConcreteRole new_role = new ConcreteRole(new_concrete_roles.get(role.getConcreteRoleType().getId()),
				role.getValue(), role.getValueType());
		return new_role;
	}

	private RoleGroup makeRoleGroup(RoleGroup rg) {
		RoleGroup new_rg = new RoleGroup();
		rg.getRoles().forEach(role -> new_rg.addRole(makeNewRole(role)));
		rg.getConcreteRoles().forEach(role -> new_rg.addConcreteRole(makeNewConcreteRole(role)));
		return new_rg;
	}

}
