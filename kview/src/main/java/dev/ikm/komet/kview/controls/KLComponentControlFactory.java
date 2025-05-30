package dev.ikm.komet.kview.controls;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.tinkar.common.id.IntIdCollection;
import dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculator;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.provider.search.TypeAheadSearch;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
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

    public static KLComponentControl createTypeAheadComponentControl(ViewCalculator viewCalculator) {
        KLComponentControl componentControl = new KLComponentControl();
        NavigationCalculator navigationCalculator = viewCalculator.navigationCalculator();
        componentControl.setTypeAheadCompleter(createGenericTypeAheadFunction(navigationCalculator));

        // add the function to render the component name
        componentControl.setComponentNameRenderer(createComponentNameRenderer(viewCalculator));

        StringConverter<EntityProxy> stringToEntityProxyConverter = createStringToEntityProxyConverter(navigationCalculator);
        componentControl.setTypeAheadStringConverter(stringToEntityProxyConverter);

        componentControl.setSuggestionsCellFactory(_ -> createComponentSuggestionNode(stringToEntityProxyConverter));
        return componentControl;
    }

    public static <T extends IntIdCollection> KLComponentListControl createTypeAheadComponentListControl(ViewCalculator viewCalculator) {
        KLComponentListControl<T> componentListControl = new KLComponentListControl<>();
        NavigationCalculator navigationCalculator = viewCalculator.navigationCalculator();
        componentListControl.setTypeAheadCompleter(createGenericTypeAheadFunction(navigationCalculator));

        // add the function to render the component name
        componentListControl.setComponentNameRenderer(createComponentNameRenderer(viewCalculator));

        StringConverter<EntityProxy> stringToEntityProxyConverter = createStringToEntityProxyConverter(navigationCalculator);
        componentListControl.setTypeAheadStringConverter(stringToEntityProxyConverter);

        componentListControl.setSuggestionsCellFactory(_ -> createComponentSuggestionNode(stringToEntityProxyConverter));
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


    private static Function<EntityProxy, String> createComponentNameRenderer(ViewCalculator viewCalculator) {
        return (entityProxy) ->
            viewCalculator.languageCalculator()
                    .getFullyQualifiedDescriptionTextWithFallbackOrNid(entityProxy.nid());
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

    private static ListCell<EntityProxy> createComponentSuggestionNode(StringConverter<EntityProxy> stringConverter) {
        return new ListCell<>() {
            StackPane stackPane;
            Label label;
            ImageView imageView;

            {
                stackPane = new StackPane();
                VBox suggestionContainer = new VBox();

                label = new Label();

                imageView = new ImageView();
                imageView.setFitHeight(16);
                imageView.setFitWidth(16);

                label.setGraphic(imageView);

                Separator separator = new Separator(Orientation.HORIZONTAL);

                suggestionContainer.getChildren().setAll(label, separator);

                stackPane.setAlignment(Pos.CENTER_LEFT);
                stackPane.getChildren().add(suggestionContainer);

                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }

            @Override
            protected void updateItem(EntityProxy item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(stackPane);

                    String text = stringConverter != null ? stringConverter.toString(item) : item.toString();
                    label.setText(text);

                    Image identiconImage = Identicon.generateIdenticonImage(item.publicId());
                    imageView.setImage(identiconImage);
                }
            }
        };
    }
}
