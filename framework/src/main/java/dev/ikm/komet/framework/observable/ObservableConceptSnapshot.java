package dev.ikm.komet.framework.observable;

import org.eclipse.collections.api.list.ImmutableList;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptVersionRecord;

import java.util.Comparator;
import java.util.function.Predicate;

public class ObservableConceptSnapshot
        extends ObservableEntitySnapshot<ObservableConcept, ObservableConceptVersion, ConceptVersionRecord> {

    public ObservableConceptSnapshot(ViewCalculator viewCalculator, ObservableConcept entity) {
        super(viewCalculator, entity);
    }

    @Override
    public ImmutableList<ObservableConceptVersion> getProcessedVersions() {
        return super.getProcessedVersions();
    }

    @Override
    public void filterProcessedVersions(Predicate<ObservableConceptVersion> filter) {
        super.filterProcessedVersions(filter);
    }

    @Override
    public void sortProcessedVersions(Comparator<ObservableConceptVersion> comparator) {
        super.sortProcessedVersions(comparator);
    }

    @Override
    public ObservableConcept observableEntity() {
        return super.observableEntity();
    }

    @Override
    public ImmutableList<ObservableConceptVersion> getUncommittedVersions() {
        return super.getUncommittedVersions();
    }

    @Override
    public ImmutableList<ObservableConceptVersion> getHistoricVersions() {
        return super.getHistoricVersions();
    }

    @Override
    public Latest<ObservableConceptVersion> getLatestVersion() {
        return super.getLatestVersion();
    }
}
