package dev.ikm.komet.kview.mvvm.view.common;

import dev.ikm.tinkar.terms.EntityProxy;

import java.util.UUID;

/**
 * Constants used to render different pattern instances of different types
 */
public final class PatternConstants {

    public static final EntityProxy IDENTIFIER_PATTERN_PROXY = EntityProxy.make("Identifier pattern",
            new UUID[] {UUID.fromString("65dd3f06-71ff-5650-8fb3-ce4019e50642")});

    public static final EntityProxy INFERRED_DEFINITION_PATTERN_PROXY = EntityProxy.make("Inferred definition pattern",
            new UUID[] {UUID.fromString("9f011812-15c9-5b1b-85f8-bb262bc1b2a2")});

    public static final EntityProxy INFERRED_NAVIGATION_PATTERN_PROXY = EntityProxy.make("Inferred navigation pattern",
            new UUID[] {UUID.fromString("a53cc42d-c07e-5934-96b3-2ede3264474e")});

    public static final EntityProxy PATH_MEMBERSHIP_PROXY = EntityProxy.make("Path membership",
            new UUID[] {UUID.fromString("add1db57-72fe-53c8-a528-1614bda20ec6")});

    public static final EntityProxy STATED_DEFINITION_PATTERN_PROXY = EntityProxy.make("Stated definition pattern",
            new UUID[] {UUID.fromString("e813eb92-7d07-5035-8d43-e81249f5b36e")});

    public static final EntityProxy STATED_NAVIGATION_PATTERN_PROXY = EntityProxy.make("Stated navigation pattern",
            new UUID[] {UUID.fromString("d02957d6-132d-5b3c-adba-505f5778d998")});

    public static final EntityProxy UK_DIALECT_PATTERN_PROXY = EntityProxy.make("UK Dialect Pattern",
            new UUID[] {UUID.fromString("561f817a-130e-5e56-984d-910e9991558c")});

    public static final EntityProxy US_DIALECT_PATTERN_PROXY = EntityProxy.make("US Dialect Pattern",
            new UUID[] {UUID.fromString("08f9112c-c041-56d3-b89b-63258f070074")});

    public static final EntityProxy VERSION_CONTROL_PATH_ORIGIN_PATTERN_PROXY = EntityProxy.make("Version control path origin pattern",
            new UUID[] {UUID.fromString("70f89dd5-2cdb-59bb-bbaa-98527513547c")});
}
