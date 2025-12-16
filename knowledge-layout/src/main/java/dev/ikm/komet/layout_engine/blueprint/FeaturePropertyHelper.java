package dev.ikm.komet.layout_engine.blueprint;

import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.komet.framework.observable.FeatureKey;
import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.layout.KlViewLayoutLifecycle;
import dev.ikm.komet.layout.area.KlAreaForFeature;
import dev.ikm.komet.layout.area.KlFeaturePropertyForArea;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.binary.Encodable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Subscription;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.prefs.BackingStoreException;

public class FeaturePropertyHelper<F extends Feature<?>> implements KlFeaturePropertyForArea<F>, KlViewLayoutLifecycle {

    final ObjectProperty<ReadOnlyProperty<F>> featurePropertyWrapper = new SimpleObjectProperty<>();
    final Subscription featurePropertySubscription = featurePropertyWrapper.subscribe(this::propertyChanged);

    final AtomicReference<Subscription> transientSubscriptions = new AtomicReference<>(Subscription.EMPTY);
    final AtomicReference<StateAndContextBlueprint.LifecycleState> lifecycleState;
    private final KometPreferences preferences;
    private final BiConsumer<F, F> featureChangedCallback;

    public FeaturePropertyHelper(KometPreferences preferences, AtomicReference<StateAndContextBlueprint.LifecycleState> lifecycleState, BiConsumer<F, F> featureChangedCallback) {
        this.preferences = preferences;
        this.lifecycleState = lifecycleState;
        this.featureChangedCallback = featureChangedCallback;
    }

    public void setFeatureProperty(ReadOnlyProperty<F> featurePropertyWrapper) {
        this.featurePropertyWrapper.setValue(featurePropertyWrapper);
    }

    public void propertyChanged(ReadOnlyProperty<F> oldProperty,
                                ReadOnlyProperty<F> newProperty) {
        transientSubscriptions.getAndSet(Subscription.EMPTY).unsubscribe();
        transientSubscriptions.get().and(newProperty.subscribe(featureChangedCallback));
        transientSubscriptions.get().and(newProperty.subscribe(this::featureChanged));
        this.saveToPreferences();

        // NOTE: Logic to prevent calling the callback until after the preferences have been restored.
        if (lifecycleState.get().ordinal() >= StateAndContextBlueprint.LifecycleState.UNBOUND.ordinal()) {
            F oldValue = oldProperty != null ? oldProperty.getValue() : null;
            F newValue = newProperty != null ? newProperty.getValue() : null;
            featureChangedCallback.accept(oldValue, newValue);
        }

    }

    private void featureChanged() {
        saveToPreferences();
    }

    public void saveToPreferences() {
        getFeature().ifPresentOrElse(feature -> preferences.putObject(KlAreaForFeature.PreferenceKeys.AREA_FEATURE_KEY, feature.featureKey()),
                () -> preferences.putObject(KlAreaForFeature.PreferenceKeys.AREA_FEATURE_KEY, Encodable.nullEncodable));
    }

    public final void restoreFromPreferencesOrDefaults() {
        try {
            preferences.sync();
            preferences.getObject(KlAreaForFeature.PreferenceKeys.AREA_FEATURE_KEY).ifPresent(object -> {
                if (object instanceof FeatureKey featureKey) {
                    Feature<?> feature = ObservableEntity.get(featureKey.nid()).getFeature(featureKey);
                    setFeatureProperty((ReadOnlyProperty<F>) feature.featureProperty());
                }
            });
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
        // NOTE: Logic to prevent calling the callback until after the preferences have been restored.
        lifecycleState.set(StateAndContextBlueprint.LifecycleState.UNBOUND);
    }

    public Optional<F> getFeature() {
        if (featurePropertyWrapper.get() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(featurePropertyWrapper.get().getValue());
    }

    @Override
    public Property<ReadOnlyProperty<F>> featurePropertyWrapper() {
        return featurePropertyWrapper;
    }

    @Override
    public void knowledgeLayoutUnbind() {
        featurePropertySubscription.unsubscribe();
    }

    @Override
    public void knowledgeLayoutBind() {
        this.lifecycleState.set(StateAndContextBlueprint.LifecycleState.BOUND);
    }
}
