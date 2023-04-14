package dev.ikm.komet.details.concept;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.controlsfx.control.PropertySheet;
import dev.ikm.komet.framework.view.SimpleEqualityBasedListProperty;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.ProxyFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author kec
 */
public class PropertySheetEntityListWrapper<T extends EntityFacade> implements PropertySheet.Item {

    private final ListProperty<T> conceptListProperty;
    private final String name;
    private SimpleEqualityBasedListProperty<T> allowedValuesListProperty;
    private SimpleBooleanProperty allowDuplicates = new SimpleBooleanProperty(false);

    public PropertySheetEntityListWrapper(ViewCalculator viewCalculator, ListProperty<T> conceptListProperty) {
        if (viewCalculator == null) {
            throw new NullPointerException("Manifold cannot be null");
        }
        if (conceptListProperty == null) {
            throw new NullPointerException("conceptListProperty cannot be null");
        }
        this.conceptListProperty = conceptListProperty;
        this.name = viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(ProxyFactory.fromXmlFragment(conceptListProperty.getName()));
    }


    @Override
    public Class<?> getType() {
        return null;
    }

    @Override
    public String getCategory() {
        return null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public ObservableList<T> getValue() {
        return conceptListProperty.get();
    }

    @Override
    public void setValue(Object value) {
        if (conceptListProperty.get() != value) {
            conceptListProperty.set((ObservableList<T>) value);
        }
    }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.of(conceptListProperty);
    }

    public Optional<SimpleEqualityBasedListProperty<? extends EntityFacade>> getConstraints() {
        return Optional.ofNullable(this.allowedValuesListProperty);
    }

    public void setConstraints(ConceptFacade[] constraints) {
        setConstraints(Arrays.asList(constraints));
    }

    public void setConstraints(List<? extends ConceptFacade> constraints) {
        setConstraints(FXCollections.observableArrayList(constraints));
    }

    public void setConstraints(ObservableList<? extends ConceptFacade> constraints) {
        this.allowedValuesListProperty = new SimpleEqualityBasedListProperty(constraints);
    }

    public boolean allowDuplicates() {
        return allowDuplicates.get();
    }

    public SimpleBooleanProperty allowDuplicatesProperty() {
        return allowDuplicates;
    }

    public void setAllowDuplicates(boolean allowDuplicates) {
        this.allowDuplicates.set(allowDuplicates);
    }
}
