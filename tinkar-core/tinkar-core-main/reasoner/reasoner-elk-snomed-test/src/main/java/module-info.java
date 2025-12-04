import dev.ikm.tinkar.reasoner.service.ReasonerService;

open module dev.ikm.tinkar.reasoner.elksnomed.test {
    requires org.eclipse.collections.api;
    requires org.eclipse.collections.impl;
	requires org.junit.jupiter.api;
	requires org.slf4j;

	requires dev.ikm.tinkar.collection;
	requires dev.ikm.tinkar.entity;
	requires dev.ikm.tinkar.ext.lang.owl;
	requires dev.ikm.tinkar.reasoner.service;
	requires dev.ikm.tinkar.reasoner.elksnomed;

	requires dev.ikm.elk.snomed;
	requires dev.ikm.elk.snomed.owlel;
	requires dev.ikm.elk.snomed.test;

	exports dev.ikm.tinkar.reasoner.elksnomed.test;

	// TODO
	uses ReasonerService;

}
