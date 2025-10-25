package dev.ikm.komet.framework.observable;

import dev.ikm.tinkar.component.FieldDefinition;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.PatternFacade;
import javafx.beans.property.*;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

public sealed class ObservableFeature<DT> implements Feature<DT>, Field<DT>
        permits ObservableField {
    private final SimpleObjectProperty<Field<DT>> fieldProperty = new SimpleObjectProperty<>();
    public final BooleanProperty refreshProperties = new SimpleBooleanProperty(false);
    private ObservableComponent containingComponent;
    private SimpleObjectProperty<DT> valueProperty = new SimpleObjectProperty<>();
    public final boolean writeOnEveryChange;
    public final FeatureKey featureKey;
    private final ReadOnlyProperty<? extends Feature<DT>> featureProperty = new ReadOnlyObjectWrapper<>(this).getReadOnlyProperty();

    public ObservableFeature(FeatureKey featureKey, Field<DT> attribute, ObservableComponent containingComponent, boolean writeOnEveryChange) {
        this.featureKey = featureKey;
        this.containingComponent = containingComponent;
        this.writeOnEveryChange = writeOnEveryChange;
        this.fieldProperty.set(attribute);
        if (attribute != null) {
            valueProperty.set(attribute.value());
        }
        valueProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                handleValueChange(newValue);
                fieldProperty.set(field().with(newValue));
            }
        });
        refreshProperties.addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                writeToDatabase(value());
            }
        });
    }

    public ObservableFeature(FeatureKey featureKey, Field<DT> attribute, ObservableComponent containingComponent) {
        this(featureKey, attribute, containingComponent, true);
    }

    private void handleValueChange(Object newValue) {
        if (writeOnEveryChange && !refreshProperties.get()) {
            writeToDatabase(newValue);
        }
    }

    @Override
    public int patternNid() {
        return field().patternNid();
    }

    @Override
    public DT value() {
        return valueProperty.getValue();
    }

    public ObjectProperty<DT> valueProperty() {
        return valueProperty;
    }

    public FieldRecord<DT> field() {
        return (FieldRecord<DT>) fieldProperty.get();
    }

    @Override
    public int indexInPattern() {
        return field().indexInPattern();
    }

    public ObservableComponent containingComponent() {
        return containingComponent;
    }

    @Override
    public PatternFacade pattern() {
        return EntityProxy.Pattern.make(field().patternNid());
    }

    @Override
    public ReadOnlyProperty<? extends Feature<DT>> featureProperty() {
        return this.featureProperty;
    }

    @Override
    public FieldDefinition fieldDefinition(StampCalculator stampCalculator) {
        PatternEntity<PatternEntityVersion> pattern = Entity.getFast(field().patternNid());
        return stampCalculator.latestPatternEntityVersion(pattern).get().fieldDefinitions().get(field().indexInPattern());
    }

    public ObjectProperty<Field<DT>> fieldProperty() {
        return fieldProperty;
    }

    public void writeToDatabase(Object newValue) {
        StampRecord stamp = Entity.getStamp(field().versionStampNid());
        // Get current version
        SemanticVersionRecord version = Entity.getVersionFast(field().nid(), field().versionStampNid());
        SemanticRecord semantic = Entity.getFast(field().nid());
        MutableList fieldsForNewVersion = Lists.mutable.of(version.fieldValues().toArray());
        fieldsForNewVersion.set(indexInPattern(), newValue);

        if (stamp.lastVersion().committed()) {

            // Create transaction
            Transaction t = Transaction.make();
            // newStamp already written to the entity store.
            StampEntity newStamp = t.getStampForEntities(stamp.state(), stamp.authorNid(), stamp.moduleNid(), stamp.pathNid(), version.entity());

            // Create a new version.
            SemanticVersionRecord newVersion = version.with().fieldValues(fieldsForNewVersion.toImmutable()).stampNid(newStamp.nid()).build();

            SemanticRecord analogue = semantic.with(newVersion).build();

            // Entity provider will broadcast the nid of the changed entity.
            Entity.provider().putEntity(analogue);
        } else {
            SemanticVersionRecord newVersion = version.withFieldValues(fieldsForNewVersion.toImmutable());
            // if a version with the same stamp as newVersion exists, that version will be removed
            // prior to adding the new version so you don't get duplicate versions with the same stamp.
            SemanticRecord analogue = semantic.with(newVersion).build();
            // Entity provider will broadcast the nid of the changed entity.
            Entity.provider().putEntity(analogue);
        }
    }

    @Override
    public final FeatureKey featureKey() {
        return featureKey;
    }

}
