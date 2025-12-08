package dev.ikm.komet.layout_engine.layout;

import dev.ikm.komet.layout.KnowledgeLayout;
import dev.ikm.komet.layout.area.GridStep;

/**
 * The ColumnIncrementLayoutComputer class is a specific implementation of the
 * abstract GridIncrementLayoutComputer class. It is responsible for managing
 * grid layouts with a column-wise stepping strategy.
 * <p>
 * This class overrides the abstract step method to return GridStep.COLUMN,
 * ensuring that the grid layout progresses incrementally along the columns
 * during computation.
 * <p>
 * Responsibilities:
 * <p> - Defines column-wise stepping behavior for grid layout management.
 * <p> - Leverages the functionality provided by GridIncrementLayoutComputer
 *   for layout computation.
 * <p>
 * Constructor Details:
 * <p> - Accepts a KlWidget instance and associates this layout computer with it
 *   by calling the superclass constructor.
 */
public class ColumnIncrementLayoutComputer extends GridIncrementLayoutComputer {

    private ColumnIncrementLayoutComputer(KnowledgeLayout masterLayout) {
        super(masterLayout);
    }

    @Override
    protected GridStep step() {
        return GridStep.COLUMN;
    }

    public static ColumnIncrementLayoutComputer create(KnowledgeLayout masterLayout) {
        return new ColumnIncrementLayoutComputer(masterLayout);
    }

}