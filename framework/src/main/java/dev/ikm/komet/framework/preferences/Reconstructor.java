package dev.ikm.komet.framework.preferences;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for a static method that can reconstruct an object from a saved configuration, and should have
 * the signature:
 *
 * @Reconstructor public static Object create(ObservableViewNoOverride windowView, KometPreferences nodePreferences)
 * <p>
 * Where the node preferences are for the object to be reconstructed.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Reconstructor {
}
