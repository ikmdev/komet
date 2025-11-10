package dev.ikm.komet.layout.area;

import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.KlParent;
import javafx.scene.layout.Region;

public non-sealed interface KlAreaForAssociation<FX extends Region> extends KlArea<FX>, KlParent<FX> {
    // Association area walks up nodes until it

    // Chronology panels need to put the
    // ObjectProperty<OE> chronologyProperty();
    //
     // ObjectProperty<OV> versionProperty();

    // Stamps are associated with versions
    // Semantics are associated with components
    // Walk up the node hierarchy until an association property is found?

    /*
    How to Show Descriptions for Component

    Walk up Fx node graph to find property chronologyProperty, then display the description according to coded rules
    in the area.

    How to show Stamp for version

    Walk up the Fx node graph to find property versionProperty, then display the stamp according to the coded rules
    in the area.

    How to display dialect for description walk up the Fx node graph to find chronologyProperty, if it is a type of
    description semantic, display the dialect according to the coded rules in the area. If not, either hide, go blank,
    or display an error message according to the rules in the area.

    Where should the properties be put?
    They should be put on the border pane.
    Standard structure:

    KlArea:
        Border Pane: properties
            center: GridPane
            top: Context controls
            right: Context controls
            bottom: Context controls

     */
/*
We need to link one area to a property on another area. The patternId and pattern index uniquely defines all field
categories, and add the componentId to uniquely identify an instance of a field.


 */


    record LinkageKey(int patternId, int meaningId, int dataType) {

    }

    // A grid pane may have more than one component: A concept, a description, a dialect. Then what?

    // Every area has a published set of properties.
    //     findFirstField(Predicate<FieldDefinition> test)
    //   Optional<ObservableField<DT>> findField(Predicate<FieldDefinition> test)

    // If we create a pattern for each type of area that defines properties specific to an area:
    // Component Area
        // Focus
        // Focus Selected Versions

    // Search Siblings
    // Search parents
    // Search children

    // Panel with Concept, Description, Dialect, Stated Definition, Inferred Definition

    // Search for meaning and purpose.

    // Chronology Grid Area Pattern
    //    defines a field: purpose: Area Linkage, meaning: Grid Focus, datatype: Component chronology;
    //    defines a field: LinkageKey(ChronologyGridPattern, PropertyKeyForListItem(PropertyCategory.PATTERN_FIELD_DEFINITION_LIST_ELEMENT, SELECTED_VERSIONS_INDEX));

    // Chronology Grid Area
    //   Chronology Grid Area defines a field: LinkageKey(ChronologyGridPattern, PropertyKeyForListItem(PropertyCategory.PATTERN_FIELD_DEFINITION_LIST_ELEMENT, FOCUS_INDEX));
    //   Chronology Grid Area defines a field: LinkageKey(ChronologyGridPattern, PropertyKeyForListItem(PropertyCategory.PATTERN_FIELD_DEFINITION_LIST_ELEMENT, SELECTED_VERSIONS_INDEX));

    // Multiversion Grid (in Chronology Grid Area)
    // Grid searches for LinkageKey(ComponentPattern, FOCUS_VERSION_SELECTION, PropertyKey(PropertyCategory.COMPONENT_VERSIONS_LIST));
    // Optional<ObservableField<DT>> findField(Predicate<FieldDefinition> test)

    // Descriptions Grid (in Chronology Grid area)
    // Grid searches for LinkageKey(ComponentPattern, GRID_FOCUS, PropertyKey(PropertyCategory.COMPONENT_CHRONOLOGY_FIELD));
    // Description sets: Associated component with pattern description
    // Dialect sets: Associated component with pattern dialect
    // Stated definition sets: Associated component with stated definition pattern
    // Inferred definition sets: Associated component with inferred definition pattern

    non-sealed interface Factory<FX extends Region, KL extends KlAreaForAssociation<FX>> extends KlArea.Factory<FX, KL> {

    }
}
