package dev.ikm.komet.kview.controls;

import dev.ikm.komet.framework.view.ObservableEditCoordinate;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import javafx.collections.ObservableList;
import javafx.util.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditCoordinateOptionsUtils {

    private static final Logger LOG = LoggerFactory.getLogger(EditCoordinateOptionsUtils.class);
    private static final String DEFAULT_DESCRIPTION_STRING = Integer.toString(Integer.MAX_VALUE);

    public EditCoordinateOptionsUtils() {}

    private Subscription nodeSubscription;
    private Subscription viewSubscription;
    private Subscription optionSubscription;

    private boolean fromView;
    private boolean fromFilter;

    // pass changes from View (typically the nodeView.editCoordinate()) to EditCoordinateOptions (typically the defaultEditCoordinateOptions)
    public void subscribeEditCoordinateOptionsToView(EditCoordinateOptions editCoordinateOptions) {

        // remove previous subscriptions
        unsubscribeView();
        ObservableEditCoordinate observableEditCoordinateForOptionsProperty = editCoordinateOptions.observableEditCoordinateForOptionsProperty();
        EditCoordinateOptions.MainEditCoordinates mainCoordinates = editCoordinateOptions.getMainCoordinates();

        // When any coordinate property from the View changes, this subscribers will change immediately the F.O. coordinate property,
        // and the selectedOptions for the related Option, but also directly to the top F.O.
        // observableViewForFilter, so it is safe to add a listener just to this property to get notified of any change
        // in any of its coordinates (that is, options), and refresh the default F.O accordingly.
//        for (ObservableCoordinate<?> observableCoordinate : observableView.getCompositeCoordinates()) {
//            if (observableCoordinate instanceof ObservableNavigationCoordinate observableNavigationCoordinate) {
//
//            } else if (observableCoordinate instanceof ObservableStampCoordinate observableStampCoordinate) {
//
//            }
//        }
    }

    private void unsubscribeView() {
        if (viewSubscription != null) {
            viewSubscription.unsubscribe();
        }
        viewSubscription = Subscription.EMPTY;
    }

    // pass changes from FilterOptions to view (typically the nodeView)
    public void subscribeViewToFilterOptions(EditCoordinateOptions filterOptions) {
        // remove previous subscriptions
        unsubscribeNodeFilterOptions();

        // add Option to observableViewForFilterProperty subscriptions
        addOptionSubscriptions(filterOptions);
    }

    public void unsubscribeNodeFilterOptions() {
        if (nodeSubscription != null) {
            nodeSubscription.unsubscribe();
        }
        nodeSubscription = Subscription.EMPTY;
    }

    // Subscribe Option's selectedOptions observable list to notify F.O. observableViewForFilter related coordinates properties
    private void addOptionSubscriptions(EditCoordinateOptions filterOptions) {
        LOG.debug(">>>>>>>> D) adding subscriptions for options: {}",filterOptions);
        // remove previous subscriptions
        unsubscribeOptions();

        EditCoordinateOptions.MainEditCoordinates mainCoordinates = filterOptions.getMainCoordinates();

        // AUTHOR FOR CHANGE
        optionSubscription = optionSubscription.and(mainCoordinates.getAuthorForChange().selectedOptions().subscribe(() ->
                updateAuthorForChangeProperty(filterOptions)));
        updateAuthorForChangeProperty(filterOptions);
        optionSubscription = optionSubscription.and(mainCoordinates.getAuthorForChange().availableOptions().subscribe(() ->
                LOG.debug(">>>>>>>> D.1) changing getAuthorForChange() available options.")));

        // default Module
        optionSubscription = optionSubscription.and(mainCoordinates.getDefaultModule().selectedOptions().subscribe(() ->
                updateDefaultModuleProperty(filterOptions)));
        updateDefaultModuleProperty(filterOptions);
        optionSubscription = optionSubscription.and(mainCoordinates.getDefaultModule().availableOptions().subscribe(() ->
                LOG.debug(">>>>>>>> D.2) Changing getDefaultModule() available options.")));

        // destination Module
        optionSubscription = optionSubscription.and(mainCoordinates.getDestinationModule().selectedOptions().subscribe(() ->
                updateDestinationModuleProperty(filterOptions)));
        updateDestinationModuleProperty(filterOptions);
        optionSubscription = optionSubscription.and(mainCoordinates.getDestinationModule().availableOptions().subscribe(() ->
                LOG.debug(">>>>>>>> D.3) Changing getDestinationModule() available options.")));

        // destination Module
        // default PATH
        optionSubscription = optionSubscription.and(mainCoordinates.getDefaultPath().selectedOptions().subscribe(() ->
                updateDefaultPathProperty(filterOptions)));
        updateDefaultPathProperty(filterOptions);
        optionSubscription = optionSubscription.and(mainCoordinates.getDefaultPath().availableOptions().subscribe(() ->
                LOG.debug(">>>>>>>> D.4) Changing getDefaultPath() available options.")));

        // promotion PATH
        optionSubscription = optionSubscription.and(mainCoordinates.getPromotionPath().selectedOptions().subscribe(() ->
                updatePromotionPathProperty(filterOptions)));
        updatePromotionPathProperty(filterOptions);
        optionSubscription = optionSubscription.and(mainCoordinates.getPromotionPath().availableOptions().subscribe(() ->
                LOG.debug(">>>>>>>> D.5) Changing getPromotionPath() available options.")));
    }

    private void unsubscribeOptions() {
        if (optionSubscription != null) {
            optionSubscription.unsubscribe();
        }
        optionSubscription = Subscription.EMPTY;
    }

    private void updateAuthorForChangeProperty(EditCoordinateOptions filterOptions) {
        if (fromView) {
            return;
        }
        ObservableList<ConceptFacade> selectedOptions = filterOptions.getMainCoordinates().getAuthorForChange().selectedOptions();
        if (!selectedOptions.isEmpty()) {
            ConceptFacade conceptFacade = selectedOptions.getFirst();
            fromFilter = true;
            // TODO: Please verify and test the authorForChangesProperty is the correct way to set.
            filterOptions.observableEditCoordinateForOptionsProperty().authorForChangesProperty().set(conceptFacade);
            fromFilter = false;
        }
    }
    private void updateDefaultModuleProperty(EditCoordinateOptions filterOptions) {
        if (fromView) {
            return;
        }
        ObservableList<ConceptFacade> selectedOptions = filterOptions.getMainCoordinates().getDefaultModule().selectedOptions();
        if (!selectedOptions.isEmpty()) {
            ConceptFacade conceptFacade = selectedOptions.getFirst();
            fromFilter = true;
            filterOptions.observableEditCoordinateForOptionsProperty().defaultModuleProperty().set(conceptFacade);
            fromFilter = false;
        }
    }
    private void updateDestinationModuleProperty(EditCoordinateOptions filterOptions) {
        if (fromView) {
            return;
        }
        ObservableList<ConceptFacade> selectedOptions = filterOptions.getMainCoordinates().getDestinationModule().selectedOptions();
        if (!selectedOptions.isEmpty()) {
            ConceptFacade conceptFacade = selectedOptions.getFirst();
            fromFilter = true;
            filterOptions.observableEditCoordinateForOptionsProperty().destinationModuleProperty().set(conceptFacade);
            fromFilter = false;
        }
    }


    private void updateDefaultPathProperty(EditCoordinateOptions filterOptions) {
        if (fromView) {
            return;
        }
        ObservableList<ConceptFacade> selectedOptions = filterOptions.getMainCoordinates().getDefaultPath().selectedOptions();
        if (!selectedOptions.isEmpty()) {
            ConceptFacade conceptFacade = selectedOptions.getFirst();
            fromFilter = true;
            filterOptions.observableEditCoordinateForOptionsProperty().defaultPathProperty().set(conceptFacade);
            fromFilter = false;
        }
    }
    private void updatePromotionPathProperty(EditCoordinateOptions filterOptions) {
        if (fromView) {
            return;
        }
        ObservableList<ConceptFacade> selectedOptions = filterOptions.getMainCoordinates().getPromotionPath().selectedOptions();
        if (!selectedOptions.isEmpty()) {
            ConceptFacade conceptFacade = selectedOptions.getFirst();
            fromFilter = true;
            filterOptions.observableEditCoordinateForOptionsProperty().promotionPathProperty().set(conceptFacade);
            fromFilter = false;
        }
    }

    public static <T> String getDescription(ViewCalculator viewCalculator, T t) {
        return switch (t) {
            case String value -> value;
            case State value -> viewCalculator == null ?
                    Entity.getFast(value.nid()).description() :
                    getDescriptionTextOrNid(viewCalculator, value.nid());
            case Long value -> String.valueOf(value);
            case EntityFacade value -> {
                if (viewCalculator != null) {
                    yield getDescriptionTextOrNid(viewCalculator, value);
                }
                yield value.description();
            }

            default -> throw new RuntimeException("Unsupported type: " + t.getClass().getName());
        };
    }

    public static String getDescriptionTextOrNid(ViewCalculator viewCalculator, int nid) {
        try {
            return viewCalculator.getDescriptionTextOrNid(nid);
        } catch (Exception e) {
            LOG.error("Exception occurred", e);
            return DEFAULT_DESCRIPTION_STRING;
        }
    }

    public static String getDescriptionTextOrNid(ViewCalculator viewCalculator, EntityFacade entityFacade) {
        try {
            return viewCalculator.getDescriptionTextOrNid(entityFacade);
        } catch (Exception e) {
            LOG.error("Exception occurred", e);
            return DEFAULT_DESCRIPTION_STRING;
        }
    }
}
