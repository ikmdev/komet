package dev.ikm.komet.layout.area;

/**
 * The GridStep enum represents different types of axial progressions
 * that can be used within a grid layout structure. Each constant defines
 * a specific step or adjustment that can be applied to grid navigation
 * or arrangement.
 * <ul>
 * <li>ROW - Represents a step or progression across rows in the grid.</li>
 * <li>COLUMN - Represents a step or progression across columns in the grid.</li>
 * <li>ROW_AND_COLUMN - Represents a simultaneous step across both rows and columns.</li>
 * <li>RESET - Represents a reset or reinitialization of grid navigation or layout.</li>
 * </ul>
 */
public enum GridStep {
    ROW, COLUMN, ROW_AND_COLUMN;
}
