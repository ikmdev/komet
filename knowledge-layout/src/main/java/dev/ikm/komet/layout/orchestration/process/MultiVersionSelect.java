package dev.ikm.komet.layout.orchestration.process;

import dev.ikm.komet.layout.event.KlPerformanceEvent;
import dev.ikm.komet.layout.event.KlRequestEvent;
import dev.ikm.komet.layout.orchestration.KlEventOrchestrator;
import org.eclipse.collections.api.list.ImmutableList;

public class MultiVersionSelect implements KlEventOrchestrator {
    @Override
    public ImmutableList<KlRequestEvent> orchestrate(KlPerformanceEvent klPerformanceEvent) {
        return null;
    }
}
