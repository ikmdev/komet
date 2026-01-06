package dev.ikm.komet.layout_engine.layout;

import dev.ikm.komet.layout.KnowledgeLayout;
import dev.ikm.komet.layout.area.GridStep;

/**
 * The RowIncrementLayoutComputer class is a concrete implementation of the
 * GridIncrementLayoutComputer. It is specifically configured to increment
 * the layout along the rows of a grid.
 * <p>
 * This class overrides the `step` method to define the grid increment behavior,
 * ensuring that all grid manipulations within this implementation are row-based.
 * <p>
 * This class is used in scenarios requiring layout adjustments aligned
 * with rows. It interacts with the grid state manager to update positions
 * for layout computations involving row stepping.
 */
public class RowIncrementLayoutComputer extends GridIncrementLayoutComputer {

    private RowIncrementLayoutComputer(KnowledgeLayout masterLayout) {
        super(masterLayout);
    }

    @Override
    protected GridStep step() {
        return GridStep.ROW;
    }

    public static RowIncrementLayoutComputer create(KnowledgeLayout masterLayout) {
        return new RowIncrementLayoutComputer(masterLayout);
    }

}