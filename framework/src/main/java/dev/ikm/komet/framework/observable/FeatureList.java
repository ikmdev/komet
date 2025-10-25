package dev.ikm.komet.framework.observable;

import dev.ikm.tinkar.terms.PatternFacade;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.collections.ModifiableObservableListBase;
import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.List;

public final class FeatureList<F extends Feature<?>>
        extends ModifiableObservableListBase<F>
        implements Feature<FeatureList<F>> {

    private final List<F> backingList;
    private final FeatureKey featureKey;
    private final PatternFacade patternFacade;
    private final int indexInPattern;
    private final ObservableComponent containingComponent;
    private final ReadOnlyProperty<FeatureList<F>> featureProperty = new ReadOnlyObjectWrapper<>(this).getReadOnlyProperty();

    public static <F extends Feature<?>> FeatureList<F> makeEmptyList(FeatureKey featureKey, PatternFacade patternFacade, int indexInPattern, ObservableComponent containingComponent) {
        return new FeatureList<>(featureKey, patternFacade, indexInPattern, containingComponent);
    }

    public static <F extends Feature<?>> FeatureList<F> makeWithBackingList(List<F> backingList, FeatureKey featureKey, PatternFacade patternFacade, int indexInPattern, ObservableComponent containingComponent) {
        return new FeatureList<>(backingList, featureKey, patternFacade, indexInPattern, containingComponent);
    }

    public static <F extends Feature<?>> FeatureList<F> makeWithBackingList(ImmutableList<F> backingList, FeatureKey featureKey, PatternFacade patternFacade, int indexInPattern, ObservableComponent containingComponent) {
        return new FeatureList(backingList, featureKey, patternFacade, indexInPattern, containingComponent);
    }

    private FeatureList(FeatureKey featureKey, PatternFacade patternFacade, int indexInPattern, ObservableComponent containingComponent) {
        this.featureKey = featureKey;
        this.patternFacade = patternFacade;
        this.indexInPattern = indexInPattern;
        this.containingComponent = containingComponent;
        this.backingList = Lists.mutable.empty();
    }

    private FeatureList(List<F> backingList, FeatureKey featureKey, PatternFacade patternFacade, int indexInPattern, ObservableComponent containingComponent) {
        this.featureKey = featureKey;
        this.patternFacade = patternFacade;
        this.indexInPattern = indexInPattern;
        this.backingList = backingList;
        this.containingComponent = containingComponent;
    }

    private FeatureList(ImmutableList<F> backingList, FeatureKey featureKey, PatternFacade patternFacade, int indexInPattern, ObservableComponent containingComponent) {
        this.featureKey = featureKey;
        this.patternFacade = patternFacade;
        this.indexInPattern = indexInPattern;
        this.backingList = backingList.castToList();
        this.containingComponent = containingComponent;
    }

    @Override
    public FeatureKey featureKey() {
        return featureKey;
    }

    public F get(int index) {
        return backingList.get(index);
    }

    public int size() {
        return backingList.size();
    }

    protected void doAdd(int index, F element) {
        backingList.add(index, element);
    }

    protected F doSet(int index, F element) {
        return backingList.set(index, element);
    }

    protected F doRemove(int index) {
        return backingList.remove(index);
    }

    @Override
    public ObservableComponent containingComponent() {
        return this.containingComponent;
    }

    public boolean setAll(ImmutableCollection col) {
        return setAll(col.castToCollection());
    }

    public boolean addAll(ImmutableCollection c) {
        return super.addAll(c.castToCollection());
    }

    @Override
    public int patternNid() {
        return this.patternFacade.nid();
    }

    @Override
    public int indexInPattern() {
        return indexInPattern;
    }

    public FeatureList value() {
        return this;
    }

    @Override
    public ReadOnlyProperty<FeatureList<F>> featureProperty() {
        return this.featureProperty;
    }
}
