package dev.ikm.komet.framework.observable;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityFacade;

public final class ObservablePatternVersion
        extends ObservableVersion<PatternVersionRecord>
        implements PatternEntityVersion {
    final SimpleObjectProperty<EntityFacade> purposeProperty = new SimpleObjectProperty<>(this, "Pattern purpose");
    final SimpleObjectProperty<EntityFacade> meaningProperty = new SimpleObjectProperty<>(this, "Pattern meaning");
    final ImmutableList<ObservableFieldDefinition> observableFieldDefinitions;

    ObservablePatternVersion(PatternVersionRecord patternVersionRecord) {
        super(patternVersionRecord);
        purposeProperty.set(Entity.getFast(patternVersionRecord.semanticPurposeNid()));
        purposeProperty.addListener(this::purposeChanged);
        meaningProperty.set(Entity.getFast(patternVersionRecord.semanticMeaningNid()));
        meaningProperty.addListener(this::meaningChanged);
        MutableList<ObservableFieldDefinition> mutableFieldDefinitions = Lists.mutable.ofInitialCapacity(patternVersionRecord.fieldDefinitions().size());
        for (FieldDefinitionRecord fieldDefinition : patternVersionRecord.fieldDefinitions()) {
            mutableFieldDefinitions.add(new ObservableFieldDefinition(fieldDefinition));
        }
        this.observableFieldDefinitions = mutableFieldDefinitions.toImmutable();
    }

    private void purposeChanged(ObservableValue<? extends EntityFacade> observableValue, EntityFacade oldValue, EntityFacade newValue) {
        handleChange(FIELDS.PURPOSE, observableValue, newValue);
    }

    private void meaningChanged(ObservableValue<? extends EntityFacade> observableValue, EntityFacade oldValue, EntityFacade newValue) {
        handleChange(FIELDS.MEANING, observableValue, newValue);
    }

    private void handleChange(FIELDS field, ObservableValue<? extends EntityFacade> observableValue, EntityFacade newValue) {
        StampRecord stamp = Entity.getStamp(getVersionRecord().stampNid());
        PatternVersionRecord version = getVersionRecord();
        if (stamp.lastVersion().committed()) {

            // Create transaction
            Transaction t = Transaction.make();
            // newStamp already written to the entity store.
            StampEntity newStamp = t.getStampForEntities(stamp.state(), stamp.authorNid(), stamp.moduleNid(), stamp.pathNid(), version.entity());

            // Create new version...
            PatternVersionRecord newVersion = switch (field) {
                case MEANING -> version.with()
                        .semanticMeaningNid(meaningProperty.get().nid())
                        .stampNid(newStamp.nid()).build();
                case PURPOSE -> version.with()
                        .semanticPurposeNid(purposeProperty.get().nid())
                        .stampNid(newStamp.nid()).build();
            };

            PatternRecord analogue = newVersion.chronology().with(newVersion).build();

            // Entity provider will broadcast the nid of the changed entity.
            Entity.provider().putEntity(analogue);
        } else {
            PatternVersionRecord newVersion = switch (field) {
                case MEANING -> version.withSemanticMeaningNid(meaningProperty.get().nid());
                case PURPOSE -> version.withSemanticPurposeNid(purposeProperty.get().nid());
            };

            PatternRecord analogue = newVersion.chronology().with(newVersion).build();

            // Entity provider will broadcast the nid of the changed entity.
            Entity.provider().putEntity(analogue);
        }
    }

    public SimpleObjectProperty<EntityFacade> purposeProperty() {
        return purposeProperty;
    }

    public SimpleObjectProperty<EntityFacade> meaningProperty() {
        return meaningProperty;
    }

    @Override
    protected PatternVersionRecord withStampNid(int stampNid) {
        return version().withStampNid(stampNid);
    }

    @Override
    public PatternVersionRecord getVersionRecord() {
        return version();
    }

    @Override
    public ImmutableList<ObservableFieldDefinition> fieldDefinitions() {
        return this.observableFieldDefinitions;
    }

    @Override
    public int semanticPurposeNid() {
        return version().semanticPurposeNid();
    }

    @Override
    public int semanticMeaningNid() {
        return version().semanticMeaningNid();
    }

    enum FIELDS {PURPOSE, MEANING}
}
