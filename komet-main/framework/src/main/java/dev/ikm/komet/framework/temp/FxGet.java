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
package dev.ikm.komet.framework.temp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.coordinate.PathService;
import dev.ikm.tinkar.coordinate.stamp.StampPathImmutable;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.TreeMap;

public class FxGet {
    private static final Logger LOG = LoggerFactory.getLogger(FxGet.class);

    private static ObservableList<ImmutableList<PatternFacade>> NAVIGATION_OPTIONS = FXCollections.observableArrayList();
    private static ObservableMap<PublicIdStringKey, StampPathImmutable> PATHS = FXCollections.observableMap(new TreeMap<>());

    static {
        NAVIGATION_OPTIONS.addAll(
                Lists.immutable.of(TinkarTerm.INFERRED_NAVIGATION_PATTERN),
                Lists.immutable.of(TinkarTerm.STATED_NAVIGATION_PATTERN));
    }

    public static Collection<? extends ConceptFacade> allowedLanguages() {
        return Lists.immutable.of(TinkarTerm.ENGLISH_LANGUAGE, TinkarTerm.SPANISH_LANGUAGE).castToList();
    }

    public static ImmutableList<ImmutableList<? extends ConceptFacade>> allowedDescriptionTypeOrder() {

        return Lists.immutable.of(
                Lists.immutable.of(TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE, TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE),
                Lists.immutable.of(TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE, TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE));
    }

    public static ImmutableList<ImmutableList<? extends PatternFacade>> allowedDialectTypeOrder() {
        return Lists.immutable.of(
                Lists.immutable.of(TinkarTerm.US_DIALECT_PATTERN, TinkarTerm.GB_DIALECT_PATTERN),
                Lists.immutable.of(TinkarTerm.GB_DIALECT_PATTERN, TinkarTerm.US_DIALECT_PATTERN));
    }

    public static ObservableList<ImmutableList<PatternFacade>> navigationOptions() {
        return NAVIGATION_OPTIONS;
    }

    public static ObservableMap<PublicIdStringKey, StampPathImmutable> pathCoordinates(ViewCalculator viewCalculator) {
        if (PATHS.isEmpty()) {
            //TODO add commit listener, and update when new semantic or a commit.
            addPaths(viewCalculator);
        }
        return PATHS;
    }

    private static void addPaths(ViewCalculator viewCalculator) {

        PathService.get().getPaths().forEach(stampPathImmutable -> {
            String pathDescription = viewCalculator.getPreferredDescriptionStringOrNid(stampPathImmutable.pathConceptNid());
            PublicIdStringKey pathKey = new PublicIdStringKey(stampPathImmutable.pathConcept().publicId(), pathDescription);
            PATHS.put(pathKey, stampPathImmutable);
        });
    }

}
