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
package dev.ikm.tinkar.reasoner.elksnomed.test;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.DataServiceController;
import dev.ikm.tinkar.common.service.DataUriOption;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.Coordinates.Edit;
import dev.ikm.tinkar.coordinate.Coordinates.Language;
import dev.ikm.tinkar.coordinate.Coordinates.Logic;
import dev.ikm.tinkar.coordinate.Coordinates.Navigation;
import dev.ikm.tinkar.coordinate.Coordinates.Position;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.StampPositionRecord;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculatorWithCache;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.TinkarTerm;

public abstract class PrimitiveDataTestUtil {

	private static final Logger LOG = LoggerFactory.getLogger(PrimitiveDataTestUtil.class);

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

	public static void deleteDirectory(Path path) throws IOException {
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	public static void copyDb(String source_name, String target_name) throws IOException {
		Path source_path = Paths.get("target", "db", source_name).toAbsolutePath();
		Path target_path = Paths.get("target", "db", target_name).toAbsolutePath();
		assumeTrue(Files.exists(source_path));
		copyDirectory(source_path, target_path);
	}

	public static void deleteDb(String name) throws IOException {
		Path path = Paths.get("target", "db", name).toAbsolutePath();
		deleteDirectory(path);
	}

	public static void setupPrimitiveData(String name) throws IOException {
		setupPrimitiveData("db", name);
	}

	public static void setupPrimitiveData(String dir, String name) throws IOException {
		LOG.info("Setup for: " + name);
		Path target_path = Paths.get("target", dir, name).toAbsolutePath();
		LOG.info("Target: " + target_path);
		// Temp until test data artifacts are in maven repo
		assumeTrue(Files.exists(target_path));
		LOG.info("DataServiceController: " + PrimitiveData.getControllerOptions());
//		for (DataServiceController<?> dsc : PrimitiveData.getControllerOptions()) {
//		for (DataUriOption duo : dsc.providerOptions()) {
//			LOG.info("DataUriOption: " + duo);
//		}
		CachingService.clearAll();
		PrimitiveData.selectControllerByName("Open SpinedArrayStore");
		DataServiceController<?> dsc = PrimitiveData.getController();
		DataUriOption duo = new DataUriOption(name, target_path.toUri());
		LOG.info("PrimitiveData: " + dsc + " " + duo + " " + duo.uri());
		dsc.setDataUriOption(duo);
		PrimitiveData.setController(dsc);
	}

	public static void stopPrimitiveData() {
		LOG.info("stopPrimitiveData");
		PrimitiveData.stop();
		LOG.info("Stopped");
	}

	public static ViewCalculator getViewCalculator() {
		ViewCoordinateRecord vcr = Coordinates.View.DefaultView();
		ViewCalculatorWithCache vc = ViewCalculatorWithCache.getCalculator(vcr);
		return vc;
	}

	public static ViewCalculator getViewCalculator(String time) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
		Instant instant = LocalDate.parse(time, dtf).atStartOfDay().toInstant(ZoneOffset.UTC);
		// workaround until db create issue fixed
//		instant = instant.plus(4, ChronoUnit.HOURS);
		StampPositionRecord pos = StampPositionRecord.make(instant, TinkarTerm.DEVELOPMENT_PATH);
		StampCoordinateRecord scr = StampCoordinateRecord.make(StateSet.ACTIVE_AND_INACTIVE, pos, IntIds.set.empty());
		ViewCoordinateRecord vcr = ViewCoordinateRecord.make(scr, Language.UsEnglishRegularName(), Logic.ElPlusPlus(),
				Navigation.inferred(), Edit.Default());
		ViewCalculatorWithCache vc = ViewCalculatorWithCache.getCalculator(vcr);
		return vc;
	}

	public static ViewCalculator getViewCalculatorPrimordial() {
		StampCoordinateRecord scr = StampCoordinateRecord.make(StateSet.ACTIVE_AND_INACTIVE,
				Position.LatestOnDevelopment(), IntIds.set.of(TinkarTerm.PRIMORDIAL_MODULE.nid()));
		ViewCoordinateRecord vcr = ViewCoordinateRecord.make(scr, Language.UsEnglishRegularName(), Logic.ElPlusPlus(),
				Navigation.inferred(), Edit.Default());
		ViewCalculatorWithCache vc = ViewCalculatorWithCache.getCalculator(vcr);
		return vc;
	}

	public static HashSet<Integer> getPrimordialNids() throws Exception {
		HashSet<Integer> nids = new HashSet<>();
		ViewCalculator vc = PrimitiveDataTestUtil.getViewCalculatorPrimordial();
		vc.forEachSemanticVersionOfPattern(TinkarTerm.IDENTIFIER_PATTERN.nid(), (semanticEntityVersion, _) -> {
			int conceptNid = semanticEntityVersion.referencedComponentNid();
			if (vc.latestIsActive(conceptNid))
				nids.add(conceptNid);
		});
		return nids;
	}

	public static HashSet<Integer> getPrimordialNidsWithSctids() throws Exception {
		ViewCalculator vc = getViewCalculator();
		return getPrimordialNids().stream().filter(nid -> getSctid(nid, vc) != null)
				.collect(Collectors.toCollection(HashSet::new));
	}

	public static String getSctid(int conceptNid, ViewCalculator vc) {
		ArrayList<String> ret = new ArrayList<>();
		Latest<PatternEntityVersion> latestIdPattern = vc.latestPatternEntityVersion(TinkarTerm.IDENTIFIER_PATTERN);
		EntityService.get().forEachSemanticForComponentOfPattern(conceptNid, TinkarTerm.IDENTIFIER_PATTERN.nid(),
				(semanticEntity) -> {
					if (vc.latest(semanticEntity).isPresent()) {
						SemanticEntityVersion latestSemanticVersion = vc.latest(semanticEntity).get();
						EntityProxy identifierSource = latestIdPattern.get()
								.getFieldWithMeaning(TinkarTerm.IDENTIFIER_SOURCE, latestSemanticVersion);
						if (PublicId.equals(identifierSource, TinkarTerm.SCTID)) {
//							String idSourceName = vc.getPreferredDescriptionTextWithFallbackOrNid(identifierSource);
							String idValue = latestIdPattern.get().getFieldWithMeaning(TinkarTerm.IDENTIFIER_VALUE,
									latestSemanticVersion);
							ret.add(idValue);
						}
					}
//					else {
//						throw new RuntimeException(
//								"No latest for " + conceptNid + " " + PrimitiveData.text(conceptNid));
//					}
				});
		if (ret.isEmpty())
			return null;
		return ret.getFirst();
	}

}
