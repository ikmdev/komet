package dev.ikm.komet.kview.controls;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.tinkar.common.id.IntIdCollection;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.uuid.UuidUtil;
import dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculator;
import dev.ikm.tinkar.coordinate.stamp.calculator.LatestVersionSearchResult;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.terms.EntityFacade;
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
import org.eclipse.collections.api.list.ImmutableList;

import java.util.*;
import java.util.function.*;

public class KLComponentControlFactory {

    public static final int MAX_INLINE_SEARCH_RESULTS = 15;

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public static KLComponentControl createComponentControl(ViewCalculator viewCalculator) {
        KLComponentControl componentControl = new KLComponentControl();
        NavigationCalculator navigationCalculator = viewCalculator.navigationCalculator();
        componentControl.setTypeAheadCompleter(createInlineSearchFunction(navigationCalculator));

        // add the function to render the component name
        componentControl.setComponentNameRenderer(createComponentNameRenderer(viewCalculator));

        // String converter
        StringConverter<EntityFacade> stringToEntityFacadeConverter = createStringToEntityFacadeConverter(navigationCalculator);
        componentControl.setTypeAheadStringConverter(stringToEntityFacadeConverter);

        // suggestions cell factory
        componentControl.setSuggestionsCellFactory(_ -> createComponentSuggestionNode(stringToEntityFacadeConverter));

        return componentControl;
    }

    public static <T extends IntIdCollection> KLComponentCollectionControl createComponentListControl(ViewCalculator viewCalculator) {
        KLComponentCollectionControl<T> componentListControl = new KLComponentCollectionControl<>();
        NavigationCalculator navigationCalculator = viewCalculator.navigationCalculator();

        componentListControl.setTypeAheadCompleter(createInlineSearchFunction(navigationCalculator));

        // add the function to render the component name
        componentListControl.setComponentNameRenderer(createComponentNameRenderer(viewCalculator));

        StringConverter<EntityFacade> stringToEntityFacadeConverter = createStringToEntityFacadeConverter(navigationCalculator);
        componentListControl.setTypeAheadStringConverter(stringToEntityFacadeConverter);

        componentListControl.setSuggestionsCellFactory(_ -> createComponentSuggestionNode(stringToEntityFacadeConverter));

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
    private static Function<String, List<EntityFacade>> createUUIDConverterFunction(NavigationCalculator navigationCalculator) {
        return newSearchText -> {
            List<EntityFacade> entityFacadeResults = new ArrayList<>();

            UuidUtil.getUUID(newSearchText).ifPresent(
                    uuid -> EntityHandle.get(PrimitiveData.nid(PublicIds.of(uuid))).ifPresent(
                    entityFacade -> entityFacadeResults.add(entityFacade.toProxy())
                    )
            );
            return entityFacadeResults;
        };
    }

    /**
     * If the user enters a valid UUID this method will return the associated Component, otherwise will match
     * based on the component descriptions.
     *
     * @param navigationCalculator the navigation calculator.
     * @return a List containing the associated concept or an empty list if there is no Concept associated with the UUID
     */
    private static Function<String, List<EntityFacade>> createInlineSearchFunction(NavigationCalculator navigationCalculator) {
        return newSearchText -> {
            List<EntityFacade> entityFacadeResults = new ArrayList<>();

            if (UuidUtil.isUUID(newSearchText)) {
                UuidUtil.getUUID(newSearchText).ifPresent(
                        uuid -> EntityHandle.get(uuid).ifPresent(
                                entityFacade -> entityFacadeResults.add(entityFacade.toProxy())
                        )
                );
            } else {
                try {
                    ImmutableList<LatestVersionSearchResult> inlineResults = navigationCalculator.search(newSearchText, MAX_INLINE_SEARCH_RESULTS);
                    inlineResults.forEach(latestVersionSearchResult ->
                            latestVersionSearchResult.latestVersion().ifPresent(matchedSemantic ->
                                    EntityHandle.get(matchedSemantic.referencedComponentNid())
                                            .ifPresent(entity -> entityFacadeResults.add(entity.toProxy()))));

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return entityFacadeResults;
        };
    }

    private static Function<EntityFacade, String> createComponentNameRenderer(ViewCalculator viewCalculator) {
        return (entityFacade) ->
            viewCalculator.languageCalculator()
                    .getFullyQualifiedDescriptionTextWithFallbackOrNid(entityFacade.nid());
    }

    private static StringConverter<EntityFacade> createStringToEntityFacadeConverter(NavigationCalculator navigationCalculator) {
        return new StringConverter<>() {
            @Override
            public String toString(EntityFacade conceptFacade) {
                return navigationCalculator.getFullyQualifiedDescriptionTextWithFallbackOrNid(conceptFacade.nid());
            }

            @Override
            public EntityFacade fromString(String string) {
                return null;
            }
        };
    }

    private static ListCell<EntityFacade> createComponentSuggestionNode(StringConverter<EntityFacade> stringConverter) {
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
            protected void updateItem(EntityFacade item, boolean empty) {
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
        return new AutoCompleteTextField.HeaderPane<EntityFacade>() {
            Label headerLabel = new Label();
            {
                headerLabel.getStyleClass().add("header");
            }

            @Override
            public Node createContent() {
                return headerLabel;
            }

            @Override
            public void updateContent(List<EntityFacade> suggestions) {
                headerLabel.setText(suggestions.size() + " results");
            }
        };
    }
}
