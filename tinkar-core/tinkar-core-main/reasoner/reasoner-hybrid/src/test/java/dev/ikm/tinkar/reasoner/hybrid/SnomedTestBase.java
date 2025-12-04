package dev.ikm.tinkar.reasoner.hybrid;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.SnomedDescriptions;
import dev.ikm.elk.snomed.test.SnomedVersionBase;

public abstract class SnomedTestBase extends SnomedVersionBase {

	private static final Logger LOG = LoggerFactory.getLogger(SnomedTestBase.class);

	@BeforeEach
	protected void filesExist() {
		assertTrue(Files.exists(axioms_file), "No file: " + axioms_file);
		assertTrue(Files.exists(concepts_file), "No file: " + concepts_file);
		assertTrue(Files.exists(rels_file), "No file: " + rels_file);
		if (Integer.parseInt(getVersion()) >= 20210731)
			assertTrue(Files.exists(values_file), "No file: " + values_file);
		assertTrue(Files.exists(descriptions_file), "No file: " + descriptions_file);
		LOG.info("Files exist");
		LOG.info("\t" + axioms_file);
		LOG.info("\t" + concepts_file);
		LOG.info("\t" + rels_file);
		LOG.info("\t" + values_file);
		LOG.info("\t" + descriptions_file);
	}

	@Test
	public void versionDataFile() throws IOException {
		String version = SnomedDescriptions.getVersion(descriptions_file);
		LOG.info("Version: " + version);
		assertTrue(version.contains(getInternationalVersion()));
	}

	@Test
	public void versionClass() throws IOException {
		assertTrue(this.getClass().getSimpleName().contains(getVersion()));
	}

}
