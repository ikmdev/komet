package dev.ikm.komet.framework.preferences;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import org.controlsfx.control.PropertySheet;
import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.coordinate.view.ViewCoordinate;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.ProxyFactory;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author kec
 */
public class PropertySheetItemConceptWrapper implements ConceptFacade, PropertySheet.Item, PreferenceChanged {

    private final ViewCoordinate viewCoordinate;
    private final String name;
    private final SimpleObjectProperty<ConceptFacade> conceptProperty;
    private final SimpleBooleanProperty allowSearchProperty = new SimpleBooleanProperty(this, "allow search", true);
    private final SimpleBooleanProperty allowHistoryProperty = new SimpleBooleanProperty(this, "allow history", true);
    private final BooleanProperty changedProperty = new SimpleBooleanProperty(this, "changed", false);
    private ObservableList<ConceptFacade> allowedValues = FXCollections.observableArrayList();

    public PropertySheetItemConceptWrapper(ViewCoordinate viewCoordinate,
                                           ObjectProperty<? extends ConceptFacade> conceptProperty, int... allowedValues) {
        this(viewCoordinate,
                viewCoordinate.languageCalculator().getPreferredDescriptionTextWithFallbackOrNid(ProxyFactory.fromXmlFragment(conceptProperty.getName())),
                conceptProperty, allowedValues);
    }

    public PropertySheetItemConceptWrapper(ViewCoordinate viewCoordinate, String name,
                                           ObjectProperty<? extends ConceptFacade> conceptProperty, int... allowedValues) {
        this.viewCoordinate = viewCoordinate;
        this.name = name;
        this.conceptProperty = (SimpleObjectProperty<ConceptFacade>) conceptProperty;
        if (allowedValues.length > 0) {
            this.conceptProperty.set(Entity.getFast(allowedValues[0]));
        }
        for (int allowedNid : allowedValues) {
            this.allowedValues.add(Entity.getFast(allowedNid));
        }
        bindProperties();

    }

    private void bindProperties() {
        this.conceptProperty.addListener((observable, oldValue, newValue) -> {
            setValue(newValue);
            changedProperty.setValue(true);
        });
        this.allowHistoryProperty.addListener((observable, oldValue, newValue) -> {
            changedProperty.setValue(true);
        });
        this.allowSearchProperty.addListener((observable, oldValue, newValue) -> {
            changedProperty.setValue(true);
        });
        this.allowedValues.addListener(new WeakListChangeListener<>((ListChangeListener.Change<? extends ConceptFacade> c) -> {
            changedProperty.setValue(true);
        }));
    }

    public PropertySheetItemConceptWrapper(ViewCoordinate viewCoordinate, String name,
                                           ObjectProperty<? extends ConceptFacade> conceptProperty) {
        this(viewCoordinate, name, conceptProperty, (ConceptFacade[]) new ConceptFacade[0]);
    }

    public PropertySheetItemConceptWrapper(ViewCoordinate viewCoordinate, String name,
                                           ObjectProperty<? extends ConceptFacade> conceptProperty, ConceptFacade... allowedValues) {
        this.viewCoordinate = viewCoordinate;
        this.name = name;
        this.conceptProperty = (SimpleObjectProperty<ConceptFacade>) conceptProperty;
        if (allowedValues.length > 0) {
            this.conceptProperty.set(allowedValues[0]);
        }
        this.allowedValues.addAll(Arrays.asList(allowedValues));
        bindProperties();
    }

    @Override
    public PublicId publicId() {
        return this.conceptProperty.get().publicId();
    }

    @Override
    public int nid() {
        return this.conceptProperty.get().nid();
    }

    @Override
    public BooleanProperty changedProperty() {
        return changedProperty;
    }

    public boolean allowSearch() {
        return allowSearchProperty.get();
    }

    public void setAllowSearch(boolean allowSearch) {
        this.allowSearchProperty.set(allowSearch);
    }

    public SimpleBooleanProperty allowSearchProperty() {
        return allowSearchProperty;
    }

    public SimpleBooleanProperty allowHistoryProperty() {
        return allowHistoryProperty;
    }

    public boolean allowHistory() {
        return allowHistoryProperty.get();
    }

    public void setAllowHistory(boolean allowHistory) {
        this.allowHistoryProperty.set(allowHistory);
    }

    public String getFullyQualifiedName() {
        return this.viewCoordinate.languageCalculator().getFullyQualifiedDescriptionTextWithFallbackOrNid(conceptProperty.get());
    }

    public Optional<String> getRegularName() {
        return Optional.of(viewCoordinate.languageCalculator().getPreferredDescriptionTextWithFallbackOrNid(conceptProperty.get()));
    }

    @Override
    public Class<?> getType() {
        return Entity.class;
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
        return "Select the proper concept value for the version you wish to create. ";
    }

    @Override
    public ConceptFacade getValue() {
        return this.conceptProperty.getValue();
    }

    @Override
    public void setValue(Object value) {
        try {
            // Concept sequence property may throw a runtime exception if it cannot be changed
            this.conceptProperty.setValue((ConceptFacade) value);
        } catch (RuntimeException ex) {
            AlertStreams.getRoot().dispatch(AlertObject.makeError(ex));
        }
    }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.of(this.conceptProperty);
    }

    public ObservableList<ConceptFacade> getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(ObservableList<ConceptFacade> allowedValues) {
        this.allowedValues = allowedValues;
        this.allowedValues.addListener((ListChangeListener.Change<? extends ConceptFacade> c) -> {
            changedProperty.setValue(true);
        });
    }

    public void setDefaultValue(Object value) {
        setValue(value);
    }

    @Override
    public String toString() {
        return "Property sheet item for "
                + viewCoordinate.languageCalculator().getPreferredDescriptionTextWithFallbackOrNid(getSpecification());
    }

    public EntityProxy.Concept getSpecification() {
        return ProxyFactory.fromXmlFragment(this.conceptProperty.getName());
    }

}
