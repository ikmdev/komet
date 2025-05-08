package dev.ikm.komet.kview.controls;

import dev.ikm.tinkar.common.id.IntIdCollection;
import dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculator;
import dev.ikm.tinkar.provider.search.TypeAheadSearch;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class KLComponentControlFactory {

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public static KLComponentControl createTypeAheadComponentControl(NavigationCalculator calculator) {
        KLComponentControl componentControl = new KLComponentControl();
        componentControl.setTypeAheadCompleter(createGenericTypeAheadFunction(calculator));
        componentControl.setTypeAheadStringConverter(createStringToEntityProxyConverter(calculator));
        return componentControl;
    }

    public static <T extends IntIdCollection> KLComponentListControl createTypeAheadComponentListControl(NavigationCalculator calculator) {
        KLComponentListControl<T> componentListControl = new KLComponentListControl<>();
        componentListControl.setTypeAheadCompleter(createGenericTypeAheadFunction(calculator));
        componentListControl.setTypeAheadStringConverter(createStringToEntityProxyConverter(calculator));
        return componentListControl;
    }

    /***************************************************************************
     *                                                                         *
     * Private Implementation                                                  *
     *                                                                         *
     **************************************************************************/

    private static Function<String, List<EntityProxy>> createGenericTypeAheadFunction(NavigationCalculator navigationCalculator) {
        return newSearchText -> {
            TypeAheadSearch typeAheadSearch = TypeAheadSearch.get();
            List<EntityProxy> conceptFacades = new ArrayList<>();

            List<ConceptFacade> typeaheadItems = typeAheadSearch.typeAheadSuggestions(
                    navigationCalculator, /* nav calculator */
                    newSearchText, /* text */
                    10  /* max results returned */
            );
            typeaheadItems.forEach(conceptFacade -> conceptFacades.add(conceptFacade.toProxy()));
            return conceptFacades;
        };
    }

    private static StringConverter<EntityProxy> createStringToEntityProxyConverter(NavigationCalculator navigationCalculator) {
        return new StringConverter<>() {
            @Override
            public String toString(EntityProxy conceptFacade) {
                return navigationCalculator.getFullyQualifiedDescriptionTextWithFallbackOrNid(conceptFacade.nid());
            }

            @Override
            public EntityProxy fromString(String string) {
                return null;
            }
        };
    }
}
