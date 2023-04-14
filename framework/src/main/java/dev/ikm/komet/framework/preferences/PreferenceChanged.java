package dev.ikm.komet.framework.preferences;

import javafx.beans.property.BooleanProperty;

/**
 * @author kec
 */
public interface PreferenceChanged {

    BooleanProperty changedProperty();

}
