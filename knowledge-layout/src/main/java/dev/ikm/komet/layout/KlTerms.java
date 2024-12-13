package dev.ikm.komet.layout;

import dev.ikm.tinkar.terms.EntityProxy;

import java.util.UUID;

public class KlTerms {
    public static final EntityProxy.Concept CHANGE_VIEW = EntityProxy.Concept.make( "Change View", UUID.fromString("fe16b994-a15f-4fbb-8e89-edd6f0b57717"));
    public static final EntityProxy.Concept CHANGE_FOCUS = EntityProxy.Concept.make( "Change Focus", UUID.fromString("1b39f53a-f32a-4e5b-b487-abf9d9dd81ad"));

    public static final EntityProxy.Concept COMPONENT_CHANGED = EntityProxy.Concept.make( "Component Changed", UUID.fromString("b1f2d4ab-8ac1-4d0b-9de4-61bf70be907e"));
    public static final EntityProxy.Concept FIELD_CHANGED = EntityProxy.Concept.make( "Field Changed", UUID.fromString("d5ecc848-b25d-451e-adc6-08b895363a02"));

    public static final EntityProxy.Concept COMMITTED_CHANGE = EntityProxy.Concept.make( "Commited Change", UUID.fromString("6b9c9f6f-558d-497a-b638-91ef029b5158"));
    public static final EntityProxy.Concept UNCOMMITTED_CHANGE = EntityProxy.Concept.make( "Uncommitted Change", UUID.fromString("5265e678-99de-4707-8522-658fd2d60b76"));

    public static final EntityProxy.Concept PATTERN_MEANING_ATTRIBUTE = EntityProxy.Concept.make("Pattern meaning attribute", UUID.fromString("b39eb788-a8ba-4a82-8d29-884feab1f323"));
    public static final EntityProxy.Concept PATTERN_PURPOSE_ATTRIBUTE = EntityProxy.Concept.make("Pattern purpose attribute", UUID.fromString("7e718b8b-5017-40b3-8631-4b190fe139ac"));
    public static final EntityProxy.Concept FIELD_MEANING = EntityProxy.Concept.make("Field meaning", UUID.fromString("728fa99a-4392-45b8-bfaf-084171cbed08"));
    public static final EntityProxy.Concept FIELD_PURPOSE = EntityProxy.Concept.make("Field purpose", UUID.fromString("6da9d3ab-35dd-4046-a417-3473e8f7043b"));
    public static final EntityProxy.Concept FIELD_DATA_TYPE = EntityProxy.Concept.make("Field data type", UUID.fromString("acc001dd-3430-42fb-ad6c-bf787a0211b9"));
    public static final EntityProxy.Concept FIELD_VALUE = EntityProxy.Concept.make("Field value", UUID.fromString("7249ce90-c93d-45b6-91eb-7b3674440a5e"));

}
