package dev.ikm.komet.framework.rulebase;

import java.util.UUID;

public interface Consequence<T> {
    UUID consequenceUUID();

    UUID ruleUUID();

    T get();
}
