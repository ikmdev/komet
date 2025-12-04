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
package dev.ikm.tinkar.coordinate.view;

import dev.ikm.tinkar.coordinate.edit.EditCoordinateDelegate;
import dev.ikm.tinkar.coordinate.language.LanguageCoordinate;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculator;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculatorDelegate;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculatorWithCache;
import dev.ikm.tinkar.coordinate.logic.LogicCoordinate;
import dev.ikm.tinkar.coordinate.navigation.NavigationCoordinate;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinate;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface ViewCoordinate extends LanguageCalculatorDelegate, EditCoordinateDelegate {

    static UUID getViewUuid(ViewCoordinate viewCalculator) {
        throw new UnsupportedOperationException();
//        ArrayList<UUID> uuidList = new ArrayList<>();
//        uuidList.add(manifoldCoordinate.getEditCoordinate().getEditCoordinateUuid());
//        uuidList.add(manifoldCoordinate.getNavigationCoordinate().getNavigationCoordinateUuid());
//        uuidList.add(manifoldCoordinate.getVertexSort().getVertexSortUUID());
//        uuidList.add(manifoldCoordinate.getVertexStatusSet().getStatusSetUuid());
//        uuidList.add(manifoldCoordinate.getViewStampFilter().getStampFilterUuid());
//        uuidList.add(manifoldCoordinate.getLanguageCoordinate().getLanguageCoordinateUuid());
//        uuidList.add(UuidT5Generator.get(manifoldCoordinate.getCurrentActivity().name()));
//        StringBuilder sb = new StringBuilder(uuidList.toString());
//        return UUID.nameUUIDFromBytes(sb.toString().getBytes());
    }

    ViewCoordinateRecord toViewCoordinateRecord();

    @Override
    default LanguageCalculator languageCalculator() {
        return LanguageCalculatorWithCache.getCalculator(stampCoordinate().toStampCoordinateRecord(),
                languageCoordinateList());
    }

    StampCoordinate stampCoordinate();

    default <T extends LanguageCoordinate> List<T> languageCoordinates() {
        Iterable<T> languageCoordinateIterable = languageCoordinateIterable();
        if (languageCoordinateIterable instanceof ImmutableList<T> immutableList) {
            return immutableList.castToList();
        }
        if (languageCoordinateIterable instanceof MutableList<T> mutableList) {
            return mutableList;
        }
        if (languageCoordinateIterable instanceof List<T> list) {
            return list;
        }
        List<T> newList = new ArrayList<>();
        languageCoordinateIterable.forEach(languageCoordinate -> newList.add(languageCoordinate));
        return newList;
    }

    <T extends LanguageCoordinate> Iterable<T> languageCoordinateIterable();

    default String toUserString() {
        StringBuilder sb = new StringBuilder("View: ");
        sb.append("\n").append(navigationCoordinate().toUserString());
        sb.append("\n\nView filter:\n").append(stampCoordinate().toUserString());
        sb.append("\n\nLanguage coordinates:\n");
        for (LanguageCoordinate languageCoordinate : languageCoordinateIterable()) {
            sb.append("  ").append(languageCoordinate.toUserString()).append("\n");
        }
        sb.append("\n\nLogic:\n").append(logicCoordinate().toUserString());
        return sb.toString();
    }

    NavigationCoordinate navigationCoordinate();

    LogicCoordinate logicCoordinate();

}
