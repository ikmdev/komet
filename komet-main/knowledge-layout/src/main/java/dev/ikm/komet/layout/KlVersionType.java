package dev.ikm.komet.layout;

import dev.ikm.komet.framework.observable.ObservableVersion;

public interface KlVersionType<OV extends ObservableVersion> {
    /**
     * Retrieves the class type of the observable version class that this factory creates components for.
     * Enables runtime access to the generic version class that would otherwise be erased.
     *
     * @return A {@link Class} object representing the type of the observable version (OV).
     */
    Class<OV> versionType();
}
