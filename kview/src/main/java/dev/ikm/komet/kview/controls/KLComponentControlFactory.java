package dev.ikm.komet.kview.controls;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.tinkar.common.id.IntIdCollection;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.uuid.UuidUtil;
import dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculator;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import java.util.UUID;
import java.util.function.Function;

public class KLComponentControlFactory {

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public static KLComponentControl createComponentControl(ViewCalculator viewCalculator) {
        KLComponentControl componentControl = new KLComponentControl();
        NavigationCalculator navigationCalculator = viewCalculator.navigationCalculator();
        componentControl.setTypeAheadCompleter(createUUIDConverterFunction(navigationCalculator));

        // add the function to render the component name
        componentControl.setComponentNameRenderer(createComponentNameRenderer(viewCalculator));

        // String converter
        StringConverter<EntityProxy> stringToEntityProxyConverter = createStringToEntityProxyConverter(navigationCalculator);
        componentControl.setTypeAheadStringConverter(stringToEntityProxyConverter);

        // suggestions cell factory
        componentControl.setSuggestionsCellFactory(_ -> createComponentSuggestionNode(stringToEntityProxyConverter));

        return componentControl;
    }

    public static <T extends IntIdCollection> KLComponentCollectionControl createComponentListControl(ViewCalculator viewCalculator) {
        KLComponentCollectionControl<T> componentListControl = new KLComponentCollectionControl<>();
        NavigationCalculator navigationCalculator = viewCalculator.navigationCalculator();

        componentListControl.setTypeAheadCompleter(createUUIDConverterFunction(navigationCalculator));

        // add the function to render the component name
        componentListControl.setComponentNameRenderer(createComponentNameRenderer(viewCalculator));

        StringConverter<EntityProxy> stringToEntityProxyConverter = createStringToEntityProxyConverter(navigationCalculator);
        componentListControl.setTypeAheadStringConverter(stringToEntityProxyConverter);

        componentListControl.setSuggestionsCellFactory(_ -> createComponentSuggestionNode(stringToEntityProxyConverter));

        // header node
        componentListControl.setTypeAheadHeaderPane(createTypeAheadHeaderPane());

        // dropping multiple concepts
        componentListControl.setOnDroppingMultipleConcepts(publicIds -> {
            ArrayList<Integer> newNids = new ArrayList<>();

            publicIds.forEach(uuidArrayList -> {
                for (UUID[] uuidArray : uuidArrayList) {
                    EntityHandle.get(PublicIds.of(uuidArray)).ifPresent(entity -> newNids.add(entity.nid()));
                }
            });

            int[] newNidsIntArray = newNids.stream().mapToInt(i -> i).toArray();
            componentListControl.setValue((T)componentListControl.getValue().with(newNidsIntArray));
        });

        return componentListControl;
    }

    /***************************************************************************
     *                                                                         *
     * Private Implementation                                                  *
     *                                                                         *
     **************************************************************************/

    /**
     * If the user enters a valid UUID this method will return the associated Concept.
     *
     * @param navigationCalculator the navigation calculator.
     * @return a List containing the associated concept or an empty list if there is no Concept associated with the UUID
     */
    private static Function<String, List<EntityProxy>> createUUIDConverterFunction(NavigationCalculator navigationCalculator) {
        return newSearchText -> {
            List<EntityProxy> entityProxyResults = new ArrayList<>();

            UuidUtil.getUUID(newSearchText).ifPresent(
                    uuid -> EntityHandle.get(PrimitiveData.nid(PublicIds.of(uuid))).ifPresent(
                    entityFacade -> entityProxyResults.add(entityFacade.toProxy())
                    )
            );
            return entityProxyResults;
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

    private static AutoCompleteTextField.HeaderPane createTypeAheadHeaderPane() {
        return new AutoCompleteTextField.HeaderPane<EntityProxy>() {
            Label headerLabel = new Label();
            {
                headerLabel.getStyleClass().add("header");
            }

            @Override
            public Node createContent() {
                return headerLabel;
            }

            @Override
            public void updateContent(List<EntityProxy> suggestions) {
                headerLabel.setText(suggestions.size() + " results");
            }
        };
    }
}
