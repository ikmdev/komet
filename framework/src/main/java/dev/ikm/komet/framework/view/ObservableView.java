/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government
 * employees, or under US Veterans Health Administration contracts.
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government
 * employees are USGovWork (17USC §105). Not subject to copyright.
 *
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 *
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */


package dev.ikm.komet.framework.view;

//~--- JDK imports ------------------------------------------------------------


//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import dev.ikm.tinkar.coordinate.logic.PremiseType;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateDelegate;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityProxy;

import java.util.ArrayList;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface ObservableManifoldCoordinate.
 *
 * @author kec
 */
public interface ObservableView
        extends ViewCoordinateDelegate, ObservableCoordinate<ViewCoordinateRecord> {

    default Property<?>[] getBaseProperties() {
        return new Property<?>[]{
                stampCoordinate().allowedStatesProperty(),
        };
    }

    default ObservableCoordinate<?>[] getCompositeCoordinates() {
        ArrayList<ObservableCoordinate<?>> compositeCoordinates = new ArrayList<>(3 + languageCoordinates().size());
        compositeCoordinates.add(stampCoordinate());
        compositeCoordinates.add(logicCoordinate());
        compositeCoordinates.add(navigationCoordinate());
        for (ObservableLanguageCoordinate languageCoordinate : languageCoordinates()) {
            compositeCoordinates.add(languageCoordinate);
        }
        return compositeCoordinates.toArray(new ObservableCoordinate[compositeCoordinates.size()]);
    }

    @Override
    ObservableStampCoordinate stampCoordinate();

    ListProperty<ObservableLanguageCoordinateBase> languageCoordinates();

    @Override
    ObservableNavigationCoordinate navigationCoordinate();

    @Override
    ObservableLogicCoordinate logicCoordinate();

    @Override
    ObservableEditCoordinate editCoordinate();

    /**
     * Will change all contained paths (vertex, edge, and language), to the provided path.
     */
    default void setViewPath(int pathConceptNid) {
        setViewPath(EntityProxy.Concept.make(pathConceptNid));
    }

    void setViewPath(ConceptFacade pathConcept);

    default void setPremiseType(PremiseType premiseType) {
        navigationCoordinate().setPremiseType(premiseType);
    }

    void setAllowedStates(StateSet StateSet);

    ViewCalculator calculator();
}

