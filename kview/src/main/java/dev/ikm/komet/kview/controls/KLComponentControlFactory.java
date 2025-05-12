package dev.ikm.komet.kview.controls;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.tinkar.common.id.IntIdCollection;
import dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculator;
import dev.ikm.tinkar.provider.search.TypeAheadSearch;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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

        StringConverter<EntityProxy> stringToEntityProxyConverter = createStringToEntityProxyConverter(calculator);
        componentControl.setTypeAheadStringConverter(stringToEntityProxyConverter);

        componentControl.setSuggestionsNodeFactory(entityProxy -> createComponentSuggestionNode(entityProxy, stringToEntityProxyConverter));
        return componentControl;
    }

    public static <T extends IntIdCollection> KLComponentListControl createTypeAheadComponentListControl(NavigationCalculator calculator) {
        KLComponentListControl<T> componentListControl = new KLComponentListControl<>();
        componentListControl.setTypeAheadCompleter(createGenericTypeAheadFunction(calculator));

        StringConverter<EntityProxy> stringToEntityProxyConverter = createStringToEntityProxyConverter(calculator);
        componentListControl.setTypeAheadStringConverter(stringToEntityProxyConverter);

        componentListControl.setSuggestionsNodeFactory(entityProxy -> createComponentSuggestionNode(entityProxy, stringToEntityProxyConverter));
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
            List<EntityProxy> conceptFacadeToEntityProxys = new ArrayList<>();

            List<ConceptFacade> typeaheadItems = typeAheadSearch.typeAheadSuggestions(
                    navigationCalculator, /* nav calculator */
                    newSearchText, /* text */
                    10  /* max results returned */
            );
            typeaheadItems.forEach(conceptFacade -> conceptFacadeToEntityProxys.add(conceptFacade.toProxy()));
            return conceptFacadeToEntityProxys;
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

    private static Node createComponentSuggestionNode(EntityProxy entity, StringConverter<EntityProxy> stringConverter) {
        StackPane stackPane = new StackPane();
        VBox suggestionContainer = new VBox();

        String text = stringConverter != null ? stringConverter.toString(entity) : entity.toString();
        Label label = new Label(text);

        Image identiconImage = Identicon.generateIdenticonImage(entity.publicId());
        ImageView imageView = new ImageView(identiconImage);
        imageView.setFitHeight(16);
        imageView.setFitWidth(16);

        label.setGraphic(imageView);

        Separator separator = new Separator(Orientation.HORIZONTAL);

        suggestionContainer.getChildren().setAll(label, separator);

        stackPane.setAlignment(Pos.CENTER_LEFT);
        stackPane.getChildren().add(suggestionContainer);

        return stackPane;
    }
}
