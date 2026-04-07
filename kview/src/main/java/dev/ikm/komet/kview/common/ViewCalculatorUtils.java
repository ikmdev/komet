package dev.ikm.komet.kview.common;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.terms.ComponentWithNid;
import dev.ikm.tinkar.terms.PatternFacade;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utility class that provides helper methods for working with ViewCalculator and
 * displaying component information in JavaFX UI controls.
 */
public class ViewCalculatorUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ViewCalculatorUtils.class);

    /**
     * Initializes an empty ComboBox for displaying {@link ComponentWithNid} items.
     * This method configures the ComboBox with an empty ObservableList and sets up the cell factory
     * to display the preferred description text for each item using the provided ViewProperties.
     *
     * @param comboBox The JavaFX ComboBox to initialize
     * @param viewProperties A supplier for the {@link ViewProperties} used for text representation of concepts
     */
    public static <T extends ComponentWithNid> void initComboBox(ComboBox<T> comboBox, Supplier<ViewProperties> viewProperties) {
        initComboBox(comboBox, FXCollections.observableArrayList(), viewProperties);
    }

    /**
     * Initializes a ComboBox with specified items for displaying {@link ComponentWithNid} objects.
     * This method configures the ComboBox with the provided ObservableList and sets up the cell factory
     * to display the preferred description text for each item using the provided ViewProperties.
     *
     * @param comboBox The JavaFX ComboBox to initialize
     * @param items The ObservableList of items to set in the ComboBox
     * @param viewProperties A supplier for the {@link ViewProperties} used for text representation of concepts
     */
    public static <T extends ComponentWithNid> void initComboBox(ComboBox<T> comboBox, ObservableList items, Supplier<ViewProperties> viewProperties) {
        comboBox.setItems(items);

        comboBox.setCellFactory(_ -> createConceptListCell(viewProperties));
        comboBox.setButtonCell(createConceptListCell(viewProperties));
    }

    private static <T extends ComponentWithNid> ListCell<T> createConceptListCell(Supplier<ViewProperties> viewProperties) {
        return new ListCell<T>(){
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(getDescriptionTextWithFallbackOrNid(item, viewProperties.get()));
                }
            }
        };
    }

    /**
     * Gets the preferred description text for a component or falls back to displaying its native identifier (NID).
     * This utility method attempts to retrieve the preferred description text for the given component
     * using the provided ViewProperties. If the text cannot be found, it returns the component's NID as a string.
     *
     * @param conceptEntity The component entity for which to retrieve the description text
     * @param viewProperties The {@link ViewProperties} used to calculate the preferred description
     * @return A string containing either the preferred description text or the component's NID
     */
    public static <T extends ComponentWithNid> String getDescriptionTextWithFallbackOrNid(T conceptEntity, ViewProperties viewProperties) {
        String descr = "" + conceptEntity.nid();

        if (viewProperties != null) {
            descr = viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(conceptEntity.nid());
        }
        return descr;
    }

    /**
     * Generates tooltip text for a stamp based on its native identifier (NID).
     * This method retrieves the stamp entity for the given NID and formats it into a human-readable
     * tooltip containing status, time, author, module, and path information.
     *
     * @param stampNid The NID of the stamp entity
     * @param viewCalculator The {@link ViewCalculator} used to get human-readable descriptions for the stamp components
     * @return A formatted string containing the stamp details, or empty string if the stamp entity cannot be found
     */
    public static String getStampToolTipText(int stampNid, ViewCalculator viewCalculator) {
        StringBuilder tooltipText = new StringBuilder();
        Entity.get(stampNid).ifPresent(entity -> {
            if (entity instanceof StampEntity<?> stampEntity) {
                tooltipText.append(getStampToolTipText(stampEntity, viewCalculator));
            }
        });
        return tooltipText.toString();
    }

    /**
     * Generates tooltip text for a StampEntity.
     * This method formats the stamp entity information into a human-readable tooltip containing
     * status, time, author, module, and path information.
     *
     * @param stampEntity The {@link StampEntity} to generate tooltip text for
     * @param viewCalculator The {@link ViewCalculator} used to get human-readable descriptions for the stamp components
     * @return A formatted string containing the stamp details
     */
    public static String getStampToolTipText(StampEntity<?> stampEntity, ViewCalculator viewCalculator) {
        return """
            Status:\t%s
            Time:\t%s
            Author:\t%s
            Module:\t%s
            Path:\t%s
            """.formatted(
                stampEntity.state(),
                DateTimeUtil.format(stampEntity.time(), DateTimeUtil.SEC_FORMATTER),
                viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(stampEntity.authorNid()),
                viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(stampEntity.moduleNid()),
                viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(stampEntity.pathNid()));
    }

    public static String retrieveDisplayName(PatternFacade patternFacade, ViewProperties viewProperties) {
        ViewCalculator viewCalculator = viewProperties.calculator();
        Optional<String> optionalStringRegularName = viewCalculator.getRegularDescriptionText(patternFacade);
        Optional<String> optionalStringFQN = viewCalculator.getFullyQualifiedNameText(patternFacade);
        return optionalStringRegularName.orElseGet(optionalStringFQN::get);
    }

    public static Function<Integer, String> getFetchSemanticDescriptionFunction(ViewProperties viewProperties) {
        return semanticNid -> {
            StringBuilder sb = new StringBuilder();
            ViewCalculator viewCalculator = viewProperties.calculator();
            EntityHandle.get(semanticNid).ifSemantic(semanticEntity -> {
                EntityHandle.get(semanticEntity.referencedComponentNid()).ifPresent(referencedComponent -> {
                    sb.append(viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(referencedComponent)).append(" in ");
                }).ifAbsent(() -> {
                    LOG.error("Unable to find entity for referencedComponent '{}' for Semantic {}: ",
                            semanticEntity.referencedComponentNid(), semanticEntity);
                    sb.append(" missing referenced component " + semanticEntity.referencedComponentNid());
                });
                sb.append(ViewCalculatorUtils.retrieveDisplayName(semanticEntity.pattern(), viewProperties));
            }).ifStamp(stampEntity -> {
                sb.append(viewCalculator.getTextForStamp(stampEntity));
            }).ifConcept(conceptEntity -> {
                sb.append(viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(conceptEntity));
            }).ifPattern(patternEntity -> {
                sb.append(viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(patternEntity));
            }).ifAbsent(() -> LOG.error("Unable to find entity for nid: " + semanticNid));
            return sb.toString();
        };
    }
}