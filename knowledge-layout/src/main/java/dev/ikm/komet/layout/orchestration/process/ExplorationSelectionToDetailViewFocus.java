package dev.ikm.komet.layout.orchestration.process;

import dev.ikm.komet.layout.event.KlPerformanceEvent;
import dev.ikm.komet.layout.event.KlRequestEvent;
import dev.ikm.komet.layout.orchestration.KlEventOrchestrator;
import org.eclipse.collections.api.list.ImmutableList;

public class ExplorationSelectionToDetailViewFocus implements KlEventOrchestrator {
    @Override
    public ImmutableList<KlRequestEvent> orchestrate(KlPerformanceEvent klPerformanceEvent) {

        // Identify detail views listening to the exploration view...
        /*
            KlComponents register themselves and give themselves a "context"? Use PublicIdStringKey<> for contexts...

            Default behavior within a context, then a "bridge" or "filter" between contexts...

            A hierarchy of contexts...

            Field context (STAMP entities have fields, so Concepts have fields. Patterns and Semantics have fields)
            Field Definition Context

            Version Context

            Component Context

            Semantic Context
            Pattern Context
            Concept Context

            View Coordinate Context
            Language Coordinate Context
            Navigation Coordinate Context
            Stamp Coordinate Context

            Edit Coordinate Context


         */

        // In same window (context?)

        // In other window (context?)

        /*

Asynchronous events?

IKM events:
    Initiate event on Fx App thread
    Dispatch event on virtual thread
    Use structured concurrency


Outgoing:
        Single-click <selectedAttribute>|<context>:
        Double-click <selectedAttribute>|<context>:
        Drag <selectedAttribute>:
        Context-actions-request <selectedAttribute>|<context>:
        Action-select <selectedAttribute>|<context>:

Incoming:
        Drop allow|accept <selectedAttribute>
        Context action list, responding to event ID, <component field>|<component focus>
        Context suggestions:
            semantics to add|change
                axioms to add|change,
                descriptions to add|change,
                membership to add|change

         Component actions:
            Change field
            Commit version
            Cancel version

        Layout actions:
            Change Coordinate

Each component pane is a context? Takes the PublicIdStringKey from the component it contains...
So component context it will change when the component changes.


Selection list: ImmutableList<EntitySelection>

Listening:

Filtering (at context)

         */

        return null;
    }
}
