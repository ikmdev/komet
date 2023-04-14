package dev.ikm.komet.navigator.graph;

import dev.ikm.tinkar.common.id.IntIdSet;

import java.util.OptionalInt;

public interface MultiParentVertex {
    boolean isRoot();

    boolean isDefined();

    boolean isMultiParent();

    boolean isSecondaryParentOpened();

    int getConceptNid();

    IntIdSet getTypeNids();

    int getMultiParentDepth();

    OptionalInt getOptionalParentNid();

}
