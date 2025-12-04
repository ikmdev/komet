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
package dev.ikm.tinkar.coordinate;

import dev.ikm.tinkar.coordinate.language.LanguageCoordinateRecord;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculatorWithCache;
import dev.ikm.tinkar.coordinate.navigation.NavigationCoordinateRecord;
import dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculatorWithCache;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinate;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculatorWithCache;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculatorWithCache;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

public class Calculators {

    public static class View {
        public static ViewCalculatorWithCache Default() {
            return ViewCalculatorWithCache.getCalculator(
                    Coordinates.View.DefaultView());
        }
    }

    public static class Navigation {
        public static final NavigationCalculatorWithCache inferred(StampCoordinate stampCoordinate,
                                                                   ImmutableList<LanguageCoordinateRecord> languageCoordinateList) {
            return NavigationCalculatorWithCache.getCalculator(stampCoordinate.toStampCoordinateRecord(),
                    languageCoordinateList,
                    NavigationCoordinateRecord.makeInferred());
        }

        public static final NavigationCalculatorWithCache stated(StampCoordinate stampCoordinate,
                                                                 ImmutableList<LanguageCoordinateRecord> languageCoordinateList) {
            return NavigationCalculatorWithCache.getCalculator(stampCoordinate.toStampCoordinateRecord(),
                    languageCoordinateList,
                    NavigationCoordinateRecord.makeStated());
        }
    }

    public static class Stamp {
        public static StampCalculatorWithCache DevelopmentLatest() {
            return StampCalculatorWithCache.getCalculator(Coordinates.Stamp.DevelopmentLatest());
        }

        public static StampCalculatorWithCache DevelopmentLatestActiveOnly() {
            return StampCalculatorWithCache.getCalculator(Coordinates.Stamp.DevelopmentLatestActiveOnly());
        }

        public static StampCalculatorWithCache MasterLatest() {
            return StampCalculatorWithCache.getCalculator(Coordinates.Stamp.MasterLatest());
        }

        public static StampCalculatorWithCache MasterLatestActiveOnly() {
            return StampCalculatorWithCache.getCalculator(Coordinates.Stamp.MasterLatestActiveOnly());
        }
    }


    public static class Language {
        /**
         * A stampCoordinateRecord that completely ignores language - descriptions ranked by this coordinate will only be ranked by
         * description type and module preference.  This stampCoordinateRecord is primarily useful as a fallback coordinate.
         *
         *
         * @return the language stampCoordinateRecord
         *
         */
        public static LanguageCalculatorWithCache AnyLanguageRegularName(StampCoordinate stampCoordinate) {
            return LanguageCalculatorWithCache.getCalculator(stampCoordinate.toStampCoordinateRecord(),
                    Lists.immutable.of(Coordinates.Language.AnyLanguageRegularName()));
        }

        /**
         * A stampCoordinateRecord that completely ignores language - descriptions ranked by this coordinate will only be ranked by
         * description type and module preference.  This stampCoordinateRecord is primarily useful as a fallback coordinate.
         *
         * @return the language stampCoordinateRecord
         */
        public static LanguageCalculatorWithCache AnyLanguageFullyQualifiedName(StampCoordinate stampCoordinate) {
            return LanguageCalculatorWithCache.getCalculator(stampCoordinate.toStampCoordinateRecord(),
                    Lists.immutable.of(Coordinates.Language.AnyLanguageFullyQualifiedName()));
        }

        /**
         * A stampCoordinateRecord that completely ignores language - descriptions ranked by this coordinate will only be ranked by
         * description type and module preference.  This stampCoordinateRecord is primarily useful as a fallback coordinate.
         *
         * @return a stampCoordinateRecord that prefers definitions, of arbitrary language.
         * type
         */
        public static LanguageCalculatorWithCache AnyLanguageDefinition(StampCoordinate stampCoordinate) {
            return LanguageCalculatorWithCache.getCalculator(stampCoordinate.toStampCoordinateRecord(),
                    Lists.immutable.of(Coordinates.Language.AnyLanguageDefinition()));
        }

        /**
         * @return US English language stampCoordinateRecord, preferring FQNs, but allowing regular names, if no FQN is found.
         */
        public static LanguageCalculatorWithCache UsEnglishFullyQualifiedName(StampCoordinate stampCoordinate) {
            return LanguageCalculatorWithCache.getCalculator(stampCoordinate.toStampCoordinateRecord(),
                    Lists.immutable.of(Coordinates.Language.UsEnglishFullyQualifiedName()));
        }

        /**
         * @return US English language stampCoordinateRecord, preferring regular name, but allowing FQN names is no regular name is found.
         */
        public static LanguageCalculatorWithCache UsEnglishRegularName(StampCoordinate stampCoordinate) {
            return LanguageCalculatorWithCache.getCalculator(stampCoordinate.toStampCoordinateRecord(),
                    Lists.immutable.of(Coordinates.Language.UsEnglishRegularName()));
        }

        public static LanguageCalculatorWithCache GbEnglishFullyQualifiedName(StampCoordinate stampCoordinate) {
            return LanguageCalculatorWithCache.getCalculator(stampCoordinate.toStampCoordinateRecord(),
                    Lists.immutable.of(Coordinates.Language.GbEnglishFullyQualifiedName()));
        }

        public static LanguageCalculatorWithCache GbEnglishPreferredName(StampCoordinate stampCoordinate) {
            return LanguageCalculatorWithCache.getCalculator(stampCoordinate.toStampCoordinateRecord(),
                    Lists.immutable.of(Coordinates.Language.GbEnglishPreferredName()));
        }

        public static LanguageCalculatorWithCache SpanishFullyQualifiedName(StampCoordinate stampCoordinate) {
            return LanguageCalculatorWithCache.getCalculator(stampCoordinate.toStampCoordinateRecord(),
                    Lists.immutable.of(Coordinates.Language.SpanishFullyQualifiedName()));
        }

        public static LanguageCalculatorWithCache SpanishPreferredName(StampCoordinate stampCoordinate) {
            return LanguageCalculatorWithCache.getCalculator(stampCoordinate.toStampCoordinateRecord(),
                    Lists.immutable.of(Coordinates.Language.SpanishPreferredName()));
        }
    }

}
