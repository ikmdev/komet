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
package dev.ikm.komet.reasoner.elkowl;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.jupiter.api.AfterAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.tinkar.common.service.DataServiceController;
import dev.ikm.tinkar.common.service.DataUriOption;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculatorWithCache;
import dev.ikm.tinkar.terms.TinkarTerm;

public abstract class PrimitiveDataTestBase {

	private static final Logger LOG = LoggerFactory.getLogger(PrimitiveDataTestBase.class);

	public static void copyDirectory(Path source_path, Path target_path) throws IOException {
		Files.walkFileTree(source_path, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				Path resolve = target_path.resolve(source_path.relativize(dir)).normalize();
				Files.createDirectories(resolve);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Path resolve = target_path.resolve(source_path.relativize(file));
				Files.copy(file, resolve, StandardCopyOption.REPLACE_EXISTING);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	public static void setupPrimitiveData(String name) throws IOException {
		LOG.info("Setup for: " + name);
		LOG.info("DataServiceController: " + PrimitiveData.getControllerOptions());
//		for (DataServiceController<?> dsc : PrimitiveData.getControllerOptions()) {
//		for (DataUriOption duo : dsc.providerOptions()) {
//			LOG.info("DataUriOption: " + duo);
//		}
		PrimitiveData.selectControllerByName("Open SpinedArrayStore");
		DataServiceController<?> dsc = PrimitiveData.getController();
//		Path source_path = Paths.get(System.getProperty("user.home"), "SolorTest", name).toAbsolutePath();
		Path target_path = Paths.get("target", "db", name).toAbsolutePath();
//		LOG.info("Source: " + source_path);
		LOG.info("Target: " + target_path);
		// Temp until test data artifacts are in maven repo
		assumeTrue(Files.exists(target_path));
		assertTrue(Files.exists(target_path));
//		copyDirectory(source_path, target_path);
		DataUriOption duo = new DataUriOption(name, target_path.toUri());
		LOG.info("PrimitiveData: " + dsc + " " + duo + " " + duo.uri());
		dsc.setDataUriOption(duo);
		PrimitiveData.setController(dsc);
	}

	@AfterAll
	public static void stopPrimitiveData() {
		LOG.info("stopPrimitiveData");
		PrimitiveData.stop();
		LOG.info("Stopped");
	}

	public static ViewCalculator getViewCalculator() {
		ViewCoordinateRecord vcr = Coordinates.View.DefaultView();
		ViewCalculatorWithCache viewCalculator = ViewCalculatorWithCache.getCalculator(vcr);
		return viewCalculator;
	}

	public ElkOwlAxiomData buildAxiomData() throws Exception {
		LOG.info("buildAxiomData");
		ViewCalculator viewCalculator = getViewCalculator();
		ElkOwlAxiomData axiomData = new ElkOwlAxiomData();
		ElkOwlAxiomDataBuilder builder = new ElkOwlAxiomDataBuilder(viewCalculator,
				TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN, axiomData);
		builder.build();
		return axiomData;
	}

}
