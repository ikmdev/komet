package dev.ikm.komet.framework.propsheet;

import javafx.beans.property.SimpleObjectProperty;
import dev.ikm.tinkar.entity.PatternEntityVersion;

public record FieldDefinitionRecord(SimpleObjectProperty valueProperty, String propertyDescription,
                                    String propertyName,
                                    PatternEntityVersion enclosingPatternVersion) {
}
